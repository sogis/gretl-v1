/**
 * Runs tests against a Jenkins running on OpenShift. For that the GRETL Jenkins inside OpenShift is needed.
 * The Jenkinis will be prepared and the port is forwarded to give access to it.
 * Needed parameter:
 * - gitRepository: Url of the Git repository with the source code. (text parameter)
 * - openShiftCluster: OpenShift Cluster to be used. (text parameter)
 * - openShiftProject: OpenShift project to be used. (text parameter)
 * - ocToolName: Jenkins custom tool name of oc client. (text parameter)
 * - openShiftDeployTokenName: amount of tags to keep. (text parameter)
 *
 * - jenkinsUser
 * - jenkinsToken
 * The API token is available in your personal configuration page. Click your name on the top right corner on every page, then click "Configure" to see your API token. (The URL $root/me/configure is a good shortcut.) You can also change your API token from here.
 *
 * Optional parameter:
 * - buildTime: Test build time in minutes. Control over port forward duration. (text parameter)
 */
pipeline {
    agent any
    tools {
        'jdk' "OpenJDK 1.8"
    }
    stages {
        stage('check parameter') {
            steps {
                script {
                    check.mandatoryParameter('openShiftCluster')
                    check.mandatoryParameter('openShiftProject')
                    check.mandatoryParameter('ocToolName')
                    check.mandatoryParameter('openShiftDeployTokenName')
                    check.mandatoryParameter('jenkinsUser')
                    check.mandatoryParameter('jenkinsToken')
                }
            }
        }
        stage('prepare') {
            steps {
                git url: 'https://github.com/sogis/gretl.git', branch: 'systemTest'

                sh 'rm -rf build-tmp'
                sh 'mkdir build-tmp'

                sh 'cp -R inttest/* build-tmp/'

                sh 'ls -la build-tmp'
            }
        }
        stage('system-test') {
            steps {
                script{
                    timeout(20) {
                        def ocDir = tool params.ocToolName
                        withEnv(["PATH+OC=${ocDir}"]) {
                            sh "oc version"
                            openshift.withCluster(params.openShiftCluster, params.openShiftDeployTokenName) {
                                openshift.withProject(params.openShiftProject) {
                                    echo "Running in project: ${openshift.project()}"

                                    def gretlIS = openshift.selector('is/gretl').object()
                                    println "GRETL Runtime version: -> " + gretlIS.spec.tags['from'].name


                                    // find jenkins pod with name: jenkins
                                    def podSelector = openshift.selector('pods', [name: 'jenkins'])
                                    def dbPod = podSelector.name()
                                    String shortName = dbPod.substring(dbPod.indexOf("/") + 1);
                                    println "Jenkins pod: " + shortName

                                    parallel(
                                        port_forward: {
                                            try {
                                                def buildTime = 3
                                                if (params.buildTime != null) {
                                                    buildTime = params.buildTime as int
                                                }
                                                timeout(buildTime) {
                                                    echo "Forward Jenkins API port"
                                                    openshift.raw( 'port-forward', shortName, '8081:8080' )
                                                }
                                            } catch (err) {} // catch timeout
                                            println "port forward done (time: ${buildTime})"
                                        },
                                        test: {
                                            echo "run system tests ..."
                                            dir('build-tmp') {
                                                sh './gradlew -version'
                                                sh "./gradlew clean build testSystem -Dgretltest_jenkins_uri=http://localhost:8081 -Dgretltest_jenkins_user=${params.jenkinsUser} -Dgretltest_jenkins_pwd=${params.jenkinsToken} --refresh-dependencies"
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            post {
                always {
                    junit 'build-tmp/build/test-results/**/*.xml'  // Requires JUnit plugin
                }
            }
        }
    }
}
