package org.example.adminpanel;

import org.bukkit.plugin.java.JavaPlugin;
import org.example.adminpanel.commands.AdminCommand;
import org.example.adminpanel.listeners.AdminListener;
import org.example.adminpanel.listeners.ModerationListener;
import org.example.adminpanel.managers.GuiManager;
import org.example.adminpanel.managers.ModerationManager;

public class AdminPlugin extends JavaPlugin {

    private static AdminPlugin instance;
    private GuiManager guiManager;
    private ModerationManager moderationManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Configuration
        saveDefaultConfig();
        
        // Managers
        this.guiManager = new GuiManager(this);
        this.moderationManager = new ModerationManager();
        
        // Commandes
        getCommand("admin").setExecutor(new AdminCommand(this));
        
        // Listeners
        getServer().getPluginManager().registerEvents(new AdminListener(this), this);
        getServer().getPluginManager().registerEvents(new ModerationListener(this), this);
        
        getLogger().info("AdminPanel active !");
    }

    @Override
    public void onDisable() {
        getLogger().info("AdminPanel desactive.");
    }

    public static AdminPlugin getInstance() {
        return instance;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public ModerationManager getModerationManager() {
        return moderationManager;
    }
}
