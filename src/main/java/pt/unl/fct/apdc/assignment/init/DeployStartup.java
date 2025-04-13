package pt.unl.fct.apdc.assignment.init;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;

import static pt.unl.fct.apdc.assignment.util.StringUtil.normalizeProfileType;
import static pt.unl.fct.apdc.assignment.util.StringUtil.normalizeEmail;

import java.util.logging.Logger;

public class DeployStartup {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final Logger LOG = Logger.getLogger(DeployStartup.class.getName());

    private static final String ROOT_USERNAME = "root";
    private static final String ROOT_PASSWORD = "Admin123.";
    private static final String ROOT_EMAIL = "root@root.com";
    private static final String ROOT_PHONE = "000000000";
    private static final String ROOT_PROFILE = "privado";
    private static final String ROOT_ROLE = "ADMIN";
    private static final String ROOT_STATE = "ATIVADA";
    private static final String ROOT_PHOTO = "https://storage.googleapis.com/shining-expanse-453014-c4.appspot.com/Admin_user.jpg";
    private static final String EMPTY_STRING = "NOT DEFINED";

    public static void createRootUser() {
        LOG.info("Creating root user...");
    
        try {
            KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
            Key rootKey = userKeyFactory.newKey(ROOT_USERNAME);
    
            Entity existing = datastore.get(rootKey);
            if (existing != null) {
                LOG.info("Conta 'root' já existe.");
                
            } else {
                Entity root = Entity.newBuilder(rootKey)
                        .set("user_name", ROOT_USERNAME)
                        .set("user_pwd", DigestUtils.sha512Hex(ROOT_PASSWORD))
                        .set("user_email", normalizeEmail(ROOT_EMAIL))
                        .set("user_phone", ROOT_PHONE)
                        .set("user_profile", normalizeProfileType(ROOT_PROFILE))
                        .set("user_role", ROOT_ROLE)
                        .set("user_account_state", ROOT_STATE)
                        .set("user_creation_time", Timestamp.now())
                        // optional fields
                        .set("user_cc", EMPTY_STRING)
                        .set("user_nif", EMPTY_STRING)
                        .set("user_employer", EMPTY_STRING)
                        .set("user_job", EMPTY_STRING)
                        .set("user_address", EMPTY_STRING)
                        .set("user_employer_nif", EMPTY_STRING)
                        .set("user_photo_url", ROOT_PHOTO)
                        .build();

                datastore.put(root);
                LOG.info("Conta 'root' criada com sucesso.");
            }
        
        } catch (DatastoreException e) {
            LOG.severe("Erro ao criar root user: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LOG.severe("Erro inesperado ao criar root user: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}
