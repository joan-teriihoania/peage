package fr.joanteriihoania.peage;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Chat {

    private static final String prefix = "&9&l[&r&bPéage&r&9&l]&r";

    public static void send(CommandSender sender, String text){
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " &r" + text));
    }

    public static void send(CommandSender sender, String[] texts){
        for(String text: texts){
            send(sender, text);
        }
    }

}
