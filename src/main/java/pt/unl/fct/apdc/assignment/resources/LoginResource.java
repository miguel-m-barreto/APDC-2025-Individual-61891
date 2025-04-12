package pt.unl.fct.apdc.assignment.resources;


import java.util.logging.Logger;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.apdc.assignment.util.AuthToken;
import pt.unl.fct.apdc.assignment.util.LoginData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreLoginUtil;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueryUtil;


@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final String MESSAGE_INVALID_CREDENTIALS = "Incorrect username or password.";
	private static final String MESSAGE_USER_NOT_FOUND = "User does not exist.";

	private static final String LOG_MESSAGE_LOGIN_ATTEMP = "Login attempt by user: ";
	private static final String LOG_MESSAGE_LOGIN_SUCCESSFUL = "Login successful by user: ";
	private static final String LOG_MESSAGE_WRONG_PASSWORD = "Wrong password for: ";
	private static final String LOG_MESSAGE_UNKNOWN_USER = "Failed login attempt for user: ";

	private static final String USER_PWD = "user_pwd";
	
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


	private final Gson g = new Gson();

	public LoginResource() {
		
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data,
							@Context HttpServletRequest request,
							@Context HttpHeaders headers) {
		LOG.fine(LOG_MESSAGE_LOGIN_ATTEMP + data.identifier);

		// Tenta obter o utilizador por username
		Optional<Entity> userOpt = DatastoreQueryUtil.getUserByUsername(data.identifier);

		// Se não encontrar, tenta por email
		if (userOpt.isEmpty()) {
			userOpt = DatastoreQueryUtil.getUserByEmail(data.identifier);
			if (userOpt.isEmpty()) {
				LOG.warning(LOG_MESSAGE_UNKNOWN_USER + data.identifier);
				return Response.status(Status.FORBIDDEN).entity(MESSAGE_USER_NOT_FOUND).build();
			}
		}

		Entity user = userOpt.get();
		String username = user.getKey().getName(); // username verdadeiro

		Transaction txn = datastore.newTransaction();
		try {
			// Verifica a password
			String hashedPWD = user.getString(USER_PWD);
			if (!hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
				LOG.warning(LOG_MESSAGE_WRONG_PASSWORD + data.identifier);
				
				// Se o utilizador não existir, não faz nada
				// Se o utilizador existir, atualiza o número de tentativas falhadas
				Key statsKey = DatastoreLoginUtil.createStatsKey(username);
				Entity currentStats = txn.get(statsKey);
				Entity updatedStats = DatastoreLoginUtil.buildUserStatsOnFailure(currentStats, statsKey);
	
				txn.put(updatedStats);
				txn.commit();
				return Response.status(Status.FORBIDDEN).entity(MESSAGE_INVALID_CREDENTIALS).build();
			}

			// Login bem-sucedido
			Key statsKey = DatastoreLoginUtil.createStatsKey(username);
			Key logKey = DatastoreLoginUtil.createLogKey(username);
	
			Entity currentStats = txn.get(statsKey);
			Entity updatedStats = DatastoreLoginUtil.buildUserStatsOnSuccess(currentStats, statsKey);
			Entity userLog = DatastoreLoginUtil.buildUserLog(username, request, headers, logKey);
	
			txn.put(updatedStats, userLog);
			txn.commit();
	
			String role = user.getString("user_role");
			AuthToken token = new AuthToken(username, role);
	
			JsonObject responseJson = new JsonObject();
			responseJson.addProperty("user", username);
			responseJson.addProperty("role", role);
			responseJson.addProperty("message", "Bem-vindo, " + username + "! Estás autenticado como " + role + ".");
			responseJson.add("token", g.toJsonTree(token));
	
			LOG.info(LOG_MESSAGE_LOGIN_SUCCESSFUL + username);
			return Response.ok(responseJson.toString()).build();
		} catch (Exception e) {
			if (txn.isActive()) txn.rollback();
			LOG.severe("Erro no login: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}



	/*
	 * serviço REST que permita obter as horas dos logins das últimas 24 horas, 3 de cada vez
	 *//*
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
	}*/


}
