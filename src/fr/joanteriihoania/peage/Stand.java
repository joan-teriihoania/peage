package fr.joanteriihoania.peage;

import org.bukkit.block.Sign;

import java.util.ArrayList;

public class Stand implements Structure {

    private static int autoinc;
    private static ArrayList<Stand> allStands = new ArrayList<>();
    private int id;
    private String name;
    private ArrayList<Guichet> content;
    private double price;

    public Stand(String name, ArrayList<Guichet> content, double price) {
        while (existsId(autoinc)) autoinc++;
        id = autoinc;
        autoinc++;
        this.price = price;
        this.name = name;
        this.content = content;
        allStands.add(this);
    }

    public static ArrayList<Stand> getAllStands() {
        return allStands;
    }

    public static Stand getStandFromName(String text){
        for (Stand stand: allStands){
            if (stand.name.equals(text)){
                return stand;
            }
        }
        return null;
    }

    public static Stand getStandFromId(String text){
        for (Stand stand: allStands){
            if (stand.getUniqueId().equals(text)){
                return stand;
            }
        }
        return null;
    }

    public static boolean existsName(String text){
        for (Stand stand: allStands){
            if (stand.name.equals(text)){
                return true;
            }
        }
        return false;
    }

    public static boolean existsId(int id){
        for (Stand stand: allStands){
            if (stand.id == id){
                return true;
            }
        }
        return false;
    }

    public Stand(ArrayList<Guichet> content, double price){
        this(autoinc + "", content, price);
    }

    public Stand(double price){
        this(autoinc + "", new ArrayList<>(), price);
    }

    public Stand(){
        this(autoinc + "", new ArrayList<>(), 0);
    }

    public void delete(){
        for (Network network: Network.getAllNetworks()){
            network.getContent().remove(this);
        }

        for(Guichet guichet: content){
            guichet.delete();
        }

        allStands.remove(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUniqueId() {
        return ""+ id;
    }

    public void setUniqueId(String id){
        this.id = Integer.parseInt(id);
    }

    public boolean contains(Guichet guichet){
        return content.contains(guichet);
    }

    public boolean contains(String guichet){
        for (Guichet element: content){
            if (element.getUniqueId().equals(guichet)){
                return true;
            }
        }
        return false;
    }

    public ArrayList<Guichet> getContent() {
        return content;
    }

    public void addContent(Guichet guichet){
        content.add(guichet);
    }

    public void removeContent(Guichet guichet){
        content.remove(guichet);
    }

    public void setContent(ArrayList<Guichet> content) {
        this.content = content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public String save(){
        return getUniqueId() + ":" + getName() + ":" + getPrice();
    }
}
