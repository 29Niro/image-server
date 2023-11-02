package com.example.imageserver;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class AzureBlobStorageService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.blob-endpoint}")
    private String blobEndpoint;

    public BlobContainerClient getContainerClient(String containerName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        return blobServiceClient.getBlobContainerClient(containerName);
    }

    public void uploadImage(String containerName, String blobName, InputStream imageStream) throws IOException {
        BlobContainerClient containerClient = getContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(imageStream, imageStream.available());
    }

    public BlobClient getImage(String containerName, String blobName) {
        BlobContainerClient containerClient = getContainerClient(containerName);
        return containerClient.getBlobClient(blobName);
    }

    public List<String> getAllImageUrls(String containerName) {
        BlobContainerClient containerClient = getContainerClient(containerName);
        List<String> imageUrls = new ArrayList<>();

        for (BlobItem blobItem : containerClient.listBlobs()) {
            String imageUrl = blobEndpoint + "/" + containerName + "/" + blobItem.getName();
            imageUrls.add(imageUrl);
        }

        return imageUrls;
    }

    public void deleteImage(String containerName, String blobName) {
        BlobContainerClient containerClient = getContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.delete();
    }

    public void deleteImages(String containerName, List<String> blobNames) {
        BlobContainerClient containerClient = getContainerClient(containerName);
        for (String blobName : blobNames) {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.delete();
        }
    }


}
