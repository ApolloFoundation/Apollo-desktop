 /*
 * Copyright © 2020 Apollo Foundation
 */
package com.apollocurrency.aplwallet.apldesktop;

import org.slf4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import static org.slf4j.LoggerFactory.getLogger;

public class DesktopMain {

    // Linux path version
    public static String logDir = System.getProperty("user.home") + "/.apl-blockchain/apl-blockchain-logs/apl-desktop.log";
    private static Logger LOG;

    private static DesktopSystemTray desktopSystemTray;
    private static DesktopApplication desktopApp;
    private static String OS = System.getProperty("os.name").toLowerCase();
    private static boolean workingAPI = false;

    public static void main(String[] args) {
        String apiUrl = DesktopConfig.getInstance().getWelcomePageURI();
        LOG = getLogger(DesktopMain.class);
        LOG.debug("WelcomePageURI apiUrl = {}", apiUrl);

        desktopApp = new DesktopApplication();
        Thread desktopAppThread = new Thread(() -> {
            desktopApp.launch();
        });
        desktopAppThread.start();

        final int nTriesMax = 200;

        LookAndFeel.init();

        desktopSystemTray = new DesktopSystemTray();
        SwingUtilities.invokeLater(desktopSystemTray::createAndShowGUI);
        if (OS.equalsIgnoreCase("linux")) {
            // bind data for running commands: Open Wallet in browser, View log file
            try {
                LOG.debug("logDir = {}", logDir);
                desktopSystemTray.setToolTip(
                    new SystemTrayDataProvider("Server message", new URI(apiUrl), new File(logDir)));
            } catch (URISyntaxException e) {
                LOG.error("Error on setting tool tip", e);
                throw new RuntimeException(e);
            }
        }

        for (int i = 0; i < nTriesMax; i++) {
            workingAPI = checkAPI();
            if (workingAPI) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                LOG.info("GUI thread was interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
        if (workingAPI) {
            DesktopApplication.ENABLE_JAVASCRIPT_DEBUGGER = startDebug(args);
            DesktopApplication.startDesktopApplication(apiUrl);
        } else {
            Platform.runLater(DesktopMain::showAPIError);
        }
    }

    private static boolean startDebug(String[] args) {
        boolean startDebug = false;
        if (args.length == 1) {
            if ("-d".equals(args[0])) {
                startDebug = true;
            }
        } else if (args.length > 1) {
            throw new IllegalArgumentException("Only one '-d' cmd arg is supported");
        }
        return startDebug;
    }

    private static void showAPIError() {
        String url = DesktopConfig.getInstance().getWelcomePageURI();
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Apollo API is not available");
        alert.setContentText("Ooops, Apollo backend is not available at " + url + " Please start it and then start Desktop Wallet again!");
        alert.showAndWait();
        System.err.println("Apollo API is not available at " + url + " Exiting GUI");
        System.exit(2);
    }

    private static boolean checkAPI() {
        String url = DesktopConfig.getInstance().getWelcomePageURI();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        boolean res = false;
        var request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            LOG.debug("WebUI response {}", response.statusCode());
            res = response.statusCode() == 200;
            if (res) {
                DesktopApplication.updateSplashScreenStatus(response.body());
            } else {
                LOG.debug("WebUI error response = '{}'", response.body());
            }
        } catch (IOException ex) {
            LOG.debug("WebUI is not available at {}", url);
        } catch (InterruptedException ex) {
            LOG.debug("Http client Interrupted",ex);
            Thread.currentThread().interrupt();
        }

        return res;
    }

    public void setServerStatus(String message, URI wallet, File logFileDir) {
        desktopSystemTray.setToolTip(new SystemTrayDataProvider(message, wallet, logFileDir));
    }

    public void launchDesktopApplication() {
        LOG.info("Launching desktop wallet");
        desktopApp.startDesktopApplication(DesktopConfig.getInstance().getWelcomePageURI());
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
