pipelineJob('patch-testing-pipeline') {
    properties {
        disableConcurrentBuilds()
    }
    description('This job runs tests on patches.')
    parameters {
        stringParam('PIPELINE_REPO', 'https://gitlab.cee.redhat.com/keycloak/keycloak-pipeline.git', '')
        stringParam('PIPELINE_BRANCH', 'main', '')
        textParam('CI_MESSAGE', '', 'UMB message body')
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('$PIPELINE_REPO')
                        credentials('rh-sso-cd-service-gitlab')  // Add credentials parameter
                    }
                    extensions {
                        cloneOptions {
                            honorRefspec(true)
                            noTags(false)
                        }
                        relativeTargetDirectory('.')
                    }
                    branch('$PIPELINE_BRANCH')
                }
            }
            scriptPath('patch-testing-pipeline/Jenkinsfile') // Adjust Jenkinsfile path according to your structure
        }
    }
}
