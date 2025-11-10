#!/bin/bash

echo "======================================"
echo "QuickPay Money Transfer System"
echo "Iteration 1 - Monolithic Application"
echo "======================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"
echo ""

# Check if PostgreSQL is running
echo "🔍 Checking PostgreSQL connection..."
if ! nc -z localhost 5432 2>/dev/null; then
    echo "⚠️  PostgreSQL is not running on localhost:5432"
    echo "   Please start PostgreSQL or update connection in application.properties"
    echo ""
    read -p "Continue anyway? (y/n): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo "✅ PostgreSQL is running"
fi

echo ""
echo "🏗️  Building application..."
cd ../quickpay-monolith

# Clean and build
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo ""
echo "✅ Build successful"
echo ""
echo "🚀 Starting QuickPay application..."
echo "   Access URL: http://localhost:8080"
echo "   Health Check: http://localhost:8080/api/health"
echo ""
echo "   Press Ctrl+C to stop"
echo ""

# Start application
java -jar target/quickpay-monolith-1.0.0.jar