

## Running the Application
```bash
mvn spring-boot:run
```

## Testing the Application
```bash
curl http://localhost:8080/api/transfers/health
``` 


## Accessing the H2 Database Console
```bash
http://localhost:8080/h2-console
```
JDBC URL: jdbc:h2:mem:transferdb
Username: sa
Password: (leave blank)

## Sample Transfer Request
```bash
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": 1,
    "toAccountId": 2,
    "amount": 1000.00,
    "description": "Test transfer"
  }'
```




## Unit Tests
```bash
# Run all tests
mvn test
# Run only TransferServiceTest
mvn test -Dtest=TransferServiceTest

# Run only FeeCalculationServiceTest
mvn test -Dtest=FeeCalculationServiceTest

# Run only ValidationServiceTest
mvn test -Dtest=ValidationServiceTest

# Run single test method
mvn test -Dtest=TransferServiceTest#testSuccessfulTransfer_WithFee

# Run multiple specific tests
mvn test -Dtest=TransferServiceTest#testSuccessfulTransfer_WithFee,testTransferFails_InsufficientBalance

# Detailed output
mvn test -X

# Show test names as they run
mvn test -Dsurefire.printSummary=true

# Generate HTML test report
mvn clean test surefire-report:report

# Open test report
open target/site/surefire-report.html

# Generate coverage report
mvn clean test jacoco:report

# Open in browser (MacOS)
open target/site/jacoco/index.html

# Check coverage thresholds
mvn clean test jacoco:check