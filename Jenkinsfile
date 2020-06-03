pipeline {
    agent {
    kubernetes {
      defaultContainer 'jnlp'
      yaml """
apiVersion: v1
kind: Pod
metadata:
labels:
  component: ci
spec:
  # Use service account that can deploy to all namespaces
  serviceAccountName: default
  containers:
  - name: docker
    image: docker:1.11
    command: ['cat']
    tty: true
    volumeMounts:
    - name: dockersock
      mountPath: /var/run/docker.sock
  - name: kubectl
    image: gcr.io/cloud-builders/kubectl
    command:
    - cat
    tty: true
  volumes:
  - name: dockersock
    hostPath:
      path: /var/run/docker.sock
"""
}
  }
    

    environment {
        PROJECT = "k8s-the-hard-way-277515"
        APP_NAME = "producer"
        CLUSTER = "gke-cluster"
        CLUSTER_ZONE = "europe-west1"
        CONTAINER_REG="gcr.io/k8s-the-hard-way-277515"
        IMAGE_TAG = "gcr.io/${PROJECT}/${APP_NAME}"
        JENKINS_CRED = "jenkins-gcr"
    }
    stages {

        stage("Build Docker Image"){
            
            steps {
                container('docker') {
                    echo "Building Docker image...${CONTAINER_REG}/${APP_NAME}"
                    script {
                        app = docker.build("${CONTAINER_REG}/${APP_NAME}", "${APP_NAME}/.")                
                    }
                    
                    echo "Docker image built."
                }
            }
        }

        stage("Get image Version"){
            steps {
                container('docker') {
                    echo "Getting Version from file."
                    script {
                        version = readFile "${APP_NAME}/VERSION"
                    }
                
                    echo "Done. The version is ${version}."
                }
            }
            
        }

        stage('Push image') {
            steps {
                container('docker') {
                    echo "Pushing Version ${version} to GCR..."
                    script {
                        docker.withRegistry('https://gcr.io', 'gcr:jenkins-gcr') {
                        app.push(version)
                        }
                    }              
                    echo "done"
                }
            }          
        }

        stage('Deploy Canary') {
            // Production branch
            when { branch 'canary' }
            steps{
                container('kubectl') {
                echo "Deploy  Canary."
                sh("sed -i.bak 's#gcr.io/k8s-the-hard-way-277515/benthos-producer:1.0.0#${IMAGE_TAG}:${version}#' ./${APP_NAME}/kubernetes/canary/*.yaml")
                step([$class: 'KubernetesEngineBuilder', namespace:'canary', projectId: env.PROJECT, clusterName: env.CLUSTER, zone: env.CLUSTER_ZONE, manifestPattern: "${APP_NAME}/kubernetes/canary", credentialsId: env.JENKINS_CRED, verifyDeployments: true])
                step([$class: 'KubernetesEngineBuilder', namespace:'canary', projectId: env.PROJECT, clusterName: env.CLUSTER, zone: env.CLUSTER_ZONE, manifestPattern: "${APP_NAME}/kubernetes/servicemonitor", credentialsId: env.JENKINS_CRED, verifyDeployments: false])
                echo "done"
                }
            }
        }

        stage('Deploy Production') {
            // Production branch
            when { branch 'master' }
            steps{
                container('kubectl') {
                echo "Deploy to Production."
                sh("sed -i.bak 's#gcr.io/k8s-the-hard-way-277515/benthos-producer:1.0.0#${IMAGE_TAG}:${version}#' ./${APP_NAME}/kubernetes/prod/*.yaml")
                step([$class: 'KubernetesEngineBuilder', namespace:'production', projectId: env.PROJECT, clusterName: env.CLUSTER, zone: env.CLUSTER_ZONE, manifestPattern: "${APP_NAME}/kubernetes/prod", credentialsId: env.JENKINS_CRED, verifyDeployments: true])
                step([$class: 'KubernetesEngineBuilder', namespace:'production', projectId: env.PROJECT, clusterName: env.CLUSTER, zone: env.CLUSTER_ZONE, manifestPattern: "${APP_NAME}/kubernetes/servicemonitor", credentialsId: env.JENKINS_CRED, verifyDeployments: false])
                echo "Done."
                }
            }
        }    
    }
}