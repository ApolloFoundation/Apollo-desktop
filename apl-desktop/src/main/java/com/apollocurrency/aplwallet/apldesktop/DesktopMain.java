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

import org.slf4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import static org.slf4j.LoggerFactory.getLogger;

public class DesktopMain {

    public static String logDir = System.getProperty("user.home" + "/.apl-blockchain/apl-desktop");
    private static Logger LOG;

    private static DesktopSystemTray desktopSystemTray;
    private static DesktopApplication desktopApp;
    private static String OS = System.getProperty("os.name").toLowerCase();
    private static boolean workingAPI = false;

    public static void main(String[] args) {
        String apiUrl = DesktopConfig.getInstance().getWelocmePageURI();
        LOG = getLogger(DesktopMain.class);

        desktopApp = new DesktopApplication();
        Thread desktopAppThread = new Thread(() -> {
            desktopApp.launch();
        });
        desktopAppThread.start();

        final int nTriesMax = 20;

        LookAndFeel.init();

        desktopSystemTray = new DesktopSystemTray();
        SwingUtilities.invokeLater(desktopSystemTray::createAndShowGUI);

        for (int i = 0; i < nTriesMax; i++) {
            workingAPI = checkAPI();
            if (workingAPI) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                LOG.info("GUI thread was interrupted", e);
            }
        }
        if (workingAPI) {
            DesktopApplication.startDesktopApplication(apiUrl);
        } else {
            Platform.runLater(DesktopMain::showAPIError);
        }
    }

    private static void showAPIError() {
        String url = DesktopConfig.getInstance().getWelocmePageURI();
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Apollo API is not available");
        alert.setContentText("Ooops, Apollo backend is not available at " + url + " Please start it and then start Desktop Wallet again!");
        alert.showAndWait();
        System.err.println("Apollo API is not available at " + url + " Exiting GUI");
        System.exit(2);
    }

    private static boolean checkAPI() {
        String url = DesktopConfig.getInstance().getWelocmePageURI();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        boolean res = false;
        var request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            res = response.statusCode() == 200;
            if (res) {
                DesktopApplication.updateSplashScreenStatus(response.body());
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
