package fr.joanteriihoania.peage;

import org.bukkit.entity.Player;
import java.util.ArrayList;

public class Network implements Structure {

    private static int autoinc;
    private static ArrayList<Network> allNetworks = new ArrayList<>();
    private int id;
    private String name;
    private ArrayList<Stand> content;
    private Player owner;

    public Network(String name, ArrayList<Stand> content, Player owner) {
        while (existsId(autoinc)) autoinc++;
        id = autoinc;
        autoinc++;
        this.name = name;
        this.content = content;
        this.owner = owner;
        allNetworks.add(this);
    }

    public Network(ArrayList<Stand> content, Player owner){
        this(autoinc + "", content, owner);
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
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
        for(Stand stand: content){
            stand.delete();
        }

        allNetworks.remove(this);
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
