package com.adobe.test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class LambdaMethodHandler implements RequestHandler<Map<String,String>, String> {

    @Override
    public String handleRequest(Map<String,String> event, Context context) {
        LambdaLogger logger = context.getLogger();
        String response = "200 OK";
        // log execution details
        try {
            if (event.get("test_type") == null) {
                throw new RuntimeException("test_type not provided");
            } else if (event.get("test_type").equals("basic")) {
                BasicTestConfig config = getBasicTestConfig(event);
                S3Test.doEncryptedPutAndGet(config, context);
            } else if (event.get("test_type").equals("copy")) {
                CopyTestConfig copyTestConfig = getCopyTestConfig(event);
                S3Test.doLargeEncryptedObjectCopy(copyTestConfig, context);
            } else {
                throw new RuntimeException("unknown test_type provided "+event.get("test_type"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return response;
    }

    private CopyTestConfig getCopyTestConfig(Map<String,String> event) {
        return new CopyTestConfig(
                event.get("bucket_name"),
                event.get("source_path"),
                event.get("destination_path"),
                event.get("kms_key_id")
        );
    }

    private BasicTestConfig getBasicTestConfig(Map<String,String> event) {
        return new BasicTestConfig(
                event.get("bucket_name"),
                event.get("put_object_key"),
                event.get("kms_key_id")
        );
    }
}