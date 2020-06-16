package fr.joanteriihoania.peage;

import fr.joanteriihoania.peage.commands.CommandPeage;
import fr.joanteriihoania.peage.tabcompleters.TabCompleterPeage;
import jdk.nashorn.internal.ir.annotations.Ignore;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;


public class Main extends JavaPlugin implements Listener {

    /** Shared public variables */
    Economy economy;
    PluginDescriptionFile pDF = getDescription();
    ArrayList<Network> networks = new ArrayList<>();
    CommandPeage commandPeage = new CommandPeage(networks, this);
    TabCompleterPeage tabCompleterPeage = new TabCompleterPeage();
    LoopEvent loopEvent = new LoopEvent();

    HashMap<Guichet, Integer> guichetsTriggered = new HashMap<>();


    @Override
    public void onEnable() {
        saveDefaultConfig();
        Guichet.setMain(this);
        Signs.setPrefix(getConfig().getString("prefix"));
        Chat.setPrefix(getConfig().getString("prefix"));
        commandPeage.setNetworks(networks);

        Objects.requireNonNull(getCommand("peage")).setExecutor(commandPeage);
        Objects.requireNonNull(getCommand("peage")).setTabCompleter(tabCompleterPeage);

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
        Guichet.closeAll();
        getServer().getScheduler().runTaskLater(this, () -> Bukkit.getPluginManager().callEvent(loopEvent), 20);
    }

    @Override
    public void onDisable() {
        Guichet.disableAll();
        Guichet.openAll();

        try {
            saveData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Console.output(pDF.getName() + " v" + pDF.getVersion() + " disabled");
    }

    @EventHandler
    public void onTick(LoopEvent event){
        int timeOutGuichet = 15*2;
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
            guichetToDelete.close();
            guichetToDelete.refresh();
        }

        for (Guichet guichet: Guichet.getAllGuichets()){
            guichet.onTick(guichetsTriggered);
        }

        getServer().getScheduler().runTaskLater(this, () -> Bukkit.getPluginManager().callEvent(loopEvent), (long) 10);
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
    public void onPistonExtend(BlockPistonExtendEvent event){
        boolean protectGuichetFromExplosion = getConfig().getBoolean("protectGuichetFromPistonInteraction");
        if(!protectGuichetFromExplosion) return;

        for (Guichet guichet: Guichet.getAllGuichets()) {
            for (Block blockDestroyed : event.getBlocks()) {
                List<Block> toCheck = guichet.getProtectedBlocks();
                toCheck.add(guichet.getSign().getBlock());
                for(Block blockProtected: toCheck) {
                    if (blockProtected.getX() == blockDestroyed.getX() && blockProtected.getY() == blockDestroyed.getY() && blockProtected.getZ() == blockDestroyed.getZ()) {
                        if (guichet.getNetwork().getOwner() != null && guichet.getNetwork().getOwner().isOnline()) {
                            Chat.send(Objects.requireNonNull(guichet.getNetwork().getOwner().getPlayer()), "&cTentative de destruction (piston) d'un guichet détectée aux coordonnées &f" + blockDestroyed.getX() + " " + blockDestroyed.getY() + " " + blockDestroyed.getZ());
                        }
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event){
        boolean protectGuichetFromExplosion = getConfig().getBoolean("protectGuichetFromPistonInteraction");
        if(!protectGuichetFromExplosion) return;

        for (Guichet guichet: Guichet.getAllGuichets()) {
            for (Block blockDestroyed : event.getBlocks()) {
                List<Block> toCheck = guichet.getProtectedBlocks();
                toCheck.add(guichet.getSign().getBlock());

                for(Block blockProtected: toCheck) {
                    if (blockProtected.getX() == blockDestroyed.getX() && blockProtected.getY() == blockDestroyed.getY() && blockProtected.getZ() == blockDestroyed.getZ()) {
                        if (guichet.getNetwork().getOwner() != null && guichet.getNetwork().getOwner().isOnline()) {
                            Chat.send(Objects.requireNonNull(guichet.getNetwork().getOwner().getPlayer()), "&cTentative de destruction (piston) d'un guichet détectée aux coordonnées &f" + blockDestroyed.getX() + " " + blockDestroyed.getY() + " " + blockDestroyed.getZ());
                        }
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockExplode(EntityExplodeEvent event){
        boolean protectGuichetFromExplosion = getConfig().getBoolean("protectGuichetFromExplosion");
        if(!protectGuichetFromExplosion) return;

        for (Guichet guichet: Guichet.getAllGuichets()){
            for (Block blockDestroyed : event.blockList()) {
                List<Block> toCheck = guichet.getProtectedBlocks();
                toCheck.add(guichet.getSign().getBlock());

                for(Block blockProtected: toCheck) {
                    if (blockProtected.getX() == blockDestroyed.getX() && blockProtected.getY() == blockDestroyed.getY() && blockProtected.getZ() == blockDestroyed.getZ()) {
                        if (guichet.getNetwork().getOwner() != null && guichet.getNetwork().getOwner().isOnline()) {
                            Chat.send(Objects.requireNonNull(guichet.getNetwork().getOwner().getPlayer()), "&cTentative de destruction (explosion) d'un guichet détectée aux coordonnées &f" + blockDestroyed.getX() + " " + blockDestroyed.getY() + " " + blockDestroyed.getZ());
                        }
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        for(Guichet guichet: Guichet.getAllGuichets()){
            for(Block block: guichet.getProtectedBlocks()){
                if (block != null) {
                    Location eventLocation = event.getBlock().getLocation();
                    Location blockLocation = block.getLocation();
                    if (eventLocation.getBlockX() == blockLocation.getBlockX() && eventLocation.getBlockY() == blockLocation.getBlockY() && eventLocation.getBlockZ() == blockLocation.getBlockZ()) {
                        event.setCancelled(true);
                    }
                }
            }
        }

        BlockState blockState = event.getBlock().getState();
        Player player = event.getPlayer();
        if (blockState instanceof Sign){
            if (Guichet.exists(blockState.getLocation())){
                Guichet guichet = Guichet.getGuichetFromLocation(blockState.getLocation());
                if (guichet != null){
                    if (guichet.getNetwork().isOwner(player)) {
                        guichet.delete();
                        Chat.send(player, "&aGuichet &r" + guichet.getName() + "&a de &r" + guichet.getStand().getName() + "&a par &r" + guichet.getNetwork().getName() + "&a supprimé.");
                    } else {
                        event.setCancelled(true);
                        Chat.send(player, "&cVous n'êtes pas le propriétaire de ce réseau.");
                    }
                }
            }
        }
    }


    @EventHandler
    public void onSignPlaced(BlockPlaceEvent event){
        BlockState blockState = event.getBlock().getState();
        Player player = event.getPlayer();
        if (blockState instanceof Sign){
            if (Guichet.exists(blockState.getLocation())){
                Guichet guichet = Guichet.getGuichetFromLocation(blockState.getLocation());
                if (guichet != null){
                    guichet.setSign((Sign) blockState);
                    guichet.refresh();
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        for (Network network: Network.getAllNetworks()){
            OfflinePlayer owner = network.getOwner();
            if (owner != null && owner.getName() != null && owner.getName().equals(event.getPlayer().getName())){
                for (Stand stand: network.getContent()){
                    boolean warn = false;
                    for(Guichet guichet: stand.getContent()){
                        if (guichet.getSign() != null){
                            if (!(guichet.getSign().getBlock().getState() instanceof Sign)) {
                                Location tempLoc = guichet.getSign().getLocation();
                                Chat.send(event.getPlayer(), "&cWARN: Guichet with no sign at &r" + tempLoc.getBlockX() + " " + tempLoc.getBlockY() + " " + tempLoc.getBlockZ());
                                warn = true;
                            }
                        }
                    }

                    if(warn) {
                        Chat.send(event.getPlayer(),
                                "&fThis may be caused by &cplayer/terrain destruction&f or &cchunk corruption&f. " +
                                        "The guichet will be loaded for now but you should consider check it yourself. " +
                                        "The guichet &ccan be deleted&f for memory preservation in the future."
                        );
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRightClickSign(PlayerInteractEvent event){
        Block block = event.getClickedBlock();
        if (block != null) {
            BlockState blockState = block.getState();
            Player player = event.getPlayer();
            if (blockState instanceof Sign && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (Guichet.exists(blockState.getLocation())) {
                    Guichet guichet = Guichet.getGuichetFromLocation(blockState.getLocation());
                    if (guichet != null && !guichetsTriggered.containsKey(guichet)) {
                        if (guichet.clicked(player)){
                            guichetsTriggered.put(guichet, 0);
                        }
                    }
                }
            }
        }
    }

    public boolean isGuichetTriggered(Guichet guichet){
        return guichetsTriggered.containsKey(guichet);
    }

    public void addGuichetTriggered(Guichet guichet){
        if (!guichetsTriggered.containsKey(guichet)) {
            guichetsTriggered.put(guichet, 0);
        }
    }

    public void removeGuichetTriggered(Guichet guichet){
        guichetsTriggered.remove(guichet, 0);
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
        Network.getAllNetworks().clear();
        Stand.getAllStands().clear();
        Guichet.getAllGuichets().clear();


        File mainFile = new File(dataFolder, "main");
        Scanner mainReader = new Scanner(mainFile);
        while (mainReader.hasNextLine()) {
            String data = mainReader.nextLine();
            if(!data.equals("")) {
                Network network = new Network();
                network.setUniqueId(data.split(":")[0]);
                network.setName(data.split(":")[1]);
                networks.add(network);

                Console.output("INFO:Network loaded : " + network.getName() + " (" + network.getUniqueId() + ")");

                File networksFile = new File(networksFolder, network.getUniqueId());
                Scanner networkReader = new Scanner(networksFile);
                while (networkReader.hasNextLine()) {
                    String networkData = networkReader.nextLine();
                    if (!networkData.equals("")) {
                        if (networkData.startsWith("OWNER:") && networkData.split(":").length > 1){
                            network.setOwner(Bukkit.getPlayer(networkData.split(":")[1]));
                        } else {
                            Stand stand = new Stand(Double.parseDouble(networkData.split(":")[2]));
                            stand.setUniqueId(networkData.split(":")[0]);
                            stand.setName(networkData.split(":")[1]);
                            network.addContent(stand);

                            Console.output("INFO:  Stand loaded : " + stand.getName() + " (" + stand.getUniqueId() + ")");

                            File standsFile = new File(standsFolder, stand.getUniqueId());
                            Scanner standsReader = new Scanner(standsFile);
                            while (standsReader.hasNextLine()) {
                                String standData = standsReader.nextLine();
                                String[] standDataArray = standData.split(":");
                                if (!standData.equals("") && standDataArray.length == 6) {
                                    Guichet guichet = new Guichet();
                                    Location tempLoc = new Location(getServer().getWorld(standDataArray[2]), Integer.parseInt(standDataArray[3]), Integer.parseInt(standDataArray[4]), Integer.parseInt(standDataArray[5]));
                                    Block tempBlock = tempLoc.getBlock();
                                    if (tempBlock.getState() instanceof Sign) {
                                        guichet.setSign((Sign) tempBlock.getState());
                                    } else {
                                        Console.output(new String[]{
                                                "WARN:    Guichet with no sign detected at coordinates " + tempLoc.getBlockX() + " " + tempLoc.getBlockY() + " " + tempLoc.getBlockZ(),
                                                "WARN:    This may be caused by player or terrain destruction or chunk corruption.",
                                                "WARN:    The guichet will be loaded but you should consider check it yourself.",
                                                "WARN:    Next reload, the guichet will be deleted for memory preservation."
                                        });
                                    }

                                    guichet.setUniqueId(standDataArray[0]);
                                    guichet.setName(standDataArray[1]);
                                    guichet.setNetwork(network);
                                    guichet.setStand(stand);
                                    stand.addContent(guichet);

                                    Console.output("INFO:    Guichet loaded : " + guichet.getName() + " (" + guichet.getUniqueId() + ")");
                                }
                            }
                            standsReader.close();
                        }
                    }
                }
                networkReader.close();
            }
        }
        mainReader.close();
        commandPeage.setNetworks(networks);

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
                Console.output("  Stand saved : " + stand.getName() + " (" + stand.getUniqueId() + ")");
                File standFile = new File(standsFolder, stand.getUniqueId());
                standFile.createNewFile();

                String guichetInStand = "";
                for (Guichet guichet: stand.getContent()){
                    Console.output("    Guichet saved : " + guichet.getName() + " (" + guichet.getUniqueId() + ")");
                    guichetInStand += "\n" + guichet.save();
                }
                PrintWriter printWriterStands =  new PrintWriter(standFile);
                printWriterStands.write(guichetInStand);
                printWriterStands.close();

                standInNetwork += "\n" + stand.save();
            }


            OfflinePlayer owner = network.getOwner();
            String ownerString = "";
            if(owner != null) {
                ownerString = "OWNER:" + owner.getName();
            }

            PrintWriter printWriterNetworks =  new PrintWriter(networkFile);
            printWriterNetworks.write(ownerString + standInNetwork);
            printWriterNetworks.close();

            allNetworks += "\n" + network.save();
        }

        PrintWriter printWriterMain =  new PrintWriter(mainFile);
        printWriterMain.write(allNetworks);
        printWriterMain.close();

        Console.output("Data saved");
    }
}
