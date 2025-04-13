package pt.unl.fct.apdc.assignment.util.datastore;

import pt.unl.fct.apdc.assignment.util.data.RegisterData;

public class DatastoreRegister {

    /**
     * Valida os dados de registo verificando se já existem entradas com os mesmos valores.
     * Reutiliza os métodos estáticos do DatastoreUtil.
     */
    public static boolean validateRegisterData(RegisterData data) {
        return DatastoreQueries.getUserByUsername(data.username).isEmpty() &&
               DatastoreQueries.getUserByEmail(data.email).isEmpty() &&
               DatastoreQueries.getUserByPhone(data.phone).isEmpty() &&
               DatastoreQueries.getUserByCC(data.cc).isEmpty() &&
               DatastoreQueries.getUserByNif(data.nif).isEmpty();
    }
}
