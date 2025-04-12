package pt.unl.fct.apdc.assignment.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.cloud.storage.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;


public class MediaUtil {

	private static final Logger LOG = Logger.getLogger(MediaUtil.class.getName());
	private static final String BUCKET_NAME = "shining-expanse-453014-c4.appspot.com";

	public static String uploadPhotoFromFile(String bucketName, String objectName, File file) throws IOException {
		Storage storage = StorageOptions.getDefaultInstance().getService();
	
		objectName = objectName.replace("\\", "/"); // Normalizar caminhos
		BlobId blobId = BlobId.of(bucketName, objectName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
				.setContentType("image/gif") // or "image/jpeg", etc
				.build();
	
		storage.create(blobInfo, new FileInputStream(file));
		// Set public read access so image can be accessed via direct URL
		storage.createAcl(blobId, Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)); // 👈 important
	
		return "https://storage.googleapis.com/" + bucketName + "/" + objectName;
	}
	

	public static String resolvePhotoUrlOrUpload(String objectName, String pathOrUrl, String username, Logger LOG) {
		if (pathOrUrl == null || pathOrUrl.isEmpty()) {
			LOG.info("No photo path or URL provided.");
			return null;
		}

		if (pathOrUrl.toLowerCase().startsWith("http")) {
			try {
				new URL(pathOrUrl).toURI(); // validates URL format
				LOG.info("Valid URL provided: " + pathOrUrl);
				return pathOrUrl;
			} catch (Exception e) {
				return null;
			}
		} else {
			File file = new File(pathOrUrl);
			if (file.exists()) {
				try {
					String fullObjectName = "users/" + username + "/" + file.getName();
					LOG.warning("Invalid URL format for photo: " + pathOrUrl);
					return uploadPhotoFromFile(BUCKET_NAME, fullObjectName, file);
				} catch (IOException e) {
					LOG.log(Level.WARNING, "Failed to upload photo from path: " + pathOrUrl, e);
					return null;
				}
			} else {
				LOG.warning("Photo file not found at path: " + pathOrUrl);
				return null;
			}
		}
	}
}
