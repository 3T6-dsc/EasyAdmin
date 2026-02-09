package org.example.adminpanel.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.example.adminpanel.AdminPlugin;
import org.example.adminpanel.utils.ChatUtils;

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
        
        // Check Mute personnel
        if (plugin.getModerationManager().isMuted(p.getUniqueId())) {
            e.setCancelled(true);
            p.sendMessage(ChatUtils.format(plugin.getConfig().getString("messages.mute-message")));
            return;
        }

        // Check Chat Locked global
        if (plugin.getModerationManager().isChatLocked()) {
            if (!p.hasPermission("adminpanel.chat.bypass")) {
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
                if (!joiner.hasPermission("adminpanel.vanish")) {
                    joiner.hidePlayer(plugin, online);
                }
            }
        }
    }
}