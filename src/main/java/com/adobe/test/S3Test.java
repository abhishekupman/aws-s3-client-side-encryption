package com.adobe.test;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3EncryptionClientV2Builder;
import com.amazonaws.services.s3.AmazonS3EncryptionV2;
import com.amazonaws.services.s3.model.CryptoConfigurationV2;
import com.amazonaws.services.s3.model.CryptoMode;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.Copy;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.util.IOUtils;

import java.io.IOException;

public class S3Test {


    public static String doEncryptedPutAndGet(BasicTestConfig config, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        logger.log("start putting data to S3 with client side encryption "+config.getBucketName()+" object "+ config.getPutBucketKey());
        AmazonS3EncryptionV2 s3EncryptionV2 = getAWSS3EncryptionClient(config.getKmsKeyId());
        String dataToPut = "Hi. This is some safe data !!";
        s3EncryptionV2.putObject(config.getBucketName(), config.getPutBucketKey(), dataToPut);
        logger.log("data "+dataToPut+ " has been uploaded to "+config.getBucketName());

        S3Object object = s3EncryptionV2.getObject(config.getBucketName(), config.getPutBucketKey());
        String dataGot = IOUtils.toString(object.getObjectContent());
        logger.log("data "+dataGot+ " has been received from s3 path "+config.getPutBucketKey());
        assert dataGot.equals(dataToPut);
        return dataGot;
    }

    public static void doLargeEncryptedObjectCopy(CopyTestConfig config, Context context) throws IOException, InterruptedException {
        LambdaLogger logger = context.getLogger();
        logger.log("copy large copy from path "+config.getSourceObjectPath() +" to "+config.getDestinationObjectPath());
        AmazonS3EncryptionV2 s3EncryptionV2 = getAWSS3EncryptionClient(config.getKmsKeyId());
        TransferManager tfm = TransferManagerBuilder.standard().withS3Client(s3EncryptionV2).build();
        Copy copyOp = tfm.copy(config.getBucketName(), config.getSourceObjectPath(), config.getBucketName(), config.getDestinationObjectPath());
        while (!copyOp.isDone()) {
            Thread.sleep(100);
            double percentTransferred = copyOp.getProgress().getPercentTransferred();
            logger.log("Percentage complete" + percentTransferred);
        }
        logger.log("done copy to destination");
    }
    private static AmazonS3EncryptionV2 getAWSS3EncryptionClient(String kmsKeyId) {
        return AmazonS3EncryptionClientV2Builder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCryptoConfiguration(new CryptoConfigurationV2().withCryptoMode(CryptoMode.StrictAuthenticatedEncryption))
                .withEncryptionMaterialsProvider(new KMSEncryptionMaterialsProvider(kmsKeyId))
                .build();
    }


}
