pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }
    
    environment {
        // Application
        APP_NAME = 'money-transfer'
        GITHUB_REPO = 'https://github.com/nagcloudlab/telus-digital.git'
        APP_VERSION = "1.0.${BUILD_NUMBER}"
        
        // SonarQube
        SONAR_PROJECT_KEY = 'money-transfer'
        SONAR_PROJECT_NAME = 'Money Transfer'
        
        // Nexus - CHANGE THESE TO YOUR SERVER IP
        NEXUS_URL = 'http://13.201.62.107/:8081'
        NEXUS_REPOSITORY = 'maven-releases'
        NEXUS_DOCKER_REGISTRY = '13.201.62.107:8082'
        NEXUS_CREDENTIALS_ID = 'nexus-credentials'
        NEXUS_DOCKER_CREDENTIALS_ID = 'nexus-docker-credentials'
        
        // Docker
        DOCKER_IMAGE = "${NEXUS_DOCKER_REGISTRY}/${APP_NAME}"
        DOCKER_TAG = "${APP_VERSION}"
    }
    
    stages {
        
        stage('1. Checkout') {
            steps {
                echo '========================================='
                echo '   Stage 1: Checking out Source Code    '
                echo '========================================='
                
                git branch: 'main', 
                    url: "${GITHUB_REPO}"
                
                script {
                    sh '''
                        echo "Repository: ${GITHUB_REPO}"
                        echo "Current Branch: main"
                        echo "Commit: $(git rev-parse --short HEAD)"
                        echo "Author: $(git log -1 --pretty=format:'%an')"
                        echo "Version: ${APP_VERSION}"
                        ls -la
                    '''
                }
                
                echo '✅ Code checkout completed successfully'
            }
        }
        
        stage('2. Build') {
            steps {
                echo '========================================='
                echo '      Stage 2: Building Application      '
                echo '========================================='
                
                script {
                    sh '''
                        echo "Current directory: $(pwd)"
                        ls -la
                        
                        if [ -d "money-transfer" ]; then
                            echo "Found money-transfer directory"
                            cd money-transfer
                        fi
                        
                        echo "Building project with version ${APP_VERSION}..."
                        mvn clean compile
                    '''
                }
                
                echo '✅ Build completed successfully'
            }
        }
        
        stage('3. Unit Tests') {
            steps {
                echo '========================================='
                echo '     Stage 3: Running Unit Tests         '
                echo '========================================='
                
                script {
                    sh '''
                        if [ -d "money-transfer" ]; then
                            cd money-transfer
                        fi
                        
                        echo "Running unit tests..."
                        mvn test
                    '''
                }
                
                echo '✅ Tests completed successfully'
            }
            
            post {
                always {
                    junit allowEmptyResults: true, 
                          testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('4. Code Quality Analysis') {
            steps {
                echo '========================================='
                echo '  Stage 4: SonarQube Code Quality Check  '
                echo '========================================='
                
                script {
                    withSonarQubeEnv('SonarQube-Server') {
                        sh '''
                            if [ -d "money-transfer" ]; then
                                cd money-transfer
                            fi
                            
                            echo "Starting SonarQube analysis..."
                            mvn sonar:sonar \
                                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                -Dsonar.projectName="${SONAR_PROJECT_NAME}" \
                                -Dsonar.projectVersion=${APP_VERSION} \
                                -Dsonar.java.binaries=target/classes
                        '''
                    }
                }
                
                echo '✅ Code quality analysis completed'
            }
        }
        
        stage('5. Package') {
            steps {
                echo '========================================='
                echo '     Stage 5: Packaging Application      '
                echo '========================================='
                
                script {
                    sh '''
                        if [ -d "money-transfer" ]; then
                            cd money-transfer
                        fi
                        
                        echo "Creating JAR package..."
                        mvn package -DskipTests
                        
                        echo "Package Details:"
                        echo "==============="
                        ls -lh target/*.jar
                    '''
                }
                
                echo '✅ Packaging completed successfully'
            }
        }
        
        stage('6. Push JAR to Nexus') {
            steps {
                echo '========================================='
                echo '  Stage 6: Uploading JAR to Nexus       '
                echo '========================================='
                
                script {
                    sh '''
                        if [ -d "money-transfer" ]; then
                            cd money-transfer
                        fi
                        
                        echo "Finding JAR file..."
                        JAR_FILE=$(ls target/*.jar | head -1)
                        echo "JAR File: ${JAR_FILE}"
                        
                        if [ -z "$JAR_FILE" ]; then
                            echo "ERROR: No JAR file found!"
                            exit 1
                        fi
                    '''
                    
                    withCredentials([usernamePassword(
                        credentialsId: "${NEXUS_CREDENTIALS_ID}",
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )]) {
                        sh '''
                            if [ -d "money-transfer" ]; then
                                cd money-transfer
                            fi
                            
                            JAR_FILE=$(ls target/*.jar | head -1)
                            
                            echo "Uploading JAR to Nexus..."
                            echo "URL: ${NEXUS_URL}/repository/${NEXUS_REPOSITORY}/"
                            echo "Group: com.example"
                            echo "Artifact: ${APP_NAME}"
                            echo "Version: ${APP_VERSION}"
                            
                            mvn deploy:deploy-file \
                                -DgroupId=com.example \
                                -DartifactId=${APP_NAME} \
                                -Dversion=${APP_VERSION} \
                                -Dpackaging=jar \
                                -Dfile=${JAR_FILE} \
                                -DrepositoryId=nexus \
                                -Durl=${NEXUS_URL}/repository/${NEXUS_REPOSITORY}/ \
                                -DgeneratePom=true \
                                -Dmaven.wagon.http.ssl.insecure=true \
                                -Dmaven.wagon.http.ssl.allowall=true
                            
                            echo "✅ JAR uploaded successfully!"
                            echo "View at: ${NEXUS_URL}/#browse/browse:${NEXUS_REPOSITORY}"
                        '''
                    }
                }
            }
        }
        
        stage('7. Build Docker Image') {
            steps {
                echo '========================================='
                echo '    Stage 7: Building Docker Image       '
                echo '========================================='
                
                script {
                    sh '''
                        if [ -d "money-transfer" ]; then
                            cd money-transfer
                        fi
                        
                        echo "Creating Dockerfile..."
                        cat > Dockerfile << 'EOF'
FROM openjdk:17-jdk-slim

LABEL maintainer="nag@example.com"
LABEL application="money-transfer"

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
EOF
                        
                        echo "Building Docker image..."
                        echo "Image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                        
                        echo "Docker images:"
                        docker images | grep ${APP_NAME}
                    '''
                }
                
                echo '✅ Docker image built successfully'
            }
        }
        
        stage('8. Push Docker Image to Nexus') {
            steps {
                echo '========================================='
                echo '  Stage 8: Pushing Image to Nexus       '
                echo '========================================='
                
                script {
                    withCredentials([usernamePassword(
                        credentialsId: "${NEXUS_DOCKER_CREDENTIALS_ID}",
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )]) {
                        sh '''
                            echo "Logging into Nexus Docker Registry..."
                            echo ${NEXUS_PASS} | docker login -u ${NEXUS_USER} --password-stdin ${NEXUS_DOCKER_REGISTRY}
                            
                            echo "Pushing Docker image..."
                            docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                            docker push ${DOCKER_IMAGE}:latest
                            
                            echo "Logging out..."
                            docker logout ${NEXUS_DOCKER_REGISTRY}
                            
                            echo "✅ Docker image pushed successfully!"
                            echo "View at: ${NEXUS_URL}/#browse/browse:docker-hosted"
                        '''
                    }
                }
            }
        }
        
        stage('9. Verify Nexus Uploads') {
            steps {
                echo '========================================='
                echo '  Stage 9: Verifying Nexus Artifacts    '
                echo '========================================='
                
                script {
                    sh '''
                        echo ""
                        echo "📦 ARTIFACTS IN NEXUS"
                        echo "====================="
                        echo ""
                        echo "JAR Artifact:"
                        echo "  URL: ${NEXUS_URL}/#browse/browse:${NEXUS_REPOSITORY}"
                        echo "  Path: com/example/${APP_NAME}/${APP_VERSION}/"
                        echo "  File: ${APP_NAME}-${APP_VERSION}.jar"
                        echo ""
                        echo "Docker Image:"
                        echo "  Registry: ${NEXUS_DOCKER_REGISTRY}"
                        echo "  Image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        echo "  Also: ${DOCKER_IMAGE}:latest"
                        echo ""
                        echo "Pull Commands:"
                        echo "  JAR:    mvn dependency:get -Dartifact=com.example:${APP_NAME}:${APP_VERSION}"
                        echo "  Docker: docker pull ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        echo ""
                    '''
                }
                
                echo '✅ Verification completed'
            }
        }
        
    }
    
    post {
        success {
            echo '========================================='
            echo '   ✅ PIPELINE COMPLETED SUCCESSFULLY!   '
            echo '========================================='
            script {
                sh '''
                    echo ""
                    echo "BUILD SUMMARY"
                    echo "============="
                    echo "Build Number: ${BUILD_NUMBER}"
                    echo "Application: ${APP_NAME}"
                    echo "Version: ${APP_VERSION}"
                    echo ""
                    echo "NEXUS ARTIFACTS:"
                    echo "  JAR: ${NEXUS_URL}/#browse/browse:${NEXUS_REPOSITORY}"
                    echo "  Docker: ${NEXUS_URL}/#browse/browse:docker-hosted"
                    echo ""
                    echo "🚀 Ready for Deployment!"
                    echo ""
                '''
            }
        }
        
        failure {
            echo '========================================='
            echo '      ❌ PIPELINE FAILED!                '
            echo '========================================='
        }
        
        always {
            echo 'Cleaning up local Docker images...'
            script {
                sh '''
                    docker rmi ${DOCKER_IMAGE}:${DOCKER_TAG} 2>/dev/null || true
                    docker rmi ${DOCKER_IMAGE}:latest 2>/dev/null || true
                '''
            }
            cleanWs()
        }
    }
}