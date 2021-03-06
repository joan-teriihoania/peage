package fr.joanteriihoania.peage;

import fr.joanteriihoania.peage.commands.CommandPeage;
import fr.joanteriihoania.peage.tabcompleters.TabCompleterPeage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

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

    DynmapAPI dynmapAPI;
    MarkerAPI markerAPI;
    Plugin dynmap;
    MarkerSet markerSet;
    MarkerIcon markerIcon;


    @Override
    public void onEnable() {
        loadDynmap();

        saveDefaultConfig();
        Guichet.setMainInstance(this);
        Network.setMainInstance(this);
        EconomyCustom.setMainInstance(this);
        Network.setMaxLife(Integer.parseInt(Objects.requireNonNull(getConfig().getString("maxLife"))));
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
        if(markerSet != null) {
            markerSet.deleteMarkerSet();
            markerSet = null;
        }

        Network.disableAll();
        Guichet.disableAll();
        Guichet.openAll();

        try {
            saveData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Console.output(pDF.getName() + " v" + pDF.getVersion() + " disabled");
    }

    public void loadDynmap(){
        dynmap = getServer().getPluginManager().getPlugin("dynmap");
        if (dynmap != null){
            dynmapAPI = (DynmapAPI)dynmap;
            markerAPI = dynmapAPI.getMarkerAPI();
            markerSet = markerAPI.getMarkerSet("peage.markerset");
            if (markerSet == null){
                markerSet = markerAPI.createMarkerSet("peage.markerset", getConfig().getString("dynmapLayerName"), null, false);
            } else {
                markerSet.setMarkerSetLabel(getConfig().getString("dynmapLayerName"));
            }

            markerIcon = markerAPI.getMarkerIcon("truck");

            /*
                try (InputStream png = getClass().getResourceAsStream("/markerIcon.png")) {
                    markerIcon = markerAPI.createMarkerIcon("peage.markerIcon", "peage_guichet_markericon", png);
                } catch (IOException e) {
                    markerIcon = markerAPI.getMarkerIcon("truck");
                }
             */
        }
    }

    @EventHandler
    public void onTick(LoopEvent event){
        int refreshPerSecond = getConfig().getInt("refreshPerSecond");
        if (refreshPerSecond <= 0) refreshPerSecond = 2;
        int timeOutGuichet = getConfig().getInt("timeOutGuichet")*refreshPerSecond;
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

        for(Network network: Network.getAllNetworks()){
            network.onTick();
        }

        getServer().getScheduler().runTaskLater(this, () -> Bukkit.getPluginManager().callEvent(loopEvent), (long) 20/refreshPerSecond);
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
                            Chat.send(Objects.requireNonNull(guichet.getNetwork().getOwner().getPlayer()), "&cTentative de destruction (piston) détectée aux coordonnées &f" + blockDestroyed.getX() + " " + blockDestroyed.getY() + " " + blockDestroyed.getZ());
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
                            Chat.send(Objects.requireNonNull(guichet.getNetwork().getOwner().getPlayer()), "&cTentative de destruction (piston) détectée aux coordonnées &f" + blockDestroyed.getX() + " " + blockDestroyed.getY() + " " + blockDestroyed.getZ());
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
                            Chat.send(Objects.requireNonNull(guichet.getNetwork().getOwner().getPlayer()), "&cTentative de destruction (explosion) détectée aux coordonnées &f" + blockDestroyed.getX() + " " + blockDestroyed.getY() + " " + blockDestroyed.getZ());
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
        BlockState blockState = event.getBlock().getState();
        Player player = event.getPlayer();
        if (blockState instanceof Sign){
            Guichet guichet = Guichet.getGuichetFromLocation(blockState.getLocation());
            if (guichet != null){
                if (guichet.getNetwork().isOwner(player) || guichet.getNetwork().isTrusted(player.getName())) {
                    guichet.delete(true);
                    Chat.send(player, "&aGuichet &r" + guichet.getName() + "&a de &r" + guichet.getStand().getName() + "&a par &r" + guichet.getNetwork().getName() + "&a supprimé.");
                    return;
                } else {
                    event.setCancelled(true);
                    Chat.send(player, "&cVous n'êtes pas propriétaire ou co-propriétaire de ce réseau.");
                    return;
                }
            }

            Network network = Network.getNetworkFromLocation(blockState.getLocation());
            if (network != null){
                if (network.isOwner(player)){
                    double amountToRefund = network.getLife();
                    int ironBlockToRefund = (int) Math.floor(amountToRefund / 9);
                    int ironIngotToRefund = network.getLife() - ironBlockToRefund * 9;
                    player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, ironIngotToRefund));
                    player.getInventory().addItem(new ItemStack(Material.IRON_BLOCK, ironBlockToRefund));
                    EconomyCustom.deposit(player, getConfig().getDouble("price.create.server") * 0.5);
                    network.setLife(0);
                    network.setControlPannel(null);
                    Chat.send(player, "&aLe serveur du réseau &r" + network.getName() + "&a a été supprimé.");
                    return;
                } else {
                    event.setCancelled(true);
                    network.decLife();
                    network.refreshControlPannel();
                    return;
                }
            }
        }

        for(Guichet guichet: Guichet.getAllGuichets()){
            for(Block block: guichet.getProtectedBlocks()){
                if (block != null) {
                    Location eventLocation = event.getBlock().getLocation();
                    Location blockLocation = block.getLocation();
                    if (eventLocation.getBlockX() == blockLocation.getBlockX() && eventLocation.getBlockY() == blockLocation.getBlockY() && eventLocation.getBlockZ() == blockLocation.getBlockZ()) {
                        event.setCancelled(true);
                        return;
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
        Chat.sendUnsentMessages(event.getPlayer());
        for (Network network: Network.getAllNetworks()){
            if (network.isOutOfOrder()){
                Chat.send(event.getPlayer(), "&cRéseau &f"+network.getName()+"&c hors-service.");
            }

            Player owner = network.getOwner();
            if (owner != null) {
                owner.getName();
                if (owner.getName().equals(event.getPlayer().getName())) {
                    network.setOwner(event.getPlayer());
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
                Guichet guichet = Guichet.getGuichetFromLocation(blockState.getLocation());
                if (guichet != null && !guichetsTriggered.containsKey(guichet)) {
                    if (guichet.clicked(player)){
                        guichetsTriggered.put(guichet, 0);
                    }
                }

                Network network = Network.getNetworkFromLocation(blockState.getLocation());
                if (network != null){
                    if (player.getInventory().getItemInMainHand().getType() == Material.IRON_INGOT){
                        if(network.incLife()){
                            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);
                        }
                    }

                    if (player.getInventory().getItemInMainHand().getType() == Material.IRON_BLOCK){
                        if(network.incLife(9)){
                            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);
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
        if (markerSet != null) {
            markerSet.deleteMarkerSet();
            markerSet = null;
        }

        loadDynmap();

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
                        } else if (networkData.startsWith("LIFE:") && networkData.split(":").length > 1) {
                            network.setLife(Integer.parseInt(networkData.split(":")[1]));
                        } else if (networkData.startsWith("CONTROLPANNEL:") && networkData.split(":").length > 1) {
                            String[] networkCoor = networkData.split(":")[1].split("#");
                            Location tempLoc = new Location(getServer().getWorld(networkCoor[0]), Integer.parseInt(networkCoor[1]), Integer.parseInt(networkCoor[2]), Integer.parseInt(networkCoor[3]));
                            Block tempBlock = tempLoc.getBlock();
                            if (tempBlock.getState() instanceof Sign) {
                                network.setControlPannel((Sign) tempBlock.getState());
                            }
                        } else if (networkData.startsWith("TRUSTED:") && networkData.split(":").length > 1) {
                            for (String trustedPlayername: networkData.split(":")[1].split(",")) {
                                network.addTrustedPlayer(trustedPlayername);
                            }
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

                                    if (markerSet != null){
                                        markerSet.createMarker("guichet-" + guichet.getUniqueId(), "Péage de ["+guichet.getNetwork().getName()+"] - Guichet n°"+guichet.getUniqueId() + " de " + guichet.getStand().getName(), tempLoc.getWorld().getName(), tempLoc.getX(), tempLoc.getY(), tempLoc.getZ(), markerIcon, false);
                                    }

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
            ArrayList<String> trustedPlayers = network.getTrustedPlayers();
            String ownerString = "";
            String trustedPlayersString = "";
            String controlPannelCoor = "";
            if(owner != null) {
                ownerString = "\nOWNER:" + owner.getName();
            }

            if (!trustedPlayers.isEmpty()){
                trustedPlayersString = "\nTRUSTED:" + String.join(",", trustedPlayers);
            }

            if (network.getControlPannel() != null){
                controlPannelCoor = "\nCONTROLPANNEL:" + network.getControlPannelCoor();
            }

            PrintWriter printWriterNetworks =  new PrintWriter(networkFile);
            printWriterNetworks.write("LIFE:" + network.getLife() + controlPannelCoor + ownerString + trustedPlayersString + standInNetwork);
            printWriterNetworks.close();

            allNetworks += "\n" + network.save();
        }

        PrintWriter printWriterMain =  new PrintWriter(mainFile);
        printWriterMain.write(allNetworks);
        printWriterMain.close();

        Console.output("Data saved");
    }
}
