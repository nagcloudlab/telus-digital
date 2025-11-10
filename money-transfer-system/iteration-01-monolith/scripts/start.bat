@echo off
echo ======================================
echo QuickPay Money Transfer System
echo Iteration 1 - Monolithic Application
echo ======================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java is not installed. Please install Java 17 or higher.
    pause
    exit /b 1
)

echo ✅ Java is installed
echo.

echo 🏗️  Building application...
cd ..\quickpay-monolith

REM Clean and build
call mvnw.cmd clean package -DskipTests

if %errorlevel% neq 0 (
    echo ❌ Build failed
    pause
    exit /b 1
)

echo.
echo ✅ Build successful
echo.
echo 🚀 Starting QuickPay application...
echo    Access URL: http://localhost:8080
echo    Health Check: http://localhost:8080/api/health
echo.
echo    Press Ctrl+C to stop
echo.

REM Start application
java -jar target\quickpay-monolith-1.0.0.jar