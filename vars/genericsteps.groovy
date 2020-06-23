#!/usr/bin/groovy

/**
 * Generic Jenkins pipeline steps that can be reused throughout the Jenkins server.
 * @author Andrew Jarombek
 * @since 6/22/2020
 */

def checkoutRepo(String name, String branch) {
    dir("repos/$name") {
        git.basicClone(name, branch)
    }
}

def postScript(String bodyTitle, String bodyContent, String jobName, String buildStatus,
               String buildNumber, String buildUrl) {
    email.sendEmail(bodyTitle, bodyContent, jobName, buildStatus, buildNumber, buildUrl)
    cleanWs()
}