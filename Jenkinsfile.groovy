pipeline {
    agent any

    parameters {
        string(name: 'RELEASE', description: 'Release version for the patch')
        string(name: 'URL', description: 'URL to download the .zip file')
        string(name: 'ZIP_FILE', description: 'Name of the .zip file to download and unpack')
        string(name: 'JAR_FILE', description: 'Name of the .jar file to create')
        string(name: 'PATCH_CREATOR_PATH', description: 'Path to patch-creator.jar')
        string(name: 'RH_SSO_PATH', description: 'Path to RH-SSO source code')
        string(name: 'SSH_HOST', description: 'SSH host address')
        string(name: 'SSH_USER', description: 'SSH username')
        string(name: 'TARGET_DIR', description: 'Target directory on the remote server')
    }

    stages {
        // This stage prepares the environment for the pipeline
        stage('Prepare Environment') {
            steps {
                script {
                    try {
                        // Clean the workspace
                        deleteDir()
                    } catch (Exception e) {
                        error("Error encountered at the 'Prepare Environment' stage: ${e.message}")
                    }
                }
            }
        }

        // This stage downloads and unpacks the required zip file
        stage('Download and Unpack Zip') {
            steps {
                script {
                    try {
                        // Download the zip file
                        sh "curl -O ${params.URL}/${params.ZIP_FILE}"
                        // Unpack the zip file
                        sh "unzip ${params.ZIP_FILE}"
                    } catch (Exception e) {
                        error("Error encountered at the 'Download and Unpack Zip' stage: ${e.message}")
                    }
                }
            }
        }

        // This stage creates the required patch using the provided JAR file
        stage('Patch Creation') {
            steps {
                script {
                    try {
                        sh """
                        java -jar ${params.PATCH_CREATOR_PATH} -p sso -v ${params.RELEASE} -tm -debug -i 12345 -d "test patch" -c ${params.JAR_FILE}
                        """
                    } catch (Exception e) {
                        error("Error encountered at the 'Patch Creation' stage: ${e.message}")
                    }
                }
            }
        }

        // This stage implements the created patch into the RH-SSO source code
        stage('Patch Implementation') {
            steps {
                script {
                    try {
                        sh """
                        service myJavaApplication stop
                        mv ${params.RH_SSO_PATH} ${params.RH_SSO_PATH}.backup
                        cp /path/to/generated/patch/file ${params.RH_SSO_PATH}
                        service myJavaApplication start
                        """
                    } catch (Exception e) {
                        error("Error encountered at the 'Patch Implementation' stage: ${e.message}")
                    }
                }
            }
        }

        // This stage uploads the required files and stages the release on the remote server
        stage('Upload and Stage Release') {
            steps {
                script {
                    try {
                        // Upload the content to the rcm-guest volume
                        sh "rsync -rlp --info=progress2 ${params.ZIP_FILE} ${params.SSH_USER}@${params.SSH_HOST}:${params.TARGET_DIR}"
                        
                        // Stage the release
                        sh "ssh ${params.SSH_USER}@${params.SSH_HOST} 'stage-mw-release ${params.RELEASE}'"
                    } catch (Exception e) {
                        error("Error encountered at the 'Upload and Stage Release' stage: ${e.message}")
                    }
                }
            }
        }
    }

    post {
        // Sends UMB messages depending on the outcome of the pipeline
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
