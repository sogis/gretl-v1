#!groovy​

pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 5, unit: 'MINUTES')
    }
    triggers {
        pollSCM('H/15 * * * *')
    }
    tools {
        'jdk' "OpenJDK 1.8"
    }
    stages {
        stage('Preparation') {
            steps {
                sh './gradlew -version'
            }
        }
        stage('Build') {
            steps {
                sh './gradlew build'
            }
            post {
                always {
                    junit 'build/test-results/**/*.xml'  // Requires JUnit plugin
                }
            }
        }
    }
}
