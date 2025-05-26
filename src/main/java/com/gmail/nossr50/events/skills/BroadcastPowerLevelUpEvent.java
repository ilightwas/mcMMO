package com.gmail.nossr50.events.skills;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;

public class BroadcastPowerLevelUpEvent extends Event implements Cancellable {

    private static final @NotNull HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final Player player;
    private final Component message;
    private final int level;
    private final String powerLevelTitle;

    public BroadcastPowerLevelUpEvent(@NotNull Player player, @NotNull Component message, int level, @NotNull String powerLevelTitle) {
        this.message = message;
        this.level = level;
        this.player = player;
        this.powerLevelTitle = powerLevelTitle;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Component getMessage() {
        return message;
    }

    public int getLevel() {
        return level;
    }

    public @NotNull String getPowerLevelTitle() {
        return powerLevelTitle;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

}
