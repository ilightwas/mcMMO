package com.gmail.nossr50.util.player;

import com.gmail.nossr50.datatypes.LevelUpBroadcastPredicate;
import com.gmail.nossr50.datatypes.PowerLevelUpBroadcastPredicate;
import com.gmail.nossr50.datatypes.interactions.NotificationType;
import com.gmail.nossr50.datatypes.notifications.SensitiveCommandType;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.events.skills.BroadcastPowerLevelUpEvent;
import com.gmail.nossr50.events.skills.BroadcastSkillLevelUpEvent;
import com.gmail.nossr50.events.skills.McMMOPlayerNotificationEvent;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.config.skills.SkillTitleConfig;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.sounds.SoundManager;
import com.gmail.nossr50.util.sounds.SoundType;
import com.gmail.nossr50.util.text.McMMOMessageType;
import com.gmail.nossr50.util.text.StringUtils;
import com.gmail.nossr50.util.text.TextComponentFactory;
import java.time.LocalDate;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public class NotificationManager {

    public static final String HEX_BEIGE_COLOR = "#c2a66e";
    public static final String HEX_LIME_GREEN_COLOR = "#8ec26e";

    /**
     * Sends players notifications from mcMMO Does so by sending out an event so other plugins can
     * cancel it
     *
     * @param player target player
     * @param notificationType notifications defined type
     * @param key the locale key for the notifications defined message
     */
    public static void sendPlayerInformation(Player player, NotificationType notificationType,
            String key) {
        if (UserManager.getPlayer(player) == null || !UserManager.getPlayer(player)
                .useChatNotifications()) {
            return;
        }

        McMMOMessageType destination
                = mcMMO.p.getAdvancedConfig().doesNotificationUseActionBar(notificationType)
                ? McMMOMessageType.ACTION_BAR : McMMOMessageType.SYSTEM;

        Component message = TextComponentFactory.getNotificationTextComponentFromLocale(key);
        McMMOPlayerNotificationEvent customEvent = checkNotificationEvent(player, notificationType,
                destination, message);

        sendNotification(player, customEvent);
    }


    public static boolean doesPlayerUseNotifications(Player player) {
        if (UserManager.getPlayer(player) == null) {
            return false;
        } else {
            return UserManager.getPlayer(player).useChatNotifications();
        }
    }

    /**
     * Sends players notifications from mcMMO This does this by sending out an event so other
     * plugins can cancel it This event in particular is provided with a source player, and players
     * near the source player are sent the information
     *
     * @param targetPlayer the recipient player for this message
     * @param notificationType type of notification
     * @param key Locale Key for the string to use with this event
     * @param values values to be injected into the locale string
     */
    public static void sendNearbyPlayersInformation(Player targetPlayer,
            NotificationType notificationType, String key,
            String... values) {
        sendPlayerInformation(targetPlayer, notificationType, key, values);
    }

    public static void sendPlayerInformationChatOnly(Player player, String key, String... values) {
        if (UserManager.getPlayer(player) == null || !UserManager.getPlayer(player)
                .useChatNotifications()) {
            return;
        }

        String preColoredString = LocaleLoader.getString(key, (Object[]) values);
        player.sendMessage(preColoredString);
    }

    public static void sendPlayerInformationChatOnlyPrefixed(Player player, String key,
            String... values) {
        if (UserManager.getPlayer(player) == null || !UserManager.getPlayer(player)
                .useChatNotifications()) {
            return;
        }

        String preColoredString = LocaleLoader.getString(key, (Object[]) values);
        String prefixFormattedMessage = LocaleLoader.getString("mcMMO.Template.Prefix",
                preColoredString);
        player.sendMessage(prefixFormattedMessage);
    }

    public static void sendPlayerInformation(Player player, NotificationType notificationType,
            String key,
            String... values) {
        if (UserManager.getPlayer(player) == null || !UserManager.getPlayer(player)
                .useChatNotifications()) {
            return;
        }

        McMMOMessageType destination =
                mcMMO.p.getAdvancedConfig().doesNotificationUseActionBar(notificationType)
                        ? McMMOMessageType.ACTION_BAR : McMMOMessageType.SYSTEM;

        Component message = TextComponentFactory.getNotificationMultipleValues(key, values);
        McMMOPlayerNotificationEvent customEvent = checkNotificationEvent(player, notificationType,
                destination, message);

        sendNotification(player, customEvent);
    }

    private static void sendNotification(Player player, McMMOPlayerNotificationEvent customEvent) {
        if (customEvent.isCancelled()) {
            return;
        }

        final Audience audience = mcMMO.getAudiences().player(player);

        Component notificationTextComponent = customEvent.getNotificationTextComponent();
        if (customEvent.getChatMessageType() == McMMOMessageType.ACTION_BAR) {
            audience.sendActionBar(notificationTextComponent);

            // If the message is being sent to the action bar we need to check if a copy is also sent to the chat system
            if (customEvent.isMessageAlsoBeingSentToChat()) {
                //Send copy to chat system
                audience.sendMessage(notificationTextComponent);
            }
        } else {
            audience.sendMessage(notificationTextComponent);
        }
    }

    private static McMMOPlayerNotificationEvent checkNotificationEvent(Player player,
            NotificationType notificationType,
            McMMOMessageType destination,
            Component message) {
        //Init event
        McMMOPlayerNotificationEvent customEvent = new McMMOPlayerNotificationEvent(player,
                notificationType, message, destination,
                mcMMO.p.getAdvancedConfig().doesNotificationSendCopyToChat(notificationType));

        //Call event
        Bukkit.getServer().getPluginManager().callEvent(customEvent);
        return customEvent;
    }

    /**
     * Handles sending level up notifications to a mmoPlayer
     *
     * @param mmoPlayer target mmoPlayer
     * @param skillName skill that leveled up
     * @param newLevel new level of that skill
     */
    public static void sendPlayerLevelUpNotification(McMMOPlayer mmoPlayer,
            PrimarySkillType skillName,
            int levelsGained, int newLevel) {
        if (!mmoPlayer.useChatNotifications()) {
            return;
        }

        McMMOMessageType destination
                = mcMMO.p.getAdvancedConfig()
                .doesNotificationUseActionBar(NotificationType.LEVEL_UP_MESSAGE)
                ? McMMOMessageType.ACTION_BAR : McMMOMessageType.SYSTEM;

        Component levelUpTextComponent = TextComponentFactory.getNotificationLevelUpTextComponent(
                skillName, levelsGained, newLevel);
        McMMOPlayerNotificationEvent customEvent = checkNotificationEvent(
                mmoPlayer.getPlayer(),
                NotificationType.LEVEL_UP_MESSAGE,
                destination,
                levelUpTextComponent);

        sendNotification(mmoPlayer.getPlayer(), customEvent);
    }

    public static void broadcastTitle(Server server, String title, String subtitle, int i1, int i2,
            int i3) {
        for (Player player : server.getOnlinePlayers()) {
            player.sendTitle(title, subtitle, i1, i2, i3);
        }
    }

    public static void sendPlayerUnlockNotification(McMMOPlayer mmoPlayer,
            SubSkillType subSkillType) {
        if (!mmoPlayer.useChatNotifications()) {
            return;
        }

        //CHAT MESSAGE
        mcMMO.getAudiences().player(mmoPlayer.getPlayer()).sendMessage(Identity.nil(),
                TextComponentFactory.getSubSkillUnlockedNotificationComponents(
                        mmoPlayer.getPlayer(), subSkillType));

        //Unlock Sound Effect
        SoundManager.sendCategorizedSound(mmoPlayer.getPlayer(),
                mmoPlayer.getPlayer().getLocation(),
                SoundType.SKILL_UNLOCKED, SoundCategory.MASTER);
    }

    /**
     * Sends a message to all admins with the admin notification formatting from the locale Admins
     * are currently players with either Operator status or Admin Chat permission
     *
     * @param msg message fetched from locale
     */
    private static void sendAdminNotification(String msg) {
        //If its not enabled exit
        if (!mcMMO.p.getGeneralConfig().adminNotifications()) {
            return;
        }

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.isOp() || Permissions.adminChat(player)) {
                player.sendMessage(
                        LocaleLoader.getString("Notifications.Admin.Format.Others", msg));
            }
        }

        //Copy it out to Console too
        mcMMO.p.getLogger().info(LocaleLoader.getString("Notifications.Admin.Format.Others", msg));
    }

    /**
     * Sends a confirmation message to the CommandSender who just executed an admin command
     *
     * @param commandSender target command sender
     * @param msg message fetched from locale
     */
    private static void sendAdminCommandConfirmation(CommandSender commandSender, String msg) {
        commandSender.sendMessage(LocaleLoader.getString("Notifications.Admin.Format.Self", msg));
    }

    /**
     * Convenience method to report info about a command sender using a sensitive command
     *
     * @param commandSender the command user
     * @param sensitiveCommandType type of command issued
     */
    public static void processSensitiveCommandNotification(CommandSender commandSender,
            SensitiveCommandType sensitiveCommandType, String... args) {
        /*
         * Determine the 'identity' of the one who executed the command to pass as a parameters
         */
        String senderName = LocaleLoader.getString("Server.ConsoleName");

        if (commandSender instanceof Player) {
            senderName = ((Player) commandSender).getDisplayName()
                    + ChatColor.RESET + "-" + ((Player) commandSender).getUniqueId();
        }

        //Send the notification
        switch (sensitiveCommandType) {
            case XPRATE_MODIFY:
                sendAdminNotification(
                        LocaleLoader.getString("Notifications.Admin.XPRate.Start.Others",
                                addItemToFirstPositionOfArray(senderName, args)));
                sendAdminCommandConfirmation(
                        commandSender,
                        LocaleLoader.getString("Notifications.Admin.XPRate.Start.Self", args));
                break;
            case XPRATE_END:
                sendAdminNotification(
                        LocaleLoader.getString(
                                "Notifications.Admin.XPRate.End.Others",
                                addItemToFirstPositionOfArray(senderName, args)));
                sendAdminCommandConfirmation(commandSender,
                        LocaleLoader.getString("Notifications.Admin.XPRate.End.Self", args));
                break;
        }
    }

    /**
     * Takes an array and an object, makes a new array with object in the first position of the new
     * array, and the following elements in this new array being a copy of the existing array
     * retaining their order
     *
     * @param itemToAdd the string to put at the beginning of the new array
     * @param existingArray the existing array to be copied to the new array at position [0]+1
     * relative to their original index
     * @return the new array combining itemToAdd at the start and existing array elements following
     * while retaining their order
     */
    public static String[] addItemToFirstPositionOfArray(String itemToAdd,
            String... existingArray) {
        String[] newArray = new String[existingArray.length + 1];
        newArray[0] = itemToAdd;

        System.arraycopy(existingArray, 0, newArray, 1, existingArray.length);

        return newArray;
    }

    public static boolean shouldBroadcast(final int level, final int levelInterval, final UUID uuid, final int extra1, final int extra2) {    
        /*
         * Blocks are the range from 0 to levelInterval - 1
         * e.g: for 100, 0 to 99
         */
        int block = level / levelInterval;
        
        /*
         * At which block to start
         * e.g: level 450, levelInterval 100
         * block = 4; 4 * 100 = start at 400
         */
        int blockStart = block * levelInterval;

        /*
         * offset will be between 0 and levelInterval - 1
         */
        int offset = generateOffset(block, levelInterval, uuid, extra1, extra2);
        // mcMMO.p.getLogger().info(String.format("Block: %d, Offset: %d, BlockStart: %d", block, offset, blockStart));
        
        /* offset will be between [0 - 99], e.g: 68
        * level(450) == 400+68 -> false
        * On next level up the offset will be the same until
        * the broadcast will not happen until the player hits level 468
        * next broadcast will happen only on the next block e.g: 500
        */
        return level == blockStart + offset;
    }

    public static int generateOffset(int block, int levelInterval, UUID uuid, int extra1, int extra2) {
        if (levelInterval < 1) {
            throw new IllegalArgumentException("Interval must be at least 1.");
        }

        long MSB = uuid.getMostSignificantBits();
        long LSB = uuid.getLeastSignificantBits();
        int x = 67;

        /*
         * UUIDs already have a good entropy
         * Use both 32 bytes of the MSB and seed the early bits
         */
        x = x ^ (int) (MSB & 0xFFFFFFFF);
        x = x * 0x1F + (int) ((MSB >>> 32));

        /*
         * Insert the seed values
         * with lesser entropy
         * and mix them with the early
         * 64 bytes from the MSB
         */
        x = x * 0x1F + block;
        x = x * 0x1F ^ extra1;
        x = x * 0x1F + extra2;
        x = ((x << 5) - x) ^ levelInterval;

        /*
         * Use the last 64 bytes of the LSB
         * to wrap the values and increase entropy
         */
        x = x ^ (int) (LSB & 0xFFFFFFFF);
        x = x * 0x1F + (int) (LSB >>> 32);

        /* Grab a value in the range of levelInterval */
        return (Math.abs(x) % levelInterval);
    }


    public static void processLevelUpBroadcasting(@NotNull McMMOPlayer mmoPlayer,
            @NotNull PrimarySkillType primarySkillType, int level) {
        if (level <= 0) {
            return;
        }

        //Check if broadcasting is enabled
        if (mcMMO.p.getGeneralConfig().shouldLevelUpBroadcasts()) {
            //Permission check
            if (!Permissions.levelUpBroadcast(mmoPlayer.getPlayer())) {
                return;
            }

            boolean shouldBroadcast = false;
            int levelInterval = mcMMO.p.getGeneralConfig().getLevelUpBroadcastInterval();
            if (!mcMMO.p.getGeneralConfig().shouldLevelUpBroadcastsUseRandomIntervals()) {
                shouldBroadcast = level % levelInterval == 0;
            } else {
                int seed = mcMMO.p.getGeneralConfig().getLevelUpBroadcastIntervalSeed();
                // mcMMO.p.getLogger().info(String.format("Level: %d, Interval: %d, Seed: %d, Skill: %s, SkillHash: %s", level, levelInterval, seed, primarySkillType.name(), primarySkillType.hashCode()));
                shouldBroadcast = shouldBroadcast(level, levelInterval, mmoPlayer.getPlayer().getUniqueId(), primarySkillType.hashCode(), seed);
            }

            if (shouldBroadcast) {
                String skillName = mcMMO.p.getSkillTools().getLocalizedSkillName(primarySkillType);
                //Grab appropriate audience
                Audience audience = mcMMO.getAudiences()
                        .filter(getLevelUpBroadcastPredicate(mmoPlayer.getPlayer()));
                //TODO: Make prettier
                HoverEvent<Component> levelMilestoneHover = Component.text(
                                mmoPlayer.getPlayer().getName())
                        .append(Component.newline())
                        .append(Component.text(LocalDate.now().format(StringUtils.DAY_MONTH_YEAR)))
                        .append(Component.newline())
                        .append(Component.text(
                                skillName
                                        + " chegou ao nível "+level)).color(TextColor.fromHexString(HEX_BEIGE_COLOR))
                        .asHoverEvent();

                String localeMessage = LocaleLoader.getString(
                        "Broadcasts.LevelUpMilestone", mmoPlayer.getPlayer().getDisplayName(),
                        level,
                        mcMMO.p.getSkillTools().getLocalizedSkillName(primarySkillType));

                String skillTitleName = SkillTitleConfig.getInstance().getSkillTitleName(primarySkillType, level);
                Component message = LegacyComponentSerializer.legacySection().deserialize(localeMessage)
                        .append(Component.newline())
                        .append(LegacyComponentSerializer.legacySection().deserialize(SkillTitleConfig.getSkillTitleMessage(skillTitleName)))
                        .hoverEvent(levelMilestoneHover);

                // TODO: Update system msg API
                mcMMO.p.getFoliaLib().getScheduler().runNextTick(
                        t -> audience.sendMessage(message));
                String skillColor = Misc.toHexColor(Misc
                        .fromBarColor(ExperienceConfig.getInstance().getExperienceBarColor(primarySkillType)));
                Bukkit.getPluginManager().callEvent(new BroadcastSkillLevelUpEvent(mmoPlayer.getPlayer(), message,
                        skillName, level, skillColor, skillTitleName));
            }
        }
    }

    //TODO: Remove the code duplication, am lazy atm
    //TODO: Fix broadcasts being skipped for situations where a player skips over the milestone like with the addlevels command
    public static void processPowerLevelUpBroadcasting(@NotNull McMMOPlayer mmoPlayer,
            int powerLevel) {
        if (powerLevel <= 0) {
            return;
        }

        //Check if broadcasting is enabled
        if (mcMMO.p.getGeneralConfig().shouldPowerLevelUpBroadcasts()) {
            //Permission check
            if (!Permissions.levelUpBroadcast(mmoPlayer.getPlayer())) {
                return;
            }

            boolean shouldBroadcast = false;
            int levelInterval = mcMMO.p.getGeneralConfig().getPowerLevelUpBroadcastInterval();

            if (!mcMMO.p.getGeneralConfig().shouldPowerLevelUpBroadcastsUseRandomIntervals()) {
                shouldBroadcast = powerLevel % levelInterval == 0;
            } else {
                int seed = mcMMO.p.getGeneralConfig().getLevelUpBroadcastIntervalSeed();
                shouldBroadcast = shouldBroadcast(powerLevel, levelInterval, mmoPlayer.getPlayer().getUniqueId(), 9000, seed);
            }

            if (shouldBroadcast) {
                //Grab appropriate audience
                Audience audience = mcMMO.getAudiences()
                        .filter(getPowerLevelUpBroadcastPredicate(mmoPlayer.getPlayer()));
                //TODO: Make prettier
                HoverEvent<Component> levelMilestoneHover = Component.text(
                                mmoPlayer.getPlayer().getName())
                        .append(Component.newline())
                        .append(Component.text(LocalDate.now().format(StringUtils.DAY_MONTH_YEAR)))
                        .append(Component.newline())
                        .append(Component.text("Chegou ao nível de poder "+powerLevel)).color(TextColor.fromHexString(HEX_BEIGE_COLOR))
                        .asHoverEvent();

                String localeMessage = LocaleLoader.getString("Broadcasts.PowerLevelUpMilestone", mmoPlayer.getPlayer().getDisplayName(), powerLevel);
                String powerLevelTitleName = SkillTitleConfig.getInstance()
                        .getPowerLevelTitleName(powerLevel);
                Component message = LegacyComponentSerializer.legacySection().deserialize(localeMessage)
                        .append(Component.newline())
                        .append(LegacyComponentSerializer.legacySection().deserialize(SkillTitleConfig.getPowerLevelTitleMessage(powerLevelTitleName)))
                        .hoverEvent(levelMilestoneHover);

                mcMMO.p.getFoliaLib().getScheduler().runNextTick(t -> audience.sendMessage(message));
                Bukkit.getPluginManager().callEvent(new BroadcastPowerLevelUpEvent(mmoPlayer.getPlayer(), message, powerLevel, powerLevelTitleName));
            }
        }
    }

    //TODO: Could cache
    public static @NotNull LevelUpBroadcastPredicate<CommandSender> getLevelUpBroadcastPredicate(
            @NotNull CommandSender levelUpPlayer) {
        return new LevelUpBroadcastPredicate<>(levelUpPlayer);
    }

    public static @NotNull PowerLevelUpBroadcastPredicate<CommandSender> getPowerLevelUpBroadcastPredicate(
            @NotNull CommandSender levelUpPlayer) {
        return new PowerLevelUpBroadcastPredicate<>(levelUpPlayer);
    }

}
