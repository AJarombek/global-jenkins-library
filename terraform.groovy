#!/usr/bin/groovy

/**
 * Functions used to create Infrastructure with Terraform
 * @author Andrew Jarombek
 * @since 5/12/2019
 */

/**
 * Creates Infrastructure based on Terraform scripts.
 * @param directory The directory inside the Git repository containing the Terraform scripts.
 * @param repository The Git repository containing the Terraform scripts.
 * @return Returns void value when the Terraform scripts are done executing.
 */
def terraformApply(String directory, String repository) {

    // First, checkout the Terraform scripts from a Git repository
    stage ('checkout') {
        cleanWs()
        checkout([$class: 'GitSCM',
                  branches: [[name: '*/master']],
                  credentialsId: "865da7f9-6fc8-49f3-aa56-8febd149e72b",
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
                sh "terraform plan -detailed-exitcode"
            }
        }
    }
    stage('Apply') {

    }
}

def terraformDestroy(String directory, String repository) {

}