package pt.unl.fct.apdc.assignment.util;

import com.google.cloud.storage.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MediaUtil {

	public static String uploadPhotoFromFile(String bucketName, String objectName, File file) throws IOException {
		Storage storage = StorageOptions.getDefaultInstance().getService();

		BlobId blobId = BlobId.of(bucketName, objectName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
				.setContentType("image/jpeg")
				.build();

		storage.create(blobInfo, new FileInputStream(file));

		return "https://storage.googleapis.com/" + bucketName + "/" + objectName;
	}
}
