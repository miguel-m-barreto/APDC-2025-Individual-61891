package pt.unl.fct.apdc.assignment.util;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;

public class GcsUtil {

    public static String getDefaultBucketName() {
        AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();
        return appIdentity.getDefaultGcsBucketName();
    }
}