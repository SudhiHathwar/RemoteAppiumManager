package com.hariharanweb.remoteappiummanager;

import com.hariharanweb.remoteappiummanager.controller.AppiumController;
import com.hariharanweb.remoteappiummanager.controller.ArtifactsController;
import com.hariharanweb.remoteappiummanager.controller.DeviceController;
import com.hariharanweb.remoteappiummanager.controller.MachineController;
import com.hariharanweb.remoteappiummanager.transformers.JsonTransformer;
import spark.Spark;

import java.io.IOException;
import java.util.logging.Logger;

import static com.hariharanweb.helpers.Helpers.isPortAvailable;
import static spark.Spark.*;

public class Server {

    public static void main(String[] args) throws IOException {

        final Logger LOGGER =
                Logger.getLogger(Server.class.getName());
        if (System.getProperty("port") != null) {
            int port = Integer.parseInt(System.getProperty("port"));
            if (isPortAvailable(port)) {
                port(port);
            } else {
                throw new RuntimeException("Port" + port + " in use");
            }
            LOGGER.info("Started Server on port" + System.getProperty("port"));
        }
        Spark.staticFiles.externalLocation(System.getProperty("user.dir") + "/target/");

        DeviceController deviceController = new DeviceController();
        AppiumController appiumController = new AppiumController();
        MachineController machineController = new MachineController();
        ArtifactsController artifactsController = new ArtifactsController();

        get("/", (req, res) -> "Server is Running!!!");
        //Get all Android, IOS Devices & Booted Sims
        get("/devices", deviceController.getDevices, new JsonTransformer());

        path("/devices", () -> {
            //Get all Android Devices
            get("/android", deviceController.getAndroidDevices, new JsonTransformer());
            //Get all Booted Sims + Real devices
            get("/ios", deviceController.getIOSDevices, new JsonTransformer());
            path("/ios", () -> {
                //Get all iOS Real devices
                get("/realDevices", deviceController.getIOSRealDevices, new JsonTransformer());
                get("/bootedSims", deviceController.getBootedSims, new JsonTransformer());
                path("/webkitproxy", () -> {
                    get("/start", deviceController.startWebkitProxy, new JsonTransformer());
                    get("/stop", deviceController.stopWebkitProxy, new JsonTransformer());
                });
            });
        });

        path("/device", () -> {
            //Returns Specific Android or IOS Device
            get("/:udid", deviceController.getDevice, new JsonTransformer());
            path("/ios", () -> {
                //Return iOS Sim
                get("/simulator", deviceController.getSimulator, new JsonTransformer());
            });
        });
        path("/device/adblog", () -> {
            get("/start", deviceController.startADBLog, new JsonTransformer());
            get("/stop/:udid", deviceController.stopADBLog, new JsonTransformer());
        });
        path("/appium", () -> {
            post("/start", appiumController.startAppium, new JsonTransformer());
            get("/stop", appiumController.stopAppium, new JsonTransformer());
            get("/isRunning", appiumController.isAppiumServerRunning, new JsonTransformer());
            get("/logs", appiumController.getAppiumLogs);
        });

        path("/machine", () -> {
            get("/xcodeVersion", machineController.getXCodeVersion);
            get("/availablePort", machineController.getAvailablePort);
        });

        path("/artifacts", ()-> {
           post("/upload", artifactsController.upload);
        });

        after((request, response) -> response.header("Content-Type", "application/json"));
    }
}
