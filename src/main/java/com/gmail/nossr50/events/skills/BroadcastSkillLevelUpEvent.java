package com.gmail.nossr50.events.skills;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;

public class BroadcastSkillLevelUpEvent extends Event implements Cancellable {

    private static final @NotNull HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final Player player;
    private final Component message;
    private final String skillName;
    private final int level;

    public BroadcastSkillLevelUpEvent(@NotNull Player player, @NotNull Component message, @NotNull String skillName,
            int level) {
        this.player = player;
        this.message = message;
        this.skillName = skillName;
        this.level = level;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Component getMessage() {
        return message;
    }

    public @NotNull String getSkillName() {
        return skillName;
    }

    public int getLevel() {
        return level;
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
