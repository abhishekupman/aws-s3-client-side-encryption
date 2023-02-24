package com.adobe.test;


import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.s3.AmazonS3EncryptionClientV2Builder;
import com.amazonaws.services.s3.AmazonS3EncryptionV2;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.*;
import com.amazonaws.services.s3.transfer.model.UploadResult;

import java.io.File;

public class S3MultipartUploadTest {
    public static final String BUCKET_NAME = "byos-s3-test-bucket";
    public static final String GET_OBJECT_KEY = "first_test.txt";
    public static final String PUT_OBJECT_KEY = "u2net_1.onnx";
    private static final String KMS_KEY = "703b2ecb-8c13-48b3-996f-31e0b279d1ea";
    private static final String PATH = "/Users/abhishekupman/Downloads/u2net.onnx";

    public static void main(String[] args) throws Exception {
        AmazonS3EncryptionV2 s3EncryptionV2 = getAWSS3EncryptionClient();

        TransferManager tfm = TransferManagerBuilder.standard().
                withS3Client(s3EncryptionV2).
                withMultipartUploadThreshold(10 * 1024 * 1024l).
                withMinimumUploadPartSize(30*1024*1024l).
                build();
        Upload upload = tfm.upload(BUCKET_NAME, PUT_OBJECT_KEY, new File(PATH));
        showTransferProgress(upload);
        UploadResult uploadResult = upload.waitForUploadResult();
        System.out.println(uploadResult);
    }

    private static AmazonS3EncryptionV2 getAWSS3EncryptionClient() {
        AWSKMS kmsClient = AWSKMSClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();
        return AmazonS3EncryptionClientV2Builder.standard()
                .withRegion(Regions.US_EAST_1)
                .withKmsClient(kmsClient)
                .withCryptoConfiguration(new CryptoConfigurationV2().withCryptoMode(CryptoMode.AuthenticatedEncryption))
                .withEncryptionMaterialsProvider(new KMSEncryptionMaterialsProvider(KMS_KEY))
                .build();
    }

    // Prints progress while waiting for the transfer to finish.
    private static void showTransferProgress(Transfer xfer) {
        // snippet-start:[s3.java1.s3_xfer_mgr_progress.poll]
        // print the transfer's human-readable description
        System.out.println(xfer.getDescription());
        // print an empty progress bar...
        // update the progress bar while the xfer is ongoing.
        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
            // Note: so_far and total aren't used, they're just for
            // documentation purposes.
            TransferProgress progress = xfer.getProgress();
            long so_far = progress.getBytesTransferred();
            long total = progress.getTotalBytesToTransfer();
            double pct = progress.getPercentTransferred();
            System.out.println("completed percentage is "+pct);
        } while (!xfer.isDone());
        // print the final state of the transfer.
        Transfer.TransferState xfer_state = xfer.getState();
        System.out.println(": " + xfer_state);
    }

}
