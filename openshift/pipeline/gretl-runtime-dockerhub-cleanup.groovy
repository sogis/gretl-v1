import com.puzzleitc.jenkins.DockerHub

/**
 * Used to keep only a certain amount of versions of a Docker Hub repository.
 * Needed parameter:
 * - dockerHubUser: Docker Hub user for authentication (text parameter)
 * - dockerHubPwd: Docker Hub password for authentication (password parameter)
 * - organisation: organisation of the repository (text parameter)
 * - repository: repository to check the tags (text parameter)
 * - numberOfTagsToKeep: amount of tags to keep (text parameter)
 * - testMode: true does not delete any tags (boolean parameter)
 */

def checkMandatoryParameter(parameterName) {
    if (!params.containsKey(parameterName)) {
        currentBuild.result = 'ABORTED'
        error('missing parameter: ' + parameterName)
    }
}

node {
    def dockerHub = new DockerHub(this)
    def token
    def buildNumberTags

    stage ('check parameter') {
        checkMandatoryParameter('dockerHubUser')
        checkMandatoryParameter('dockerHubPwd')
        checkMandatoryParameter('organisation')
        checkMandatoryParameter('repository')
        checkMandatoryParameter('numberOfTagsToKeep')
        checkMandatoryParameter('testMode')
    }
    stage ('login') {
        token = dockerHub.createLoginToken(params.dockerHubUser, dockerHubPwd)
        if (token == null) {
            currentBuild.result = 'FAILURE'
            error('Login not successful!')
        }
    }
    stage('read tags') {
        def tags = dockerHub.readTags(params.organisation, params.repository, token)
        println "all tags: " + tags

        buildNumberTags = tags.findAll{ it != null && it.isInteger()  }.collect { it as int }.sort()
        println "build number tags: " + buildNumberTags
    }
    stage('delete tags') {
        int numberToKeep = params.numberOfTagsToKeep as Integer
        if (buildNumberTags.size() > numberToKeep) {
            def numberToDelete = buildNumberTags.size() - numberToKeep
            for (int i = 0; i < numberToDelete; i++) {
                String delTag = buildNumberTags.get(i)

                println "tag to remove: " + delTag
                if (params.testMode) {
                    println "testMode: tag <" + delTag + "> would be removed"
                } else {
                    println "remove tag: " + delTag
                    dockerHub.deleteByTag(dockerHubUser, params.repository, delTag, token)
                }
            }
        } else {
            println "nothing to do, only " + buildNumberTags.size() + " tags available."
        }
    }
}
