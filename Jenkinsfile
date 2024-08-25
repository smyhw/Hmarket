pipeline {
    agent any
    stages {
        stage('Build') {
            tools {
                jdk "jdk21"
            }
            steps {
                sh './gradlew publish'
                sh './gradlew javadoc'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            cleanWs()
        }
    }
}
