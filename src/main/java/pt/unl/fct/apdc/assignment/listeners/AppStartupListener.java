package pt.unl.fct.apdc.assignment.listeners;

import com.google.cloud.datastore.*;
import com.google.cloud.Timestamp;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.logging.Logger;


public class AppStartupListener implements ServletContextListener {
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final Logger LOG = Logger.getLogger(AppStartupListener.class.getName());

    private static final String ROOT_USERNAME = "root";
    private static final String ROOT_PASSWORD = "Admin123."; // Considere obter de uma variável de ambiente
    private static final String ROOT_EMAIL = "root@root.com";
    private static final String ROOT_PHONE = "000000000"; // Não é necessário para o root
    private static final String ROOT_PROFILE = "privado";
    private static final String ROOT_ROLE = "ADMIN";
    private static final String ROOT_STATE = "ATIVO";
    private static final String ROOT_PHOTO = "https://storage.googleapis.com/shining-expanse-453014-c4.appspot.com/Admin_user.jpg";


    @Override
    public void contextInitialized(ServletContextEvent sce) {
       
        KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
        Key rootKey = userKeyFactory.newKey(ROOT_USERNAME);

        if (datastore.get(rootKey) == null) {
            Entity rootUser = Entity.newBuilder(rootKey)
                    .set("user_name", ROOT_USERNAME)
                    .set("user_pwd", DigestUtils.sha512Hex(ROOT_PASSWORD))
                    .set("user_email", ROOT_EMAIL)
                    .set("user_phone", ROOT_PHONE)
                    .set("user_profile", ROOT_PROFILE)
                    .set("user_role", ROOT_ROLE)
                    .set("user_state", ROOT_STATE)
                    .set("user_creation_time", Timestamp.now())
                    .set("user_photo", ROOT_PHOTO)
                    .build();

            datastore.put(rootUser);
            LOG.info("Conta 'root' criada com sucesso.");
        } else {
            LOG.info("Conta 'root' já existe.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Não há necessidade de fazer nada específico ao destruir o contexto ou há?
    }
}
