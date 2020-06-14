package fr.joanteriihoania.peage;

import java.util.ArrayList;

public class Network implements Structure {

    private static int autoinc;
    private static ArrayList<Network> allNetworks = new ArrayList<>();
    private int id;
    private String name;
    private ArrayList<Stand> content;

    public Network(String name, ArrayList<Stand> content) {
        id = autoinc;
        autoinc++;
        this.name = name;
        this.content = content;
        allNetworks.add(this);
    }

    public Network(ArrayList<Stand> content){
        this(autoinc + "", content);
    }

    public static boolean existsName(String text){
        for (Network network: allNetworks){
            if (network.name.equals(text)){
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

    public Network(){
        this(autoinc + "", new ArrayList<>());
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
