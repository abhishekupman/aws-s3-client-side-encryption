package com.adobe.test;

import lombok.NonNull;
import lombok.Value;

@Value
public class BasicTestConfig {
    @NonNull String bucketName;
    @NonNull String putBucketKey;
    @NonNull String kmsKeyId;
}
