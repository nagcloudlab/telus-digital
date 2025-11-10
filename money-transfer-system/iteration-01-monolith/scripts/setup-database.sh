#!/bin/bash

echo "======================================"
echo "QuickPay - Database Setup"
echo "======================================"
echo ""

# Database configuration
DB_NAME="quickpay_db"
DB_USER="quickpay_user"
DB_PASSWORD="quickpay_password"

echo "🗄️  Setting up PostgreSQL database..."
echo ""

# Check if PostgreSQL is installed
if ! command -v psql &> /dev/null; then
    echo "❌ PostgreSQL is not installed"
    echo "   Install PostgreSQL from: https://www.postgresql.org/download/"
    exit 1
fi

echo "✅ PostgreSQL is installed"
echo ""

# Create database and user
echo "📝 Creating database and user..."
sudo -u postgres psql <<EOF
-- Drop if exists
DROP DATABASE IF EXISTS $DB_NAME;
DROP USER IF EXISTS $DB_USER;

-- Create user
CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';

-- Create database
CREATE DATABASE $DB_NAME OWNER $DB_USER;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;

\q
EOF

if [ $? -eq 0 ]; then
    echo "✅ Database created successfully"
    echo ""
    echo "📊 Database Details:"
    echo "   Database: $DB_NAME"
    echo "   User: $DB_USER"
    echo "   Password: $DB_PASSWORD"
    echo "   Host: localhost"
    echo "   Port: 5432"
    echo ""
    echo "🔗 Connection String:"
    echo "   jdbc:postgresql://localhost:5432/$DB_NAME"
else
    echo "❌ Database creation failed"
    exit 1
fi