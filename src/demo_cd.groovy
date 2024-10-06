@Library("shared-lib") _

def podYaml = libraryResource "pod-build-container.yaml"

def imageName = ""
def imageVersion = ""

pipeline{
    agent {
        kubernetes {
            yaml podYaml
            defaultContainer 'docker'
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
                container('jnlp') {
                    script{
                        gitCheckout("git@github.com:WizzzOzzz-Ori/python_app_for_demo.git", "main")
                    }
                }
            }
        }

        stage("Gather metadata"){
            steps{
                script{
                    def versionsYaml = readYaml file: "kubernetes/versions.yaml"
                    imageName = versionsYaml.app_py.image_name
                    imageVersion = versionsYaml.app_py.image_version
                    echo "imageName ${imageName}\nimageVersion ${imageVersion}"
                    def appPodYaml = "\"${readFile "kubernetes/app_pod.yaml"}\""
                    appPodYaml = envsubst(appPodYaml, [image_name: imageName, image_version: imageVersion])
                    writeFile file: "kubernetes/app_pod.yaml", text: appPodYaml
                }
            }
        }

        stage("Deploy pod"){
            steps{
                container("kubectl"){
                    script{
                        withCredentials([file(credentialsId: 'kubeconfig', variable: 'kubeconfig')]){
                            sh "kubectl apply -f kubernetes/app_pod.yaml --kubeconfig ${kubeconfig}"
                            sh "kubectl apply -f kubernetes/app_service.yaml --kubeconfig ${kubeconfig}"
                        }
                    }
                }
            }
        }
    }
}