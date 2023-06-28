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
                // Clean the workspace
                deleteDir()
            }
        }

        stage('Download and Unpack Zip') {
            steps {
                // Download the zip file
                sh "curl -O ${params.URL}/${params.ZIP_FILE}"
                // Unpack the zip file
                sh "unzip ${params.ZIP_FILE}"
            }
        }

        stage('Patch Creation') {
            steps {
                sh """
                java -jar /path/to/patch-creator.jar -p sso -v ${params.RELEASE} -tm -debug -i 12345 -d "test patch" -c ${params.JAR_FILE}
                """
            }
        }

        stage('Patch Implementation') {
            steps {
                sh """
                service myJavaApplication stop
                mv /path/to/myJavaApplication.jar /path/to/myJavaApplication.jar.backup
                cp /path/to/generated/patch/file /path/to/myJavaApplication.jar
                service myJavaApplication start
                """
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
