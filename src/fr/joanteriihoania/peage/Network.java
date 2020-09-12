package fr.joanteriihoania.peage;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import java.util.ArrayList;

public class Network implements Structure {

    private static int autoinc;
    private static int maxLife;
    private static ArrayList<Network> allNetworks = new ArrayList<>();
    private static Main mainInstance;
    private int id;
    private String name;
    private ArrayList<Stand> content;
    private Player owner;
    private int life = 0;
    private ArrayList<String> trustedPlayers = new ArrayList<>();
    private Sign controlPannel;

    public static int getMaxLife() {
        return maxLife;
    }

    public static void setMaxLife(int maxLife) {
        Network.maxLife = maxLife;
    }

    public static void setMainInstance(Main mainInstance) {
        Network.mainInstance = mainInstance;
    }

    public Network(String name, ArrayList<Stand> content, Player owner) {
        while (existsId(autoinc)) autoinc++;
        id = autoinc;
        autoinc++;
        this.name = name;
        this.content = content;
        this.owner = owner;
        allNetworks.add(this);
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public boolean incLife(int amount){
        if (life+amount >= maxLife) return false;
        life += amount;
        return true;
    }

    public boolean incLife(){
        return incLife(1);
    }

    public boolean decLife(){
        if (life <= 0) return false;
        life--;
        return true;
    }

    public Sign getControlPannel() {
        return controlPannel;
    }

    public String getControlPannelCoor() {
        return controlPannel.getWorld().getName() + "#" + controlPannel.getX() + "#" + controlPannel.getY() + "#" + controlPannel.getZ();
    }

    public static boolean exists(Location locNetwork){
        for(Network element: allNetworks){
            if (element.getControlPannel() != null) {
                Location locElement = element.getControlPannel().getLocation();
                if (locElement.getBlockX() == locNetwork.getBlockX() && locElement.getBlockY() == locNetwork.getBlockY() && locElement.getBlockZ() == locNetwork.getBlockZ()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Network getNetworkFromLocation(Location locNetwork){
        for(Network element: allNetworks){
            if (element.getControlPannel() != null) {
                Location locElement = element.getControlPannel().getLocation();
                if (locElement.getBlockX() == locNetwork.getBlockX() && locElement.getBlockY() == locNetwork.getBlockY() && locElement.getBlockZ() == locNetwork.getBlockZ()) {
                    return element;
                }
            }
        }
        return null;
    }

    public ArrayList<Block> getProtectedBlocks(){
        ArrayList<Block> blocks = new ArrayList<>();
        if (controlPannel != null) {
            blocks.add(controlPannel.getBlock());
            Block block = controlPannel.getBlock();
            BlockData data = block.getBlockData();
            if (data instanceof Directional) {
                Directional directional = (Directional) data;
                blocks.add(block.getRelative(directional.getFacing().getOppositeFace()));
            }
        }

        return blocks;
    }

    public boolean isOutOfOrder(){
        if (life <= 0) return true;
        if (controlPannel == null) return true;
        if (!(controlPannel.getBlock().getState() instanceof Sign)) {
            return true;
        }
        return false;
    }

    public double usageModifyer(){
        if (controlPannel == null) return 0;
        Block block = controlPannel.getBlock();
        return (block.getTemperature() + block.getHumidity());
    }

    public void onTick(){
        double random = Math.random() * (100 - 0);
        double percentagePerSecond = 5;
        double percentagePerTick = (percentagePerSecond / mainInstance.getConfig().getInt("refreshPerSecond")) * usageModifyer();

        if (random < percentagePerTick) {
            if (controlPannel != null){
                World world = controlPannel.getBlock().getWorld();
                //world.playSound(controlPannel.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1, 1);
                //world.spawnParticle(Particle.SMOKE_NORMAL, controlPannel.getLocation(), 1);
            }
            decLife();
        }

        refreshControlPannel();

        for (Stand stand: content){
            for (Guichet guichet: stand.getContent()){
                //if (isOutOfOrder() != guichet.isOutOfOrder()) {
                    guichet.setOutOfOrder(isOutOfOrder());
                //}
            }
        }
    }

    public void refreshControlPannel(){
        Sign sign = controlPannel;
        if (sign != null) {
            String integrity = "&fIntégrité : " + life;
            String status = "";
            if (isOutOfOrder()){
                status = "&cHors-service";
            } else {
                status = "&aEn ligne";
            }

            Signs.set(sign, new String[]{
                    "&a" + name,
                    status,
                    integrity
            });
        }
    }

    public void setControlPannel(Sign controlPannel) {
        this.controlPannel = controlPannel;
    }

    public Network(ArrayList<Stand> content, Player owner){
        this(autoinc + "", content, owner);
    }

    public Player getOwner() {
        return owner;
    }

    public ArrayList<String> getTrustedPlayers() {
        return trustedPlayers;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public void setTrustedPlayers(ArrayList<String> trustedPlayers) {
        this.trustedPlayers = trustedPlayers;
    }

    public void addTrustedPlayer(String playername){
        trustedPlayers.add(playername);
    }

    public void removeTrustedPlayer(String playername){
        trustedPlayers.remove(playername);
    }

    public static boolean existsName(String text){
        for (Network network: allNetworks){
            if (network.name.equals(text)){
                return true;
            }
        }
        return false;
    }

    public static boolean existsId(int id){
        for (Network network: allNetworks){
            if (network.id == id){
                return true;
            }
        }
        return false;
    }

    public void refresh(){
        for(Stand stand: content){
            Guichet.refreshAll(stand);
        }
    }

    public static Network getNetworkFromName(String text){
        for (Network network: allNetworks){
            if (network.name.equals(text)){
                return network;
            }
        }
        return null;
    }

    public static Network getNetworkFromId(String text){
        for (Network network: allNetworks){
            if (network.getUniqueId().equals(text)){
                return network;
            }
        }
        return null;
    }

    public static ArrayList<Network> getOwnedBy(Player player){
        ArrayList<Network> toreturn = new ArrayList<>();
        for (Network network: allNetworks){
            if (network.isOwner(player)){
                toreturn.add(network);
            }
        }
        return toreturn;
    }

    public static ArrayList<Network> getAllNetworks() {
        return allNetworks;
    }

    public boolean isOwner(Player player){
        if(player.hasPermission("peage.admin")) return true;
        if (owner == null) return false;
        return player.getName().equals(owner.getName());
    }

    public boolean isTrusted(String playername){
        if (trustedPlayers.isEmpty()) return false;
        return trustedPlayers.contains(playername);
    }

    public Network(){
        this(autoinc + "", new ArrayList<>(), null);
    }

    public boolean standNameExists(String text){
        for (Stand stand: content){
            if (stand.getName().equals(text)){
                return true;
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
        return "" + id;
    }

    public void setUniqueId(String id){
        this.id = Integer.parseInt(id);
    }

    public boolean contains(Stand stand){
        return content.contains(stand);
    }

    public boolean contains(String stand){
        for (Stand element: content){
            if (element.getUniqueId().equals(stand)){
                return true;
            }
        }
        return false;
    }

    public boolean containsName(String stand){
        for (Stand element: content){
            if (element.getName().equals(stand)){
                return true;
            }
        }
        return false;
    }

    public Stand getStandFromName(String stand){
        for (Stand element: content){
            if (element.getName().equals(stand)){
                return element;
            }
        }
        return null;
    }

    public void delete(){
        allNetworks.remove(this);

        for(Stand stand: content){
            stand.delete(false);
        }

        if (controlPannel != null && controlPannel.getBlock().getState() instanceof Sign){
            Signs.set(controlPannel, new String[]{
                    "",
                    "&cRéseau supprimé",
                    ""
            });
        }
    }

    public ArrayList<Stand> getContent() {
        return content;
    }

    public void addContent(Stand stand){
        content.add(stand);
    }

    public void addContent(ArrayList<Stand> stands){
        content.addAll(stands);
    }

    public void setContent(ArrayList<Stand> content) {
        this.content = content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String save(){
        return getUniqueId() + ":" + getName();
    }
}
