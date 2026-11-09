// Jenkins pipeline for the REST API Automation Framework.
//
// Demonstrates reading environment/credential values entirely from the CLI (via -D system
// properties sourced from Jenkins parameters and credentials), so the pipeline needs no code
// changes to target a different environment or rotate the Restful-Booker / ReqRes credentials.
pipeline {
    agent any

    parameters {
        choice(name: 'TEST_ENV', choices: ['qa', 'staging'], description: 'Environment to run against')
    }

    environment {
        // Populate these in Jenkins > Credentials as "Secret text" entries.
        ENCRYPTION_KEY = credentials('rest-api-framework-encryption-key')
        REQRES_API_KEY = credentials('reqres-api-key')
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn -q -DskipTests compile'
            }
        }

        stage('Test') {
            steps {
                sh """
                    mvn test \
                        -Denv=${params.TEST_ENV} \
                        -DencryptionKey=${ENCRYPTION_KEY} \
                        -Dreqres.api.key=${REQRES_API_KEY}
                """
            }
        }

        stage('Report') {
            steps {
                sh 'mvn allure:report'
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/site/allure-maven-plugin/**', allowEmptyArchive: true
        }
    }
}
