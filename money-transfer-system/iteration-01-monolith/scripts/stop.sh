#!/bin/bash

echo "🛑 Stopping QuickPay application..."

# Find and kill the process
PID=$(ps aux | grep 'quickpay-monolith' | grep -v grep | awk '{print $2}')

if [ -z "$PID" ]; then
    echo "ℹ️  Application is not running"
else
    kill -9 $PID
    echo "✅ Application stopped (PID: $PID)"
fi