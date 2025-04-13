package pt.unl.fct.apdc.assignment.util.datastore;

import com.google.cloud.datastore.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;

import java.util.List;
import java.util.Optional;

public class DatastoreLogin {

	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	/**
	 * Cria uma nova entidade de log de login do utilizador.
	 */
	public static Entity buildUserLog(String username, HttpServletRequest request, HttpHeaders headers, Key logKey) {
		return Entity.newBuilder(logKey)
				.set("user_login_ip", request.getRemoteAddr())
				.set("user_login_host", request.getRemoteHost())
				.set("user_login_latlon",
						Optional.ofNullable(headers.getHeaderString("X-AppEngine-CityLatLong"))
								.map(val -> StringValue.newBuilder(val).setExcludeFromIndexes(true).build())
								.orElse(StringValue.newBuilder("").setExcludeFromIndexes(true).build()))
				.set("user_login_city", headers.getHeaderString("X-AppEngine-City"))
				.set("user_login_country", headers.getHeaderString("X-AppEngine-Country"))
				.set("user_login_time", System.currentTimeMillis())
				.build();
	}

	/**
	 * Atualiza ou cria a entidade de estatísticas de login com sucesso.
	 */
	public static Entity buildUserStatsOnSuccess(Entity currentStats, Key statsKey) {
		if (currentStats == null) {
			long currentTime = System.currentTimeMillis();
			return Entity.newBuilder(statsKey)
					.set("user_stats_logins", 1L)
					.set("user_stats_failed", 0L)
					.set("user_first_login", currentTime)
					.set("user_last_login", currentTime)
					.build();
		}
		return Entity.newBuilder(statsKey)
				.set("user_stats_logins", currentStats.getLong("user_stats_logins") + 1)
				.set("user_last_login", System.currentTimeMillis())
				.build();
	}

	/**
	 * Atualiza ou cria a entidade de estatísticas em caso de falha de login.
	 */
	public static Entity buildUserStatsOnFailure(Entity currentStats, Key statsKey) {
		if (currentStats == null) {
			return Entity.newBuilder(statsKey)
					.set("user_stats_logins", 0L)
					.set("user_stats_failed", 1L)
					.set("user_first_login", System.currentTimeMillis())
					.set("user_last_login", System.currentTimeMillis())
					.build();
		}
		return Entity.newBuilder(statsKey)
				.set("user_stats_failed", currentStats.getLong("user_stats_failed") + 1)
				.set("user_last_attempt", System.currentTimeMillis())
				.build();
	}

	/**
	 * Cria a key para a entidade UserStats do utilizador.
	 */
	public static Key createStatsKey(String username) {
		return datastore.newKeyFactory()
				.addAncestors(PathElement.of("User", username))
				.setKind("UserStats")
				.newKey("counters");
	}

	/**
	 * Cria e aloca a key para a entidade UserLog do utilizador.
	 */
	public static Key createLogKey(String username) {
		return datastore.allocateId(
				datastore.newKeyFactory()
						.addAncestors(PathElement.of("User", username))
						.setKind("UserLog")
						.newKey());
	}	

	public static void deleteExpiredSessions(String username) {
		List<Entity> expiredSessions = DatastoreQuery.getExpiredSessions(username);
		for (Entity expired : expiredSessions) {
			datastore.delete(expired.getKey());
		}
	}
	
}