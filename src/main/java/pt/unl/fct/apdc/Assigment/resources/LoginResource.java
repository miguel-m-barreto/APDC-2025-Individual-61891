package pt.unl.fct.apdc.assigment.resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.apdc.assigment.util.AuthToken;
import pt.unl.fct.apdc.assigment.util.LoginData;



@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final String MESSAGE_INVALID_CREDENTIALS = "Incorrect username or password.";
	private static final String MESSAGE_NEXT_PARAMETER_INVALID = "Request parameter 'next' must be greater or equal to 0.";

	private static final String LOG_MESSAGE_LOGIN_ATTEMP = "Login attempt by user: ";
	private static final String LOG_MESSAGE_LOGIN_SUCCESSFUL = "Login successful by user: ";
	private static final String LOG_MESSAGE_WRONG_PASSWORD = "Wrong password for: ";
	private static final String LOG_MESSAGE_UNKNOW_USER = "Failed login attempt for username: ";

	private static final String USER_PWD = "user_pwd";
	private static final String USER_LOGIN_TIME = "user_login_time";
	
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	private final Gson g = new Gson();

	public LoginResource() {
		
	}
	
	//JAKARTA
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	// POST http://localhost:8080/rest/login/
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);
		
		if(data.username.equals("almiscar") && data.password.equals("123")) { 
			AuthToken at = new AuthToken(data.username);
			return Response.ok(g.toJson(at)).build();
		}

		// Se o username ou password estiverem errados, retorna um erro 403 (Forbidden)
		// 403 - Forbidden: O servidor entendeu o pedido, mas recusa-se a autorizá-lo.
		return Response.status(Status.FORBIDDEN)
						.entity("Incorrect username or password.")
						.build();

		// 	response.ok().build() dá return do code HTTP 200 
		/* 
		*	return Response.ok(new AuthToken(data.username)).build();
		*/
	}
	
	/* 	
	*	Valores entre {} no @PATH representam variáveis, ou seja 
	*	valores de entrada que são enviados no URL
	*/
	@GET
	@Path("/{username}") // diz que o username é um parâmetro de entrada e vai ser passado na URL
	// GET http://localhost:8080/rest/login/almiscar é suposto retornar false
	// false = not available
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		if (username.trim().equals("almiscar")) {
			return Response.ok().entity(g.toJson(false)).build();					
		} else {
			return Response.ok().entity(g.toJson(true)).build();
		}
		
	}

	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doLoginV1(LoginData data) {
		LOG.fine(LOG_MESSAGE_LOGIN_ATTEMP + data.username);

		Key userKey = userKeyFactory.newKey(data.username);

		Entity user = datastore.get(userKey);
		if (user != null) {
			String hashedPWD = (String) user.getString(USER_PWD);
			if (hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
				LOG.info(LOG_MESSAGE_LOGIN_SUCCESSFUL + data.username);
				AuthToken token = new AuthToken(data.username);
				return Response.ok(g.toJson(token)).build();
			} else {
				LOG.warning(LOG_MESSAGE_WRONG_PASSWORD + data.username);
				return Response.status(Status.FORBIDDEN)
						.entity(MESSAGE_INVALID_CREDENTIALS)
						.build();
			}
		} else {
			LOG.warning(LOG_MESSAGE_UNKNOW_USER + data.username);
			return Response.status(Status.FORBIDDEN)
					.entity(MESSAGE_INVALID_CREDENTIALS)
					.build();
		}
	}


}
