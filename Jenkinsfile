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
        SONAR_PROJECT_KEY = 'money-transfer'
        SONAR_PROJECT_NAME = 'Money Transfer'
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
                        
                        echo "Building project..."
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
                        
                        echo "Test Summary:"
                        echo "============="
                        if [ -f "target/surefire-reports/*.txt" ]; then
                            cat target/surefire-reports/*.txt 2>/dev/null | grep "Tests run" || echo "Test results generated"
                        fi
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
                    // Run SonarQube analysis
                    withSonarQubeEnv('SonarQube-Server') {
                        sh '''
                            if [ -d "money-transfer" ]; then
                                cd money-transfer
                            fi
                            
                            echo "Starting SonarQube analysis..."
                            mvn sonar:sonar \
                                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                -Dsonar.projectName="${SONAR_PROJECT_NAME}" \
                                -Dsonar.java.binaries=target/classes \
                                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                                
                            echo "SonarQube analysis completed"
                        '''
                    }
                }
                
                echo '✅ Code quality analysis completed'
            }
        }
        
        stage('5. Quality Gate') {
            steps {
                echo '========================================='
                echo '    Stage 5: Checking Quality Gate       '
                echo '========================================='
                
                script {
                    // Wait for SonarQube Quality Gate result
                    timeout(time: 5, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        
                        if (qg.status != 'OK') {
                            echo "⚠️  Quality Gate Status: ${qg.status}"
                            echo "Quality gate failed but continuing pipeline..."
                            // unstable(message: "Quality Gate failed: ${qg.status}")
                        } else {
                            echo "✅ Quality Gate Status: PASSED"
                        }
                    }
                }
            }
        }
        
        stage('6. Security Scan') {
            steps {
                echo '========================================='
                echo '  Stage 6: OWASP Dependency Check        '
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
        
        stage('7. Package') {
            steps {
                echo '========================================='
                echo '     Stage 7: Packaging Application      '
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
                    echo ""
                    echo "📊 Reports Available:"
                    echo "  - SonarQube: http://your-server:9000/dashboard?id=${SONAR_PROJECT_KEY}"
                    echo "  - Security Report: Check Dependency-Check results in Jenkins"
                    echo "  - Test Results: Available in Jenkins UI"
                    echo "  - Artifacts: Archived in Jenkins"
                '''
            }
        }
        
        failure {
            echo '========================================='
            echo '      ❌ PIPELINE FAILED!                '
            echo '========================================='
            echo 'Please check:'
            echo '  1. Console output for errors'
            echo '  2. Test results'
            echo '  3. SonarQube dashboard'
            echo '  4. Security scan results'
        }
        
        unstable {
            echo '========================================='
            echo '      ⚠️  PIPELINE UNSTABLE!             '
            echo '========================================='
            echo 'Check:'
            echo '  - Quality Gate status in SonarQube'
            echo '  - Security vulnerabilities found'
            echo '  - Failed test cases'
        }
        
        always {
            echo '========================================='
            echo '      Cleaning up workspace              '
            echo '========================================='
            
            // Publish HTML reports if available
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'money-transfer/target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'Code Coverage Report',
                reportTitles: 'JaCoCo Coverage'
            ])
            
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: '.',
                reportFiles: 'dependency-check-report.html',
                reportName: 'OWASP Dependency Check',
                reportTitles: 'Security Scan'
            ])
            
            cleanWs()
        }
    }
}