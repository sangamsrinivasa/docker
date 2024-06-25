pipeline {
    agent any

    environment {
        // Define SonarQube server URL and credentials
        SONARQUBE_URL = 'http://your-sonarqube-server-url'
        SONARQUBE_TOKEN = credentials('sonarqube-token-id') // Store token in Jenkins creds
        
        // Define Artifactory server and credentials
        ARTIFACTORY_URL = 'http://your-artifactory-server-url'
        ARTIFACTORY_CREDENTIALS = 'artifactory-credentials-id' // Store Artifactory creds in Jenkins
    }

    stages {
        stage('Git Checkout') {
            steps {
                // Checkout your code from your Git Repo
                checkout git
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

        stage('Upload to Artifactory') {
            when {
                // Only run this stage if the build result is successful
                expression {
                    currentBuild.result == null || currentBuild.result == 'SUCCESS'
                }
            }
            steps {
                script {
                    // Define Artifactory server
                    def server = Artifactory.server 'Artifactory-Server'

                    // Define the build info
                    def buildInfo = Artifactory.newBuildInfo()
                    buildInfo.env.capture = true

                    // Define the upload spec
                    def uploadSpec = """{
                        "files": [{
                            "pattern": "build/*.zip",
                            "target": "generic-local/myproject/"
                        }]
                    }"""

                    // Upload files to Artifactory
                    server.upload spec: uploadSpec, buildInfo: buildInfo

                    // Publish build info to Artifactory
                    server.publishBuildInfo buildInfo
                }
            }
        }
    }
}

