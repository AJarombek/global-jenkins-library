#!/usr/bin/groovy

/**
 * Functions used to shorten Jenkins pipeline definitions.
 * @author Andrew Jarombek
 * @since 3/24/2020
 */

/**
 * Standard set of configurations (agents, triggers, and options) for a Jenkins job.
 */
def defaultConfig() {
    masterAgent()
    dailyTigger()
    defaultOptions()
}

/**
 * Set the job agent as the master (the VM/Container that the Jenkins server is running on).
 */
def masterAgent() {
    agent {
        label 'master'
    }
}

/**
 * Trigger to run the job daily while the Jenkins server is running (the Jenkins server is run on a schedule to save
 * resources/money).
 */
def dailyTigger() {
    triggers {
        cron('H 0 * * *')
    }
}

/**
 * Standard set of options for a Jenkins job.
 */
def defaultOptions() {
    options {
        ansiColor('xterm')
        timeout(time: 1, unit: 'HOURS')
        buildDiscarder(
            logRotator(daysToKeepStr: '10', numToKeepStr: '5')
        )
    }
}