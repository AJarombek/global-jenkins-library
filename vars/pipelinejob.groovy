#!/usr/bin/groovy

/**
 * Functions used to shorten Jenkins pipeline definitions.
 * @author Andrew Jarombek
 * @since 3/24/2020
 */

/**
 *
 * @param config
 */
def testApplicationPipeline(Map config) {

    // 'agent' configuration
    def agentLabel = config.agent.label ?: 'master'

    // 'triggers' configuration
    def triggersCron = config.triggers.cron ?: 'H 0 * * *'

    // 'options' configuration
    def timeoutLength = config.options.time ?: 1
    def timeoutUnit = config.options.unit ?: 'HOURS'
    def daysToKeepStr = config.options.daysToKeepStr ?: '10'
    def numToKeepStr = config.options.numToKeepStr ?: '5'

    // 'stages' configuration
    def repository = config.stages.repository ?: ''
    def branch = config.stages.branch ?: 'master'
    def setupProjectScript = config.stages.setupProjectScript ?: { -> sh('echo "Setup Project Stage"')}
    def executeTestsScript = config.stages.executeTestsScript ?: { -> sh('echo "Execute Tests Stage"')}

    // 'post' configuration
    def postScript = config.post.script ?: { -> sh('echo "Post Script"')}

    pipeline {
        agent {
            label agentLabel
        }
        triggers {
            cron(triggersCron)
        }
        options {
            ansiColor('xterm')
            timeout(time: timeoutLength, unit: timeoutUnit)
            buildDiscarder(
                logRotator(daysToKeepStr: daysToKeepStr, numToKeepStr: numToKeepStr)
            )
        }
        stages {
            stage("Clean Workspace") {
                steps {
                    script {
                        cleanWs()
                    }
                }
            }
            stage("Checkout Repository") {
                steps {
                    script {
                        git.basicClone(repository, branch)
                    }
                }
            }
            stage("Setup Project") {
                steps {
                    script {
                        setupProjectScript()
                    }
                }
            }
            stage("Execute Tests") {
                steps {
                    script {
                        executeTestsScript()
                    }
                }
            }
        }
        post {
            always {
                script {
                    postScript()
                }
            }
        }
    }
}