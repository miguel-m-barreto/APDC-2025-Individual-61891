package pt.unl.fct.apdc.assignment.util.datastore;

import pt.unl.fct.apdc.assignment.util.RegisterData;

public class DatastoreRegisterUtil {

    /**
     * Valida os dados de registo verificando se já existem entradas com os mesmos valores.
     * Reutiliza os métodos estáticos do DatastoreUtil.
     */
    public static boolean validateRegisterData(RegisterData data) {
        return DatastoreQueryUtil.getUserByUsername(data.username).isEmpty() &&
               DatastoreQueryUtil.getUserByEmail(data.email).isEmpty() &&
               DatastoreQueryUtil.getUserByPhone(data.phone).isEmpty() &&
               DatastoreQueryUtil.getUserByCC(data.cc).isEmpty() &&
               DatastoreQueryUtil.getUserByNif(data.nif).isEmpty();
    }
}
