package pt.unl.fct.apdc.assignment.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.Response.Status;

@Path("/media")
public class MediaResource {

	@GET
	@Path("/download/{bucket}/{object}")
	public Response downloadFile(@PathParam("bucket") String bucket, @PathParam("object") String object) {

		Storage storage = StorageOptions.getDefaultInstance().getService();
		Blob blob = storage.get(BlobId.of(bucket, object));

		StreamingOutput stream = new StreamingOutput() {

			@Override
			public void write(OutputStream output) throws IOException {

				// Download object to the output stream. See Google's documentation.
				blob.downloadTo(output);
				output.flush();
			}
		};

		return Response.ok(stream)
				.header("Content-Type", blob.getContentType())
				.build();
	}

	@GET
	@Path("/download2/{bucket}/{object}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadFile2(@PathParam("bucket") String bucket, @PathParam("object") String object) {

		Storage storage = StorageOptions.getDefaultInstance().getService();
		Blob blob = storage.get(BlobId.of(bucket, object));

		return Response.ok(blob.getContent())
				.header("Content-Type", blob.getContentType())
				.build();
	}

	@SuppressWarnings("deprecation")
	@POST
	@Path("/upload/{bucket}/{object}")
	public Response uploadFile(@PathParam("bucket") String bucket, @PathParam("object") String object,
			@HeaderParam("Content-Type") String contentType,
			@Context HttpServletRequest request) {

		// Upload to Google Cloud Storage (see Google's documentation)
		Storage storage = StorageOptions.getDefaultInstance().getService();
		BlobId blobId = BlobId.of(bucket, object);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
		// The following is deprecated since it is better to upload directly to GCS from
		// the client
		try {
			storage.create(blobInfo, request.getInputStream());
			return Response.ok().build();
		} catch (IOException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@SuppressWarnings("deprecation")
	@POST
	@Path("/upload/public/{bucket}/{object}")
	public Response uploadPublicFile(@PathParam("bucket") String bucket, @PathParam("object") String object,
			@HeaderParam("Content-Type") String contentType,
			@Context HttpServletRequest request) {

		// Upload to Google Cloud Storage (see Google's documentation)
		Storage storage = StorageOptions.getDefaultInstance().getService();
		BlobId blobId = BlobId.of(bucket, object);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType)
					.setAcl(Collections.singletonList(Acl.newBuilder(Acl.User.ofAllUsers(),Acl.Role.READER).build()))
					.build();
		// The following is deprecated since it is better to upload directly to GCS from
		// the client
		try {
			storage.create(blobInfo, request.getInputStream());
			return Response.ok().build();
		} catch (IOException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@SuppressWarnings("deprecation")
	@POST
	@Path("/upload/public/auto/{bucket}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadWithAutoName(@PathParam("bucket") String bucket,
									@HeaderParam("Content-Type") String contentType,
									@Context HttpServletRequest request) {
		Storage storage = StorageOptions.getDefaultInstance().getService();

		String objectName = "userphoto-" + java.util.UUID.randomUUID();  // auto filename
		BlobId blobId = BlobId.of(bucket, objectName);

		BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
				.setContentType(contentType)
				.setAcl(Collections.singletonList(
						Acl.newBuilder(Acl.User.ofAllUsers(), Acl.Role.READER).build()))
				.build();

		try {
			storage.create(blobInfo, request.getInputStream());

			String publicUrl = "https://storage.googleapis.com/" + bucket + "/" + objectName;
			JsonObject response = new JsonObject();
			response.addProperty("url", publicUrl);
			response.addProperty("object", objectName);

			return Response.ok(response.toString()).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity("Erro ao fazer upload.").build();
		}
	}

}
