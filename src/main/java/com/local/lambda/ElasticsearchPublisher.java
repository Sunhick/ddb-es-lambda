package com.local.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.model.User;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

@Log4j2
public class ElasticsearchPublisher implements RequestHandler<DynamodbEvent, String> {

    private static final String INSERT_EVENT = "INSERT";
    private static final String UPDATE_EVENT = "MODIFY";
    private static final String DELETE_EVENT = "REMOVE";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final DynamoDBMapper mapper;
    private final JestClient jest;

    public ElasticsearchPublisher() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        mapper = new DynamoDBMapper(client);

        final String ES_ENDPOINT = System.getenv("ES_ENDPOINT");
        HttpClientConfig httpClientConfig = new HttpClientConfig.Builder(ES_ENDPOINT)
                .multiThreaded(true).build();

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(httpClientConfig);
        jest = factory.getObject();
    }

    @Override
    public String handleRequest(DynamodbEvent input, Context context) {
        log.debug("About to publish entry into the elastic search. Count: {}",
                input.getRecords().size());

        for (DynamodbEvent.DynamodbStreamRecord record : input.getRecords()){
            log.debug("EventId: {} EventName: {} Entry: {}", record.getEventID(),
                    record.getEventName(), record.getDynamodb().toString());
            log.debug("Event source arn: {}", record.getEventSourceARN());

            processRecord(record.getEventName(), record.getDynamodb());
        }

        return null;
    }

    private void processRecord(String eventName, StreamRecord record) {
        switch (eventName) {
            case INSERT_EVENT:
            case UPDATE_EVENT:
                upsert(record.getNewImage());
                break;

            default:
                break;
        }
    }

    private void upsert(Map<String, AttributeValue> record) {
        User user = mapper.marshallIntoObject(User.class, record);
        log.info(user);

        try {
            final Index.Builder builder = new Index.Builder(OBJECT_MAPPER.writeValueAsString(user))
                    .index("user")
                    .type("user")
                    .id(user.getId().toString());

            final Index putRequest = builder.build();

            jest.execute(putRequest);
        } catch (final Exception e) {
            log.error("Error in adding to es", e);
        }
    }
}