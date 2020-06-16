package fr.joanteriihoania.peage;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

public class Signs {

    private static String prefix;

    public static void setPrefix(String prefix) {
        Signs.prefix = prefix;
    }

    public static void set(Sign sign, int index, String text){
        sign.setLine(0, ChatColor.translateAlternateColorCodes('&', prefix + " &r"));
        sign.setLine(index, ChatColor.translateAlternateColorCodes('&', text));
        sign.update();
    }

    public static void set(Sign sign, String[] texts){
        for(int i = 1; i < 4; i++){
            set(sign, i, texts[i-1]);
        }
    }

}
