@Library("shared-lib") _

def podYaml = libraryResource "pod-build-container.yaml"

pipeline{
    // agent any
    agent {
        kubernetes {
            yaml podYaml
        }
    }
    

    stages{
        stage("clean Workspace"){
            steps{
                script{
                    sh "rm -rf * .git"
                }
            }
        }

        stage("git checkout"){
            steps{
                script{
                    gitCheckout("git@github.com:WizzzOzzz-Ori/python_app_for_demo.git", "main")
                }
            }
        }

        stage("build docker"){
            steps{
                container('docker') {
                    script{
                        sh "ls -l /var/run/docker.sock"
                        dockerBuild()
                    }
                }
            }
        }
    }
}