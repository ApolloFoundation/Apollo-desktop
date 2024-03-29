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
 * Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apldesktop;

//import com.apollocurrency.aplwallet.apl.util.Constants;
//import com.apollocurrency.aplwallet.apl.util.Version;
import com.sun.javafx.scene.web.Debugger;
import com.vladsch.javafx.webview.debugger.DevToolsDebuggerServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import netscape.javascript.JSObject;
import org.slf4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.apollocurrency.aplwallet.apldesktop.DesktopApplication.MainApplication.showStage;
import javax.net.ssl.HttpsURLConnection;
import static org.slf4j.LoggerFactory.getLogger;

import javafx.beans.value.ChangeListener;
import org.w3c.dom.Document;
import com.sun.javafx.webkit.WebConsoleListener;

public class DesktopApplication extends Application {
    private static final Logger LOG = getLogger(DesktopApplication.class);

    private static final MainApplication MAIN_APPLICATION = MainApplication.getInstance();
    private static final SplashScreen SPLASH_SCREEN = SplashScreen.getInstance();
    private static final DbRecoveringUI DB_RECOVERING_UI = DbRecoveringUI.getInstance();
    public static final int DEBUGGER_PORT = 32322;
    static boolean ENABLE_JAVASCRIPT_DEBUGGER = false;
    static volatile Stage mainStage;
    private static volatile boolean isSplashScreenLaunched = false;
    private static volatile Stage screenStage;
    private static volatile Stage changelogStage;
    private static String apiUrl;


    public static void refreshMainApplication() {
        MainApplication.refresh();
    }

    private static String getUrl() {

        String url = apiUrl;


//TODO:  Do we need that?
        //String defaultAccount = aplGlobalObjects.getStringProperty("apl.defaultDesktopAccount");
        String defaultAccount = "";
        if (defaultAccount != null && !defaultAccount.isEmpty() && !defaultAccount.equals("")) {
            url += "?account=" + defaultAccount;
        }
        return url;
    }

    public static void shutdownSplashScreen() {
        SPLASH_SCREEN.shutdown();
        isSplashScreenLaunched = false;
    }

    public static void updateSplashScreenStatus(String newStatus) {
        SPLASH_SCREEN.setLastStatus(newStatus);
    }

    //rewrite (start on existing stage)
    public static void launch() {
        Application.launch(DesktopApplication.class);


        if (mainStage != null) {
            Platform.runLater(() -> showStage(false));
        }
    }


    public static void startDesktopApplication(String APIlocation) {
        apiUrl = APIlocation;
        if (apiUrl.startsWith("https")) {
            HttpsURLConnection.setDefaultSSLSocketFactory(TrustAllSSLProvider.getSslSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(TrustAllSSLProvider.getHostNameVerifier());
        }
        if (isSplashScreenLaunched) {
            shutdownSplashScreen();
        }
        shutdownSplashScreen();

        Platform.runLater(MAIN_APPLICATION::startDesktopApplication);

//TODO:: make with changelog
//        if (!Constants.VERSION.toString().equals(optionDAO.get("Previous launch APP Version")))
//        {
//            Platform.runLater(MAIN_APPLICATION::startChangelogWindow);
//            optionDAO.set("Previous launch APP Version", Constants.VERSION.toString());
//
//        }

    }

    //start javaFx splash screen
    public static void showSplashScreen() {
        if (!isSplashScreenLaunched) {
            LOG.info("Starting splash screen");
            SPLASH_SCREEN.startAndShow();
            isSplashScreenLaunched = true;
        } else {
            LOG.info("Splash screen has already started");
        }
    }

    @SuppressWarnings("unused")
    public static void shutdown() {
        System.out.println("shutting down JavaFX platform");
        Platform.runLater(() -> {
            if (screenStage.isShowing()) {
                screenStage.close();
            }
            if (mainStage.isShowing()) {
                mainStage.close();
            }
        });
        Platform.exit();
        if (ENABLE_JAVASCRIPT_DEBUGGER) {
            try {
                Class<?> aClass = Class.forName("com.mohamnag.fxwebview_debugger.DevToolsDebuggerServer");
                aClass.getMethod("stopDebugServer").invoke(null);
            } catch (Exception e) {
                LOG.info("Error shutting down webview debugger", e);
            }
        }
        System.out.println("JavaFX platform shutdown complete");
    }

    public static void showError(String message) {
        Platform.runLater(() -> {
            Text text = new Text(message);
            text.setWrappingWidth(330);
            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER);
            hbox.setPadding(new Insets(10, 10, 0, 10));
            hbox.getChildren().add(text);
            text.setTextAlignment(TextAlignment.JUSTIFY);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setResizable(true);
            alert.setHeaderText("Multiple Apollo wallets were launched");
            alert.setWidth(350);
            alert.setHeight(200);
            alert.getDialogPane().setContent(hbox);
            alert.showAndWait();

            System.exit(0);
        });
    }

    private static void showInfoBox(String message) {
        Platform.runLater(() -> {
            Text text = new Text(message);
            text.setWrappingWidth(330);
            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER);
            hbox.setPadding(new Insets(10, 10, 0, 10));
            hbox.getChildren().add(text);
            text.setTextAlignment(TextAlignment.JUSTIFY);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setResizable(true);
            alert.setHeaderText("Message");
            alert.setWidth(350);
            alert.setHeight(200);
            alert.getDialogPane().setContent(hbox);
            alert.showAndWait();

        });
    }


    @Override
    public void start(Stage primaryStage) {
        Thread.currentThread().setName("JavaFXApplicationThread");
        mainStage = primaryStage;
        screenStage = new Stage();
        showSplashScreen();
    }

    public static class SplashScreen {
        private static SplashScreen instance = new SplashScreen();
        private AtomicBoolean shutdown = new AtomicBoolean(false);
        private volatile String lastStatus;

        private SplashScreen() {
        }

        public static SplashScreen getInstance() {
            return instance;
        }

        public String getLastStatus() {
            return lastStatus;
        }

        public void setLastStatus(String lastStatus) {
            this.lastStatus = lastStatus;
        }

        public void startAndShow() {
            screenStage.setTitle("Apollo wallet");
            AnchorPane pane = new AnchorPane();
            Scene scene = new Scene(pane);
            URL cssUrl = getClass().getClassLoader().getResource("css/splash-screen.css");
            pane.getStylesheets().addAll(cssUrl.toExternalForm());
            pane.setId("main-pane");
            ProgressIndicator indicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
            indicator.setId("progress-indicator");
            AnchorPane.setTopAnchor(indicator, 75.0);
            AnchorPane.setLeftAnchor(indicator, 225.0);
            pane.getChildren().add(indicator);
            Text statusText = new Text();
            Text versionText = new Text();
            versionText.setId("version-text");
            //TODO: read from backend
            versionText.setText("Wallet version " +Constants.VERSION);
            statusText.setId("status-text");
            statusText.setText("Apollo wallet is loading. Please, wait");
            AnchorPane.setTopAnchor(versionText, 130.0);
            AnchorPane.setLeftAnchor(versionText, 190.0);
            AnchorPane.setTopAnchor(statusText, 200.0);
            AnchorPane.setLeftAnchor(statusText, 60.0);
            pane.getChildren().add(statusText);
            pane.getChildren().add(versionText);
            screenStage.setScene(scene);
            screenStage.setHeight(217);
            screenStage.setWidth(500);
            screenStage.initStyle(StageStyle.UNDECORATED);
            screenStage.show();
            Runnable statusUpdater = () -> {
                while (!shutdown.get()) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(200);
                        Platform.runLater(() -> {
                            String lastStatus = getLastStatus();
                            if (lastStatus != null && !statusText.getText().equalsIgnoreCase(lastStatus)) {
                                statusText.setText(lastStatus);
                            }
                        });
                    } catch (InterruptedException e) {
                        LOG.info("GUI thread was interrupted", e);
                    }
                }
                LOG.info("Shutdown splash screen");
                Platform.runLater(() -> screenStage.hide());
                shutdown.set(false);
            };
            Thread updateSplashScreenThread = new Thread(statusUpdater, "SplashScreenStatusUpdaterThread");
            updateSplashScreenThread.setDaemon(true);
            updateSplashScreenThread.start();
        }

        public void shutdown() {
            shutdown.set(true);
        }
    }

    public static class MainApplication {
        private static final Set DOWNLOAD_REQUEST_TYPES = new HashSet<>(Arrays.asList("downloadTaggedData", "downloadPrunableMessage"));
        private static volatile WebEngine webEngine;
        private static volatile WebEngine webEngine2;
        private static MainApplication instance = new MainApplication();
        private static WebView browser;
        private static WebView invisible;
        //        private JSObject ars;
        private volatile long updateTime;
        private JavaScriptBridge javaScriptBridge;

        private MainApplication() {
        }

        public static void refresh() {
            Platform.runLater(() -> showStage(true));
        }

        static void showStage(boolean isRefresh) {
            if (isRefresh) {
                webEngine.load(getUrl());
            }
            if (!mainStage.isShowing()) {
                mainStage.show();
            } else if (mainStage.isIconified()) {
                mainStage.setIconified(false);
            } else {
                mainStage.toFront();
            }
        }

        public static MainApplication getInstance() {
            return instance;
        }

        private static void enableFirebug(final WebEngine engine) {
            //uncomment it to enable firebug
//            engine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://cdnjs.cloudflare.com/ajax/libs/firebug-lite/1.4.0/firebug-lite.min.js' + '#startOpened');}");
        }

        public void startDesktopApplication() {
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            browser = new WebView();

            invisible = new WebView();

            int height = (int) Math.min(primaryScreenBounds.getMaxY() - 100, 1000);
            int width = (int) Math.min(primaryScreenBounds.getMaxX() - 100, 1618);
            browser.setMinHeight(height);
            browser.setMinWidth(width);
            webEngine = browser.getEngine();
//            webEngine.documentProperty().addListener((ChangeListener<Document>) (prop, oldDoc, newDoc) -> enableFirebug(webEngine));

// webEngine will use default user data directory
//            TODO figure out why do we require user config dir here for localstorage
//            webEngine.setUserDataDirectory(new File(RuntimeEnvironment.getInstance().getDirProvider().getUserConfigDirectory()));

            WebConsoleListener.setDefaultListener(new WebConsoleListener(){
                @Override
                public void messageAdded(WebView webView, String message, int lineNumber, String sourceId) {
                    LOG.debug("Console: [" + sourceId + ":" + lineNumber + "] " + message);
                }
            });

            Worker<Void> loadWorker = webEngine.getLoadWorker();
            loadWorker.stateProperty().addListener((ov, oldState, newState) -> {
                LOG.debug("loadWorker old state " + oldState + " new state " + newState);
                if (newState != Worker.State.SUCCEEDED) {
                    LOG.debug("loadWorker state change ignored");
                    return;
                }

                JSObject window = (JSObject) webEngine.executeScript("window");
                javaScriptBridge = new JavaScriptBridge(this); // Must be a member variable to prevent gc
                window.setMember("java", javaScriptBridge);
//                        Locale locale = Locale.getDefault();
//                        String language = locale.getLanguage().toLowerCase() + "-" + locale.getCountry().toUpperCase();
//                        window.setMember("javaFxLanguage", language);

                webEngine.executeScript("console.log = function(msg) { java.log(msg); };");
//TODO: Get Blockchain config from API
//                        mainStage.setTitle(blockchainConfig.getProjectName() + " Desktop - " + webEngine.getLocation());
//                        mainStage.setTitle("Apollo" + " Desktop - " + webEngine.getLocation());

                // updateClientState("Desktop Wallet started");

                if (ENABLE_JAVASCRIPT_DEBUGGER) {

                    try {
                        Class webEngineClazz = WebEngine.class;

                        Field debuggerField = webEngineClazz.getDeclaredField("debugger");
                        debuggerField.setAccessible(true);

                        Debugger debugger = (Debugger) debuggerField.get(webEngine);
                        DevToolsDebuggerServer bridge = new DevToolsDebuggerServer(debugger, DEBUGGER_PORT, 0, null, null);
                        CompletableFuture.runAsync(()-> {
                            long maxWaitingTime = 20_000;
                            long currentWaitingTime = 0;
                            while (true) {
                                if (currentWaitingTime > maxWaitingTime) {
                                    LOG.error("Unable to connect debugger to port {}, maybe that port is already in use", DEBUGGER_PORT);
                                    break;
                                }
                                long beginIterationTime = System.currentTimeMillis();
                                if (!bridge.getDebugUrl().isBlank()) {
                                    LOG.info("Chrome Debugger URL is {}", bridge.getDebugUrl().replace("chrome-", ""));
                                    break;
                                }
                                try {
                                    Thread.sleep(50);
                                    currentWaitingTime += (System.currentTimeMillis() - beginIterationTime);
                                } catch (InterruptedException e) {
                                    LOG.error("Debugger connection waiting was interrupted", e);
                                }
                            }
                        });

                        mainStage.setOnCloseRequest(windowEvent -> bridge.stopDebugServer(stopStatus -> LOG.info("DevToolsDebugServer {} at port {}", stopStatus ? "stopped" : "not stopped", DEBUGGER_PORT)));
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        LOG.info("Cannot start JavaFx debugger", e);
                    }
                }
            });

            // Invoked by the webEngine popup handler
            // The invisible webView does not show the link, instead it opens a browser window
            invisible.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> popupHandlerURLChange(newValue));

            // Invoked when changing the document.location property, when issuing a download request
            webEngine.locationProperty().addListener((observable, oldValue, newValue) -> webViewURLChange(newValue));

            // Invoked when clicking a link to external site like Help or API console
            webEngine.setCreatePopupHandler(
                config -> {
                    LOG.info("popup request from webEngine");
                    LOG.info(webEngine.getLocation());
                    return invisible.getEngine();
                });

            webEngine.load(getUrl());

            Scene scene = new Scene(browser);
            //TODO:
            //String address = API.getServerRootUri().toString();
            String address = "http://localhost:7876/";
            mainStage.getIcons().add(new Image(address + "/img/apl-icon-32x32.png"));
            mainStage.initStyle(StageStyle.DECORATED);
            mainStage.setScene(scene);
            mainStage.sizeToScene();
            mainStage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::showOnCloseWarnAlert);
            mainStage.show();
            Platform.setImplicitExit(false); // So that we can reopen the application in case the user closed it
        }

        public void startChangelogWindow() {
            changelogStage = new Stage();
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            WebView browser = new WebView();
            // browser.setOnContextMenuRequested(new WalletContextMenu());

            int height = (int) Math.min(primaryScreenBounds.getMaxY() - 100, 500);
            int width = (int) Math.min(primaryScreenBounds.getMaxX() - 100, 720);
            browser.setMinHeight(height);
            browser.setMinWidth(width);
            webEngine2 = browser.getEngine();
            URL changelogUrl = getClass().getClassLoader().getResource("html/changelog.html");

            webEngine2.load(changelogUrl.toString());

            Scene scene = new Scene(browser);
            String address = "http://localhost:7876/";//TODO: Make it right API.getServerRootUri().toString();
            changelogStage.getIcons().add(new Image(address + "/img/apl-icon-32x32.png"));
            changelogStage.initStyle(StageStyle.DECORATED);
            changelogStage.setScene(scene);
            changelogStage.sizeToScene();
            changelogStage.show();
            Platform.setImplicitExit(false); // So that we can reopen the application in case the user closed it
        }

        public <T> void showOnCloseWarnAlert(T event) {

            Alert warnAlert = new Alert(Alert.AlertType.WARNING, "", ButtonType.YES, ButtonType.NO);
            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER);
            hbox.setPadding(new Insets(5, 5, 0, 5));
            Text text = new Text("By closing desktop you will not stop apollo wallet completely. Apollo desktop app will work in tray (if " +
                "supported) or in browser mode in " +
                "background and will be available also in your browser at " + getUrl() + ". If you want to close completely application -> " +
                "click " +
                "YES");

            text.setFont(Font.font("Verdana", 11));

            text.setTextAlignment(TextAlignment.JUSTIFY);
            hbox.setMaxSize(500, 250);
            text.setWrappingWidth(350);
            hbox.getChildren().add(text);
            warnAlert.getDialogPane().setContent(hbox);
            warnAlert.setResizable(true);
            warnAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            warnAlert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

            warnAlert.setHeaderText("Apollo desktop app shutdown will not" + System.lineSeparator() + " cause shutdown of Apollo wallet");

            ButtonType clickedButton = warnAlert.showAndWait().orElse(ButtonType.NO);
            if (clickedButton == ButtonType.YES) {
                Platform.exit();
            }
        }

        //   @SuppressWarnings("WeakerAccess")
        public void popupHandlerURLChange(String newValue) {
            LOG.info("popup request for " + newValue);
            Platform.runLater(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(newValue));
                } catch (Exception e) {
                    LOG.info("Cannot open " + newValue + " error " + e.getMessage());
                }
            });
        }

        private void webViewURLChange(String newValue) {
            LOG.info("webview address changed to " + newValue);
            URL url;
            try {
                url = new URL(newValue);
            } catch (MalformedURLException e) {
                LOG.info("Malformed URL " + newValue, e);
                return;
            }
            String query = url.getQuery();
            if (query == null) {
                return;
            }
            String[] paramPairs = query.split("&");
            Map<String, String> params = new HashMap<>();
            for (String paramPair : paramPairs) {
                String[] keyValuePair = paramPair.split("=");
                if (keyValuePair.length == 2) {
                    params.put(keyValuePair[0], keyValuePair[1]);
                }
            }

            if (newValue.startsWith("blob:")) {
                download(newValue);
            } else {
                LOG.info(String.format("requestType %s is not a download request"));
            }
        }

        private void download(String requestType) { //, Map<String, String> params) {
            LOG.info("I want to download file");
//TODO: Rewrite download function
            /*    long transactionId = Convert.parseUnsignedLong(params.get("transaction"));
            TaggedData taggedData = TaggedData.getData(transactionId);
            boolean retrieve = "true".equals(params.get("retrieve"));
            if (requestType.equals("downloadTaggedData")) {
                if (taggedData == null && retrieve) {
                    //TODO: Do something with tagged data
                    /*try {
                        if (blockchainProcessor.restorePrunedTransaction(transactionId) == null) {
                            growl("Pruned transaction data not currently available from any peer");
                            return;
                        }
                    }
                    catch (IllegalArgumentException e) {
                        growl("Pruned transaction data cannot be restored using desktop wallet without full blockchain. Use Web Wallet instead");
                        return;
                    }*//*
                    taggedData = TaggedData.getData(transactionId);
                }
                if (taggedData == null) {
                    growl("Tagged data not found");
                    return;
                }
                byte[] data = taggedData.getData();
                String filename = taggedData.getFilename();
                if (filename == null || filename.trim().isEmpty()) {
                    filename = taggedData.getName().trim();
                }
                downloadFile(data, filename);
            } else if (requestType.equals("downloadPrunableMessage")) {
                PrunableMessage prunableMessage = PrunableMessage.getPrunableMessage(transactionId);
                if (prunableMessage == null && retrieve) {
                    //TODO: Do something with tagged data
                    /*try {
                        if (blockchainProcessor.restorePrunedTransaction(transactionId) == null) {
                            growl("Pruned message not currently available from any peer");
                            return;
                        }
                    }
                    catch (IllegalArgumentException e) {
                        growl("Pruned message cannot be restored using desktop wallet without full blockchain. Use Web Wallet instead");
                        return;
                    }*//*
                    prunableMessage = PrunableMessage.getPrunableMessage(transactionId);
                }
                String secretPhrase = params.get("secretPhrase");
                byte[] sharedKey = Convert.parseHexString(params.get("sharedKey"));
                if (sharedKey == null) {
                    sharedKey = Convert.EMPTY_BYTE;
                }
                if (sharedKey.length != 0 && secretPhrase != null) {
                    growl("Do not specify both secret phrase and shared key");
                    return;
                }
                byte[] data = null;
                if (prunableMessage != null) {
                    try {
                        if (secretPhrase != null) {
                            data = prunableMessage.decrypt(secretPhrase);
                        } else if (sharedKey.length > 0) {
                            data = prunableMessage.decryptUsingSharedKey(sharedKey);
                        } else {
                            data = prunableMessage.getMessage();
                        }
                    }
                    catch (RuntimeException e) {
                        LOG.debug("Decryption of message to recipient failed: " + e.toString());
                        growl("Wrong secretPhrase or sharedKey");
                        return;
                    }
                }
                if (data == null) {
                    data = Convert.EMPTY_BYTE;
                }
                downloadFile(data, "" + transactionId);
            }
                    */
        }

        private void downloadFile(byte[] data, String filename) {
            Path folderPath = Paths.get(System.getProperty("user.home"), "downloads");
            folderPath.toFile().mkdirs();
            Path path = Paths.get(folderPath.toString(), filename);
            LOG.info("Downloading data to " + path.toAbsolutePath());
            try {
                OutputStream outputStream = Files.newOutputStream(path);
                outputStream.write(data);
                outputStream.close();
                growl(String.format("File %s saved to folder %s", filename, folderPath));

            } catch (IOException e) {
                growl("Download failed " + e.getMessage(), e);
            }
        }

        public void stop() {
            System.out.println("DesktopApplication stopped"); // Should never happen
        }

        private void growl(String msg) {
            growl(msg, null);
        }

        private void growl(String msg, Exception e) {
            if (e == null) {
                showInfoBox(msg);
                LOG.info(msg);
            } else {
                LOG.info(msg, e);
            }
            //ars.call("growl", msg);
        }


    }
//TODO: use supervisor
    private static class DbRecoveringUI {
        private static DbRecoveringUI instance = new DbRecoveringUI();

        private DbRecoveringUI() {
        }

        public static DbRecoveringUI getInstance() {
            return instance;
        }



        private Alert prepareAlert(Alert.AlertType alertType, String title, String contentText, int height, ButtonType... buttons) {
            Alert alert = new Alert(alertType, contentText);
            alert.getDialogPane().setMinHeight(height); // resizing
            alert.setTitle(title);
            if (buttons != null && buttons.length != 0) {
                alert.getButtonTypes().clear();
                for (ButtonType button : buttons) {
                    alert.getButtonTypes().add(button);
                }
            }
            return alert;
        }

    }

}
