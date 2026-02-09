package org.example.adminpanel.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.example.adminpanel.AdminPlugin;
import org.example.adminpanel.utils.ChatUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiManager {

    private final AdminPlugin plugin;

    public GuiManager(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    // --- HOLDERS ---
    public static class AdminHolder implements InventoryHolder {
        private final String type;
        private Player target;
        private int page = 0; // Ajout page

        public AdminHolder(String type) { this.type = type; }
        public AdminHolder(String type, Player target) { this.type = type; this.target = target; }
        public AdminHolder(String type, int page) { this.type = type; this.page = page; } // Constructeur pagination
        
        public String getType() { return type; }
        public Player getTarget() { return target; }
        public int getPage() { return page; }
        @Override public @NotNull Inventory getInventory() { return Bukkit.createInventory(null, 9); } 
    }

    // --- MENUS ---

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new AdminHolder("MAIN"), 36, Component.text("Administration"));

        // Ligne 1
        inv.setItem(10, createItem(Material.PLAYER_HEAD, "&eGestion des Joueurs", "&7Liste des joueurs en ligne"));
        
        // Gestion Monde
        if (player.hasPermission("easyadmin.world")) {
            inv.setItem(11, createItem(Material.GRASS_BLOCK, "&2Gestion Monde", "&7Météo et Temps"));
        } else {
            inv.setItem(11, createItem(Material.BARRIER, "&cGestion Monde", "&7Permission requise"));
        }

        // Gestion Chat
        if (player.hasPermission("easyadmin.chat.manage")) {
            inv.setItem(12, createItem(Material.OAK_SIGN, "&6Gestion Chat", "&7Clear & Lock"));
        } else {
             inv.setItem(12, createItem(Material.BARRIER, "&cGestion Chat", "&7Permission requise"));
        }
        
        // Monitoring
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1048576; // Convert to MB
        long freeMemory = runtime.freeMemory() / 1048576;
        long usedMemory = maxMemory - freeMemory;
        
        double[] tps = Bukkit.getTPS();
        String tpsString = String.format("%.2f", tps[0]);

        List<String> monitorLore = new ArrayList<>();
        monitorLore.add("&7TPS (1m): &a" + tpsString);
        monitorLore.add("&7RAM: &e" + usedMemory + "MB &7/ &6" + maxMemory + "MB");
        inv.setItem(13, createMonitoringItem(Material.REDSTONE_BLOCK, "&cMonitoring Serveur", monitorLore));

        // Actions Perso (Vanish)
        if (player.hasPermission("easyadmin.vanish")) {
            boolean isVanished = plugin.getModerationManager().isVanished(player.getUniqueId());
            ItemStack vanishItem = createItem(Material.POTION, isVanished ? "&aVanish (ON)" : "&cVanish (OFF)", "&7Devenir invisible");
            if (isVanished) addGlow(vanishItem);
            inv.setItem(14, vanishItem);
        } else {
             inv.setItem(14, createItem(Material.BARRIER, "&cVanish", "&7Permission requise"));
        }

        inv.setItem(15, createItem(Material.COMMAND_BLOCK, "&cReload Plugin", "&7Recharger la configuration"));
        inv.setItem(16, createItem(Material.BOOK, "&bCrédits", "&7Obtenir le livre des crédits"));
        
        inv.setItem(31, createItem(Material.BARRIER, "&cFermer", "&7Quitter le menu"));

        fillBorders(inv);
        player.openInventory(inv);
    }

    public void openChatMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new AdminHolder("CHAT"), 27, Component.text("Gestion du Chat"));

        boolean isLocked = plugin.getModerationManager().isChatLocked();

        inv.setItem(11, createItem(Material.PAPER, "&eEffacer le Chat", "&7Supprimer les messages pour tous"));
        
        ItemStack lockItem = createItem(Material.IRON_BARS, isLocked ? "&aDéverrouiller Chat" : "&cVerrouiller Chat", "&7État actuel: " + (isLocked ? "&cVerrouillé" : "&aOuvert"));
        if (isLocked) addGlow(lockItem);
        inv.setItem(15, lockItem);

        inv.setItem(22, createItem(Material.ARROW, "&cRetour", "&7Menu principal"));
        fillBorders(inv);
        player.openInventory(inv);
    }

    public void openPlayerList(Player player) {
        openPlayerList(player, 0);
    }

    public void openPlayerList(Player player, int page) {
        Inventory inv = Bukkit.createInventory(new AdminHolder("PLAYERS", page), 54, Component.text("Joueurs (Page " + (page + 1) + ")"));

        List<Player> allPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        // Configuration de la pagination
        int maxSlots = 45; // 5 lignes pour les joueurs
        int startIndex = page * maxSlots;
        int endIndex = Math.min(startIndex + maxSlots, allPlayers.size());

        // Remplissage des joueurs
        if (startIndex < allPlayers.size()) {
            for (int i = startIndex; i < endIndex; i++) {
                Player p = allPlayers.get(i);
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(p);
                meta.setDisplayName(ChatUtils.format("&e" + p.getName()));
                List<String> lore = new ArrayList<>();
                if (plugin.getModerationManager().isVanished(p.getUniqueId())) lore.add(ChatUtils.format("&7[VANISHED]"));
                lore.add(ChatUtils.format("&7Cliquez pour gérer"));
                meta.setLore(lore);
                head.setItemMeta(meta);
                inv.setItem(i - startIndex, head);
            }
        }

        // Navigation
        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, "&ePage Précédente", null));
        }
        if (endIndex < allPlayers.size()) {
            inv.setItem(53, createItem(Material.ARROW, "&ePage Suivante", null));
        }

        ItemStack back = createItem(Material.BARRIER, "&cRetour Menu", null);
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    public void openPlayerActions(Player admin, Player target) {
        Inventory inv = Bukkit.createInventory(new AdminHolder("ACTIONS", target), 45, Component.text("Action: " + target.getName()));

        // Info Joueur
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(target);
        meta.setDisplayName(ChatUtils.format("&e" + target.getName()));
        head.setItemMeta(meta);
        inv.setItem(4, head);

        // Ligne 2 : Actions Modération & Déplacement
        inv.setItem(19, createItem(Material.ENDER_PEARL, "&bTéléportation", "&7Se TP au joueur"));
        inv.setItem(20, createItem(Material.ENDER_EYE, "&5TP Here", "&7TP le joueur sur vous")); 
        
        boolean isFrozen = plugin.getModerationManager().isFrozen(target.getUniqueId());
        ItemStack freezeItem = createItem(Material.PACKED_ICE, isFrozen ? "&bDégeler (Unfreeze)" : "&bGeler (Freeze)", "&7État: " + (isFrozen ? "Gelé" : "Libre"));
        if (isFrozen) addGlow(freezeItem);
        inv.setItem(21, freezeItem);
        
        boolean isMuted = plugin.getModerationManager().isMuted(target.getUniqueId());
        ItemStack muteItem = createItem(Material.PAPER, isMuted ? "&aUnmute" : "&cMute", "&7Empêcher de parler");
        if (isMuted) addGlow(muteItem);
        inv.setItem(22, muteItem);
        
        inv.setItem(23, createItem(Material.CHEST, "&6Inventaire (InvSee)", "&7Voir l'inventaire"));
        inv.setItem(24, createItem(Material.GOLDEN_APPLE, "&dHeal & Feed", "&7Soigner complètement"));

        inv.setItem(25, createItem(Material.REDSTONE_BLOCK, "&cKick", "&7Expulser le joueur (Raison perso)"));
        inv.setItem(26, createItem(Material.NETHERITE_SWORD, "&4Ban", "&7Bannir le joueur (Raison perso)"));

        // Ligne 3 : Gamemodes
        inv.setItem(28, createItem(Material.IRON_SWORD, "&eSurvival", "&7Mode Survie"));
        inv.setItem(29, createItem(Material.DIAMOND, "&bCreative", "&7Mode Créatif"));
        inv.setItem(30, createItem(Material.FEATHER, "&fSpectator", "&7Mode Spectateur"));
        inv.setItem(31, createItem(Material.LEATHER_BOOTS, "&aAdventure", "&7Mode Aventure"));

        // Retour
        inv.setItem(40, createItem(Material.ARROW, "&cRetour", "&7Liste des joueurs"));

        fillBorders(inv);
        admin.openInventory(inv);
    }
    
    public void openWorldMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new AdminHolder("WORLD"), 27, Component.text("Gestion du Monde"));

        inv.setItem(10, createItem(Material.SUNFLOWER, "&eJour", "&7Mettre le jour"));
        inv.setItem(12, createItem(Material.RED_BED, "&9Nuit", "&7Mettre la nuit"));
        inv.setItem(14, createItem(Material.ORANGE_TULIP, "&6Beau temps", "&7Enlever la pluie"));
        inv.setItem(16, createItem(Material.WATER_BUCKET, "&9Pluie", "&7Faire pleuvoir"));

        inv.setItem(22, createItem(Material.ARROW, "&cRetour", "&7Menu principal"));

        fillBorders(inv);
        player.openInventory(inv);
    }

    // --- UTILS ---

    private ItemStack createItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtils.format(name));
            if (lore != null) {
                meta.setLore(Collections.singletonList(ChatUtils.format(lore)));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private void addGlow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
    }
    
    private ItemStack createMonitoringItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtils.format(name));
            List<String> formattedLore = new ArrayList<>();
            for (String s : lore) formattedLore.add(ChatUtils.format(s));
            meta.setLore(formattedLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillBorders(Inventory inv) {
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        int size = inv.getSize();
        for (int i = 0; i < size; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }
    }
}