package org.example.adminpanel.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.adminpanel.AdminPlugin;
import org.example.adminpanel.utils.ChatUtils;
import org.jetbrains.annotations.NotNull;

public class AdminCommand implements CommandExecutor {

    private final AdminPlugin plugin;

    public AdminCommand(AdminPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seul un joueur peut executer cette commande.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("easyadmin.use")) {
            player.sendMessage(ChatUtils.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        // Ouvrir le menu principal
        plugin.getGuiManager().openMainMenu(player);
        return true;
    }
}