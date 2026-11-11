
pipeline {
    agent {
        label 'campusflow'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timeout(time: 45, unit: 'MINUTES')
    }

    parameters {
        booleanParam(
            name: 'DEPLOY_ENABLED',
            defaultValue: false,
            description: 'Enable deployment to the configured Kubernetes cluster.'
        )
    }

    environment {
        IMAGE_NAME = 'ghcr.io/tamojitachatterjee/campusflow'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.IMAGE_TAG = sh(
                        script: 'git rev-parse HEAD',
                        returnStdout: true
                    ).trim()
                }
                echo "Building CampusFlow image: ${env.IMAGE_NAME}:${env.IMAGE_TAG}"
            }
        }

        stage('Build and Test') {
            steps {
                sh '''
                    chmod +x mvnw
                    ./mvnw --batch-mode --no-transfer-progress verify
                '''
            }
            post {
                always {
                    archiveArtifacts(
                        artifacts: 'target/*.jar',
                        allowEmptyArchive: true
                    )
                }
            }
        }

        stage('Gitleaks Security Scan') {
            steps {
                sh 'gitleaks git . --redact --no-banner'
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build \
                      -t "$IMAGE_NAME:$IMAGE_TAG" \
                      .
                '''
            }
        }

        stage('Publish to GHCR') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'ghcr-pat',
                        usernameVariable: 'GHCR_USER',
                        passwordVariable: 'GHCR_TOKEN'
                    )
                ]) {
                    sh '''
                        set +x
                        echo "$GHCR_TOKEN" | docker login ghcr.io \
                          --username "$GHCR_USER" \
                          --password-stdin

                        docker push "$IMAGE_NAME:$IMAGE_TAG"

                        docker logout ghcr.io
                    '''
                }
            }
        }

        stage('Ansible Validation') {
            steps {
                sh '''
                    ansible-playbook --syntax-check ansible/deploy.yml
                '''
            }
        }

        stage('Kubernetes Deployment') {
            when {
                expression {
                    return params.DEPLOY_ENABLED
                }
            }
            steps {
                withCredentials([
                    file(
                        credentialsId: 'campusflow-kubeconfig',
                        variable: 'KUBECONFIG'
                    )
                ]) {
                    sh '''
                        export IMAGE_TAG="$IMAGE_TAG"
                        ansible-playbook ansible/deploy.yml
                    '''
                }
            }
        }

        stage('Deployment Health Verification') {
            when {
                expression {
                    return params.DEPLOY_ENABLED
                }
            }
            steps {
                sh '''
                    kubectl rollout status deployment/campusflow \
                      --timeout=300s

                    kubectl get pods
                    kubectl get services campusflow
                '''
            }
        }
    }

    post {
        success {
            echo "CampusFlow pipeline completed successfully."
            echo "Image: ${env.IMAGE_NAME}:${env.IMAGE_TAG}"
        }
        failure {
            echo 'CampusFlow pipeline failed. Check the stage logs.'
        }
        always {
            echo "Build result: ${currentBuild.currentResult}"
        }
    }
}
