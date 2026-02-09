package org.example.adminpanel.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ModerationManager {

    private final Set<UUID> frozenPlayers = new HashSet<>();
    private final Set<UUID> mutedPlayers = new HashSet<>();
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private boolean chatLocked = false;

    // --- SYSTEME DE RAISON (CHAT INPUT) ---
    private final Map<UUID, ReasonRequest> awaitingReason = new HashMap<>();

    public enum ActionType {
        KICK, BAN
    }

    public static class ReasonRequest {
        public final UUID targetUuid;
        public final String targetName; // Au cas où le joueur déco
        public final ActionType type;

        public ReasonRequest(Player target, ActionType type) {
            this.targetUuid = target.getUniqueId();
            this.targetName = target.getName();
            this.type = type;
        }
    }

    public void addReasonRequest(Player admin, Player target, ActionType type) {
        awaitingReason.put(admin.getUniqueId(), new ReasonRequest(target, type));
    }

    public ReasonRequest getReasonRequest(Player admin) {
        return awaitingReason.get(admin.getUniqueId());
    }

    public void removeReasonRequest(Player admin) {
        awaitingReason.remove(admin.getUniqueId());
    }

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