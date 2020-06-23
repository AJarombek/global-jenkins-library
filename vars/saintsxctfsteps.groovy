#!/usr/bin/groovy

/**
 * Jenkins pipeline steps used in SaintsXCTF pipelines.
 * @author Andrew Jarombek
 * @since 6/22/2020
 */

def buildImage(String subDir, String zipFilename, String imageName) {
    dir("repos/saints-xctf-auth/$subDir") {
        sh """
            sudo docker image build \
                -f ../Dockerfile \
                -t python-lambda-dist:latest \
                --network=host \
                --build-arg ZIP_FILENAME=$zipFilename .

            sudo docker image build -t $imageName:latest .
        """
    }
}

def pushImage(String imageName, String imageLabel, boolean isLatest) {
    withCredentials([
        usernamePassword(
            credentialsId: 'ajarombek-docker-hub',
            passwordVariable: 'dockerPassword',
            usernameVariable: 'dockerUsername'
        )
    ]) {
        sh 'sudo docker login -u $dockerUsername -p $dockerPassword'
    }

    if (isLatest) {
        sh """
            sudo docker image tag $imageName:latest ajarombek/$imageName:latest
            sudo docker push ajarombek/$imageName:latest
        """
    }

    sh """
        sudo docker image tag $imageName:latest ajarombek/$imageName:$imageLabel
        sudo docker push ajarombek/$imageName:$imageLabel
    """
}