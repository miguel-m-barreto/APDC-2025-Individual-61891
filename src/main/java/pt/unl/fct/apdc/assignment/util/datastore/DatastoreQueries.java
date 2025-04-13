package pt.unl.fct.apdc.assignment.util.datastore;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import jakarta.ws.rs.core.Response;
import pt.unl.fct.apdc.assignment.util.AuthToken;

import com.google.cloud.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static pt.unl.fct.apdc.assignment.util.StringUtil.normalizeEmail;

//  Optional<T> is a container object introduced in Java 8,
//  which may or may not contain a non-null value. 
//  It helps avoid NullPointerException and makes it clear when a value might be missing.

//this class is used to interact with the Google Cloud Datastore
//  and perform operations such as querying for users, getting user login dates, etc.
//  It uses the Google Cloud Datastore Java client library to perform these operations.
import java.util.Optional;

public class DatastoreQueries {

	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private static final KeyFactory sessionKeyFactory = datastore.newKeyFactory().setKind("Session");

	private static final String EMAIL_FIELD = "user_email";
	private static final String PHONE_FIELD = "user_phone";
	private static final String CC_FIELD = "user_cc";
	private static final String NIF_FIELD = "user_nif";
	private static final String USER_LOGIN_TIME = "user_login_time";
	private static final int DEFAULT_LIMIT = 5;
	

	public static List<Date> getLastUserLoginDates(String username) {
		return getLastUserLoginDates(username, DEFAULT_LIMIT);
	}

	public static Key getUserKey(String username) {
		return userKeyFactory.newKey(username);
	}

	public static List<Date> getLastUserLoginDates(String username, int limit) {
		//Key ancestorKey = userKeyFactory.newKey(username);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1); // 24h ago
		Timestamp yesterday = Timestamp.of(cal.getTime());

		Query<Entity> query = Query.newEntityQueryBuilder()
					.setKind("UserLog")
					.setFilter(
							CompositeFilter.and(
									PropertyFilter.hasAncestor(
										getUserKey(username)),
									PropertyFilter.ge(USER_LOGIN_TIME, yesterday)
									)
							)
					.setOrderBy(OrderBy.desc(USER_LOGIN_TIME))
					.setLimit(limit) //5 respostas de cada vez
					.build();
			
			QueryResults<Entity> logs = datastore.run(query);

			List<Date> loginDates = new ArrayList<>();
			logs.forEachRemaining(userlog -> {
				loginDates.add(userlog.getTimestamp(USER_LOGIN_TIME).toDate());
			});

		return loginDates;
	}

    public static Optional<Entity> getUserByField(String field, String value) {
		if (value == null || value.isEmpty()) {
			return Optional.empty(); // Return empty if the value is null or empty
		}
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("User")
                .setFilter(StructuredQuery.PropertyFilter.eq(field, value))
                .build();
        QueryResults<Entity> results = datastore.run(query);
        return results.hasNext() ? Optional.of(results.next()) : Optional.empty();
    }

	public static Optional<Entity> getUserByUsername(String username) {
		Key key = userKeyFactory.newKey(username);
		return Optional.ofNullable(datastore.get(key));
	}

	public static Optional<Entity> getUserByEmail(String email) {
		return getUserByField(EMAIL_FIELD, normalizeEmail(email));
	}
	
	public static Optional<Entity> getUserByUsernameOrEmail(String identifier) {
		if (identifier == null || identifier.isEmpty()) {
			return Optional.empty(); // vazio ou nulo → nada a fazer
		}
		
		Optional<Entity> userByUsername = getUserByUsername(identifier);
		if (userByUsername.isPresent()) {
			return userByUsername;
		}
		
		return getUserByEmail(identifier); // pode ser presente ou vazio
	}
	

	//Optional fields might be null, so we need to check if the entity is present before accessing it

	public static Optional<Entity> getUserByPhone(String phone) {
		return getUserByField(PHONE_FIELD, phone);
	}

	public static Optional<Entity> getUserByCC(String cc) {
		return getUserByField(CC_FIELD, cc);
	}

	public static Optional<Entity> getUserByNif(String nif) {
		return getUserByField(NIF_FIELD, nif);
	}

	// TOKENS AND SESSIONS

	public static Optional<Entity> getTokenEntityByID(String tokenID) {
		String verifier = AuthToken.getVerifier(tokenID);
		Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("Session")
            .setFilter(StructuredQuery.PropertyFilter.eq("session_verification", verifier))
            .build();

    QueryResults<Entity> results = datastore.run(query);
    return results.hasNext() ? Optional.of(results.next()) : Optional.empty();
	}	

	public static Optional<Entity> getTokenEntityByVerifier(String verifier) {
		Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("Session")
            .setFilter(StructuredQuery.PropertyFilter.eq("session_verification", verifier))
            .build();

    QueryResults<Entity> results = datastore.run(query);
    return results.hasNext() ? Optional.of(results.next()) : Optional.empty();
	}

	public static List<Entity> getTokensByUsername(String username) {
		Key userKey = getUserKey(username);
	
		Query<Entity> query = Query.newEntityQueryBuilder()
				.setKind("Session")
				.setFilter(StructuredQuery.PropertyFilter.hasAncestor(userKey))
				.build();
	
		List<Entity> tokens = new ArrayList<>();
		datastore.run(query).forEachRemaining(tokens::add);
		return tokens;
	}

	public static List<Entity> getActiveSessions(String username) {
        Key userKey = getUserKey(username);

        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("Session")
            .setFilter(PropertyFilter.hasAncestor(userKey))
            .build();

        List<Entity> activeSessions = new ArrayList<>();
        QueryResults<Entity> results = datastore.run(query);

        while (results.hasNext()) {
            Entity session = results.next();

            if (DatastoreToken.isTokenValid(session))
                activeSessions.add(session); // ativa - adiciona à lista
            else 
                datastore.delete(session.getKey()); // expirada - remove do datastore
        }

        return activeSessions;
    }

	public static List<Entity> getExpiredSessions(String username) {
		Key userKey = getUserKey(username);

        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("Session")
            .setFilter(PropertyFilter.hasAncestor(userKey))
            .build();

        List<Entity> expiredSessions = new ArrayList<>();
        QueryResults<Entity> results = datastore.run(query);

        while (results.hasNext()) {
            Entity session = results.next();

            if (!DatastoreToken.isTokenValid(session))
				expiredSessions.add(session); // ativa - adiciona à lista
        }
		
        return expiredSessions;
	}

	public static int deleteExpiredSessions(String username) {
		Key userKey = getUserKey(username);
		int count = 0;

        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("Session")
            .setFilter(PropertyFilter.hasAncestor(userKey))
            .build();
        QueryResults<Entity> results = datastore.run(query);

        while (results.hasNext()) {
            Entity session = results.next();

            if (!DatastoreToken.isTokenValid(session)) {
				count++; // expirada - adiciona à lista
				datastore.delete(session.getKey());; // ativa - adiciona à lista
			}
		}
		return count;
	}
	
	public static Datastore getDatastore() {
		return datastore;
	}
}
