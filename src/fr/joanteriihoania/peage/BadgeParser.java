package fr.joanteriihoania.peage;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class BadgeParser {

    private int id;
    private Network network;
    private Stand stand;
    private boolean fullNetwork = false;
    private String badgeType;
    private String quantityType;
    private double reduction;
    private int quantity;


    public ArrayList<String> getLore() {
        ArrayList<String> toreturn = new ArrayList<>();

        ArrayList<String> toreturnFormatted = new ArrayList<>();
        if(network != null) toreturn.add("&bRéseau : &f" + network.getName());

        if (stand == null) {
            toreturn.add("&bZone : &fRéseau entier");
        } else {
            toreturn.add("&bZone : &f" + stand.getName());
        }

        if (badgeType.equals("freepass")) {
            toreturn.add("&bBadge : &fPassage gratuit");
        } else {
            toreturn.add("&bBadge : &fRéduction de "+(reduction*100)+"%");
        }

        if (quantityType.equals("unlimited")){
            toreturn.add("&bNombre d'utilisation illimité");
        } else {
            toreturn.add("&bUtilisable &f" + quantity + "&b fois");
        }

        for (String line: toreturn){
            toreturnFormatted.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        return toreturnFormatted;
    }

    public BadgeParser fromBadge(Badge badge){
        network = badge.getNetwork();
        stand = badge.getStand();
        fullNetwork = badge.isFullNetwork();
        badgeType = badge.getBadgeType();
        quantityType = badge.getQuantityType();
        reduction = badge.getReduction();
        quantity = badge.getQuantity();
        return this;
    }

    public BadgeParser fromTag(String tag){
        String[] tagArray = tag.split("-");
        if (tagArray.length >= 1 && CheckDoubleInteger.isInteger(tagArray[0])){
            id = Integer.parseInt(tagArray[0]);
        }

        if (tagArray.length >= 6) {
            network = Network.getNetworkFromId(tagArray[2]);
            stand = Stand.getStandFromId(tagArray[3]);
            badgeType = tagArray[4];
            quantityType = tagArray[5];
        }

        if (tagArray.length >= 4 && tagArray[3].equals("all")) fullNetwork = true;
        if (tagArray.length >= 8 && badgeType.equals("reducpass") && CheckDoubleInteger.isDouble(tagArray[6])){
            reduction = Double.parseDouble(tagArray[6]);
            if (quantityType.equals("limited") && CheckDoubleInteger.isInteger(tagArray[7])){
                quantity = Integer.parseInt(tagArray[7]);
            }
        }

        if (tagArray.length >= 7 && badgeType.equals("freepass")){
            if (quantityType.equals("limited") && CheckDoubleInteger.isInteger(tagArray[6])){
                quantity = Integer.parseInt(tagArray[6]);
            }
        }

        return this;
    }

    public String getUniqueId() {
        return id + "";
    }


    public boolean canOpen(Guichet guichet){
        if (quantityType != null && quantityType.equals("limited") && quantity <= 0) return false;
        return isBadgeInNetwork(guichet.getNetwork()) && isBadgeInStand(guichet.getStand());
    }


    public boolean isBadgeInNetwork(Network bnetwork){
        if (network == null) return false;
        return bnetwork.getUniqueId().equals(network.getUniqueId());
    }

    public boolean isBadgeInStand(Stand bstand){
        if (stand == null) return true;
        return (bstand.getUniqueId().equals(stand.getUniqueId()) || stand.getName().equals("all"));
    }

    public boolean useBadge(Player player, Guichet guichet){
        boolean operationAccepted = false;

        if (badgeType != null && badgeType.equals("reducpass")) operationAccepted = guichet.clicked(player, guichet.getStand().getPrice() * (1-reduction));
        if (operationAccepted && quantityType != null && quantityType.equals("limited")) quantity--;

        ItemStack item = player.getInventory().getItemInMainHand();
        updateLore(player, item);

        return operationAccepted;
    }

    public void updateLore(Player player, ItemStack item){
        if (quantity <= 0 && quantityType != null && quantityType.equals("limited")){
            Chat.send(player, "&cVous avez atteint la limite de votre badge");
            player.getInventory().removeItem(item);
        }

        if (item.getItemMeta() != null && item.getType() == Material.PAPER) {
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(getDisplayName());
            itemMeta.setLore(getLore());
            itemMeta.setLocalizedName(getTag());

            item.setItemMeta(itemMeta);
        }
    }


    public String getTag(){
        ArrayList<String> toreturnArray = new ArrayList<>();
        toreturnArray.add(getUniqueId());
        toreturnArray.add("peage");
        if(network != null) toreturnArray.add(network.getUniqueId());
        if(stand != null) toreturnArray.add(stand.getUniqueId());
        if(stand == null) toreturnArray.add("all");
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

    public boolean isFullNetwork() {
        return fullNetwork;
    }

    public void setFullNetwork(boolean fullNetwork) {
        this.fullNetwork = fullNetwork;
    }

    public BadgeParser setId(int id) {
        this.id = id;
        return this;
    }

    public String getDisplayName() {
        if (network == null) return ChatColor.translateAlternateColorCodes('&', "&cBadge invalide");
        return ChatColor.translateAlternateColorCodes('&', "&9&l[&bBadge de &f"+network.getName()+"&9&l]");
    }

    public Network getNetwork() {
        return network;
    }

    public BadgeParser setNetwork(Network network) {
        this.network = network;
        return this;
    }

    public Stand getStand() {
        return stand;
    }

    public BadgeParser setStand(Stand stand) {
        this.stand = stand;
        return this;
    }

    public String getBadgeType() {
        return badgeType;
    }

    public BadgeParser setBadgeType(String badgeType) {
        this.badgeType = badgeType;
        return this;
    }

    public String getQuantityType() {
        return quantityType;
    }

    public BadgeParser setQuantityType(String quantityType) {
        this.quantityType = quantityType;
        return this;
    }

    public double getReduction() {
        return reduction;
    }

    public BadgeParser setReduction(double reduction) {
        this.reduction = reduction;
        return this;
    }

    public int getQuantity() {
        return quantity;
    }

    public BadgeParser setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }
}
