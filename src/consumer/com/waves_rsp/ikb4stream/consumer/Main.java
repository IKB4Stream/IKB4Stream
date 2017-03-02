/*
 * Copyright (C) 2017 ikb4stream team
 * ikb4stream is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ikb4stream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package com.waves_rsp.ikb4stream.consumer;

import com.waves_rsp.ikb4stream.consumer.manager.CommunicationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main class executes the program and launchs the CommunicationManager
 *
 * @author ikb4stream
 * @version 1.0
 */
public class Main {
    /**
     * CommunicationManager is the first class to be launch
     *
     * @see CommunicationManager#getInstance()
     */
    private static final CommunicationManager COMMUNICATION_MANAGER = CommunicationManager.getInstance();
    /**
     * Logger used to log all information in this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * Private constructor to block instantiation
     */
    private Main() {

    }

    /**
     * This method allows the execution of the program
     *
     * @param args a array of string
     */
    public static void main(String[] args) {
        printLogo();
        LOGGER.info("IKB4Stream Consumer Module start");
        COMMUNICATION_MANAGER.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("IKB4Stream Consumer Module stop");
            COMMUNICATION_MANAGER.stop();
        }));
    }

    private static final void printLogo() {
        System.out.println(
                "\n" +
                "`////////////////////////////////////////////////////////////.\n" +
                "-dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd+\n" +
                "-dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd+\n" +
                "-dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd+\n" +
                "-dddddddddddddddhoooydddddddddddddddooosdddddhsoooydddddddddd+\n" +
                "-ddddddddddddddds   /ddddddddddddddd   `ddddo.  `/hdddddddddd+\n" +
                "-ddddddddddddddds   /ddddddddddddddd   `dds-   :ydddddddddddd+\n" +
                "-ddddddddddddddds   /ddddddddddddddd   `y:   -sdddddddddddddd+\n" +
                "-ddddddddddddddds   /ddddddddddddddd    `   :dddddddddddddddd+\n" +
                "-ddddddddddddddds   /ddddddddddddddd     `   /hdddddddddddddd+\n" +
                "-ddddddddddddddds   /ddddddddddddddd    /y/   .yddddddddddddd+\n" +
                "-ddddddddddddddds   /ddddddddddddddd   `ddds.  `+dddddddddddd+\n" +
                "-ddddddddddddddds   /ddddddddddddddd   `ddddh:   :hdddddddddd+\n" +
                "-dddddddddddddddhoooydddddddddddddddooosddddddsooosdddddddddd+\n" +
                "-dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd+\n" +
                "-dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd+\n" +
                "-dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd+\n" +
                "-dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd+\n" +
                "-dddddddddddyooooooosyddddddddddddddddddddooooddddddddddddddd+\n" +
                "-ddddddddddd-          -ydddddddddddddddh-   oddddddddddddddd+\n" +
                "-ddddddddddd-   osso/   `ddddddddddddddy`  `ydddddddddddddddd+\n" +
                "-ddddddddddd-   hdddh   .ddddddddddddds`  .hddddddddddddddddd+\n" +
                "-ddddddddddd-   ...`   :hdddddddddddd+   -h```/dddddddddddddd+\n" +
                "-ddddddddddd-   ----.` `:ydddddddddd/   `++   -oydddddddddddd+\n" +
                "-ddddddddddd-   yddddh   .dddddddddd`           /dddddddddddd+\n" +
                "-ddddddddddd-   ossso:   .ddddddddddsssssso   -sydddddddddddd+\n" +
                "-ddddddddddd-          `:hddddddddddddddddh   :dddddddddddddd+\n" +
                "-dddddddddddsoooooooosyhdddddddddddddddddddoooydddddddddddddd+\n" +
                "-dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd+\n" +
                "-dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd+\n" +
                "-dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd+\n" +
                ".oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo:\n" +
                "\n" +
                "\n" +
                "           hh+\n" +
                " `:osso/ `oddho+  +o+:o/  ./oss+-`   /osso/.  -oo:oso:`:oss+. \n" +
                " +dh/:++` :dds:-  yddyo: +dd   hds`      ydh` /ddy+/hdhh+/ydh.\n" +
                " -yhyso:`  ddo    ydh`  `ddhoooydd-  sssshdd. /dd-  /dd-  -dd:\n" +
                " ..-:+dd/  dds`.  ydy    hdy        ddo  sdd. /dd.  /dd.  -dd:\n" +
                "`shysshy.  +hdyh: ydy    .ohhyyhy.  `hhyyydd. /dd.  /dd.  -dd:\n" +
                "  `.--.     `--.  ...      `.--.     `--.`..  `..   `..   `..`\n" +
                "\n");
    }
}
