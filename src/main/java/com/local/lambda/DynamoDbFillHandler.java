package com.local.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.local.model.User;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
public class DynamoDbFillHandler implements RequestHandler<Object, Void> {

    private DynamoDBMapper mapper;

    public DynamoDbFillHandler() {
        AmazonDynamoDB asyncClient = AmazonDynamoDBAsyncClientBuilder.standard().build();
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        mapper = new DynamoDBMapper(asyncClient);
    }

    @Override
    public Void handleRequest(Object input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log(String.format("Received: {0}", input));

        List<User> items = new ArrayList<>();

        String startValue = System.getenv("START_VALUE");
        String endValue = System.getenv("END_VALUE");

        Long counter = Long.parseLong(startValue);
        Long upperBound = Long.parseLong(endValue);

        log.info("Start value: {} end value: {}", counter, upperBound);
        while (counter++ < upperBound) {
            User user = User.builder()
                    .id(counter)
                    .name("dyns-" + UUID.randomUUID())
                    .country("US")
                    .gender("Male")
                    .build();

            items.add(user);
        }

        log.info("Batch updating the DDB");

        List<DynamoDBMapper.FailedBatch> failedItems = mapper.batchSave(items);

        if (failedItems.size() >= 1) {
            log.info("Failed Item count: {} Reason: {}", failedItems.size(), failedItems.get(0).getException());
        } else {
            log.info("Successfully inserted items into DB. count: {}", items.size());
        }

        return null;
    }
}
