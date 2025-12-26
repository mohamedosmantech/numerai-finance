pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = credentials('docker-registry-url')
        DOCKER_CREDENTIALS = credentials('docker-registry-credentials')
        IMAGE_NAME = 'fincalc-pro'
        IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
        SONAR_TOKEN = credentials('sonar-token')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    tools {
        jdk 'jdk-21'
        maven 'maven-3.9'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_BRANCH_NAME = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                }
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean compile -B -DskipTests'
            }
        }

        stage('Unit Tests') {
            steps {
                sh './mvnw test -B'
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                    jacoco execPattern: '**/target/jacoco.exec'
                }
            }
        }

        stage('Code Quality') {
            parallel {
                stage('SonarQube Analysis') {
                    when {
                        anyOf {
                            branch 'main'
                            branch 'develop'
                        }
                    }
                    steps {
                        withSonarQubeEnv('SonarQube') {
                            sh './mvnw sonar:sonar -Dsonar.token=${SONAR_TOKEN}'
                        }
                    }
                }

                stage('OWASP Dependency Check') {
                    steps {
                        sh './mvnw dependency-check:check -B || true'
                    }
                    post {
                        always {
                            dependencyCheckPublisher pattern: 'target/dependency-check-report.xml'
                        }
                    }
                }
            }
        }

        stage('Package') {
            steps {
                sh './mvnw package -B -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}")
                    docker.build("${DOCKER_REGISTRY}/${IMAGE_NAME}:latest")
                }
            }
        }

        stage('Push Docker Image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch pattern: 'release/*', comparator: 'GLOB'
                }
            }
            steps {
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-registry-credentials') {
                        docker.image("${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}").push()
                        if (env.GIT_BRANCH_NAME == 'main') {
                            docker.image("${DOCKER_REGISTRY}/${IMAGE_NAME}:latest").push()
                        }
                    }
                }
            }
        }

        stage('Deploy to Dev') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    deployToKubernetes('dev', IMAGE_TAG)
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                branch pattern: 'release/*', comparator: 'GLOB'
            }
            steps {
                script {
                    deployToKubernetes('staging', IMAGE_TAG)
                }
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to Production?', ok: 'Deploy'
                script {
                    deployToKubernetes('prod', IMAGE_TAG)
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            slackSend(
                color: 'good',
                message: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            )
        }
        failure {
            slackSend(
                color: 'danger',
                message: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            )
        }
    }
}

def deployToKubernetes(String environment, String imageTag) {
    withKubeConfig(credentialsId: "kubeconfig-${environment}") {
        sh """
            kubectl set image deployment/fincalc-pro \
                fincalc-pro=${DOCKER_REGISTRY}/${IMAGE_NAME}:${imageTag} \
                -n fincalc-${environment}
            kubectl rollout status deployment/fincalc-pro -n fincalc-${environment} --timeout=300s
        """
    }
}
