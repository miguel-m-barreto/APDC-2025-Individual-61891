package pt.unl.fct.apdc.assignment.util.datastore;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.cloud.datastore.*;

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
               tokenEntity.getLong("session_expiration") > System.currentTimeMillis();
    }

    public static String getUsername(Entity tokenEntity) {
        return tokenEntity.getString("session_username");
    }

    public static String getRole(Entity tokenEntity) {
        return tokenEntity.getString("session_role");
    }

    public static Optional<Entity> getLatestValidSession(String username) {
        List<Entity> sessions = DatastoreQueries.getActiveSessions(username);
        return sessions.stream()
            .max(Comparator.comparingLong(e -> e.getLong("session_expiration")));
    }

    public static int deleteAllSessions(String username) {
        int deletedCount = 0;
        List<Entity> sessions = DatastoreQueries.getTokensByUsername(username);
        for (Entity session : sessions) {
            deletedCount++;
            datastore.delete(session.getKey());
        }
        return deletedCount;
    }

    public static int deleteAllSessionsKeepLast(String username, Entity lastSession) {
        int deletedCount = 0;
        List<Entity> sessions = DatastoreQueries.getTokensByUsername(username);
        for (Entity session : sessions) {
            if (session.getKey().equals(lastSession.getKey())) {
                //nao fazer nada
                
            } else {
                deletedCount++;
                datastore.delete(session.getKey());
            }
        }
        return deletedCount;
    }

    public static int KeepLatest(String username) {
        int deletedCount = 0;
        Optional<Entity> latestSession = getLatestValidSession(username);
        if (!latestSession.isPresent()) {
            deletedCount = deleteAllSessions(username);
        }
        else {
            Entity session = latestSession.get();
            deletedCount = deleteAllSessionsKeepLast(username, session);
        }
        return deletedCount;
    }


    public static void invalidateToken(Key tokenKey) {
        datastore.delete(tokenKey);
    }
    
}
