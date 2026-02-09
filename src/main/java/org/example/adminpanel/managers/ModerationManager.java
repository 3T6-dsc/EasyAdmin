package org.example.adminpanel.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ModerationManager {

    private final Set<UUID> frozenPlayers = new HashSet<>();
    private final Set<UUID> mutedPlayers = new HashSet<>();
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private boolean chatLocked = false;

    // --- FREEZE ---
    public boolean isFrozen(UUID uuid) {
        return frozenPlayers.contains(uuid);
    }

    public boolean toggleFreeze(UUID uuid) {
        if (isFrozen(uuid)) {
            frozenPlayers.remove(uuid);
            return false;
        } else {
            frozenPlayers.add(uuid);
            return true;
        }
    }

    // --- MUTE JOUEUR ---
    public boolean isMuted(UUID uuid) {
        return mutedPlayers.contains(uuid);
    }

    public boolean toggleMute(UUID uuid) {
        if (isMuted(uuid)) {
            mutedPlayers.remove(uuid);
            return false;
        } else {
            mutedPlayers.add(uuid);
            return true;
        }
    }

    // --- VANISH ---
    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    public boolean toggleVanish(UUID uuid) {
        if (isVanished(uuid)) {
            vanishedPlayers.remove(uuid);
            return false;
        } else {
            vanishedPlayers.add(uuid);
            return true;
        }
    }

    // --- CHAT GLOBAL ---
    public boolean isChatLocked() {
        return chatLocked;
    }

    public void setChatLocked(boolean locked) {
        this.chatLocked = locked;
    }
}