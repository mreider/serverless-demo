package com.parking.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.parking.models.*;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import com.google.gson.Gson;

public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final Gson gson = new Gson();
    private final SnsClient snsClient = SnsClient.builder().build();
    private final SqsClient sqsClient = SqsClient.builder().build();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        switch (input.getPath()) {
            case "/issue":
                return issueTicket(input);
            case "/pay":
                return processPayment(input);
            default:
                return createResponse(404, "Not Found");
        }
    }

    private APIGatewayProxyResponseEvent issueTicket(APIGatewayProxyRequestEvent input) {
        try {
            ParkingTicket ticket = ParkingTicket.builder()
                .ticketId(UUID.randomUUID().toString())
                .entryTime(Instant.now())
                .status("active")
                .build();

            // Publish to SNS
            snsClient.publish(b -> b
                .topicArn(System.getenv("SNS_NOTIFICATION_TOPIC"))
                .message(gson.toJson(ticket)));

            return createResponse(200, gson.toJson(ticket));
        } catch (Exception e) {
            return createResponse(500, e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent processPayment(APIGatewayProxyRequestEvent input) {
        try {
            Payment payment = gson.fromJson(input.getBody(), Payment.class);
            
            // Send to SQS
            sqsClient.sendMessage(b -> b
                .queueUrl(System.getenv("SQS_PAYMENT_QUEUE"))
                .messageBody(gson.toJson(payment)));

            return createResponse(200, "Payment processed");
        } catch (Exception e) {
            return createResponse(500, e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
    }
}
