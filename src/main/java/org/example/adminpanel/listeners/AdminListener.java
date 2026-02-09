package org.example.adminpanel.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
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

        // --- MENU PRINCIPAL ---
        if (adminHolder.getType().equals("MAIN")) {
            switch (e.getSlot()) {
                case 10: // Joueurs
                    plugin.getGuiManager().openPlayerList(p);
                    break;
                case 11: // Monde
                    if (checkPerm(p, "adminpanel.world")) {
                        plugin.getGuiManager().openWorldMenu(p);
                    }
                    break;
                case 12: // Chat Menu
                    if (checkPerm(p, "adminpanel.chat.manage")) {
                        plugin.getGuiManager().openChatMenu(p);
                    }
                    break;
                case 13: // Monitoring (Click update)
                    plugin.getGuiManager().openMainMenu(p);
                    break;
                case 14: // Vanish
                    if (checkPerm(p, "adminpanel.vanish")) {
                        boolean isVanished = plugin.getModerationManager().toggleVanish(p.getUniqueId());
                        if (isVanished) {
                            p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.vanish-enabled")));
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                if (!online.hasPermission("adminpanel.vanish")) {
                                    online.hidePlayer(plugin, p);
                                }
                            }
                        } else {
                            p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.vanish-disabled")));
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                online.showPlayer(plugin, p);
                            }
                        }
                        plugin.getGuiManager().openMainMenu(p); // Refresh icon
                    }
                    break;
                case 15: // Reload
                    plugin.reloadConfig();
                    p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.reload")));
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
            if (!checkPerm(p, "adminpanel.chat.manage")) return;

            switch (e.getCurrentItem().getType()) {
                case PAPER: // Clear Chat
                    for (int i = 0; i < 100; i++) {
                        Bukkit.broadcast(Component.text(""));
                    }
                    Bukkit.broadcast(Component.text(ChatUtils.format(prefix + plugin.getConfig().getString("messages.chat-clear"))));
                    break;
                case IRON_BARS: // Lock/Unlock
                    boolean locked = !plugin.getModerationManager().isChatLocked();
                    plugin.getModerationManager().setChatLocked(locked);
                    if (locked) {
                        Bukkit.broadcast(Component.text(ChatUtils.format(prefix + plugin.getConfig().getString("messages.chat-locked").replace("%player%", p.getName()))));
                    } else {
                        Bukkit.broadcast(Component.text(ChatUtils.format(prefix + plugin.getConfig().getString("messages.chat-unlocked"))));
                    }
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
            if (!checkPerm(p, "adminpanel.world")) return;

            switch (e.getCurrentItem().getType()) {
                case SUNFLOWER: p.getWorld().setTime(1000); p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.time-day"))); break;
                case RED_BED: p.getWorld().setTime(13000); p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.time-night"))); break;
                case ORANGE_TULIP: p.getWorld().setStorm(false); p.getWorld().setThundering(false); p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.weather-clear"))); break;
                case WATER_BUCKET: p.getWorld().setStorm(true); p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.weather-storm"))); break;
            }
        }
        // --- LISTE JOUEURS ---
        else if (adminHolder.getType().equals("PLAYERS")) {
            if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                String targetName = e.getCurrentItem().getItemMeta().getDisplayName().substring(2); 
                Player target = Bukkit.getPlayer(targetName);
                if (target != null) {
                    plugin.getGuiManager().openPlayerActions(p, target);
                } else {
                    p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.target-offline")));
                    plugin.getGuiManager().openPlayerList(p); 
                }
            } else if (e.getCurrentItem().getType() == Material.ARROW) {
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
                    if (checkPerm(p, "adminpanel.tp")) {
                        p.teleport(target);
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.tp-success").replace("%player%", target.getName())));
                    }
                    break;
                
                case ENDER_EYE: // TP HERE
                    if (checkPerm(p, "adminpanel.tphere")) {
                        target.teleport(p);
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.tphere-success").replace("%player%", target.getName())));
                    }
                    break;

                case PACKED_ICE: // Freeze
                    if (checkPerm(p, "adminpanel.freeze")) {
                        boolean frozen = plugin.getModerationManager().toggleFreeze(target.getUniqueId());
                        String msgKey = frozen ? "messages.freeze-enabled" : "messages.freeze-disabled";
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString(msgKey).replace("%player%", target.getName())));
                        plugin.getGuiManager().openPlayerActions(p, target);
                    }
                    break;

                case PAPER: // Mute
                    if (checkPerm(p, "adminpanel.mute")) {
                        boolean muted = plugin.getModerationManager().toggleMute(target.getUniqueId());
                        String msgKey = muted ? "messages.mute-enabled" : "messages.mute-disabled";
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString(msgKey).replace("%player%", target.getName())));
                        plugin.getGuiManager().openPlayerActions(p, target);
                    }
                    break;

                case CHEST: // InvSee
                    if (checkPerm(p, "adminpanel.invsee")) {
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.invsee-success").replace("%player%", target.getName())));
                        p.openInventory(target.getInventory());
                    }
                    break;

                case GOLDEN_APPLE: // Heal
                    if (checkPerm(p, "adminpanel.heal")) {
                        target.setHealth(20);
                        target.setFoodLevel(20);
                        for (PotionEffect effect : target.getActivePotionEffects()) {
                            target.removePotionEffect(effect.getType());
                        }
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.heal-success").replace("%player%", target.getName())));
                    }
                    break;
                    
                case REDSTONE_BLOCK: // Kick
                    if (checkPerm(p, "adminpanel.kick")) {
                        target.kickPlayer(ChatUtils.format(plugin.getConfig().getString("reasons.kick")));
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.kick-success").replace("%player%", target.getName())));
                        plugin.getGuiManager().openPlayerList(p);
                    }
                    break;

                case NETHERITE_SWORD: // Ban
                    if (checkPerm(p, "adminpanel.ban")) {
                        Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), plugin.getConfig().getString("reasons.ban"), null, p.getName());
                        target.kickPlayer(ChatUtils.format(plugin.getConfig().getString("reasons.ban")));
                        p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.ban-success").replace("%player%", target.getName())));
                        plugin.getGuiManager().openPlayerList(p);
                    }
                    break;
                
                // GAMEMODES
                case IRON_SWORD: setGamemode(p, target, GameMode.SURVIVAL); break;
                case DIAMOND: setGamemode(p, target, GameMode.CREATIVE); break;
                case FEATHER: setGamemode(p, target, GameMode.SPECTATOR); break;
                case LEATHER_BOOTS: setGamemode(p, target, GameMode.ADVENTURE); break;

                case ARROW: // Retour
                    plugin.getGuiManager().openPlayerList(p);
                    break;
            }
        }
    }

    private boolean checkPerm(Player p, String perm) {
        if (!p.hasPermission(perm)) {
            p.sendMessage(ChatUtils.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-permission")));
            return false;
        }
        return true;
    }

    private void setGamemode(Player admin, Player target, GameMode gm) {
        if (!checkPerm(admin, "adminpanel.gamemode")) return;
        target.setGameMode(gm);
        String msg = plugin.getConfig().getString("messages.gamemode-change")
                .replace("%player%", target.getName())
                .replace("%gamemode%", gm.name());
        admin.sendMessage(ChatUtils.format(plugin.getConfig().getString("messages.prefix") + msg));
    }

    private void giveCreditsBook(Player p, String prefix) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta != null) {
            meta.setTitle(ChatUtils.format("&9Crédits"));
            meta.setAuthor("Youtsuho");
            meta.addPage("Devellopeur : Youtsuho");
            book.setItemMeta(meta);
            p.getInventory().addItem(book);
            p.sendMessage(ChatUtils.format(prefix + "&aVous avez reçu le livre des crédits !"));
        }
    }
}