package org.example.adminpanel.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.example.adminpanel.AdminPlugin;
import org.example.adminpanel.managers.ModerationManager;
import org.example.adminpanel.utils.ChatUtils;

import java.util.UUID;

public class ModerationListener implements Listener {

    private final AdminPlugin plugin;

    public ModerationListener(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (plugin.getModerationManager().isFrozen(e.getPlayer().getUniqueId())) {
            // Empêcher le mouvement uniquement si on change de bloc (permet de bouger la tête)
            if (e.getFrom().getBlockX() != e.getTo().getBlockX() || 
                e.getFrom().getBlockZ() != e.getTo().getBlockZ() || 
                e.getFrom().getBlockY() != e.getTo().getBlockY()) {
                
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatUtils.format(plugin.getConfig().getString("messages.freeze-message")));
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        
        // --- 1. Gestion INPUT RAISON (Kick/Ban) ---
        ModerationManager.ReasonRequest request = plugin.getModerationManager().getReasonRequest(p);
        if (request != null) {
            e.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(e.message());
            
            // Check Cancel
            if (message.equalsIgnoreCase("cancel")) {
                plugin.getModerationManager().removeReasonRequest(p);
                p.sendMessage(ChatUtils.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.action-cancelled")));
                return;
            }
            
            // Execution
            String prefix = plugin.getConfig().getString("messages.prefix");
            
            // Re-vérifier si le joueur est en ligne (pour le kick/ban effectif)
            Player target = Bukkit.getPlayer(request.targetUuid);
            
            if (request.type == ModerationManager.ActionType.KICK) {
                if (target != null && target.isOnline()) {
                    // Exécuter Kick sur le Main Thread car AsyncChat est asynchrone
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        target.kickPlayer(ChatUtils.format(message));
                    });
                    String successMsg = plugin.getConfig().getString("messages.kick-success")
                            .replace("%player%", request.targetName)
                            .replace("%reason%", message);
                    p.sendMessage(ChatUtils.format(prefix + successMsg));
                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                } else {
                    p.sendMessage(ChatUtils.format(prefix + plugin.getConfig().getString("messages.target-offline")));
                }
            } else if (request.type == ModerationManager.ActionType.BAN) {
                // Ban (fonctionne même offline si on a le nom, mais ici on a l'UUID via le request pour plus tard)
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getBanList(BanList.Type.NAME).addBan(request.targetName, message, null, p.getName());
                    if (target != null && target.isOnline()) {
                        target.kickPlayer(ChatUtils.format(message));
                    }
                });
                String successMsg = plugin.getConfig().getString("messages.ban-success")
                            .replace("%player%", request.targetName)
                            .replace("%reason%", message);
                p.sendMessage(ChatUtils.format(prefix + successMsg));
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 0.5f);
            }
            
            plugin.getModerationManager().removeReasonRequest(p);
            return;
        }
        
        // --- 2. Modération Classique ---
        
        // Check Mute personnel
        if (plugin.getModerationManager().isMuted(p.getUniqueId())) {
            e.setCancelled(true);
            p.sendMessage(ChatUtils.format(plugin.getConfig().getString("messages.mute-message")));
            return;
        }

        // Check Chat Locked global
        if (plugin.getModerationManager().isChatLocked()) {
            if (!p.hasPermission("easyadmin.chat.bypass")) {
                e.setCancelled(true);
                p.sendMessage(ChatUtils.format(plugin.getConfig().getString("messages.chat-is-locked")));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player joiner = e.getPlayer();
        
        // Cacher les admins Vanished pour le joueur qui vient de rejoindre
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (plugin.getModerationManager().isVanished(online.getUniqueId())) {
                if (!joiner.hasPermission("easyadmin.vanish")) {
                    joiner.hidePlayer(plugin, online);
                }
            }
        }
    }
}