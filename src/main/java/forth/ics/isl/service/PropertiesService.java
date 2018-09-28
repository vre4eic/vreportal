/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rousakis
 */
public class PropertiesService {

    private static final String configFilePath = "config.properties";
    private static final String applicationFilePath = "application.properties";

    public static Properties getConfigProperties() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream(configFilePath);
        Properties prop = new Properties();
        if (input != null) {
            try {
                prop.load(input);
            } catch (IOException ex) {
                Logger.getLogger(PropertiesService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return prop;
    }

    public static Properties getApplicationProperties() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream(applicationFilePath);
        Properties prop = new Properties();
        if (input != null) {
            try {
                prop.load(input);
            } catch (IOException ex) {
                Logger.getLogger(PropertiesService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return prop;
    }

}
