package pt.unl.fct.apdc.assignment.util.datastore;

import pt.unl.fct.apdc.assignment.util.data.RegisterData;

public class DatastoreRegister {

    /**
     * Valida os dados de registo verificando se já existem entradas com os mesmos valores.
     * Reutiliza os métodos estáticos do DatastoreUtil.
     */
    public static boolean validateRegisterData(RegisterData data) {
        return DatastoreQuery.getUserByUsername(data.username).isEmpty() &&
               DatastoreQuery.getUserByEmail(data.email).isEmpty() &&
               DatastoreQuery.getUserByPhone(data.phone).isEmpty() &&
               DatastoreQuery.getUserByCC(data.cc).isEmpty() &&
               DatastoreQuery.getUserByNif(data.nif).isEmpty();
    }
}
