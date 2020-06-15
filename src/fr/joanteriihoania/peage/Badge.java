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
    private double price;
    private String badgeType;
    private String quantityType;
    private double reduction;
    private int quantity;


    public Badge(Network network, Stand stand, double price, String badgeType, String quantityType, double reduction, int quantity) {
        id = autoinc;
        autoinc++;
        this.network = network;
        this.stand = stand;
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

    public static Badge getBadgeFromId(String text){
        for (Badge badge: allBadges){
            if (badge.getUniqueId().equals(text)){
                return badge;
            }
        }
        return null;
    }

    public String getUniqueId() {
        return id + "";
    }


    public String getTag(){
        ArrayList<String> toreturnArray = new ArrayList<>();
        toreturnArray.add(getUniqueId());
        toreturnArray.add("peage");
        toreturnArray.add(network.getUniqueId());
        toreturnArray.add(stand.getUniqueId());
        toreturnArray.add(badgeType);
        toreturnArray.add(quantityType);

        if (badgeType.equals("reducpass")){
            toreturnArray.add(reduction + "");
        }

        if (quantityType.equals("limited")){
            toreturnArray.add(quantity + "");
        }

        toreturnArray.add(System.currentTimeMillis() + "");
        return String.join("-", toreturnArray);
    }
}
