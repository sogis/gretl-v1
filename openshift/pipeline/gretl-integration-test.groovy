/**
 * Gets the prepared GRETL jar and runs the integration tests. For that a database from OpenShift is needed.
 * The database will be prepared and the port is forwarded to give access to it.
 * Needed parameter:
 * - buildProject: Jenkins project that builds the GRETL jar. (text parameter)
 * - openShiftCluster: OpenShift Cluster to be used. (text parameter)
 * - openShiftProject: OpenShift project to be used. (text parameter)
 * - ocToolName: Jenkins custom tool name of oc client. (text parameter)
 * - openShiftDeployTokenName: amount of tags to keep. (text parameter)
 * Optional parameter:
 * - gitBranch: Branch of the Git repository. (text parameter)
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
                    check.mandatoryParameter('buildProject')
                    check.mandatoryParameter('openShiftCluster')
                    check.mandatoryParameter('openShiftProject')
                    check.mandatoryParameter('ocToolName')
                    check.mandatoryParameter('openShiftDeployTokenName')
                }
            }
        }
        stage('prepare') {
            steps {
                // prepare GRETL jar
                sh 'rm -rf build'
                dir('build/libs') {
                    step([$class     : 'CopyArtifact',
                          projectName: params.buildProject,
                          flatten    : true
                    ]);
                }
                sh 'ls -la build/libs'

                // prepare files needed by the build
                sh 'rm -rf build-tmp'
                sh 'mkdir build-tmp'

                sh 'cp -R inttest/* build-tmp/'
                sh 'ls -la build-tmp'
            }
        }
        stage('int-test') {
            steps {
                script{
                    timeout(20) {
                        def ocDir = tool params.ocToolName
                        withEnv(["PATH+OC=${ocDir}"]) {
                            sh "oc version"
                            openshift.withCluster(params.openShiftCluster, params.openShiftDeployTokenName) {
                                openshift.withProject(params.openShiftProject) {
                                    echo "Running in project: ${openshift.project()}"

                                    // find database pod with name: postgresql
                                    def podSelector = openshift.selector('pods', [name: 'postgresql'])
                                    def dbPod = podSelector.name()
                                    String shortName = dbPod.substring(dbPod.indexOf("/") + 1);
                                    println "pod: " + shortName

                                    // prepare database
                                    openshift.raw("cp","openshift/pipeline/scripts/reset-test-db.sh","${shortName}:/tmp/")
                                    echo openshift.exec(shortName,'bash', '/tmp/reset-test-db.sh').out

                                    openshift.raw("cp","openshift/pipeline/scripts/init-test-db.sh","${shortName}:/tmp/")
                                    echo openshift.exec(shortName,'bash', '/tmp/init-test-db.sh').out

                                    parallel(
                                            a: {
                                                timeout(1) {
                                                    echo "forward database port"
                                                    openshift.raw( 'port-forward', shortName, '5432:5432' )
                                                }
                                            },
                                            b: {
                                                echo "run integration tests ..."
                                                dir('build-tmp') {
                                                    sh './gradlew -version'

                                                    sh './gradlew clean build testIntegration --refresh-dependencies'
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
