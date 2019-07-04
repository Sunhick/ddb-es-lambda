package com.local.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
@ToString
@DynamoDBTable(tableName="user-table")
public class User {
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.N)
    @DynamoDBHashKey(attributeName="id")
    @Getter
    private Long id;

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    @DynamoDBRangeKey(attributeName = "name")
    @Getter
    private String name;

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    @DynamoDBAttribute(attributeName="country")
    @Getter
    private String country;

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    @DynamoDBAttribute(attributeName="gender")
    @Getter
    private String gender;
}
