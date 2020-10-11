#!/usr/bin/groovy

/**
 * Generic Jenkins pipeline steps that can be reused throughout the Jenkins server.
 * @author Andrew Jarombek
 * @since 6/22/2020
 */

/**
 * Checkout a repository into a standard 'repo' directory.  Reusing this function makes repository code locations
 * more predictable.
 * @param name The name of the git repository.
 * @param branch The branch to checkout.
 */
def checkoutRepo(String name, String branch) {
    dir("repos/$name") {
        git.basicClone(name, branch)
    }
}

/**
 * Generic script to use in the 'post' section of a declarative pipeline.
 * @param bodyTitle The title section of the email body.
 * @param bodyContent Additional content added to the body of the email.
 * @param jobName The name of the Jenkins job which triggered this email.
 * @param buildStatus Status of the Jenkins job build that triggered this email.
 * @param buildNumber Execution number of the Jenkins job build that triggered this email.
 * @param buildUrl URL of the Jenkins job build that triggered this email.
 */
def postScript(String bodyTitle, String bodyContent, String jobName, String buildStatus,
               String buildNumber, String buildUrl) {
    email.sendEmail(bodyTitle, bodyContent, jobName, buildStatus, buildNumber, buildUrl)
    cleanWs()
}

/**
 * Run a Shell script in a specific directory.  The result of the bash script determines the status of the build.
 * @param script The shell script to run.
 * @param directory The directory to run the shell script within.
 * @param failureStatus The pipeline status if the shell script returns a non-zero exit code.
 */
def shReturnStatus(String script, String directory = '.', String failureStatus = 'UNSTABLE') {
    dir(directory) {
        def status = sh (
            script: script,
            returnStatus: true
        )

        if (status >= 1) {
            currentBuild.result = failureStatus
        } else {
            currentBuild.result = "SUCCESS"
        }
    }
}