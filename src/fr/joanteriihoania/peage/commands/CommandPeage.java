package fr.joanteriihoania.peage.commands;

import com.sun.java.accessibility.util.GUIInitializedListener;
import fr.joanteriihoania.peage.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.Stack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sun.nio.ch.Net;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CommandPeage implements CommandExecutor {

    private ArrayList<Network> networks;
    private static Main main;

    public CommandPeage(ArrayList<Network> networks, Main mainv) {
        main = mainv;
        this.networks = networks;
    }

    public ArrayList<Network> getNetworks() {
        return networks;
    }

    public void setNetworks(ArrayList<Network> networks) {
        this.networks = networks;
    }

    private void loadData(Player player){
        try {
            main.loadData();
            Chat.send(player, "&aLast saved memory state loaded");
        } catch (IOException e) {
            Chat.send(player, "&cError: Memory state load failed (" + e.getMessage() + ")");
            e.printStackTrace();
        }
    }

    private void saveData(Player player){
        try {
            main.saveData();
            Chat.send(player, "&aCurrent memory state saved");
        } catch (IOException e) {
            Chat.send(player, "&cError: Memory state save failed (" + e.getMessage() + ")");
            e.printStackTrace();
        }
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
                if (player.hasPermission("peage.admin")) {
                    if (args[0].equalsIgnoreCase("save")) {
                        saveData(player);
                        return true;
                    }

                    if (args[0].equalsIgnoreCase("load")) {
                        loadData(player);
                        return true;
                    }

                    if (args[0].equalsIgnoreCase("reload")) {
                        saveData(player);
                        loadData(player);
                        Chat.send(player, "Memory state reloaded");
                        return true;
                    }
                }

                if (args[0].equalsIgnoreCase("badge") && args.length >= 5) {
                    ItemStack badge = new ItemStack(Material.PAPER, 1);
                    ItemMeta itemMeta = badge.getItemMeta();
                    if(itemMeta != null) {
                        BadgeParser badgeParser = new BadgeParser();
                        Network badgeNetwork = Network.getNetworkFromName(args[1]);

                        if (badgeNetwork != null){
                            if (!badgeNetwork.isOwner(player)){
                                Chat.send(player, "&cVous n'êtes pas le propriétaire de ce réseau.");
                                return true;
                            }

                            if (args[2].equals("all")){
                                badgeParser.setFullNetwork(true);
                                badgeParser.setStand(null);
                            } else {
                                Stand badgeStand = Stand.getStandFromName(args[2]);
                                if (badgeStand != null){
                                    badgeParser.setStand(badgeStand);
                                } else {
                                    Chat.send(player, "&cLa zone &r" + args[2] + "&c n'existe pas.");
                                    return true;
                                }
                            }
                        } else {
                            Chat.send(player, "&cLe réseau &r" + args[1] + "&c n'existe pas.");
                            return true;
                        }

                        if (!args[3].equals("freepass") && !args[3].equals("reducpass")){
                            Chat.send(player, "&cLe type de badge &f"+args[3]+"&c n'existe pas.");
                            return true;
                        }

                        if (!args[4].equals("unlimited") && !args[4].equals("limited")){
                            Chat.send(player, "&cLe type de forfait &f"+args[4]+"&c n'existe pas.");
                            return true;
                        }

                        if (args[3].equals("reducpass")){
                            if (args.length < 6) return false;
                            if (!CheckDoubleInteger.isDouble(args[5])){
                                Chat.send(player, "&cLa réduction de &f"+args[5]+"%&c est invalide.");
                                return true;
                            }
                            badgeParser.setReduction(Double.parseDouble(args[5]) / 100);

                            if (args[4].equals("limited")) {
                                if (args.length < 7) return false;
                                if (!CheckDoubleInteger.isInteger(args[6])) {
                                    Chat.send(player, "&cLe nombre d'utilisation &f" + args[6] + "&c est invalide.");
                                    return true;
                                }
                                badgeParser.setQuantity(Integer.parseInt(args[6]));
                            }
                        } else {
                            if(args[4].equals("limited")) {
                                if (args.length < 6) return false;
                                if (!CheckDoubleInteger.isInteger(args[5])) {
                                    Chat.send(player, "&cLe nombre d'utilisation &f" + args[5] + "&c est invalide.");
                                    return true;
                                }

                                badgeParser.setQuantity(Integer.parseInt(args[5]));
                            }
                        }


                        badgeParser.setQuantityType(args[4]);
                        badgeParser.setBadgeType(args[3]);
                        badgeParser.setNetwork(badgeNetwork);

                        itemMeta.setLocalizedName(badgeParser.getTag());
                        itemMeta.setLore(badgeParser.getLore());
                        itemMeta.setDisplayName(badgeParser.getDisplayName());

                        badge.setItemMeta(itemMeta);
                        player.getInventory().addItem(badge);
                        Chat.send(player, "&aBadge ajouté à votre inventaire.");
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("pay") && args.length >= 2) {
                    Block playerBlock = player.getLocation().getBlock();
                    Location playerLocation = playerBlock.getLocation();
                    Guichet guichetTargeted = null;
                    boolean isNearGuichet = false;

                    for(Guichet searchGuichet: Guichet.getAllGuichets()){
                        if (searchGuichet.getEntranceCenter().getLocation().distance(playerLocation) <= 2){
                            guichetTargeted = searchGuichet;
                            isNearGuichet = true;
                        }
                    }

                    if (isNearGuichet) {
                        if (guichetTargeted.getUniqueId().equals(args[1])) {
                            if(!main.isGuichetTriggered(guichetTargeted)) {
                                main.addGuichetTriggered(guichetTargeted);
                                if (guichetTargeted.clicked(player)) {
                                    main.addGuichetTriggered(guichetTargeted);
                                }
                            }
                        }else {
                            Chat.send(player, "&cVous n'êtes pas à portée du guichet " + args[1]);
                        }
                    } else {
                        Chat.send(player, "&cVous n'êtes à portée d'aucun guichet.");
                    }

                    return true;
                }

                if (args[0].equalsIgnoreCase("network") && args.length > 1) {
                    if (args[1].equalsIgnoreCase("delete") && args.length >= 3) {
                        boolean networkExists = Network.existsName(args[2]);
                        Network selNetwork = Network.getNetworkFromName(args[2]);
                        if (networkExists){
                            assert selNetwork != null;

                            if (!selNetwork.isOwner(player)){
                                Chat.send(player, "&cVous n'êtes pas le propriétaire de ce réseau.");
                                return true;
                            }

                            Chat.send(player, "&aLe réseau &f" + selNetwork.getName() + "&a a été supprimé.");
                            selNetwork.delete();
                            networks.remove(selNetwork);

                        } else {
                            Chat.send(player, "&cLe réseau &r" + args[2] + "&c n'existe pas.");
                        }
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("edit") && args.length >= 5){
                        if (args[2].equalsIgnoreCase("name")){
                            if (Network.existsName(args[3])){
                                if(!Network.existsName(args[4])){
                                    Network network = Network.getNetworkFromName(args[3]);
                                    assert network != null;

                                    if (!network.isOwner(player)){
                                        Chat.send(player, "&cVous n'êtes par le propriétaire de ce réseau.");
                                        return true;
                                    }

                                    Chat.send(player, "&aLe nom du réseau &r" + network.getName() + "&a a été défini à &r" + args[4]);
                                    network.setName(args[4]);
                                    network.refresh();
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
                        Chat.send(player, "Liste des réseaux créés :");
                        for(Network listNetworks: networks){
                            Chat.send(player, "&a - " + listNetworks.getName() + " &r avec " + listNetworks.getContent().size() + " zone(s)");
                        }
                        return true;
                    }

                    if (args[1].equalsIgnoreCase("create") && args.length > 2) {
                        if (!Network.existsName(args[2])) {
                            Network network = new Network();
                            network.setName(args[2]);
                            network.setOwner(player);
                            networks.add(network);
                            Chat.send(player, "&aLe réseau &r'" + network.getName() + " (" + network.getUniqueId() + ")'&a a été créé.");
                            return true;
                        } else {
                            Chat.send(player, "&cUn réseau porte déjà le nom &r" + args[2] + "&c.");
                            return true;
                        }
                    }
                }

                if (args[0].equalsIgnoreCase("area") && args.length > 1) {
                    if (args[1].equalsIgnoreCase("delete") && args.length >= 4) {
                        boolean networkExists = Network.existsName(args[2]);
                        Network selNetwork = Network.getNetworkFromName(args[2]);
                        if (networkExists){
                            assert selNetwork != null;

                            if (!selNetwork.isOwner(player)){
                                Chat.send(player, "&cVous n'êtes par le propriétaire de ce réseau.");
                                return true;
                            }

                            if (selNetwork.standNameExists(args[3])){
                                Stand selStand = selNetwork.getStandFromName(args[3]);
                                Chat.send(player, "&aLa zone &f" + selStand.getName() + "&a a été supprimée.");
                                selStand.delete();
                            } else {
                                Chat.send(player, "&cLa zone &r" + args[3] + "&c n'existe pas.");
                            }

                        } else {
                            Chat.send(player, "&cLe réseau &r" + args[2] + "&c n'existe pas.");
                        }
                        return true;
                    }

                    if (args[1].equalsIgnoreCase("edit") && args.length >= 6) {
                        boolean networkExists = Network.existsName(args[2]);
                        Network selNetwork = Network.getNetworkFromName(args[2]);
                        if (networkExists){
                            assert selNetwork != null;

                            if (!selNetwork.isOwner(player)){
                                Chat.send(player, "&cVous n'êtes par le propriétaire de ce réseau.");
                                return true;
                            }

                            if (selNetwork.standNameExists(args[3])){
                                Stand selStand = selNetwork.getStandFromName(args[3]);
                                if (args[4].equalsIgnoreCase("name")){

                                    if (!args[5].equals("all")) {
                                        if (!selNetwork.standNameExists(args[5])) {
                                            Chat.send(player, "&aLe nom de la zone &r" + selStand.getName() + "&a de &r" + selNetwork.getName() + "&a a été défini à &r" + args[5]);
                                            selStand.setName(args[5]);
                                            Guichet.onEdit(selStand);
                                        } else {
                                            Chat.send(player, "&cUne zone porte déjà le nom &r" + args[5] + "&c sur le réseau &r" + selNetwork.getName() + "&c.");
                                        }
                                    } else {
                                        Chat.send(player, "&cLa zone &fall&c ne peut pas être utilisée");
                                    }
                                }

                                if (args[4].equalsIgnoreCase("price")){
                                    if (CheckDoubleInteger.isDouble(args[5])) {
                                        double newPrice = Double.parseDouble(args[5]);
                                        selStand.setPrice(newPrice);
                                        Chat.send(player, "&aLe prix de la zone &r" + selStand.getName() + "&a de &r" + selNetwork.getName() + "&a a été défini à &r" + newPrice);
                                        Guichet.onEdit(selStand);
                                    } else {
                                        Chat.send(player, "&cLe prix &r"+args[5]+"&c est invalide.");
                                    }
                                }
                            } else {
                                Chat.send(player, "&cLa zone &r" + args[3] + "&c n'existe pas.");
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
                            Chat.send(player, "Liste des zones du réseau &a" + selNetwork.getName() + "&r :");
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

                            if (!selNetwork.isOwner(player)){
                                Chat.send(player, "&cVous n'êtes par le propriétaire de ce réseau.");
                                return true;
                            }

                            if (!args[3].equals("all")) {
                                if (!selNetwork.standNameExists(args[3])) {
                                    Stand stand = new Stand();
                                    stand.setName(args[3]);
                                    selNetwork.addContent(stand);
                                    Chat.send(player, "&aLa zone &r'" + stand.getName() + "'&a a été créé.");
                                } else {
                                    Chat.send(player, "&cUne zone porte déjà le nom &r" + args[3] + "&c sur le réseau &r" + selNetwork.getName() + "&c.");
                                }
                            } else {
                                Chat.send(player, "&cLa zone &fall&c ne peut pas être utilisée");
                            }
                        } else {
                            Chat.send(player, "&cLe réseau &r'" + args[2] + "'&c n'existe pas.");
                        }

                        return true;
                    }

                    if (args[1].equalsIgnoreCase("set") && args.length > 3) {
                        boolean networkExists = Network.existsName(args[2]);
                        Network selNetwork = Network.getNetworkFromName(args[2]);

                        if (networkExists) {
                            assert selNetwork != null;

                            if (!selNetwork.isOwner(player)){
                                Chat.send(player, "&cVous n'êtes par le propriétaire de ce réseau.");
                                return true;
                            }

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
                                        String coor = sign.getX() + " " + sign.getY() + " " + sign.getZ();
                                        Chat.send(player, "&aUn guichet &f"+guichet.getUniqueId()+"&a de la zone &f"+selStand.getName()+"&a du réseau &f"+selNetwork.getName()+"&a a été affecté aux coordonnées &f"+coor+"&a.");
                                    } else {
                                        Chat.send(player, "&cCe panneau a déjà été affecté à un guichet.");
                                    }
                                } else {
                                    Chat.send(player, "&cVous devez regarder un panneau.");
                                }
                                return true;
                            } else {
                                Chat.send(player, "&cLa zone &r'" + args[3] + "'&c n'existe pas.");
                            }
                        } else {
                            Chat.send(player, "&cLe réseau &r'" + args[2] + "'&c n'existe pas.");
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
