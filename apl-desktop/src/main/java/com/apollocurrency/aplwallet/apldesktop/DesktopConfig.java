/*
 * Copyright © 2020 Apollo Foundation
 */
package com.apollocurrency.aplwallet.apldesktop;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.Getter;

/**
 * Configuration reader/holder
 *
 * @author alukin@gmail.com
 */
public class DesktopConfig {

    public static final String CONF_FILE = "conf/apl-desktop.properties";

    //All supported properties should be listed here and organized as getters
    public static final String WELCOME_PAGE_URI = "apl.APITURL";

    @Getter
    private String welcomePageURI = "http://localhost:7876";

    private Properties conf = new Properties();

    private static DesktopConfig instance = null;

    private DesktopConfig() {

    }

    public static DesktopConfig getInstance(){
       if (instance == null) {
            instance = new DesktopConfig();
       }
       return instance;
    }

    public boolean readConfig(InputStream is) {
        boolean res = true;
        try {
            conf.load(is);
        } catch (IOException ex) {
            res = false;
        }
        return res;
    }

    public void readConfiguration() {
        InputStream rs = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONF_FILE);
        readConfig(rs);
        //TODO: try read config files from standard locations in standard order
        //
        String ws = conf.getProperty(WELCOME_PAGE_URI);
        if (ws != null) {
            welcomePageURI = ws;
        }

    }
}
