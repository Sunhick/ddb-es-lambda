#!/usr/bin/env bash

aws cloudformation validate-template \
    --template-body file:///Users/sunilmur/Alexa/ExpDynamoDb/src/ExpDynamoDb/DynamoDb.yaml


aws cloudformation create-stack --stack-name Exp-DynamoDb-stack \
    --template-body file:///Users/sunilmur/Alexa/ExpDynamoDb/src/ExpDynamoDb/DynamoDb.yaml \
    --parameters ParameterKey=TableName,ParameterValue=user-table


aws cloudformation delete-stack --stack-name Exp-DynamoDb-stack


aws cloudformation create-stack --stack-name Exp-Lambda-stack \
    --template-body file:///Users/sunilmur/Alexa/ExpDynamoDb/src/ExpDynamoDb/Lambda.yaml \
    --parameters ParameterKey=TableName,ParameterValue=user-table


aws cloudformation delete-stack --stack-name Exp-Lambda-stack

aws cloudformation validate-template \
    --template-body file:///Users/sunilmur/Alexa/ExpDynamoDb/src/ExpDynamoDb/Lambda.yaml

./gradlew build && aws s3 cp build/distributions/ExpDynamoDbLambda-1.0.zip s3://lambdazip98/ExpDynamoDbLambda-1.0.zip

aws s3api create-bucket --bucket lambda-zip --region us-east-2


aws cloudformation create-stack --stack-name Exp-Lambda-stack \
    --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM \
    --template-body file:///Users/sunilmur/Alexa/ExpDynamoDb/src/ExpDynamoDb/Lambda.yaml \
    --parameters ParameterKey=S3BucketName,ParameterValue=lambdazip98 \
     ParameterKey=S3KeyName,ParameterValue=ExpDynamoDbLambda-1.0.zip

aws cloudformation update-stack --stack-name Exp-ES-stack \
    --template-body file:///Users/sunilmur/Alexa/ExpDynamoDb/src/ExpDynamoDb/Elasticsearch.yaml

aws lambda invoke --function-name DynamoDbFillerLambda --invocation-type Event --payload "{\"resources\": [\"Sunil\"]}" out.txt