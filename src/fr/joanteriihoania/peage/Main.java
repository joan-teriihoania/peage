package fr.joanteriihoania.peage;

import fr.joanteriihoania.peage.commands.CommandPeage;
import fr.joanteriihoania.peage.tabcompleters.TabCompleterPeage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class Main extends JavaPlugin implements Listener {

    /** Shared public variables */
    Economy economy;
    PluginDescriptionFile pDF = getDescription();
    ArrayList<Network> networks = new ArrayList<>();
    CommandPeage commandPeage = new CommandPeage(networks);
    TabCompleterPeage tabCompleterPeage = new TabCompleterPeage(networks);
    LoopEvent loopEvent = new LoopEvent();

    HashMap<Guichet, Integer> guichetsTriggered = new HashMap<>();

    @Override
    public void onEnable() {
        Guichet.setMain(this);
        commandPeage.setNetworks(networks);
        tabCompleterPeage.setNetworks(networks);

        getCommand("peage").setExecutor(commandPeage);
        getCommand("peage").setTabCompleter(tabCompleterPeage);

        getServer().getPluginManager().registerEvents(this, this);

        try {
            loadData();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Guichet.refreshAll();

        if(!setupEconomy()){
            Console.output(new String[]{
                    "Error   : The economy setup has failed during loading.",
                    "Details : ",
                    "    This will cause several problems and errors during RunTimeExecution with economy operations",
                    "    and may cause performance issues. The plugin will now be disabled to prevent any crash.",
                    "    Check your installation (especially Vault link) and the economy plugins you are using."
            }, 1);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Console.output(pDF.getName() + " v" + pDF.getVersion() + " enabled");
        getServer().getScheduler().runTaskLater(this, () -> Bukkit.getPluginManager().callEvent(loopEvent), (long) 20);
    }

    @Override
    public void onDisable() {
        Guichet.disableAll();

        try {
            saveData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Console.output(pDF.getName() + " v" + pDF.getVersion() + " disabled");
    }

    @EventHandler
    public void onTick(LoopEvent event){
        int timeOutGuichet = 3;
        ArrayList<Guichet> guichetsToDelete = new ArrayList<>();
        for (Map.Entry<Guichet, Integer> entry: guichetsTriggered.entrySet()){
            if (entry.getValue() > timeOutGuichet){
                guichetsToDelete.add(entry.getKey());
            } else {
                entry.setValue(entry.getValue() + 1);
            }
        }

        for (Guichet guichetToDelete: guichetsToDelete){
            guichetsTriggered.remove(guichetToDelete);
            guichetToDelete.refresh();
        }

        getServer().getScheduler().runTaskLater(this, () -> Bukkit.getPluginManager().callEvent(loopEvent), (long) 20);
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            Console.output("No Vault plugin detected");
            return false;
        }

        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    public Economy getEconomy() {
        return economy;
    }

    @EventHandler
    public void onSignDestroyed(BlockBreakEvent event){
        BlockState blockState = event.getBlock().getState();
        Player player = event.getPlayer();
        if (blockState instanceof Sign){
            if (Guichet.exists(blockState.getLocation())){
                Guichet guichet = Guichet.getGuichetFromLocation(blockState.getLocation());
                if (guichet != null){
                    guichet.remove();
                    Chat.send(player, "&aGuichet &r" + guichet.getName() + "&a de &r" + guichet.getStand().getName() + "&a par &r" + guichet.getNetwork().getName() + "&a supprimé.");
                }
            }
        }
    }


    @EventHandler
    public void onSignDestroyed(BlockPlaceEvent event){
        BlockState blockState = event.getBlock().getState();
        Player player = event.getPlayer();
        if (blockState instanceof Sign){
            if (Guichet.exists(blockState.getLocation())){
                Guichet guichet = Guichet.getGuichetFromLocation(blockState.getLocation());
                if (guichet != null){
                    guichet.refresh();
                }
            }
        }
    }

    @EventHandler
    public void onRightClickSign(PlayerInteractEvent event){
        Block block = event.getClickedBlock();
        assert block != null;
        BlockState blockState = block.getState();
        Player player = event.getPlayer();
        if (blockState instanceof Sign && event.getAction() == Action.RIGHT_CLICK_BLOCK){
            if (Guichet.exists(blockState.getLocation())){
                Guichet guichet = Guichet.getGuichetFromLocation(blockState.getLocation());
                if (guichet != null && !guichetsTriggered.containsKey(guichet)){
                    guichetsTriggered.put(guichet, 0);
                    guichet.clicked(player);
                }
            }
        }
    }

    public void loadData() throws FileNotFoundException {
        Console.output("Loading data...");
        File dataFolder = new File(getDataFolder(), "peageData");
        File networksFolder = new File(dataFolder, "networks");
        File standsFolder = new File(dataFolder, "stands");
        File guichetsFolder = new File(dataFolder, "guichets");

        dataFolder.mkdirs();
        networksFolder.mkdirs();
        standsFolder.mkdirs();
        guichetsFolder.mkdirs();

        networks = new ArrayList<>();


        File mainFile = new File(dataFolder, "main");
        Scanner mainReader = new Scanner(mainFile);
        while (mainReader.hasNextLine()) {
            String data = mainReader.nextLine();
            if(!data.equals("")) {
                Network network = new Network();
                network.setUniqueId(data.split(":")[0]);
                network.setName(data.split(":")[1]);
                networks.add(network);

                Console.output("Network loaded : " + network.getName() + " (" + network.getUniqueId() + ")");

                File networksFile = new File(networksFolder, network.getUniqueId());
                Scanner networkReader = new Scanner(networksFile);
                while (networkReader.hasNextLine()) {
                    String networkData = networkReader.nextLine();
                    if (!networkData.equals("")) {
                        Stand stand = new Stand(Double.parseDouble(networkData.split(":")[2]));
                        stand.setUniqueId(networkData.split(":")[0]);
                        stand.setName(networkData.split(":")[1]);
                        network.addContent(stand);

                        Console.output("  - Stand loaded : " + stand.getName() + " (" + stand.getUniqueId() + ")");

                        File standsFile = new File(standsFolder, stand.getUniqueId());
                        Scanner standsReader = new Scanner(standsFile);
                        while (standsReader.hasNextLine()) {
                            String standData = standsReader.nextLine();
                            String standDataArray[] = standData.split(":");
                            if(!standData.equals("") && standDataArray.length == 6) {
                                Guichet guichet = new Guichet();
                                Location tempLoc = new Location(getServer().getWorld(standDataArray[2]), Integer.parseInt(standDataArray[3]), Integer.parseInt(standDataArray[4]), Integer.parseInt(standDataArray[5]));
                                Block tempBlock = tempLoc.getBlock();
                                if (tempBlock.getState() instanceof Sign){
                                    guichet.setSign((Sign) tempBlock.getState());
                                }

                                guichet.setUniqueId(standDataArray[0]);
                                guichet.setName(standDataArray[1]);
                                guichet.setNetwork(network);
                                guichet.setStand(stand);
                                stand.addContent(guichet);

                                Console.output("    > Guichet loaded : " + guichet.getName() + " (" + guichet.getUniqueId() + ")");
                            }
                        }
                        standsReader.close();
                    }
                }
                networkReader.close();
            }
        }
        mainReader.close();
        commandPeage.setNetworks(networks);
        tabCompleterPeage.setNetworks(networks);

        Console.output("Data loaded");
    }


    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public void saveData() throws IOException {
        Console.output("Saving data from "+networks.size()+" networks...");

        deleteDirectory(new File(getDataFolder(), "peageData"));
        File dataFolder = new File(getDataFolder(), "peageData");
        File networksFolder = new File(dataFolder, "networks");
        File standsFolder = new File(dataFolder, "stands");
        File guichetsFolder = new File(dataFolder, "guichets");

        dataFolder.mkdirs();
        networksFolder.mkdirs();
        standsFolder.mkdirs();
        guichetsFolder.mkdirs();


        File mainFile = new File(dataFolder,"main");
        mainFile.createNewFile();

        String allNetworks = "";

        for (Network network: networks) {
            Console.output("Network saved : " + network.getName() + " (" + network.getUniqueId() + ")");
            File networkFile = new File(networksFolder, network.getUniqueId());
            networkFile.createNewFile();

            String standInNetwork = "";
            for (Stand stand: network.getContent()){
                Console.output("  - Stand saved : " + stand.getName() + " (" + stand.getUniqueId() + ")");
                File standFile = new File(standsFolder, stand.getUniqueId());
                standFile.createNewFile();

                String guichetInStand = "";
                for (Guichet guichet: stand.getContent()){
                    Console.output("    > Guichet saved : " + guichet.getName() + " (" + guichet.getUniqueId() + ")");
                    guichetInStand += "\n" + guichet.save();
                }
                PrintWriter printWriterStands =  new PrintWriter(standFile);
                printWriterStands.write(guichetInStand);
                printWriterStands.close();

                standInNetwork += "\n" + stand.save();
            }

            PrintWriter printWriterNetworks =  new PrintWriter(networkFile);
            printWriterNetworks.write(standInNetwork);
            printWriterNetworks.close();

            allNetworks += "\n" + network.save();
        }

        PrintWriter printWriterMain =  new PrintWriter(mainFile);
        printWriterMain.write(allNetworks);
        printWriterMain.close();

        Console.output("Data saved");
    }
}
