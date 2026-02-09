package org.example.adminpanel.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;
import org.example.adminpanel.AdminPlugin;
import org.example.adminpanel.managers.GuiManager;
import org.example.adminpanel.managers.ModerationManager;
import org.example.adminpanel.utils.ChatUtils;

public class AdminListener implements Listener {

    private final AdminPlugin plugin;

    public AdminListener(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (!(holder instanceof GuiManager.AdminHolder)) return;

        e.setCancelled(true); 

        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        GuiManager.AdminHolder adminHolder = (GuiManager.AdminHolder) holder;
        
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR || e.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        String prefix = plugin.getConfig().getString("messages.prefix");
        
        // Son de clic générique
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

        // --- MENU PRINCIPAL ---
        if (adminHolder.getType().equals("MAIN")) {
            switch (e.getSlot()) {
                case 10: // Joueurs
                    plugin.getGuiManager().openPlayerList(p, 0);
                    break;
                case 11: // Monde
                    if (checkPerm(p, "easyadmin.world")) {
                        plugin.getGuiManager().openWorldMenu(p);
                    }
                    break;
                case 12: // Chat Menu
                    if (checkPerm(p, "easyadmin.chat.manage")) {
                        plugin.getGuiManager().openChatMenu(p);
                    }
                    break;
                case 13: // Monitoring (Click update)
                    plugin.getGuiManager().openMainMenu(p);
                    break;
                case 14: // Vanish
                    if (checkPerm(p, "easyadmin.vanish")) {
                        boolean isVanished = plugin.getModerationManager().toggleVanish(p.getUniqueId());
                        if (isVanished) {
                            p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.vanish-enabled")));
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                if (!online.hasPermission("easyadmin.vanish")) {
                                    online.hidePlayer(plugin, p);
                                }
                            }
                        } else {
                            p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.vanish-disabled")));
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                online.showPlayer(plugin, p);
                            }
                        }
                        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
                        plugin.getGuiManager().openMainMenu(p); // Refresh icon
                    }
                    break;
                case 15: // Reload
                    plugin.reloadConfig();
                    p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.reload")));
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    p.closeInventory();
                    break;
                case 16: // Crédits
                    giveCreditsBook(p, prefix);
                    p.closeInventory();
                    break;
                case 31: // Fermer
                    p.closeInventory();
                    break;
            }
        }
        // --- MENU CHAT ---
        else if (adminHolder.getType().equals("CHAT")) {
            if (e.getCurrentItem().getType() == Material.ARROW) {
                plugin.getGuiManager().openMainMenu(p);
                return;
            }
            if (!checkPerm(p, "easyadmin.chat.manage")) return;

            switch (e.getCurrentItem().getType()) {
                case PAPER: // Clear Chat
                    for (int i = 0; i < 100; i++) {
                        Bukkit.broadcast(Component.text(""));
                    }
                    Bukkit.broadcast(Component.text(ChatUtils.format(prefix + plugin.getConfig().getString("messages.chat-clear"))));
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    break;
                case IRON_BARS: // Lock/Unlock
                    boolean locked = !plugin.getModerationManager().isChatLocked();
                    plugin.getModerationManager().setChatLocked(locked);
                    if (locked) {
                        Bukkit.broadcast(Component.text(ChatUtils.format(prefix + plugin.getConfig().getString("messages.chat-locked").replace("%player%", p.getName()))));
                    } else {
                        Bukkit.broadcast(Component.text(ChatUtils.format(prefix + plugin.getConfig().getString("messages.chat-unlocked"))));
                    }
                    p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1f, 1f);
                    plugin.getGuiManager().openChatMenu(p); // Refresh
                    break;
            }
        }
        // --- MENU MONDE ---
        else if (adminHolder.getType().equals("WORLD")) {
            if (e.getCurrentItem().getType() == Material.ARROW) {
                plugin.getGuiManager().openMainMenu(p);
                return;
            }
            if (!checkPerm(p, "easyadmin.world")) return;

            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            switch (e.getCurrentItem().getType()) {
                case SUNFLOWER: p.getWorld().setTime(1000); p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.time-day"))); break;
                case RED_BED: p.getWorld().setTime(13000); p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.time-night"))); break;
                case ORANGE_TULIP: p.getWorld().setStorm(false); p.getWorld().setThundering(false); p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.weather-clear"))); break;
                case WATER_BUCKET: p.getWorld().setStorm(true); p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.weather-storm"))); break;
            }
        }
        // --- LISTE JOUEURS (PAGINATION) ---
        else if (adminHolder.getType().equals("PLAYERS")) {
            int currentPage = adminHolder.getPage();
            
            if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                String targetName = e.getCurrentItem().getItemMeta().getDisplayName().substring(2); 
                Player target = Bukkit.getPlayer(targetName);
                if (target != null) {
                    plugin.getGuiManager().openPlayerActions(p, target);
                } else {
                    p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.target-offline")));
                    plugin.getGuiManager().openPlayerList(p, currentPage); 
                }
            } else if (e.getCurrentItem().getType() == Material.ARROW) {
                if (e.getSlot() == 45) { // Précédent
                    plugin.getGuiManager().openPlayerList(p, currentPage - 1);
                } else if (e.getSlot() == 53) { // Suivant
                    plugin.getGuiManager().openPlayerList(p, currentPage + 1);
                }
            } else if (e.getCurrentItem().getType() == Material.BARRIER) { // Retour (slot 49)
                plugin.getGuiManager().openMainMenu(p);
            }
        }
        // --- ACTIONS JOUEURS ---
        else if (adminHolder.getType().equals("ACTIONS")) {
            Player target = adminHolder.getTarget();
            if (target == null || !target.isOnline()) {
                 p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.target-offline")));
                 p.closeInventory();
                 return;
            }

            switch (e.getCurrentItem().getType()) {
                case ENDER_PEARL: // TP
                    if (checkPerm(p, "easyadmin.tp")) {
                        p.teleport(target);
                        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.tp-success").replace("%player%", target.getName())));
                    }
                    break;
                
                case ENDER_EYE: // TP HERE
                    if (checkPerm(p, "easyadmin.tphere")) {
                        target.teleport(p);
                        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.tphere-success").replace("%player%", target.getName())));
                    }
                    break;

                case PACKED_ICE: // Freeze
                    if (checkPerm(p, "easyadmin.freeze")) {
                        boolean frozen = plugin.getModerationManager().toggleFreeze(target.getUniqueId());
                        String msgKey = frozen ? "messages.freeze-enabled" : "messages.freeze-disabled";
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString(msgKey).replace("%player%", target.getName())));
                        p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
                        plugin.getGuiManager().openPlayerActions(p, target);
                    }
                    break;

                case PAPER: // Mute
                    if (checkPerm(p, "easyadmin.mute")) {
                        boolean muted = plugin.getModerationManager().toggleMute(target.getUniqueId());
                        String msgKey = muted ? "messages.mute-enabled" : "messages.mute-disabled";
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString(msgKey).replace("%player%", target.getName())));
                        p.playSound(p.getLocation(), Sound.BLOCK_WOOL_BREAK, 1f, 1f);
                        plugin.getGuiManager().openPlayerActions(p, target);
                    }
                    break;

                case CHEST: // InvSee
                    if (checkPerm(p, "easyadmin.invsee")) {
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.invsee-success").replace("%player%", target.getName())));
                        p.openInventory(target.getInventory());
                    }
                    break;

                case GOLDEN_APPLE: // Heal
                    if (checkPerm(p, "easyadmin.heal")) {
                        target.setHealth(20);
                        target.setFoodLevel(20);
                        for (PotionEffect effect : target.getActivePotionEffects()) {
                            target.removePotionEffect(effect.getType());
                        }
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.heal-success").replace("%player%", target.getName())));
                    }
                    break;
                    
                case REDSTONE_BLOCK: // Kick
                    if (checkPerm(p, "easyadmin.kick")) {
                        initiateReasonInput(p, target, ModerationManager.ActionType.KICK);
                    }
                    break;

                case NETHERITE_SWORD: // Ban
                    if (checkPerm(p, "easyadmin.ban")) {
                        initiateReasonInput(p, target, ModerationManager.ActionType.BAN);
                    }
                    break;
                
                // GAMEMODES
                case IRON_SWORD: setGamemode(p, target, GameMode.SURVIVAL); break;
                case DIAMOND: setGamemode(p, target, GameMode.CREATIVE); break;
                case FEATHER: setGamemode(p, target, GameMode.SPECTATOR); break;
                case LEATHER_BOOTS: setGamemode(p, target, GameMode.ADVENTURE); break;

                case ARROW: // Retour
                    plugin.getGuiManager().openPlayerList(p, 0);
                    break;
            }
        }
    }

    private boolean checkPerm(Player p, String perm) {
        if (!p.hasPermission(perm)) {
            p.sendMessage(ChatUtils.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-permission")));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return false;
        }
        return true;
    }

    private void setGamemode(Player admin, Player target, GameMode gm) {
        if (!checkPerm(admin, "easyadmin.gamemode")) return;
        target.setGameMode(gm);
        String msg = plugin.getConfig().getString("messages.gamemode-change")
                .replace("%player%", target.getName())
                .replace("%gamemode%", gm.name());
        admin.sendMessage(ChatUtils.format(plugin.getConfig().getString("messages.prefix") + msg));
        admin.playSound(admin.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
    }
    
    private void initiateReasonInput(Player admin, Player target, ModerationManager.ActionType type) {
        admin.closeInventory();
        plugin.getModerationManager().addReasonRequest(admin, target, type);
        
        String prompt = plugin.getConfig().getString("messages.prompt-reason")
                .replace("%action%", type.name())
                .replace("%target%", target.getName());
        String cancelInfo = plugin.getConfig().getString("messages.prompt-cancel-info");
        
        admin.sendMessage(ChatUtils.format(plugin.getConfig().getString("messages.prefix") + prompt));
        admin.sendMessage(ChatUtils.format(cancelInfo));
        admin.playSound(admin.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
    }

    private void giveCreditsBook(Player p, String prefix) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta != null) {
            meta.setTitle(ChatUtils.format("&6EasyAdmin v1.2.5"));
            meta.setAuthor("Youtsuho");

            // Page 1
            String page1 = ChatUtils.format(
                    "      &6&lEasyAdmin\n" +
                    "      &8&m----------\n\n" +
                    "&0Version : &71.2.5\n" +
                    "&0Auteur : &bYoutsuho\n" +
                    "&0Support : &dGemini\n\n" +
                    "&8&m-------------------\n\n" +
                    "&0Un plugin de gestion\n" +
                    "&0simple et intuitif\n" +
                    "&0pour votre serveur."
            );

            // Page 2
            String page2 = ChatUtils.format(
                    "&9&lRemerciements\n\n" +
                    "&0Merci d'utiliser\n" +
                    "&0EasyAdmin !\n\n" +
                    "&0N'hésitez pas à\n" +
                    "&0signaler les bugs\n" +
                    "&0ou à proposer des\n" +
                    "&0idées.\n\n" +
                    "      &c&l❤"
            );

            meta.addPage(page1, page2);
            book.setItemMeta(meta);
            p.getInventory().addItem(book);
            p.sendMessage(ChatUtils.format(prefix + "&aVous avez reçu le livre des crédits !"));
        }
    }
}