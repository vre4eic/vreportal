/*******************************************************************************
 * Copyright (c) 2018 VRE4EIC Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

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
