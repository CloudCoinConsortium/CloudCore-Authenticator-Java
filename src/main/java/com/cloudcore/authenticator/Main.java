package com.cloudcore.authenticator;

import com.cloudcore.authenticator.core.FileSystem;
import com.cloudcore.authenticator.raida.RAIDA;
import com.cloudcore.authenticator.utils.FileUtils;
import com.cloudcore.authenticator.utils.SimpleLogger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.cloudcore.authenticator.raida.RAIDA.updateLog;

public class Main {

    public static SimpleLogger logger;

    public static int NetworkNumber = 1;

    public static void main(String[] args) {
        SimpleLogger.writeLog("ServantAuthenticatorStarted", "");

            if (args.length != 0 && Files.exists(Paths.get(args[0]))) {
                System.out.println("New root path: " + args[0]);
                FileSystem.changeRootPath(args[0]);
            }
            //FileSystem.changeRootPath("C:\\Users\\You\\Documents\\CloudCoin\\Accounts\\DefaultUser\\");
            //SimpleLogger.writeLog("changedpath_" + FileSystem.RootPath.contains("You"), "lol");
            System.out.println(FileSystem.RootPath);

            setup();
            RAIDA.logger = logger;
            updateLog("Loading Network Directory");
            SetupRAIDA();
            FileSystem.loadFileSystem();

            if (0 != FileUtils.selectFileNamesInFolder(FileSystem.SuspectFolder).length) {
                RAIDA.processNetworkCoins(NetworkNumber);
            }

            FolderWatcher watcher = new FolderWatcher(FileSystem.SuspectFolder);
            System.out.println("Watching folders at " + FileSystem.SuspectFolder + "...");
            boolean detectingFiles = false;
            long timeWaitingForFilesToBeWritten = 0;

            while (true) {
                try {
                    Thread.sleep(100);

                    // If a change is detected, set the timer.
                    if (watcher.newFileDetected()) {
                        detectingFiles = true;
                        timeWaitingForFilesToBeWritten = System.currentTimeMillis() + 1000;
                        System.out.println("found files, waiting a second to authenticate");
                        continue;
                    }

                    if (!detectingFiles || timeWaitingForFilesToBeWritten > System.currentTimeMillis())
                        continue;

                    detectingFiles = false;

                    System.out.println("Processing Network Coins...");
                    RAIDA.processNetworkCoins(NetworkNumber);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Uncaught exception - " + e.getLocalizedMessage());
                }
            }
    }

    private static void setup() {
        FileSystem.createDirectories();
        RAIDA.getInstance();
        FileSystem.loadFileSystem();

        logger = new SimpleLogger(FileSystem.LogsFolder + "logs" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")).toLowerCase() + ".log", true);

        //Connect to Trusted Trade Socket
        //tts = new TrustedTradeSocket("wss://escrow.cloudcoin.digital/ws/", 10, OnWord, OnStatusChange, OnReceive, OnProgress);
        //tts.Connect().Wait();
    }
    public static void SetupRAIDA() {
        try
        {
            RAIDA.instantiate();
        }
        catch(Exception e)
        {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(1);
        }
        if (RAIDA.networks.size() == 0)
        {
            updateLog("No Valid Network found.Quitting!!");
            System.exit(1);
        }
        else
        {
            updateLog(RAIDA.networks.size() + " Networks found.");
            RAIDA raida = RAIDA.networks.get(0);
            for (RAIDA r : RAIDA.networks)
                if (NetworkNumber == r.networkNumber) {
                    raida = r;
                    break;
                }

            RAIDA.activeRAIDA = raida;
            if (raida == null) {
                updateLog("Selected Network Number not found. Quitting.");
                System.exit(0);
            }
        }
        //networks[0]
    }
}
