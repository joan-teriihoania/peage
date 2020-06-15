package fr.joanteriihoania.peage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class InterfaceBuyBadges implements Listener {

    private final Inventory inv;
    private String windowState;
    private int page;

    public InterfaceBuyBadges(String title, int size) {
        windowState = "home";
        page = 0;
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
        inv = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', title));

        // Put the items into the inventory
        initializeItems();
    }

    // You can call this whenever you want to put the items in
    public void initializeItems() {
        if (windowState.equals("home")) {
            ArrayList<Network> networks = Network.getAllNetworks();
            for (int i = 0 ; i < networks.size()+7*page;i++) {
                if (i < networks.size()) {
                    Network network = networks.get(i);
                    ArrayList<String> lore = new ArrayList<>();
                    int nbGuichets = 0;

                    for (Stand stand: network.getContent()){
                        nbGuichets += stand.getContent().size();
                    }

                    lore.add("&aRéseau géré par &f" + network.getOwner().getName());
                    lore.add("&bCe réseau possède &f"+nbGuichets+"&b guichet(s)");
                    lore.add("&bréparti(s) sur &f" + network.getContent().size() + "&b zone(s)");

                    inv.setItem(i+2, createGuiItem(Material.PAPER, network.getName(), lore));
                }
            }
        }
    }

    // Nice little method to create a gui item with a custom name, and description
    protected ItemStack createGuiItem(final Material material, final String name, ArrayList<String> lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        assert meta != null;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        // Set the lore of the item
        ArrayList<String> loreFormatted = new ArrayList<>();
        for (String loreline: lore){
            loreFormatted.add(ChatColor.translateAlternateColorCodes('&', loreline));
        }

        meta.setLore(loreFormatted);

        item.setItemMeta(meta);

        return item;
    }

    // You can open the inventory with this
    public void openInventory(final HumanEntity ent) {
        ent.openInventory(inv);
    }

    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getInventory() != inv) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        final Player p = (Player) e.getWhoClicked();

        // Using slots click is a best option for your inventory click's
        p.sendMessage("You clicked at slot " + e.getRawSlot());
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory() == inv) {
            e.setCancelled(true);
        }
    }

}
