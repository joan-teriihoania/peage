package fr.joanteriihoania.peage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.UUID;

public class Guichet implements Structure {

    private static int autoinc;
    private static ArrayList<Guichet> allGuichets = new ArrayList<>();
    private static Main mainInstance;
    private int id;
    private String name;
    private Sign sign;
    private Stand stand;
    private Network network;

    public Guichet(String name, Sign sign) {
        id = autoinc;
        autoinc++;
        this.name = name;
        this.sign = sign;
        allGuichets.add(this);
    }

    public Guichet(){
        this(autoinc + "", null);
    }

    public static void setMain(Main main){
        mainInstance = main;
    }

    public void setStand(Stand stand) {
        this.stand = stand;
    }

    public void setNetwork(Network network) {
        this.network = network;
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
                    "&cPlugin disabled",
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
        Sign sign = (Sign) getSign();
        if (sign != null) {
            Signs.set(sign, new String[]{
                    "&a" + network.getName(),
                    "&f" + stand.getName(),
                    "&9&l[&r&bPrix: &r" + stand.getPrice() + "&b€&r&9&l]"
            });
        }
    }

    public void clicked(Player player){
        double balance = mainInstance.economy.getBalance(player);
        if (balance - stand.getPrice() >= 0) {
            Signs.set(sign, new String[]{
                    "&aPaiement accepté",
                    "&aBonne route",
                    ""
            });

            Chat.send(player, "Vous avez payé &a" + stand.getPrice() + "&r€ au stand &a" + stand.getName() + "&r de &a" + network.getName() + "&r.");
            OfflinePlayer recipient = Bukkit.getOfflinePlayer(Bukkit.getPlayer("Nosange").getUniqueId());
            mainInstance.economy.withdrawPlayer(player, stand.getPrice());
            mainInstance.economy.depositPlayer(recipient, stand.getPrice());
            open();
        } else {
            Signs.set(sign, new String[]{
                    "",
                    "&cTransaction",
                    "&crefusée"
            });
            Chat.send(player, "&cTransaction refusée: &fSolde insuffisant&c.");
            mainInstance.getServer().getScheduler().runTaskLater(mainInstance, () -> refresh(), (long) 20);
        }
    }


    public void open(){
        System.out.println(sign.getBlock().getFace(sign.getBlock()));
    }

    public void close(){

    }

    public static void refreshAll(){
        for(Guichet element: allGuichets) {
            element.refresh();
        }
    }

    public static void refreshAll(Stand stand){
        for(Guichet element: allGuichets) {
            if (element.stand.getName() == stand.getName()){
                element.refresh();
            }
        }
    }

    public static ArrayList<Guichet> getAllGuichets() {
        return allGuichets;
    }

    public void remove(){
        allGuichets.remove(this);
        stand.removeContent(this);
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
        int x = 0;
        int y = 0;
        int z = 0;
        String world = "";

        if (sign != null){
            world = sign.getWorld().getName();
            x = sign.getLocation().getBlockX();
            y = sign.getLocation().getBlockY();
            z = sign.getLocation().getBlockZ();
        }

        return getUniqueId() + ":" + getName() + ":" + world + ":" + x + ":" + y + ":" + z;
    }
}
