/**
 * Runs tests against a Jenkins running on OpenShift. For that the GRETL Jenkins inside OpenShift is needed.
 * The Jenkinis will be prepared and the port is forwarded to give access to it.
 * Needed parameter:
 * - gitRepository: Url of the Git repository with the source code. (text parameter)
 * - openShiftCluster: OpenShift Cluster to be used. (text parameter)
 * - openShiftProject: OpenShift project to be used. (text parameter)
 * - ocToolName: Jenkins custom tool name of oc client. (text parameter)
 * - openShiftDeployTokenName: amount of tags to keep. (text parameter)
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
        stage('test') {
            steps {
                script{
                    timeout(20) {
                        def ocDir = tool params.ocToolName
                        withEnv(["PATH+OC=${ocDir}"]) {
                            sh "oc version"
                            openshift.withCluster(params.openShiftCluster, params.openShiftDeployTokenName) {
                                openshift.withProject(params.openShiftProject) {
                                    echo "Running in project: ${openshift.project()}"

                                    def bc = openshift.selector('is/gretl').object()

                                    println "is -> " + bc
                                    println "is from -> " + bc.spec.tags['from'].name




                                    parallel(
                                            a: {
                                                timeout(1) {
                                                    echo "This is branch a"
                                                    def result = openshift.raw( 'port-forward', shortname, '5432:5432' ).out
                                                    println result
                                                }
                                            },
                                            b: {
                                                echo "This is branch b"
                                                dir('build-tmp') {
                                                    sh './gradlew -version'

                                                    sh './gradlew clean build testSystem --refresh-dependencies'
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
