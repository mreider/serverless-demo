package com.parking.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;

public class NotificationHandler implements RequestHandler<SNSEvent, Void> {
    @Override
    public Void handleRequest(SNSEvent event, Context context) {
        event.getRecords().forEach(record -> {
            try {
                // Send notification (email/SMS)
                System.out.println("Processing notification: " + record.getSns().getMessage());
            } catch (Exception e) {
                System.err.println("Error processing notification: " + e.getMessage());
            }
        });
        return null;
    }
}
