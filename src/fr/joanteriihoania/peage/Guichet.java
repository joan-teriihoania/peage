package fr.joanteriihoania.peage;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import javax.imageio.plugins.jpeg.JPEGImageReadParam;
import java.net.Inet4Address;
import java.util.*;

public class Guichet implements Structure {

    private static int autoinc;
    private static ArrayList<Guichet> allGuichets = new ArrayList<>();
    private static Main mainInstance;
    private int id;
    private String name;
    private Sign sign;
    private Stand stand;
    private Network network;
    private Block entranceCenter;
    private Block entranceCenter2;
    private Block exitCenter;
    private Block fence1;
    private Block fence2;
    private Block fence3;
    private Block blockBehind;
    private boolean locked = false;
    private boolean outOfOrder = false;
    private ArrayList<Entity> enteredPlayer = new ArrayList<>();
    private ArrayList<Entity> exitedPlayer = new ArrayList<>();

    public Block getEntranceCenter() {
        return entranceCenter;
    }

    public Block getExitCenter() {
        return exitCenter;
    }

    public Guichet(String name, Sign sign) {
        while(existsId(autoinc)) autoinc++;
        id = autoinc;
        autoinc++;
        this.name = name;
        this.sign = sign;
        updateZones();
        allGuichets.add(this);
    }



    public static boolean existsId(int id){
        for (Guichet guichet: allGuichets){
            if (guichet.id == id){
                return true;
            }
        }
        return false;
    }

    public ArrayList<Block> getProtectedBlocks(){
        ArrayList<Block> blocks = new ArrayList<>();
        if (network.getControlPannel() != null) blocks.addAll(network.getProtectedBlocks());
        blocks.add(fence1);
        blocks.add(fence2);
        blocks.add(fence3);
        blocks.add(blockBehind);
        return blocks;
    }

    public Guichet(){
        this(autoinc + "", null);
    }

    public static void setMainInstance(Main main){
        mainInstance = main;
    }

    public void setStand(Stand stand) {
        this.stand = stand;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public boolean isOutOfOrder() {
        return outOfOrder;
    }

    public void setOutOfOrder(boolean outOfOrder) {
        this.outOfOrder = outOfOrder;

        if (outOfOrder) {
            close();
            if (sign == null) return;
            Signs.set(sign, new String[]{
                    "",
                    "&cPéage",
                    "&chors-service"
            });
        } else {
            refresh();
        }
    }

    public Stand getStand() {
        return stand;
    }

    public Network getNetwork() {
        return network;
    }

    public void disable(){
        Sign sign = (Sign) getSign();
        if (sign != null) {
            Signs.set(sign, new String[]{
                    "&cSystem disabled",
                    "&fDO NOT BREAK",
                    "&fTHIS SIGN"
            });
        }
    }

    public static void disableAll(){
        for(Guichet element: allGuichets) {
            element.disable();
        }
    }

    public void refresh(){
        updateZones();

        if (sign != null) {
            String price = "&9&l[&r&bPrix: &f" + stand.getPrice() + "&b€&r&9&l]";
            if(stand.getPrice() == 0) price = "&aGratuit";

            Signs.set(sign, new String[]{
                    "&a" + network.getName(),
                    "&f" + stand.getName(),
                    price
            });
        }
    }


    public void onTick(HashMap<Guichet, Integer> guichetsTriggered){
        if (entranceCenter != null) {
            Location location = entranceCenter.getLocation();
            World world = location.getWorld();
            assert world != null;
            List<Entity> nearbyEntites = (List<Entity>) world.getNearbyEntities(location, 2, 2, 2);
            for (Entity entity : nearbyEntites) {
                if (entity instanceof Player) {
                    Player player = (Player) entity;
                    ItemMeta itemMeta = player.getInventory().getItemInMainHand().getItemMeta();
                    if (itemMeta != null) {
                        BadgeParser badgeParser = new BadgeParser().fromTag(itemMeta.getLocalizedName());
                        if (badgeParser.canOpen(this) && !mainInstance.isGuichetTriggered(this) && !locked && !outOfOrder && badgeParser.useBadge(player, this)) {
                            Chat.send(player, network.getName() + " vous souhaite bonne route !");
                            mainInstance.addGuichetTriggered(this);
                            open();
                            return;
                        }
                    }

                    if (!enteredPlayer.contains(entity)) {
                        PlayerEnterEvent((Player) entity);
                        enteredPlayer.add(entity);
                    }
                }
            }

            ArrayList<Entity> noLongerEnteredPlayer = new ArrayList<>();
            for (Entity entity: enteredPlayer){
                if (!nearbyEntites.contains(entity)){
                    noLongerEnteredPlayer.add(entity);
                }
            }

            for (Entity entity: noLongerEnteredPlayer){
                enteredPlayer.remove(entity);
            }
        }

        Block exitCenter = getExitCenter();
        if (exitCenter != null) {
            Location location = exitCenter.getLocation();
            World world = location.getWorld();
            assert world != null;
            List<Entity> nearbyEntites = (List<Entity>) world.getNearbyEntities(location, 2, 2, 2);
            boolean playerIsInside = false;
            for (Entity entity : nearbyEntites) {
                if(entity instanceof Player) {
                    playerIsInside = true;
                    guichetsTriggered.remove(this);
                    PlayerExitEvent((Player) entity);
                }
            }

            if (playerIsInside){
                lock();
            } else {
                unlock();
            }
        }

        if(stand.getPrice() == 0){
            open();
        }
    }

    public static void onEdit(Stand stand){
        for (Guichet guichet: allGuichets) {
            if (stand.getUniqueId().equals(guichet.stand.getUniqueId())) {
                guichet.onEdit();
            }
        }
    }

    public void onEdit(){
        if (stand.getPrice() == 0){
            open();
        } else {
            close();
        }

        refresh();
    }

    public boolean clicked(Player player){
        return clicked(player, stand.getPrice());
    }

    public boolean clicked(Player player, double price){
        if (price == 0) return false;
        if(locked || outOfOrder) return false;
        double balance = mainInstance.economy.getBalance(player);
        if (balance - price >= 0) {
            Player owner = network.getOwner();
            if (owner != null) {
                OfflinePlayer recipient = Bukkit.getOfflinePlayer(owner.getUniqueId());
                mainInstance.economy.withdrawPlayer(player, price);
                mainInstance.economy.depositPlayer(recipient, price);

                if (!player.getName().equals(owner.getName())) {
                    Chat.send(player, "Vous avez payé &a" + price + "&r€ à &a" + network.getName() + "&r sur &a" + stand.getName() + "&r.");
                    Chat.send(owner.getPlayer(), player.getDisplayName() + " a payé &a" + price + "&r€ à &a" + network.getName() + "&r sur &a" + stand.getName() + "&r.");
                } else {
                    Chat.send(player, "Vous vous êtes payé &a" + price + "&r€ sur &a" + stand.getName() + "&r (&a" + network.getName() + "&r).");
                }

                open();
            } else {
                Chat.send(player, "&cTransaction refusée: &fRéseau sans propriétaire&c.");
                return false;
            }
        } else {
            Chat.send(player, "&cTransaction refusée: &fSolde insuffisant&c.");
            return false;
        }

        return true;
    }

    public static void openAll(){
        for(Guichet guichet: allGuichets){
            if (guichet.locked) return;
            guichet.open();
        }
    }

    public static void closeAll(){
        for(Guichet guichet: allGuichets){
            guichet.close();
        }
    }

    public void open(){
        open(false);
    }

    public void open(boolean force){
        if (sign == null) return;
        if((locked || outOfOrder) && !force) return;
        updateZones();
        fence1.setType(Material.AIR);
        fence2.setType(Material.AIR);
        fence3.setType(Material.AIR);
    }

    public void close(){
        if (sign == null) return;
        if(locked || outOfOrder) return;
        updateZones();
        fence1.setType(Material.WHITE_CONCRETE);
        fence2.setType(Material.RED_CONCRETE);
        fence3.setType(Material.WHITE_CONCRETE);
    }

    public void delete(boolean removeFromStand){
        mainInstance.removeGuichetTriggered(this);
        unlock();
        open(true);
        Signs.set(sign, new String[]{
                "",
                "&cPéage supprimé",
                ""
        });
        this.remove(removeFromStand);
    }


    public void remove(boolean removeFromStand){
        allGuichets.remove(this);
        if(removeFromStand) stand.removeContent(this);
    }

    public void PlayerEnterEvent(Player player){
        if (!mainInstance.isGuichetTriggered(this) && !locked && !outOfOrder) {
            ItemMeta itemMeta = player.getInventory().getItemInMainHand().getItemMeta();
            if(itemMeta != null){
                BadgeParser badgeParser = new BadgeParser().fromTag(itemMeta.getLocalizedName());
                if (badgeParser.canOpen(this) && badgeParser.useBadge(player, this)) {
                    Chat.send(player, network.getName() + " vous souhaite bonne route !");
                    mainInstance.addGuichetTriggered(this);
                    open();
                    return;
                }
            }

            if (stand.getPrice() > 0 && mainInstance.getConfig().getBoolean("displayMessageWhenArriveAtGuichet")) {
                TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&9&l[&r&bPéage&r&9&l]&r &9&l<&bCliquer pour payer &f" + stand.getPrice() + "&b€ et ouvrir le péage&9&l>"));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/peage pay " + getUniqueId()));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("En cliquant ici, vous acceptez que le réseau " + network.getName() + " prélève la somme de " + stand.getPrice() + "€ (valeur non-contractuelle précisée à l'achat sur le guichet utilisé) de votre compte bancaire dans les limites acceptées par votre solde. Un solde insuffisant invalidera cette transaction.").create()));
                Chat.send(player, "&bVous arrivez au guichet &f" + getUniqueId() + "&b de &f" + stand.getName() + "&b de &f" + network.getName());
                player.spigot().sendMessage(message);
            }
        }
    }

    public void PlayerExitEvent(Player player){
        if (stand.getPrice() > 0){
            close();
        }
    }

    public void outOfService(){

    }

    public void lock(){
        locked = true;
        close();
        if(sign == null) return;
        Signs.set(sign, new String[]{
                "",
                "&bPéage",
                "&bverrouillé"
        });
    }

    public void unlock(){
        locked = false;
        refresh();
    }

    public boolean isInEntrance(Player player){
        Location playerLoc = player.getLocation();
        if (playerLoc.distance(entranceCenter.getLocation()) <= 2) return true;
        if (playerLoc.distance(entranceCenter2.getLocation()) <= 2) return true;
        return false;
    }

    public void updateZones(){
        if (sign == null) return;
        Block block = sign.getBlock();
        BlockData data = block.getBlockData();
        if (data instanceof Directional) {
            Directional directional = (Directional)data;
            BlockFace facing = directional.getFacing();
            BlockFace leftFacing = null;
            blockBehind = block.getRelative(directional.getFacing().getOppositeFace());
            entranceCenter = block.getRelative(directional.getFacing());

            if (facing == BlockFace.EAST){
                leftFacing = BlockFace.NORTH;
            }
            if (facing == BlockFace.NORTH){
                leftFacing = BlockFace.WEST;
            }
            if (facing == BlockFace.WEST){
                leftFacing = BlockFace.SOUTH;
            }
            if (facing == BlockFace.SOUTH){
                leftFacing = BlockFace.EAST;
            }

            if (leftFacing != null) {
                entranceCenter2 = entranceCenter.getRelative(leftFacing.getOppositeFace());

                exitCenter = entranceCenter.getRelative(leftFacing);
                exitCenter = exitCenter.getRelative(leftFacing);
                exitCenter = exitCenter.getRelative(leftFacing);
                exitCenter = exitCenter.getRelative(leftFacing);
                exitCenter = exitCenter.getRelative(leftFacing);
                exitCenter = exitCenter.getRelative(leftFacing);

                fence2 = entranceCenter.getRelative(leftFacing);
                fence2 = fence2.getRelative(leftFacing);
                fence1 = fence2.getRelative(directional.getFacing());
                fence3 = fence2.getRelative(directional.getFacing().getOppositeFace());
            }
        }
    }

    public static boolean existsId(String text){
        for(Guichet guichet: allGuichets){
            if (guichet.getUniqueId().equals(text)){
                return true;
            }
        }
        return false;
    }

    public static Guichet getGuichetFromId(String text){
        for(Guichet guichet: allGuichets){
            if (guichet.getUniqueId().equals(text)){
                return guichet;
            }
        }
        return null;
    }

    public static void refreshAll(){
        for(Guichet element: allGuichets) {
            element.refresh();
        }
    }

    public static void refreshAll(Stand stand){
        for(Guichet element: allGuichets) {
            if (element.stand.getName().equals(stand.getName())){
                element.refresh();
            }
        }
    }

    public static ArrayList<Guichet> getAllGuichets() {
        return allGuichets;
    }

    public static Guichet getGuichetFromLocation(Location locGuichet) {
        for(Guichet element: allGuichets){
            if (element.getSign() != null) {
                Location locElement = element.getSign().getLocation();
                if (locElement.getBlockX() == locGuichet.getBlockX() && locElement.getBlockY() == locGuichet.getBlockY() && locElement.getBlockZ() == locGuichet.getBlockZ()) {
                    return element;
                }
            }
        }
        return null;
    }

    public void setSign(Sign sign) {
        this.sign = sign;
        updateZones();
        close();
    }

    public BlockState getSign() {
        return sign;
    }

    public static boolean exists(Location locGuichet){
        for(Guichet element: allGuichets){
            if (element.getSign() != null) {
                Location locElement = element.getSign().getLocation();
                if (locElement.getBlockX() == locGuichet.getBlockX() && locElement.getBlockY() == locGuichet.getBlockY() && locElement.getBlockZ() == locGuichet.getBlockZ()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUniqueId() {
        return ""+id;
    }

    public void setUniqueId(String id){
        this.id = Integer.parseInt(id);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String save(){
        if (sign != null){
            String world = "";
            world = sign.getWorld().getName();
            int x = sign.getLocation().getBlockX();
            int y = sign.getLocation().getBlockY();
            int z = sign.getLocation().getBlockZ();
            return getUniqueId() + ":" + getName() + ":" + world + ":" + x + ":" + y + ":" + z;
        } else {
            Console.output("WARN: Saving string build failed (no sign defined)");
            return "";
        }
    }
}
