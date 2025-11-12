pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }
    
    environment {
        APP_NAME = 'money-transfer'
        GITHUB_REPO = 'https://github.com/nagcloudlab/telus-digital.git'
        APP_VERSION = "${BUILD_NUMBER}"
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
                    // Display repository information
                    sh '''
                        echo "Repository: ${GITHUB_REPO}"
                        echo "Current Branch: main"
                        echo "Commit: $(git rev-parse --short HEAD)"
                        echo "Author: $(git log -1 --pretty=format:'%an')"
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
                        echo "Listing files:"
                        ls -la
                        
                        # Navigate to money-transfer directory if it exists
                        if [ -d "money-transfer" ]; then
                            echo "Found money-transfer directory"
                            cd money-transfer
                        fi
                        
                        echo "Building project..."
                        mvn clean compile -X
                    '''
                }
                
                echo '✅ Build completed successfully'
            }
        }
        
        stage('3. Test') {
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
                        
                        echo "Test Summary:"
                        echo "============="
                        if [ -f "target/surefire-reports/*.txt" ]; then
                            cat target/surefire-reports/*.txt | grep "Tests run"
                        fi
                    '''
                }
                
                echo '✅ Tests completed successfully'
            }
            
            post {
                always {
                    // Publish test results
                    junit allowEmptyResults: true, 
                          testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('4. Package') {
            steps {
                echo '========================================='
                echo '     Stage 4: Packaging Application      '
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
            
            post {
                success {
                    // Archive the artifacts
                    archiveArtifacts artifacts: '**/target/*.jar',
                                   fingerprint: true,
                                   allowEmptyArchive: false
                }
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
                    echo "Build Number: ${BUILD_NUMBER}"
                    echo "Application: ${APP_NAME}"
                    echo "Artifacts archived in Jenkins"
                '''
            }
        }
        
        failure {
            echo '========================================='
            echo '      ❌ PIPELINE FAILED!                '
            echo '========================================='
            echo 'Please check the console output for errors'
        }
        
        unstable {
            echo '========================================='
            echo '      ⚠️  PIPELINE UNSTABLE!             '
            echo '========================================='
            echo 'Some tests may have failed'
        }
        
        always {
            echo '========================================='
            echo '         Cleaning up workspace           '
            echo '========================================='
            cleanWs()
        }
    }
}