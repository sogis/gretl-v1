/**
 * Gets the prepared GRETL jar and starts an OpenShift Docker build.
 * The Docker image will be pushed to the repository with the build number as tag.
 * Needed parameter:
 * - buildProject: Jenkins project that builds the GRETL jar (text parameter)
 * - repository: repository incl. user / organisation to push the Docker image to (text parameter)
 * - openShiftCluster: OpenShift Cluster to be used. (text parameter)
 * - openShiftProject: OpenShift project to be used (text parameter)
 * - ocToolName: Jenkins custom tool name of oc client (text parameter)
 * - openShiftDeployTokenName: amount of tags to keep (text parameter)
 */

pipeline {
    agent any

    stages {
        stage('check parameter') {
            steps {
                script {
                    check.mandatoryParameter('buildProject')
                    check.mandatoryParameter('repository')
                    check.mandatoryParameter('openShiftCluster')
                    check.mandatoryParameter('openShiftProject')
                    check.mandatoryParameter('ocToolName')
                    check.mandatoryParameter('openShiftDeployTokenName')
                }
            }
        }
        stage('prepare') {
            steps {
                sh 'rm -rf build-tmp'
                sh 'mkdir build-tmp'

                sh 'cp -R docker/gretl/* build-tmp'
                sh 'cp -R dependencies.gradle build-tmp'

                dir('build-tmp') {
                    step([$class     : 'CopyArtifact',
                          projectName: params.buildProject,
                          flatten    : true
                    ]);

                    sh "echo 'jenkins build' > build.info"
                    sh "echo date: `date '+%Y-%m-%d %H:%M:%S'` >> build.info"
                    sh "echo BUILD_NUMBER: ${BUILD_NUMBER} >> build.info"
                    sh "echo BUILD_TAG: ${BUILD_TAG} >> build.info"
                }

                sh 'ls -la build-tmp'
            }
        }
        stage('OpenShift build') {
            steps {
                script{
                    timeout(20) {
                        def ocDir = tool params.ocToolName
                        withEnv(["PATH+OC=${ocDir}"]) {
                            sh "oc version"
                            openshift.withCluster(params.openShiftCluster, params.openShiftDeployTokenName) {
                                openshift.withProject(params.openShiftProject) {
                                    echo "Running in project: ${openshift.project()}"

                                    // update docker image tag with build number
                                    def imageRef = "docker.io/${params.repository}:${BUILD_NUMBER}"
                                    def bc = openshift.selector('bc/gretl').object()
                                    bc.spec.output.to['name'] = imageRef
                                    openshift.apply(bc)


                                    def builds = openshift.startBuild("gretl","--from-dir=./build-tmp/")
                                    builds.untilEach(1) {
                                        echo "Created builds so far: ${it.names()}"

                                        return it.object().status.phase == "Complete" || it.object().status.phase == "Failed"
                                    }

                                    echo "created and pushed image: ${imageRef}"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
