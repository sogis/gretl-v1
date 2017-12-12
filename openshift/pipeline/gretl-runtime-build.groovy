pipeline {
    agent any

    parameters {
        string(defaultValue: "TEST", description: 'What environment?', name: 'userFlag')
        string(name: 'buildProject',
               description: 'Which project builds the GRETL jar?',
               defaultValue: 'sogis/gretl-multibranch/master')
        string(name: 'openShiftCluster',
               description: 'Which OpenShift Cluster should be used?',
               defaultValue: 'OpenShiftAioProduction')
        string(name: 'openShiftProject',
               description: 'Which OpenShift project should be used?',
               defaultValue: 'sogis-gretl-dev')
    }

    stages {
        stage('prepare') {
            steps {
                sh 'rm -rf build-tmp'
                sh 'mkdir build-tmp'

                sh 'cp -R docker/gretl/* build-tmp'
                sh 'cp -R dependencies.gradle build-tmp'

                dir('build-tmp') {
                    step([$class     : 'CopyArtifact',
                          projectName: "${params.buildProject}",
                          flatten    : true
                    ]);
                }

                sh 'ls -la build-tmp'
            }
        }
        stage('deploy') {
            steps {
                script{
                    timeout(20) {
                        def ocDir = tool "oc3.7.0"
                        withEnv(["PATH+OC=${ocDir}"]) {
                            sh "oc version"
                            openshift.withCluster("${params.openShiftCluster}", "${params.openShiftProject}_deploy_token") {
                                openshift.verbose()
                                openshift.withProject("${params.openShiftProject}") {
                                    echo "Running in project: ${openshift.project()}"

                                    def builds = openshift.startBuild("gretl","--from-dir=./build-tmp/")
                                    builds.untilEach(1) {
                                        echo "Created builds so far: ${it.names()}"

                                        return it.object().status.phase == "Complete"
                                    }
                                }

                                  //  echo "Running in project: ${openshift.project()}"
                                  //  def builds = openshift.startBuild("gretl","--from-dir=./build-tmp/")


                                 //  def result = buildSelector.logs('-f')

                                    // You can even see exactly what oc command was executed.
                                    //echo "Logs executed: ${result.actions[0].cmd}"

                                    // And even obtain the standard output and standard error of the command.
                                 //   def logsString = result.actions[0].out
                                 //   def logsErr = result.actions[0].err
        							//println logsErr

                                //    echo "Build status: ${result.out}"
                            }
                        }
                    }
                }
            }
        }
    }
    post {
        success {
            echo 'Success'
        }
        unstable {
            echo 'Unstable'
        }
        failure {
            echo 'Error'
        }
    }
}
