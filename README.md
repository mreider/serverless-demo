# Parking Garage Serverless Application

A serverless application that manages parking tickets, payments, and real-time analytics using AWS Lambda, SNS, SQS, and Kinesis.

## Prerequisites

1. Install required tools:
   ```bash
   # Install Node.js (for Serverless Framework)
   curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
   sudo apt-get install -y nodejs

   # Install Java 11
   sudo apt-get update
   sudo apt-get install openjdk-11-jdk

   # Install Gradle
   sudo apt-get install gradle

   # Install AWS CLI
   curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
   unzip awscliv2.zip
   sudo ./aws/install

   # Install Serverless Framework
   npm install -g serverless
   ```

   Alternatively, for Mac users:
   ```bash
   # Install Homebrew (if not already installed)
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

   # Install Node.js (for Serverless Framework)
   brew install node

   # Install Java 11
   brew install openjdk@11
   
   # Link Java 11
   sudo ln -sfn $(brew --prefix)/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk

   # Install Gradle
   brew install gradle

   # Install AWS CLI
   brew install awscli

   # Install Serverless Framework
   npm install -g serverless
   ```

2. Configure AWS credentials:
   ```bash
   aws configure
   # Enter your AWS Access Key ID
   # Enter your AWS Secret Access Key
   # Enter default region (e.g., us-west-2)
   # Enter output format (json)
   ```

   Alternatively, you can manually create or edit ~/.aws/credentials:
   ```ini
   [default]
   aws_access_key_id = YOUR_ACCESS_KEY
   aws_secret_access_key = YOUR_SECRET_KEY
   region = us-west-2
   ```

## Project Setup

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd serverless-demo
   ```

2. Install project dependencies:
   ```bash
   gradle build
   ```

## Configuration

1. Update `serverless.yml` if needed:
   - Modify the region
   - Adjust function configurations
   - Change resource names

2. Environment variables are automatically set through serverless.yml:
   - SNS_NOTIFICATION_TOPIC
   - SQS_PAYMENT_QUEUE
   - KINESIS_STREAM

## Building

Build the project using Gradle:
```bash
gradle clean build shadowJar
```

This creates a JAR file at `build/libs/parking-garage.jar`

## Deployment

1. Deploy to AWS:
   ```bash
   serverless deploy
   ```

2. The deployment will create:
   - API Gateway endpoints
   - Lambda functions
   - SNS Topic
   - SQS Queue
   - Kinesis Stream

3. Note the output URLs and ARNs for:
   - API endpoints (/issue and /pay)
   - SNS Topic
   - SQS Queue
   - Kinesis Stream

## Testing

Test the API endpoints:

1. Issue a ticket:
   ```bash
   curl -X POST https://<api-id>.execute-api.<region>.amazonaws.com/dev/issue
   ```

2. Process a payment:
   ```bash
   curl -X POST https://<api-id>.execute-api.<region>.amazonaws.com/dev/pay \
   -H "Content-Type: application/json" \
   -d '{"ticketId":"123","amount":10.00,"status":"pending"}'
   ```

## Monitoring

1. View Lambda logs:
   ```bash
   serverless logs -f api
   serverless logs -f notification
   serverless logs -f streamProcessor
   ```

2. Monitor via AWS Console:
   - CloudWatch Logs
   - CloudWatch Metrics
   - X-Ray traces (if enabled)

## Cleanup

Remove all deployed resources:
```bash
serverless remove
```

## Architecture Components

1. **API Gateway + Lambda**
   - /issue endpoint: Creates parking tickets
   - /pay endpoint: Processes payments

2. **SNS Topic**
   - Handles notifications for ticket issuance and payments
   - Triggers notification Lambda function

3. **SQS Queue**
   - Manages payment processing queue
   - Handles payment validation and processing

4. **Kinesis Stream**
   - Processes real-time parking events
   - Handles analytics and monitoring

## Data Flow & Component Details

### Storage Components

1. **DynamoDB Tables** (automatically created)
   - `ParkingTickets`: Stores active and historical parking tickets
     - Primary key: ticketId
     - Fields: entryTime, status, parkingSpot
   - `Payments`: Stores payment records
     - Primary key: paymentId
     - Fields: ticketId, amount, timestamp, status

2. **SQS Queue**
   - Queue name: `${service}-${stage}-payments`
   - Stores pending payment transactions
   - Message retention: 14 days
   - Visibility timeout: 30 seconds

3. **SNS Topic**
   - Topic name: `${service}-${stage}-notifications`
   - Publishes events for:
     - Ticket issuance
     - Payment confirmation
     - Parking expiration alerts

4. **Kinesis Stream**
   - Stream name: `${service}-${stage}-events`
   - Real-time event data:
     - Vehicle entry/exit events
     - Occupancy updates
     - Payment events
   - Retention period: 24 hours

### Information Flow

1. **Ticket Issuance Flow**
   ```
   Client → API Gateway → /issue Lambda 
   → DynamoDB (save ticket)
   → SNS (notification) → Notification Lambda → Email/SMS
   → Kinesis (event logging)
   ```

2. **Payment Processing Flow**
   ```
   Client → API Gateway → /pay Lambda 
   → SQS Queue
   → Payment Processor Lambda → DynamoDB (update status)
   → SNS (confirmation) → Notification Lambda → Email/SMS
   → Kinesis (event logging)
   ```

3. **Analytics Flow**
   ```
   Sensors/Events → Kinesis Stream 
   → Stream Processor Lambda
   → CloudWatch Metrics
   → Analytics Dashboard
   ```

## API Specification

### 1. Issue Ticket Endpoint
- **URL**: `/issue`
- **Method**: `POST`
- **Request Body**: None
- **Response**:
  ```json
  {
    "ticketId": "uuid",
    "entryTime": "2023-01-01T00:00:00Z",
    "status": "active"
  }
  ```
- **Status Codes**:
  - 200: Success
  - 500: Server Error

### 2. Payment Endpoint
- **URL**: `/pay`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "ticketId": "string",
    "amount": number,
    "status": "pending"
  }
  ```
- **Response**:
  ```json
  {
    "message": "Payment processed",
    "paymentId": "uuid"
  }
  ```
- **Status Codes**:
  - 200: Success
  - 400: Invalid input
  - 404: Ticket not found
  - 500: Server Error

### 3. Ticket Status Endpoint
- **URL**: `/status/{ticketId}`
- **Method**: `GET`
- **Response**:
  ```json
  {
    "ticketId": "string",
    "entryTime": "datetime",
    "status": "active|paid|expired",
    "payment": {
      "amount": number,
      "timestamp": "datetime",
      "status": "pending|processed|failed"
    }
  }
  ```

## Rate Limits & Quotas

1. **API Gateway**
   - Burst limit: 1000 requests/second
   - Steady-state: 500 requests/second

2. **Lambda**
   - Concurrent executions: 1000
   - Memory: 256MB
   - Timeout: 30 seconds

3. **Kinesis**
   - Shards: 1
   - Throughput: 1MB/second per shard
   - Retention: 24 hours

## Troubleshooting

1. **Deployment Issues**
   - Ensure AWS credentials are correct
   - Check CloudFormation console for stack errors
   - Verify IAM permissions

2. **Runtime Issues**
   - Check CloudWatch Logs for each function
   - Verify environment variables
   - Check Lambda function timeouts

3. **Common Problems**
   - Cold starts: Adjust memory/power settings
   - Timeouts: Increase Lambda timeout values
   - Permission errors: Check IAM roles

## Security Considerations

1. **API Security**
   - Add API key authentication
   - Implement JWT validation
   - Use AWS WAF for additional protection

2. **Data Security**
   - Enable encryption at rest
   - Use KMS for sensitive data
   - Implement proper IAM policies

## Performance Optimization

1. **Lambda Functions**
   - Adjust memory allocation
   - Enable provisioned concurrency
   - Optimize cold starts

2. **Stream Processing**
   - Adjust batch size
   - Configure retry policies
   - Monitor shard count

## Development Workflow

1. Local Development:
   ```bash
   # Run tests
   gradle test

   # Local build
   gradle build

   # Package
   gradle shadowJar
   ```

2. Deployment Stages:
   ```bash
   # Deploy to dev
   serverless deploy --stage dev

   # Deploy to production
   serverless deploy --stage prod
   ```