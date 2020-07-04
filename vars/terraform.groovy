#!/usr/bin/groovy

/**
 * Functions used to create Infrastructure with Terraform
 * @author Andrew Jarombek
 * @since 5/12/2019
 */

/**
 * Initialize the Terraform configuration in a given directory.
 * @param directory The directory containing Terraform files.
 */
def terraformInit(String directory) {
    dir(directory) {
        sh """
            terraform --version
            terraform init
        """
    }
}

/**
 * Validate the Terraform files in a directory for syntax issues.
 * @param directory The directory containing Terraform files.
 */
def terraformValidate(String directory) {
    dir(directory) {
        sh "terraform validate"
    }
}

/**
 * Create a plan of the infrastructure that Terraform will create.
 * @param directory The directory containing Terraform files.
 */
def terraformPlan(String directory) {
    env.TERRAFORM_NO_CHANGES = 'false'
    env.TERRAFORM_PLAN_ERRORS = 'false'

    dir(directory) {
        def result = sh(
            script: 'terraform plan -detailed-exitcode -out=terraform.tfplan',
            returnStatus: true
        )

        // The result is 0 if the plan found no changes, 1 if there are errors with the plan,
        // and 2 if the plan is successful and changes will be made.
        switch (result) {
            case 0:
                currentBuild.result = 'SUCCESS'
                env.TERRAFORM_NO_CHANGES = 'true'
                break
            case 1:
                currentBuild.result = 'UNSTABLE'
                env.TERRAFORM_PLAN_ERRORS = 'true'
                break
            case 2:
                println 'The "terraform plan" Response Was Valid.'
                break
            default:
                println 'Unexpected Terraform exit code.'
                currentBuild.result = 'FAILURE'
        }
    }
}

/**
 * Apply/create the infrastructure defined in Terraform configuration files.
 * @param directory The directory containing Terraform files.
 * @param autoApply Whether to automatically create the infrastructure or prompt the user to confirm/deny creation.
 */
def terraformApply(String directory, boolean autoApply) {
    if (!autoApply) {
        timeout(time: 15, unit: 'MINUTES') {
            input message: 'Confirm Plan', ok: 'Apply'
        }
    }

    dir(directory) {
        sh "terraform apply -auto-approve terraform.tfplan"
    }
}

/**
 * Create a plan of the infrastructure that Terraform will destroy.
 * @param directory The directory containing Terraform files.
 */
def terraformPlanDestroy(String directory) {
    env.TERRAFORM_NO_CHANGES = 'false'
    env.TERRAFORM_PLAN_ERRORS = 'false'

    dir(directory) {
        def result = sh(
            script: 'terraform plan -destroy -detailed-exitcode',
            returnStatus: true
        )

        // The result is 0 if the plan found no changes, 1 if there are errors with the plan,
        // and 2 if the plan is successful and changes will be made.
        switch (result) {
            case 0:
                currentBuild.result = 'SUCCESS'
                env.TERRAFORM_NO_CHANGES = 'true'
                break
            case 1:
                currentBuild.result = 'UNSTABLE'
                env.TERRAFORM_PLAN_ERRORS = 'true'
                break
            case 2:
                println 'The "terraform plan" Response Was Valid.'
                break
            default:
                println 'Unexpected Terraform exit code.'
                currentBuild.result = 'FAILURE'
        }
    }
}

/**
 * Destroy the infrastructure that Terraform files in the specified directory are managing.
 * @param directory The directory containing Terraform files.
 * @param autoDestroy Whether to automatically destroy the infrastructure or
 * prompt the user to confirm/deny destruction.
 */
def terraformDestroy(String directory, boolean autoDestroy) {
    if (!autoDestroy) {
        timeout(time: 15, unit: 'MINUTES') {
            input message: 'Confirm Plan', ok: 'Apply'
        }
    }

    dir(directory) {
        sh "terraform destroy -auto-approve"
    }
}

/**
 * Creates Infrastructure based on Terraform scripts.
 * @param directory The directory inside the Git repository containing the Terraform scripts.
 * @param repository The Git repository containing the Terraform scripts.
 * @param autoApply If True, the Pipeline won't pause before Applying.  If False, manual intervention will be needed.
 * @return Returns void value when the Terraform scripts are done executing.
 */
def terraformApplyStages(String directory, String repository, boolean autoApply) {

    // First, checkout the Terraform scripts from a Git repository
    stage ('checkout') {
        cleanWs()
        checkout([$class: 'GitSCM',
                  branches: [[name: '*/master']],
                  credentialsId: "ajarombek-github",
                  userRemoteConfigs: [[url: "$repository"]]])
    }
    // Second, initialize the Terraform working directory and backend on S3
    stage('Init') {
        dir(directory) {
            ansiColor('css') {
                sh """
                    terraform --version
                    terraform init
                """
            }
        }
    }
    // Third, validate the HCL syntax in the Terraform config files
    stage('Validate') {
        dir(directory) {
            ansiColor('css') {
                sh "terraform validate"
            }
        }
    }
    // Fourth, create an execution plan for the infrastructure.
    stage('Plan') {
        dir(directory) {
            ansiColor('css') {
                def result = sh(
                    script: '''
                        terraform plan -detailed-exitcode -out=terraform.tfplan
                    ''',
                    label: 'terraform plan',
                    returnStatus: true
                )

                // The result is 0 if the plan found no changes, 1 if there are errors with the plan,
                // and 2 if the plan is successful and changes will be made.
                switch (result) {
                    case 0:
                        currentBuild.result = 'SUCCESS'
                        return
                    case 1:
                        currentBuild.result = 'UNSTABLE'
                        return
                    case 2:
                        println 'The "terraform plan" Response Was Valid.'
                        break
                    default:
                        println 'An Unknown "terraform plan" Response Was Returned.'
                        currentBuild.result = 'FAILURE'
                        return
                }
            }
        }
    }
    stage('Apply') {
        if (!autoApply) {
            try {
                timeout(time: 15, unit: 'MINUTES') {
                    input message: 'Confirm Plan', ok: 'Apply'
                }
            } catch (Throwable ex) {
                println 'Timeout Exceeded.'
                currentBuild.result = 'UNSTABLE'
            }
        }
        dir(directory) {
            ansiColor('css') {
                sh "terraform apply -auto-approve terraform.tfplan"
            }
        }
    }
}

/**
 * Destroys infrastructure defined in Terraform scripts
 * @directory The directory inside the Git repository containing the Terraform scripts.
 * @param repository The Git repository containing the Terraform scripts.
 */
def terraformDestroyStages(String directory, String repository) {

    // First, checkout the Terraform scripts from a Git repository
    stage ('checkout') {
        cleanWs()
        checkout([$class: 'GitSCM',
                  branches: [[name: '*/master']],
                  credentialsId: "ajarombek-github",
                  userRemoteConfigs: [[url: "$repository"]]])
    }
    // Second, initialize the Terraform working directory and backend on S3
    stage('Init') {
        dir(directory) {
            ansiColor('css') {
                sh """
                    terraform --version
                    terraform init
                """
            }
        }
    }
    stage('Destroy') {
        dir(directory) {
            ansiColor('css') {
                sh "terraform destroy -auto-approve"
            }
        }
    }
}