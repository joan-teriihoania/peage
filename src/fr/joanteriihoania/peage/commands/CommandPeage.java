package fr.joanteriihoania.peage.commands;

import fr.joanteriihoania.peage.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.Stack;
import org.bukkit.entity.Player;
import sun.nio.ch.Net;

import java.io.File;
import java.util.ArrayList;

public class CommandPeage implements CommandExecutor {

    private ArrayList<Network> networks;

    public CommandPeage(ArrayList<Network> networks) {
        this.networks = networks;
    }

    public ArrayList<Network> getNetworks() {
        return networks;
    }

    public void setNetworks(ArrayList<Network> networks) {
        this.networks = networks;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String msg, String[] args) {
        if (command.getName().equalsIgnoreCase("peage") && sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                Chat.send(player, new String[]{
                        "&aPlugin &lPéage&r&a créé par Arcadia_sama.",
                        "Ce plugin permet de créer des réseaux de péages équipés avec des tarifs personnalisables.",
                        "&cPlugin en développement. &rDes bugs peuvent survenir, merci de les reporter aux développeurs le plus rapidement possible.",
                });
                return true;
            } else {
                if (args[0].equalsIgnoreCase("network") && args.length > 1) {
                    if (args[1].equalsIgnoreCase("edit") && args.length >= 5){
                        if (args[2].equalsIgnoreCase("name")){
                            if (Network.existsName(args[3])){
                                if(!Network.existsName(args[4])){
                                    Network network = Network.getNetworkFromName(args[3]);
                                    assert network != null;
                                    Chat.send(player, "&aLe nom du réseau &r" + network.getName() + "&a a été défini à &r" + args[4]);
                                    network.setName(args[4]);
                                } else {
                                    Chat.send(player, "&cUn réseau porte déjà le nom &r" + args[4] + "&c.");
                                }
                            } else {
                                Chat.send(player, "&cLe réseau &r" + args[3] + "&c n'existe pas");
                            }
                        }
                        return true;
                    }

                    if (args[1].equalsIgnoreCase("list")){
                        Chat.send(player, "");
                        Chat.send(player, "");
                        Chat.send(player, "Liste des réseaux créés :");
                        for(Network listNetworks: networks){
                            Chat.send(player, "&a - " + listNetworks.getName() + " &r avec " + listNetworks.getContent().size() + " stand(s)");
                        }
                        return true;
                    }

                    if (args[1].equalsIgnoreCase("create") && args.length > 2) {
                        if (!Network.existsName(args[2])) {
                            Network network = new Network();
                            network.setName(args[2]);
                            networks.add(network);
                            Chat.send(player, "&aRéseau &r'" + network.getName() + " (" + network.getUniqueId() + ")'&a créé.");
                            return true;
                        } else {
                            Chat.send(player, "&cUn réseau porte déjà le nom &r" + args[2] + "&c.");
                            return true;
                        }
                    }
                }

                if (args[0].equalsIgnoreCase("stand") && args.length > 1) {
                    if (args[1].equalsIgnoreCase("edit") && args.length == 6) {
                        boolean networkExists = Network.existsName(args[2]);
                        Network selNetwork = Network.getNetworkFromName(args[2]);

                        if (networkExists){
                            assert selNetwork != null;
                            if (selNetwork.standNameExists(args[3])){
                                Stand selStand = selNetwork.getStandFromName(args[3]);
                                if (args[4].equalsIgnoreCase("name")){

                                    if (!selNetwork.standNameExists(args[5])){
                                        Chat.send(player, "&aLe nom du stand &r" + selStand.getName() + "&a de &r" + selNetwork.getName() + "&a a été défini à &r" + args[5]);
                                        selStand.setName(args[5]);
                                        Guichet.refreshAll(selStand);
                                    } else {
                                        Chat.send(player, "&cUn stand porte déjà le nom &r" + args[5] + "&c sur le réseau &r" + selNetwork.getName() + "&c.");
                                    }
                                }

                                if (args[4].equalsIgnoreCase("price")){
                                    double newPrice = Double.parseDouble(args[5]);
                                    selStand.setPrice(newPrice);
                                    Chat.send(player, "&aLe prix du stand &r" + selStand.getName() + "&a de &r" + selNetwork.getName() + "&a a été défini à &r" + newPrice);
                                    Guichet.refreshAll(selStand);
                                }
                            } else {
                                Chat.send(player, "&cLe stand &r" + args[3] + "&c n'existe pas.");
                            }

                        } else {
                            Chat.send(player, "&cLe réseau &r" + args[2] + "&c n'existe pas.");
                        }
                        return true;
                    }

                    if (args[1].equalsIgnoreCase("list") && args.length == 3) {
                        boolean networkExists = Network.existsName(args[2]);
                        Network selNetwork = Network.getNetworkFromName(args[2]);

                        if(networkExists){
                            Chat.send(player, "");
                            Chat.send(player, "");
                            Chat.send(player, "Liste des stands du réseau &a" + selNetwork.getName() + "&r :");
                            for(Stand listStand: selNetwork.getContent()){
                                Chat.send(player, "&a - " + listStand.getName() + " avec " + listStand.getContent().size() + " guichet(s)");
                            }
                        } else {
                            Chat.send(player, "&cLe réseau &r" + args[2] + "&c n'existe pas.");
                        }

                        return true;
                    }

                    if (args[1].equalsIgnoreCase("create") && args.length > 3) {
                        boolean networkExists = Network.existsName(args[2]);
                        Network selNetwork = Network.getNetworkFromName(args[2]);

                        if (networkExists) {
                            assert selNetwork != null;
                            if (!selNetwork.standNameExists(args[3])) {
                                Stand stand = new Stand();
                                stand.setName(args[3]);
                                selNetwork.addContent(stand);
                                Chat.send(player, "&aStand &r'" + stand.getName() + "'&a créé.");
                            } else {
                                Chat.send(player, "&cUn stand porte déjà le nom &r" + args[3] + "&c sur le réseau &r" + selNetwork.getName() + "&c.");
                            }
                        } else {
                            Chat.send(player, "&cLe network &r'" + args[2] + "'&c n'existe pas.");
                        }

                        return true;
                    }

                    if (args[1].equalsIgnoreCase("set") && args.length > 3) {
                        boolean networkExists = Network.existsName(args[2]);
                        Network selNetwork = Network.getNetworkFromName(args[2]);

                        if (networkExists) {
                            assert selNetwork != null;
                            if (selNetwork.standNameExists(args[3])) {
                                Stand selStand = selNetwork.getStandFromName(args[3]);
                                Sign sign = playerLookAtSign(player);
                                if (sign != null) {
                                    if (!Guichet.exists(sign.getLocation())) {
                                        Guichet guichet = new Guichet();
                                        guichet.setStand(selStand);
                                        guichet.setNetwork(selNetwork);
                                        guichet.setSign(sign);
                                        selStand.addContent(guichet);

                                        guichet.refresh();

                                        Chat.send(player, "&aPanneau affecté avec succès.");
                                    } else {
                                        Chat.send(player, "&cCe panneau a déjà été affecté à un guichet.");
                                    }
                                } else {
                                    Chat.send(player, "&cRegardez un &lpanneau&r&c dans un &lrayon de 5 blocs&r&c pour exécuter cette commande.");
                                }
                                return true;
                            } else {
                                Chat.send(player, "&cLe stand &r'" + args[3] + "'&c n'existe pas.");
                            }
                        } else {
                            Chat.send(player, "&cLe network &r'" + args[2] + "'&c n'existe pas.");
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private Sign playerLookAtSign(Player player){
        Block block = player.getTargetBlockExact(5);

        if (block != null && block.getState() instanceof Sign){
            return (Sign) block.getState();
        } else {
            return null;
        }
    }

}
