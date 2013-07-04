package com.me.tft_02.ghosts.util;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;

public class UpdateChecker {
    private UpdateChecker() {}

    public static boolean updateAvailable() throws Exception {
        String checkType = Config.getInstance().getPreferBeta() ? "latest" : "release";
        String version = Ghosts.p.getDescription().getVersion();
        InputStreamReader isr;

        try {
            isr = new InputStreamReader(new URL("http://api.bukget.org/api2/bukkit/plugin/ghosts/" + checkType).openStream());
        }
        catch (UnknownHostException e) {
            return false;
        }

        try {
            Object o = new JSONParser().parse(isr);

            if (!(o instanceof JSONObject)) {
                return false;
            }

            JSONObject versions = (JSONObject) ((JSONObject) o).get("versions");
            String newVersion = (String) versions.get("version");

            String[] oldTokens = version.replaceAll("(?i)(-)(.+?)(-)", "-").split("[.]|-b");
            String[] newTokens = newVersion.replaceAll("(?i)(-)(.+?)(-)", "-").split("[.]|-b");

            for (int i = 0; i < 4; i++) {
                Integer newVer = Integer.parseInt(newTokens[i]);
                Integer oldVer;

                try {
                    oldVer = Integer.parseInt(oldTokens[i]);
                }
                catch (NumberFormatException e) {
                    oldVer = 0;
                }

                if (oldVer < newVer) {
                    return true;
                }
            }

            return false;
        }
        catch (ParseException e) {
            return false;
        }
        finally {
            isr.close();
        }
    }
}
