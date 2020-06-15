package fr.joanteriihoania.peage.tabcompleters;

import fr.joanteriihoania.peage.Network;
import fr.joanteriihoania.peage.Stand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TabCompleterPeage implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String msg, String[] args) {
        ArrayList<String> listToReturn = new ArrayList<>();
        if (args.length == 1){
            if (sender.hasPermission("peage.admin")) {
                listToReturn.add("load");
                listToReturn.add("save");
                listToReturn.add("reload");
            }

            listToReturn.add("network");
            listToReturn.add("area");
        }

        if(args.length == 2 && (args[0].equals("network") || args[0].equals("area"))){
            listToReturn.add("list");
            listToReturn.add("create");
            listToReturn.add("delete");
            listToReturn.add("edit");
            listToReturn.add("set");
        }

        if(args[0].equals("network") && args.length > 2){
            if(args[1].equals("delete")) {
                if (args.length == 3) {
                    for (Network network : Network.getOwnedBy(Bukkit.getPlayer(sender.getName()))) {
                        listToReturn.add(network.getName());
                    }
                }
            }

            if(args[1].equals("edit")) {
                if (args.length == 3){
                    listToReturn.add("name");
                }

                if (args.length == 4){
                    for (Network network : Network.getOwnedBy(Bukkit.getPlayer(sender.getName()))) {
                        listToReturn.add(network.getName());
                    }
                }
            }
        }

        if(args[0].equals("area") && args.length > 2){
            if(args[1].equals("delete")) {
                if (args.length == 3) {
                    for (Network network : Network.getOwnedBy(Bukkit.getPlayer(sender.getName()))) {
                        listToReturn.add(network.getName());
                    }
                }

                if (args.length == 4) {
                    for (Network network : Network.getOwnedBy(Bukkit.getPlayer(sender.getName()))) {
                        if (network.getName().equals(args[2])) {
                            for (Stand stand : network.getContent()) {
                                listToReturn.add(stand.getName());
                            }
                        }
                    }
                }
            }

            if(args[1].equals("edit")) {
                if (args.length == 3) {
                    for (Network network : Network.getOwnedBy(Bukkit.getPlayer(sender.getName()))) {
                        listToReturn.add(network.getName());
                    }
                }

                if (args.length == 4) {
                    for (Network network : Network.getOwnedBy(Bukkit.getPlayer(sender.getName()))) {
                        if (network.getName().equals(args[2])) {
                            for (Stand stand : network.getContent()) {
                                listToReturn.add(stand.getName());
                            }
                        }
                    }
                }

                if (args.length == 5) {
                    listToReturn.add("name");
                    listToReturn.add("price");
                }
            }

            if(args[1].equals("list")) {
                if (args.length == 3) {
                    for (Network network : Network.getOwnedBy(Bukkit.getPlayer(sender.getName()))) {
                        listToReturn.add(network.getName());
                    }
                }
            }

            if(args[1].equals("create")) {
                if (args.length == 3) {
                    for (Network network : Network.getOwnedBy(Bukkit.getPlayer(sender.getName()))) {
                        listToReturn.add(network.getName());
                    }
                }
            }

            if(args[1].equals("set")) {
                if (args.length == 3){
                    for (Network network : Network.getOwnedBy(Bukkit.getPlayer(sender.getName()))) {
                        listToReturn.add(network.getName());
                    }
                }

                if (args.length == 4) {
                    for (Network network : Network.getOwnedBy(Bukkit.getPlayer(sender.getName()))) {
                        if (network.getName().equals(args[2])) {
                            for (Stand stand : network.getContent()) {
                                listToReturn.add(stand.getName());
                            }
                        }
                    }
                }
            }
        }

        return listToReturn;
    }
}
