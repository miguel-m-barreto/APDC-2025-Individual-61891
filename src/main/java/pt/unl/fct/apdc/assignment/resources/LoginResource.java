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
	private static final String LOG_MESSAGE_UNKNOW_USER = "Failed login attempt for username: ";

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
	 * Simple login
	 * Este método é usado para fazer login de um utilizador no sistema. 
	 * Ele recebe um JSON com os dados do utilizador (username e password) e verifica se o utilizador existe na base de dados. 
	 * Se o utilizador existir, ele verifica se a password está correta. 
	 * Se a password estiver correta, ele retorna um token de autenticação. 
	 * Se a password estiver errada ou o utilizador não existir, ele retorna um erro 403 (Forbidden).
	 */
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	// POST http://localhost:8080/rest/login/v1
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

	/*
	 * Login with time stamp
	 * Este método é usado para fazer login de um utilizador no sistema.
	 * Ele recebe um JSON com os dados do utilizador (username e password) e verifica se o utilizador existe na base de dados.
	 * Se o utilizador existir, ele verifica se a password está correta.
	 * Se a password estiver correta, ele atualiza o tempo de login do utilizador na base de dados e retorna um token de autenticação.
	 * Se a password estiver errada ou o utilizador não existir, ele retorna um erro 403 (Forbidden).
	 */
	@POST
	@Path("/v1a")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doLoginV1a(LoginData data) {
		LOG.fine(LOG_MESSAGE_LOGIN_ATTEMP + data.username);

		Key userKey = userKeyFactory.newKey(data.username);

		Entity user = datastore.get(userKey);
		if (user != null) {
			String hashedPWD = (String) user.getString(USER_PWD);
			if (hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
				user = Entity.newBuilder(user)
						.set(USER_LOGIN_TIME, Timestamp.now())
						.build();
				datastore.update(user);
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

	/*
	 * Login with time stamp and log
	 * Este método é usado para fazer login de um utilizador no sistema.
	 * Ele recebe um JSON com os dados do utilizador (username e password) e verifica se o utilizador existe na base de dados.
	 * Se o utilizador existir, ele verifica se a password está correta.
	 * Se a password estiver correta, ele atualiza o tempo de login do utilizador na base de dados e retorna um token de autenticação.
	 * Se a password estiver errada ou o utilizador não existir, ele retorna um erro 403 (Forbidden).
	 * Este método também cria um log de login para o utilizador, que é armazenado na base de dados.
	 * O log contém o tempo de login do utilizador.
	 */
	@POST
	@Path("/v1b")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doLoginV1b(LoginData data) {
		LOG.fine(LOG_MESSAGE_LOGIN_ATTEMP + data.username);

		Key userKey = userKeyFactory.newKey(data.username);

		Entity user = datastore.get(userKey);
		if (user != null) {
			String hashedPWD = user.getString(USER_PWD);
			if (hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
				KeyFactory logKeyFactory = datastore.newKeyFactory()
						.addAncestor(PathElement.of("User", data.username))
						.setKind("UserLog");
				Key logKey = datastore.allocateId(logKeyFactory.newKey());
				Entity userLog = Entity.newBuilder(logKey)
						.set(USER_LOGIN_TIME, Timestamp.now())
						.build();
				datastore.put(userLog);
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

	/*
	 * Criar um serviço REST que permita efetuar o login de um utilizador
	 * Recebe input em JSON com propriedades username e password
	 * Verifica se password é correta e devolve token
	 * 
	 * Cria um registo com log da conexão (com local da conexão e IP) 
	 * e atualiza contador com número de logins com sucesso e falhados
	 */
	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	// POST http://localhost:8080/rest/login/v2
	// To get parameters and headers from the request, we need to use @Context
	// @Context HttpServletRequest request
	// @Context HttpHeaders headers
	public Response doLoginV2(LoginData data,
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
				// Username does not exist
				LOG.warning(LOG_MESSAGE_LOGIN_ATTEMP + data.username);
				return Response.status(Status.FORBIDDEN)
						.entity(MESSAGE_INVALID_CREDENTIALS)
						.build();
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
						.set("user_stats_failed", 0L)
						.set("user_first_login", stats.getTimestamp("user_first_login"))
						.set("user_last_login", Timestamp.now())
						.build();

				// Batch operation
				txn.put(log, ustats);
				txn.commit();

				// Return token
				AuthToken token = new AuthToken(data.username);
				LOG.info(LOG_MESSAGE_LOGIN_SUCCESSFUL + data.username);
				return Response.ok(g.toJson(token)).build();
			} else {
				// Incorrect password
				// Copying here is even worse. Propose a better solution!
				Entity ustats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", stats.getLong("user_stats_logins"))
						// 1L means long value
						.set("user_stats_failed", stats.getLong("user_stats_failed") + 1L)
						.set("user_first_login", stats.getTimestamp("user_first_login"))
						.set("user_last_login", stats.getTimestamp("user_last_login"))
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
