#!/usr/bin/groovy

/**
 * Functions used to perform Git/GitHub operations from Jenkins.
 * @author Andrew Jarombek
 * @since 3/15/2020
 */

/**
 * Clone a repository and checkout a specific branch from my GitHub account.
 * @param repository The repository in my GitHub account to clone.
 * @param branch The branch to checkout once cloned.
 */
def basicClone(String repository, String branch = 'master') {
    checkout([
        $class: 'GitSCM',
        branches: [[name: "*/$branch"]],
        doGenerateSubmoduleConfigurations: false,
        extensions: [],
        submoduleCfg: [],
        userRemoteConfigs: [[
            credentialsId: 'ajarombek-github',
            url: "git@github.com:AJarombek/${repository}.git"
        ]]
    ])
}