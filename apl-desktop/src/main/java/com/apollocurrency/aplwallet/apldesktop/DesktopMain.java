/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

/*
 * Copyright © 2018 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apldesktop;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class DesktopMain {
    public static String logDir = System.getProperty("user.home" + "/.apl-blockchain/apl-desktop");
    private static Logger LOG;

    private static DesktopSystemTray desktopSystemTray;
    private static DesktopApplication desktopApp;
    private static String OS = System.getProperty("os.name").toLowerCase();


    public static void main(String[] args) {
        
        LOG = getLogger(DesktopMain.class);

        desktopApp = new DesktopApplication();
        Thread desktopAppThread = new Thread(() -> {
            desktopApp.launch();
        });
        desktopAppThread.start();
        new Thread(() -> runBackend(args)).start();
        Runnable statusUpdater = () -> {
            while (!checkAPI()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    LOG.info("GUI thread was interrupted", e);
                }
            }
            desktopApp.startDesktopApplication(DesktopConfig.getInstance().getWelocmePageURI());

        };

        Thread updateSplashScreenThread = new Thread(statusUpdater, "SplashScreenStatusUpdaterThread");
        updateSplashScreenThread.setDaemon(true);
        updateSplashScreenThread.start();
        LookAndFeel.init();

        desktopSystemTray = new DesktopSystemTray();
        SwingUtilities.invokeLater(desktopSystemTray::createAndShowGUI);
    }

    private static boolean checkAPI() {
        OkHttpClient client = new OkHttpClient();
        try {
            Request request = new Request.Builder().url(DesktopConfig.getInstance().getWelocmePageURI()).build();

            Response response;
            try {
                response = client.newCall(request).execute();
            } catch (IOException ex) {
                return false;
            }

            if (response.code() == 200) {
                return true;
            } else if (response.code() == 200) {
                DesktopApplication.updateSplashScreenStatus(response.body().toString());
            }
        } finally {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }

        return false;
    }

    private static void runBackend(String[] args) {
        Process p;
        try {
            String cmdArgs = String.join(" ", args);
            //TODO: Refactor that funny code below

            String command = "apl-run";
            if (System.getProperty("apl.exec.mode") != null) {
                if (System.getProperty("apl.exec.mode").equals("tor")) {
                    command = "apl-run-tor";
                } else if (System.getProperty("apl.exec.mode").equals("transport")) {
                    command = "apl-run-secure-transport";
                }
            }
            ProcessBuilder pb;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb = new ProcessBuilder(".\\" + command + ".bat ", cmdArgs);
            } else {
                pb = new ProcessBuilder("/bin/bash", "./apl-start.sh", cmdArgs);
            }
            LOG.info("Will run command: {}", pb.command());

            String tempDir = System.getProperty("java.io.tmpdir");
            File outputLogFile = Paths.get(tempDir).resolve("backend-start-output.txt").toFile();
            File errorLogFile = Paths.get(tempDir).resolve("backend-start-error.txt").toFile();
            LOG.info("Output log file: {}, Error log file: {}", outputLogFile, errorLogFile);
            pb.redirectOutput(ProcessBuilder.Redirect.to(outputLogFile))
                .redirectError(ProcessBuilder.Redirect.to(errorLogFile));
            int code = pb.start().waitFor();
            LOG.info("Backend start script returned code {}", code);
        } catch (IOException e) {
            LOG.debug(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void setServerStatus(String message, URI wallet, File logFileDir) {
        desktopSystemTray.setToolTip(new SystemTrayDataProvider(message, wallet, logFileDir));
    }

    public void launchDesktopApplication() {
        LOG.info("Launching desktop wallet");
        desktopApp.startDesktopApplication(DesktopConfig.getInstance().getWelocmePageURI());
    }

    public void shutdown() {
        desktopSystemTray.shutdown();
        desktopApp.shutdown();
    }



    public void alert(String message) {
        desktopSystemTray.alert(message);
    }

    public void updateAppStatus(String newStatus) {
        desktopApp.updateSplashScreenStatus(newStatus);
    }

    public void displayError(String errorMessage) {
        desktopApp.showError(errorMessage);
    }
}
