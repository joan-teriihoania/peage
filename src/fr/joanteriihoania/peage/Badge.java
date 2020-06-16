package fr.joanteriihoania.peage;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.concurrent.DelayQueue;

public class Badge {

    private static int autoinc;
    private static ArrayList<Badge> allBadges = new ArrayList<>();
    private int id;
    private Network network;
    private Stand stand;
    private boolean fullNetwork;
    private double price;
    private String badgeType;
    private String quantityType;
    private double reduction;
    private int quantity;


    public Badge(Network network, Stand stand, boolean fullNetwork, double price, String badgeType, String quantityType, double reduction, int quantity) {
        id = autoinc;
        autoinc++;
        this.network = network;
        this.stand = stand;
        this.fullNetwork = fullNetwork;
        this.price = price;
        this.badgeType = badgeType;
        this.quantityType = quantityType;
        this.reduction = reduction;
        this.quantity = quantity;
        allBadges.add(this);
    }

    public static ArrayList<Badge> getAllBadges() {
        return allBadges;
    }

    public boolean isFullNetwork() {
        return fullNetwork;
    }

    public static Badge getBadgeFromId(String text){
        for (Badge badge: allBadges){
            if (badge.getUniqueId().equals(text)){
                return badge;
            }
        }
        return null;
    }

    public static ArrayList<Badge> getBadgesFromNetwork(Network bnetwork){
        ArrayList<Badge> toreturn = new ArrayList<>();
        for (Badge badge: allBadges){
            if (badge.network.getUniqueId().equals(bnetwork.getUniqueId())){
                toreturn.add(badge);
            }
        }
        return toreturn;
    }

    public String getUniqueId() {
        return id + "";
    }

    public Network getNetwork() {
        return network;
    }

    public Stand getStand() {
        return stand;
    }

    public double getPrice() {
        return price;
    }

    public String getBadgeType() {
        return badgeType;
    }

    public String getQuantityType() {
        return quantityType;
    }

    public double getReduction() {
        return reduction;
    }

    public int getQuantity() {
        return quantity;
    }
}
