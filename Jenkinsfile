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
        NEXUS_URL = 'http://13.201.62.107:8081'
        NEXUS_REPOSITORY = 'maven-releases1'
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
        
        stage('5. Security Scan') {
            steps {
                echo '========================================='
                echo '  Stage 5: OWASP Dependency Check        '
                echo '========================================='
                
                script {
                    sh '''
                        if [ -d "money-transfer" ]; then
                            cd money-transfer
                        fi
                        
                        echo "Scanning for vulnerable dependencies..."
                    '''
                    
                    // Run OWASP Dependency Check
                    dependencyCheck additionalArguments: '''
                        --scan .
                        --format HTML
                        --format XML
                        --prettyPrint
                        --enableExperimental
                        ''',
                        odcInstallation: 'DP-Check'
                    
                    // Publish Dependency-Check results
                    dependencyCheckPublisher pattern: '**/dependency-check-report.xml',
                                        failedTotalCritical: 0,
                                        failedTotalHigh: 0,
                                        unstableTotalCritical: 5,
                                        unstableTotalHigh: 10
                }
                
                echo '✅ Security scan completed'
            }
        }
        
        stage('6. Package') {
            steps {
                echo '========================================='
                echo '     Stage 6: Packaging Application      '
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
        
        stage('7. Push JAR to Nexus') {
    steps {
        echo '========================================='
        echo '  Stage 7: Uploading JAR to Nexus       '
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
                    echo "User: ${NEXUS_USER}"
                    
                    # Create directory structure and upload
                    curl -v -u ${NEXUS_USER}:${NEXUS_PASS} \
                        --upload-file ${JAR_FILE} \
                        ${NEXUS_URL}/repository/${NEXUS_REPOSITORY}/com/example/${APP_NAME}/${APP_VERSION}/${APP_NAME}-${APP_VERSION}.jar
                    
                        # Upload POM
                        cat > pom-upload.xml << EOF
                        <?xml version="1.0" encoding="UTF-8"?>
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>com.example</groupId>
                            <artifactId>${APP_NAME}</artifactId>
                            <version>${APP_VERSION}</version>
                            <packaging>jar</packaging>
                        </project>
                        EOF
                    
                    curl -v -u ${NEXUS_USER}:${NEXUS_PASS} \
                        --upload-file pom-upload.xml \
                        ${NEXUS_URL}/repository/${NEXUS_REPOSITORY}/com/example/${APP_NAME}/${APP_VERSION}/${APP_NAME}-${APP_VERSION}.pom
                    
                    echo ""
                    echo "✅ JAR uploaded successfully!"
                            echo "View at: ${NEXUS_URL}/#browse/browse:${NEXUS_REPOSITORY}"
                        '''
                    }
                }
            }
        }
        
        
        
        stage('10. Verify Nexus Uploads') {
            steps {
                echo '========================================='
                echo '  Stage 10: Verifying Nexus Artifacts   '
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
                    echo "REPORTS:"
                    echo "  - SonarQube: http://your-server:9000/dashboard?id=${SONAR_PROJECT_KEY}"
                    echo "  - Security Scan: Check OWASP Dependency-Check report in Jenkins"
                    echo "  - Test Results: Available in Jenkins UI"
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
            echo 'Check:'
            echo '  1. Console output for errors'
            echo '  2. Test results'
            echo '  3. Security vulnerabilities'
            echo '  4. Nexus connectivity'
        }
        
        always {
            echo 'Publishing security report...'
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: '.',
                reportFiles: 'dependency-check-report.html',
                reportName: 'OWASP Dependency Check',
                reportTitles: 'Security Scan'
            ])
            
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