pipeline {
    agent any

    environment {
        // Define SonarQube server URL and credentials
        SONARQUBE_URL = 'http://your-sonarqube-server-url'
        SONARQUBE_TOKEN = credentials('sonarqube-token-id') // Store token in Jenkins credentials
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout your code from your SCM
                checkout scm
            }
        }

        stage('Build') {
            steps {
                // Run your build steps here
                sh 'make build'
            }
        }

        stage('Test') {
            steps {
                // Run your test steps here, e.g., pytest with coverage
                sh 'pytest --cov=myproject --cov-report=xml:coverage.xml'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube Server') {
                    // Run SonarQube scanner
                    sh 'sonar-scanner \
                        -Dsonar.projectKey=myproject \
                        -Dsonar.sources=. \
                        -Dsonar.host.url=${SONARQUBE_URL} \
                        -Dsonar.login=${SONARQUBE_TOKEN} \
                        -Dsonar.python.coverage.reportPaths=coverage.xml'
                }
            }
        }

        stage("Quality Gate") {
            steps {
                // Wait for SonarQube analysis to be completed and fail the pipeline if the quality gate is not met
                script {
                    def qg = waitForQualityGate()
                    if (qg.status != 'OK') {
                        error "Pipeline aborted due to quality gate failure: ${qg.status}"
                    }
                }
            }
        }
    }
}

