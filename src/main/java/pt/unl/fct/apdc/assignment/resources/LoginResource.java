package pt.unl.fct.apdc.assignment.resources;

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
import com.google.gson.JsonObject;


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
import pt.unl.fct.apdc.assignment.util.AuthToken;
import pt.unl.fct.apdc.assignment.util.LoginData;



@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final String MESSAGE_INVALID_CREDENTIALS = "Incorrect username or password.";
	private static final String MESSAGE_NEXT_PARAMETER_INVALID = "Request parameter 'next' must be greater or equal to 0.";

	private static final String LOG_MESSAGE_LOGIN_ATTEMP = "Login attempt by user: ";
	private static final String LOG_MESSAGE_LOGIN_SUCCESSFUL = "Login successful by user: ";
	private static final String LOG_MESSAGE_WRONG_PASSWORD = "Wrong password for: ";
	private static final String LOG_MESSAGE_UNKNOW_USER = "Failed login attempt for user: ";

	private static final String USER_PWD = "user_pwd";
	private static final String USER_LOGIN_TIME = "user_login_time";
	
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	private final Gson g = new Gson();

	public LoginResource() {
		
	}
	

	@GET
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response checkUsernameAvailable(LoginData data) {
		LOG.fine(LOG_MESSAGE_LOGIN_ATTEMP + data.username);
		
		Key userKey = userKeyFactory.newKey(data.username);
		Entity user = datastore.get(userKey);
		if (user != null && user.getString(USER_PWD).equals(DigestUtils.sha512Hex(data.password))) {
			LOG.info(LOG_MESSAGE_LOGIN_SUCCESSFUL + data.username);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1); // Adiciona 1 dia à data atual
			Timestamp yesterday = Timestamp.of(cal.getTime());

			Query<Entity> query = Query.newEntityQueryBuilder()
					.setKind("UserLog")
					.setFilter(
							CompositeFilter.and(
									PropertyFilter.hasAncestor(
										datastore.newKeyFactory().setKind("User").newKey(data.username)),
									PropertyFilter.ge(USER_LOGIN_TIME, yesterday)
									)
							)
					.setOrderBy(OrderBy.desc(USER_LOGIN_TIME))
					.setLimit(5) //5 respostas de cada vez
					.build();
			
			QueryResults<Entity> logs = datastore.run(query);

			List<Date> loginDates = new ArrayList<>();
			logs.forEachRemaining(userlog -> {
				loginDates.add(userlog.getTimestamp(USER_LOGIN_TIME).toDate());
			});

			return Response.ok(g.toJson(loginDates)).build();					
		} else {
			return Response.ok().entity(g.toJson(true)).build();
		}
		
	}

	/*
	 * Criar um serviço REST que permite efetuar o login de um utilizador
	 * Recebe input em JSON com propriedades username e password
	 * Verifica se password é correta e devolve token
	 * 
	 * Cria um registo com log da conexão (com local da conexão e IP) 
	 * e atualiza contador com número de logins com sucesso e falhados
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	// POST http://localhost:8080/rest/login/
	// To get parameters and headers from the request, we need to use @Context
	// @Context HttpServletRequest request
	// @Context HttpHeaders headers
	public Response doLogin(LoginData data,
			@Context HttpServletRequest request,
			@Context HttpHeaders headers) {
		LOG.fine(LOG_MESSAGE_LOGIN_ATTEMP + data.username);

		Key userKey = userKeyFactory.newKey(data.username);
		Key ctrsKey = datastore.newKeyFactory()
				.addAncestors(PathElement.of("User", data.username))
				.setKind("UserStats")
				.newKey("counters");
		// Generate automatically a key
		Key logKey = datastore.allocateId(
				datastore.newKeyFactory()
						.addAncestors(PathElement.of("User", data.username))
						.setKind("UserLog").newKey());

		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if (user == null) {
				/*
				// User does not exist
				LOG.warning(LOG_MESSAGE_UNKNOW_USER + data.username);
				return Response.status(Status.FORBIDDEN)
						.entity(MESSAGE_INVALID_CREDENTIALS)
						.build();*/

				// username does not exist, so we need to check if the email exists
				// We need to check if the email exists in the datastore
				Query<Entity> queryByEmail = Query.newEntityQueryBuilder()
				.setKind("User")
				.setFilter(PropertyFilter.eq("user_email", data.username.toLowerCase()))
				.build();

				QueryResults<Entity> results = txn.run(queryByEmail);
				if (results.hasNext()) {
					user = results.next();
					userKey = user.getKey(); // Atualiza a key para ser usada mais à frente
					data.username = user.getKey().getName(); // Atualiza o username para o username real para ser usado mais à frente
					//data.username = user.getString("user_name"); // Atualiza o username para o username real para ser usado mais à frente
					//data.username = user.getString("user_username"); // Atualiza o username para o username real para ser usado mais à frente
				} else {
					// User does not exist
					LOG.warning(LOG_MESSAGE_UNKNOW_USER + data.username);
					return Response.status(Status.FORBIDDEN)
							.entity(MESSAGE_INVALID_CREDENTIALS)
							.build();
				}
			}

			// We get the user stats from the storage
			Entity stats = txn.get(ctrsKey);
			if (stats == null) {
				stats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", 0L)
						.set("user_stats_failed", 0L)
						.set("user_first_login", Timestamp.now())
						.set("user_last_login", Timestamp.now())
						.build();
			}

			String hashedPWD = (String) user.getString(USER_PWD);
			if (hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
				// Login successful
				// Construct the logs
				String cityLatLong = headers.getHeaderString("X-AppEngine-CityLatLong");
				Entity log = Entity.newBuilder(logKey)
						// getRemoteAddr() returns the IP address of the client that sent the request
						// getRemoteHost() returns the hostname of the client that sent the request
						.set("user_login_ip", request.getRemoteAddr())
						.set("user_login_host", request.getRemoteHost())
						.set("user_login_latlon", cityLatLong != null
						//? means if cityLatLong is not null, then set it to the value of cityLatLong, otherwise set it to an empty string
						// : means if cityLatLong is null, then set it to an empty string
								? StringValue.newBuilder(cityLatLong).setExcludeFromIndexes(true).build()
								: StringValue.newBuilder("").setExcludeFromIndexes(true).build())
						.set("user_login_city", headers.getHeaderString("X-AppEngine-City"))
						.set("user_login_country", headers.getHeaderString("X-AppEngine-Country"))
						.set("user_login_time", Timestamp.now())
						.build();

				// Get the user statistics and updates it
				// Copying information every time a user logins may not be a good solution
				// (why?)
				Entity ustats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", stats.getLong("user_stats_logins") + 1)
						.set("user_last_login", Timestamp.now())
						.build();

				// Batch operation
				txn.put(log, ustats);
				txn.commit();

				String role = user.getString("user_role");

				// Return token
				AuthToken token = new AuthToken(data.username, role);

				JsonObject responseJson = new JsonObject();
				responseJson.addProperty("user", data.username);
				responseJson.addProperty("role", role);
				responseJson.addProperty("message", "Bem-vindo, " + data.username + "! Estás autenticado como " + role + ".");
				responseJson.add("token", g.toJsonTree(token)); // inclui o token completo

				LOG.info(LOG_MESSAGE_LOGIN_SUCCESSFUL + data.username);
				return Response.ok(responseJson.toString()).build();
			} else {
				// Incorrect password
				// Copying here is even worse. Propose a better solution!
				Entity ustats = Entity.newBuilder(ctrsKey)
						.set("user_stats_failed", stats.getLong("user_stats_failed") + 1L)
						.set("user_last_attempt", Timestamp.now())
						.build();

				txn.put(ustats);
				txn.commit();
				LOG.warning(LOG_MESSAGE_WRONG_PASSWORD + data.username);
				return Response.status(Status.FORBIDDEN).entity(MESSAGE_INVALID_CREDENTIALS).build();
			}
		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}


	/*
	 * serviço REST que permita obter as horas dos logins das últimas 24 horas, 3 de cada vez
	 */
	@POST
	@Path("/user/pagination")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLatestLogins(@QueryParam("next") String nextParam, LoginData data) {

		int next;

		// Checking for valid request parameter values
		try {
			next = Integer.parseInt(nextParam);
			if (next < 0)
				return Response.status(Status.BAD_REQUEST).entity(MESSAGE_NEXT_PARAMETER_INVALID).build();
		} catch (NumberFormatException e) {
			return Response.status(Status.BAD_REQUEST).entity(MESSAGE_NEXT_PARAMETER_INVALID).build();
		}

		Key userKey = userKeyFactory.newKey(data.username);

		Entity user = datastore.get(userKey);
		if (user != null && user.getString(USER_PWD).equals(DigestUtils.sha512Hex(data.password))) {

			// Get the date of yesterday
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -1);
			Timestamp yesterday = Timestamp.of(cal.getTime());

			Query<Entity> query = Query.newEntityQueryBuilder()
					.setKind("UserLog")
					.setFilter(
							CompositeFilter.and(
									PropertyFilter.hasAncestor(
											datastore.newKeyFactory().setKind("User").newKey(data.username)),
									PropertyFilter.ge(USER_LOGIN_TIME, yesterday)))
					.setOrderBy(OrderBy.desc(USER_LOGIN_TIME))
					.setLimit(3)
					.setOffset(next)
					.build();
			QueryResults<Entity> logs = datastore.run(query);

			List<Date> loginDates = new ArrayList<Date>();
			logs.forEachRemaining(userlog -> {
				loginDates.add(userlog.getTimestamp(USER_LOGIN_TIME).toDate());
			});

			return Response.ok(g.toJson(loginDates)).build();
		}
		return Response.status(Status.FORBIDDEN).entity(MESSAGE_INVALID_CREDENTIALS)
				.build();
	}


}
