package com.apollocurrency.aplwallet.apldesktop;

import ch.qos.logback.core.PropertyDefinerBase;
import java.io.File;

/**
 * Defines property for log dir
 * @author alukin@gmail.com
 */
public class LogDirPropertyDefiner extends PropertyDefinerBase{
    public static final String APL_LOG_DIR=".apl-blockchain/apl-blockchain-logs/";
    @Override
    public String getPropertyValue(){
        String home = System.getProperty("user.home");
        String logDir=home+"/"+APL_LOG_DIR;
        File dir = new File(logDir);
        if(!dir.exists()){
            dir.mkdirs();
        }
        return logDir;
    }

}
