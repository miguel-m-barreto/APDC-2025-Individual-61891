package pt.unl.fct.apdc.assignment.resources;

import java.io.IOException;
import java.io.File;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.appengine.repackaged.com.google.rpc.context.AttributeContext.Resource;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.apdc.assignment.util.LoginData;
import pt.unl.fct.apdc.assignment.util.MediaUtil;
import pt.unl.fct.apdc.assignment.util.RegisterData;

@Path("/register")
public class RegisterResource {

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final String ROLE_STRING = "ENDUSER";
	private static final String STATE_STRING = "DESATIVADA";
	private static final String DEFAULT_USER_PHOTO = "https://storage.cloud.google.com/shining-expanse-453014-c4.appspot.com/default_user.jpeg";
	
	private final Gson g = new Gson();


	public RegisterResource() {}	// Default constructor, nothing to do		

	/*
	 * This method is used to register a user in the system. It receives a JSON object
	 * with the user data (username, password, email and name),
	 * and it creates a new entity in the datastore with the user
	 * data. The password is hashed using SHA-512 algorithm.
	 * the creation time is also stored
	 * Better implementation of registerUserv4
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerUser(RegisterData data) {
		LOG.fine("Attempt to register user: " + data.username);
		
		if(!data.validRegistration())
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
		
		
		
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);

			Query<Entity> query = Query.newEntityQueryBuilder()
				.setKind("User")
				.setFilter(StructuredQuery.PropertyFilter.eq("user_email", data.email))
				.build();

			QueryResults<Entity> results = datastore.run(query);
			if (results.hasNext()) {
				return Response.status(Status.CONFLICT)
					.entity("Email already in use.").build();
			}

			data.photo = MediaUtil.resolvePhotoUrlOrUpload( data.photo,
															data.username,
															LOG);

			Entity user = Entity.newBuilder(userKey)
			.set("user_name", data.name)
			.set("user_pwd", DigestUtils.sha512Hex(data.password))
			.set("user_email", data.email)
			.set("user_phone", data.phone)
			.set("user_profile", data.profileType)
			.set("user_role", ROLE_STRING)
			.set("user_account_state", STATE_STRING)
			.set("user_creation_time", Timestamp.now())
			// optional fields
			.set("user_cc", data.cc != null ? data.cc : "")
			.set("user_nif", data.nif != null ? data.nif : "")
			.set("user_employer", data.employer != null ? data.employer : "")
			.set("user_job", data.job != null ? data.job : "")
			.set("user_address", data.address != null ? data.address : "")
			.set("user_employer_nif", data.employerNIF != null ? data.employerNIF : "")
			.set("user_photo_url", data.photo != null ? data.photo : DEFAULT_USER_PHOTO)
			.build();

			datastore.add(user);
			LOG.info("User registered " + data.username);
			
			return Response.ok().build();
		}
		catch(DatastoreException e) {
			LOG.log(Level.ALL, e.toString());
			return Response.status(Status.BAD_REQUEST).entity(e.getReason()).build();
		}
	}
}
