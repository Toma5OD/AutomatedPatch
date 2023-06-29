# Jenkins Pipeline for Patch Creation and Implementation in RH-SSO

Tool Type: Patch Creation and Adaptation Tool for RH-SSO  
Language: Groovy  
Developer: Tomas O Dalaigh

This Jenkins pipeline is developed by the Keycloak Automation team at Red Hat. The primary function of the pipeline is to automate the process of patch creation and implementation for the Red Hat Single Sign-On (RH-SSO) source code. The pipeline is authored in Groovy, providing scripting capabilities for the Java platform.

## Workflow

The automated patch creation and implementation process using this pipeline typically follows these steps:

1. **Issue Identification:** An issue or vulnerability is identified in the most recent RH-SSO release, usually from customer feedback or security scans, necessitating a rapid fix before the next full release.

2. **New Branch Creation:** A new branch, such as "Patch Branch", is created on GitHub to host the code changes required to address the identified issue.

3. **Identifying Files for Patch:** The diff from GitHub is analyzed to determine which files require patching.

4. **Preparing Server Distribution:** The server distribution is isolated from the artifacts created during the PNC build, and the resultant zip file is used in the patch.

5. **Running the Jenkins Pipeline:** The necessary information is entered into the Jenkins script from this repository, and the script then runs, creating and implementing the patch.

6. **Testing:** Tests are conducted as needed, varying from unit tests to full integration tests, to ensure the patch doesn't introduce new issues and effectively resolves the identified problem.

7. **Merging the Branch:** Post-verification, the "Patch Branch" is merged with the main branch, making the patch available to RH-SSO users.

## Stages

The pipeline consists of several stages:

1. **Prepare Environment:** This stage involves workspace cleaning in preparation for the pipeline run.
    ```groovy
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
    ```

2. **Download and Unpack Zip:** This stage downloads a zip file from a provided URL and unpacks it.
    ```groovy
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
    ```

3. **Patch Creation:** This stage creates a patch using a provided JAR file.
    ```groovy
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
    ```

4. **Patch Implementation:** This stage implements the created patch into the RH-SSO source code.
    ```groovy
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
    ```

## Usage

To use this Jenkins pipeline, the following parameters need to be specified:

- `RELEASE`: The release version of the patch
- `URL`: The URL to download the zip file
- `ZIP_FILE`: The name of the zip file to download and unpack
- `JAR_FILE`: The name of the JAR file to be used in patch creation
- `PATCH_CREATOR_PATH`: The path to `patch-creator.jar`
- `RH_SSO_PATH`: The path to the RH-SSO source code

These parameters can be entered manually when triggering the pipeline via the Jenkins interface or can be provided automatically if the pipeline is triggered as part of an automated process.

## Error Handling

Each pipeline stage includes error handling. In case of an error during any stage, an error message is generated specifying the name of the stage and the error message.

## Notifications

Upon pipeline completion, a Unified Message Bus (UMB) message is sent based on the outcome of the pipeline:

- If the pipeline completes successfully, a UMB message with the topic 'rh-sso.server.end.success' is sent.
- If the pipeline fails, a UMB message with the topic 'rh-sso.server.end.error.failure' is sent.
- If the pipeline is aborted, a UMB message with the topic 'rh-sso.server.end.error.aborted' is sent.

## Requirements

This pipeline requires Jenkins with the Pipeline plugin installed. Additionally, the pipeline expects the `patch-creator.jar` file and the RH-SSO source code at the specified paths.

## Additional Information

This pipeline is a work-in-progress by Tomas O Dalaigh and the Keycloak Automation team at Red Hat. Your feedback and contributions are greatly appreciated and will help us improve the pipeline.
