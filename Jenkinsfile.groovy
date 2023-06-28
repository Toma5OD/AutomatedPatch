pipeline {
    agent any

    parameters {
        string(name: 'RELEASE', defaultValue: '7.6.2', description: 'Release version for the patch')
        string(name: 'URL', defaultValue: 'http://...', description: 'URL to download the .zip file')
        string(name: 'ZIP_FILE', defaultValue: 'keycloak-services-18.0.4-patch.zip', description: 'Name of the .zip file to download and unpack')
        string(name: 'JAR_FILE', defaultValue: 'keycloak-services-18.0.4-patch.jar', description: 'Name of the .jar file to create')
    }

    stages {
        stage('Prepare Environment') {
            steps {
                script {
                    try {
                        // Clean the workspace
                        deleteDir()
                    } catch (Exception e) {
                        error("Failed to prepare the environment: ${e.message}")
                    }
                }
            }
        }

        stage('Download and Unpack Zip') {
            steps {
                script {
                    try {
                        // Download the zip file
                        sh "curl -O ${params.URL}/${params.ZIP_FILE}"
                        // Unpack the zip file
                        sh "unzip ${params.ZIP_FILE}"
                    } catch (Exception e) {
                        error("Failed to download and unpack zip: ${e.message}")
                    }
                }
            }
        }

        stage('Patch Creation') {
            steps {
                script {
                    try {
                        sh """
                        java -jar /path/to/patch-creator.jar -p sso -v ${params.RELEASE} -tm -debug -i 12345 -d "test patch" -c ${params.JAR_FILE}
                        """
                    } catch (Exception e) {
                        error("Failed to create patch: ${e.message}")
                    }
                }
            }
        }

        stage('Patch Implementation') {
            steps {
                script {
                    try {
                        sh """
                        service myJavaApplication stop
                        mv /path/to/myJavaApplication.jar /path/to/myJavaApplication.jar.backup
                        cp /path/to/generated/patch/file /path/to/myJavaApplication.jar
                        service myJavaApplication start
                        """
                    } catch (Exception e) {
                        error("Failed to implement patch: ${e.message}")
                    }
                }
            }
        }
    }

    post {
        success {
            umbSend 'rh-sso.server.end.success' // Send a UMB message when the pipeline completes successfully
        }
        failure {
            umbSend 'rh-sso.server.end.error.failure' // Send a UMB message when the pipeline fails
        }
        aborted {
            umbSend 'rh-sso.server.end.error.aborted' // Send a UMB message when the pipeline is aborted
        }
    }
}

