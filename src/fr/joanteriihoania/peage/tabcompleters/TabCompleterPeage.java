package fr.joanteriihoania.peage.tabcompleters;

import fr.joanteriihoania.peage.Network;
import fr.joanteriihoania.peage.Stand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TabCompleterPeage implements TabCompleter {

    private ArrayList<Network> networks;

    public TabCompleterPeage(ArrayList<Network> networks) {
        this.networks = networks;
    }

    public ArrayList<Network> getNetworks() {
        return networks;
    }

    public void setNetworks(ArrayList<Network> networks) {
        this.networks = networks;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String msg, String[] args) {
        ArrayList<String> listToReturn = new ArrayList<>();
        if (args.length == 1){
            listToReturn.add("network");
            listToReturn.add("stand");
        }

        if(args.length == 2){
            listToReturn.add("list");
            listToReturn.add("create");
            listToReturn.add("delete");
            listToReturn.add("edit");
            listToReturn.add("set");
        }

        if(args[0].equals("network") && args.length > 2){
            if(args[1].equals("edit")) {
                if (args.length == 3){
                    listToReturn.add("name");
                }

                if (args.length == 4){
                    for (Network network : networks) {
                        listToReturn.add(network.getName());
                    }
                }
            }
        }

        if(args[0].equals("stand") && args.length > 2){
            if(args[1].equals("edit")) {
                if (args.length == 3) {
                    for (Network network : networks) {
                        listToReturn.add(network.getName());
                    }
                }

                if (args.length == 4) {
                    for (Network network : networks) {
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
                    for (Network network : networks) {
                        listToReturn.add(network.getName());
                    }
                }
            }

            if(args[1].equals("create")) {
                if (args.length == 3) {
                    for (Network network : networks) {
                        listToReturn.add(network.getName());
                    }
                }
            }

            if(args[1].equals("set")) {
                if (args.length == 3){
                    for (Network network : networks) {
                        listToReturn.add(network.getName());
                    }
                }

                if (args.length == 4) {
                    for (Network network : networks) {
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
