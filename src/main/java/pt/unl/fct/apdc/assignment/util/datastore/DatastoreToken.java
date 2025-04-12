package pt.unl.fct.apdc.assignment.util.datastore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import pt.unl.fct.apdc.assignment.util.AuthToken;

public class DatastoreToken {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public static Entity createTokenEntity(AuthToken token) {
        Key tokenKey = datastore.newKeyFactory()
                .addAncestor(PathElement.of("User", token.username))
                .setKind("Session")
                .newKey(token.tokenID);

        return Entity.newBuilder(tokenKey)
                .set("session_username", token.username)
                .set("session_role", token.role)
                .set("session_creation", token.validFrom)
                .set("session_expiration", token.validTo)
                .set("session_verification", token.verificationCode)
                .build();
    }

    public static boolean isTokenValid(Entity tokenEntity) {
        return tokenEntity != null &&
               tokenEntity.getLong("session_expiration") >= System.currentTimeMillis();
    }

    public static String getUsername(Entity tokenEntity) {
        return tokenEntity.getString("session_username");
    }

    public static String getRole(Entity tokenEntity) {
        return tokenEntity.getString("session_role");
    }

    public static boolean isActiveSession(Entity tokenEntity) {
        return isTokenValid(tokenEntity);
    }

    public static List<Entity> getActiveSessions(String username) {
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);

        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("Session")
            .setFilter(PropertyFilter.hasAncestor(userKey))
            .build();

        List<Entity> activeSessions = new ArrayList<>();
        QueryResults<Entity> results = datastore.run(query);

        long now = System.currentTimeMillis();

        while (results.hasNext()) {
            Entity session = results.next();
            long expiration = session.getLong("session_expiration");

            if (expiration > now) {
                activeSessions.add(session); // ativa - adiciona à lista
            } else {
                datastore.delete(session.getKey()); // expirada - remove do datastore
            }
        }

        return activeSessions;
    }

    public static Optional<Entity> getLatestValidSession(String username) {
        List<Entity> sessions = getActiveSessions(username);
        return sessions.stream()
            .max(Comparator.comparingLong(e -> e.getLong("session_expiration")));
    }


}
