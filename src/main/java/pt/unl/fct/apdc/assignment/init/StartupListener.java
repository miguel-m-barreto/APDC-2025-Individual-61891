package pt.unl.fct.apdc.assignment.init;

import java.util.logging.Logger;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import pt.unl.fct.apdc.assignment.util.DeployUtils;

@WebListener
public class StartupListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(StartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info(">>> StartupListener: Initializing application...");
        DeployUtils.createRootUser();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // No cleanup needed
    }
}

