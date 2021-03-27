package com.jsql.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertiesUtil {
    
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = LogManager.getRootLogger();

    private final Properties properties = new Properties();
    
    public PropertiesUtil() {
        
        String filename = "config.properties";

        try (InputStream input = PropertiesUtil.class.getClassLoader().getResourceAsStream(filename)) {
            
            if (input == null) {
                
                LOGGER.warn("Properties file {} not found", filename);
                return;
            }

            // load a properties file from class path, inside static method
            this.getProperties().load(input);
            
        } catch (IOException e) {
            
            LOGGER.error(e, e);
        }
    }

    public Properties getProperties() {
        return this.properties;
    }
}
