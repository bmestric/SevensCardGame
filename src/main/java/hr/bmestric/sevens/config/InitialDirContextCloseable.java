package hr.bmestric.sevens.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

public class InitialDirContextCloseable extends InitialDirContext implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(InitialDirContextCloseable.class);

    public InitialDirContextCloseable(java.util.Map<String, String> environment) throws NamingException {
        super(new java.util.Hashtable<>(environment));
    }


    @SuppressWarnings("java:S1319")
    public InitialDirContextCloseable(java.util.Hashtable<String, String> environment) throws NamingException {
        this((java.util.Map<String, String>) environment);
    }

    @Override
    public void close() {
        try {
            super.close();
        } catch (NamingException e) {
            logger.error("Error closing JNDI context", e);
        }
    }
}
