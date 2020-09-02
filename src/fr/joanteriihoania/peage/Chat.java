package fr.joanteriihoania.peage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class Chat {

    private static String prefix;
    private static HashMap<String, ArrayList<String>> unsentMessages = new HashMap<>();

    public static void setPrefix(String prefix) {
        Chat.prefix = prefix;
    }

    public static void sendUnsentMessages(Player player){
        if (unsentMessages.containsKey(player.getName())){
            for (String text: unsentMessages.get(player.getName())){
                Chat.send(player, text);
            }
        }
    }

    public static void send(Player player, String text){
        if (player == null) {
            //Console.output("No player for : " + text);
            return;
        }

        if (!player.isOnline()){
            //Console.output("Saved for " + player.getName() + " : " + text);
            if (unsentMessages.containsKey(player.getName())){
                unsentMessages.get(player.getName()).add(text);
            } else {
                ArrayList<String> message = new ArrayList<>();
                message.add(text);
                unsentMessages.put(player.getName(), message);
            }
            return;
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " &r" + text));
    }

    public static void send(Player player, String[] texts){
        for(String text: texts){
            send(player, text);
        }
    }

}
