package com.parking.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;

public class StreamHandler implements RequestHandler<KinesisEvent, Void> {
    @Override
    public Void handleRequest(KinesisEvent event, Context context) {
        event.getRecords().forEach(record -> {
            try {
                // Process stream event
                String data = new String(record.getKinesis().getData().array());
                System.out.println("Processing stream event: " + data);
            } catch (Exception e) {
                System.err.println("Error processing stream event: " + e.getMessage());
            }
        });
        return null;
    }
}
