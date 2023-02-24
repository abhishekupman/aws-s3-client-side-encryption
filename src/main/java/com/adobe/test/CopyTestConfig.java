package com.adobe.test;

import lombok.NonNull;
import lombok.Value;

@Value
public class CopyTestConfig {
    @NonNull String bucketName;
    @NonNull String sourceObjectPath;
    @NonNull String destinationObjectPath;
    @NonNull String kmsKeyId;
}
