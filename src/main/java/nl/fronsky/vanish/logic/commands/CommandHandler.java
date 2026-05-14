/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.commands;

import lombok.Getter;
import lombok.NonNull;
import nl.fronsky.vanish.logic.commands.annotations.CommandClass;
import nl.fronsky.vanish.logic.commands.annotations.SubCommandMethod;
import nl.fronsky.vanish.logic.commands.interfaces.ICommandExecutor;
import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.logic.utils.Language;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class CommandHandler implements TabCompleter, CommandExecutor, ICommandExecutor {
    @Getter
    private final String name, permission;
    @Getter
    private final List<String> subcommands;
    private final boolean isValid;

    protected CommandHandler() {
        subcommands = new ArrayList<>();
        isValid = getClass().isAnnotationPresent(CommandClass.class);
        if (!isValid) {
            name = "invalid";
            permission = "invalid";
            return;
        }

        CommandClass commandClass = getClass().getAnnotation(CommandClass.class);
        name = commandClass.name();
        permission = commandClass.permission();

        for (Method method : getClass().getMethods()) {
            if (method.isAnnotationPresent(SubCommandMethod.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();

                if (parameterTypes.length != 3) continue;
                if (!parameterTypes[0].equals(CommandSender.class)) continue;
                if (!parameterTypes[1].equals(String.class)) continue;
                if (!parameterTypes[2].equals(String[].class)) continue;

                subcommands.add(method.getName());
            }
        }
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!isValid) return true;

        Player player = (sender instanceof Player) ? (Player) sender : null;

        if (!subcommands.isEmpty() && args != null && args.length > 0) {
            String subcommand = getSubcommand(args);
            if (!subcommand.isEmpty() && hasPermission(player, permission + "." + subcommand.toLowerCase())) {
                try {
                    Method method = this.getClass().getMethod(subcommand, CommandSender.class, String.class, String[].class);
                    method.invoke(this, sender, label, getSubcommandArgs(args));
                    return true;
                } catch (NoSuchMethodException | IllegalAccessException exception) {
                    Logger.severe("An error occurred while invoking subcommand method: " + subcommand);
                    Logger.debug(exception.getMessage());
                } catch (InvocationTargetException exception) {
                    Logger.exception("Error executing subcommand: " + subcommand, exception.getCause());
                }
            }
        }

        if (!hasPermission(player, permission)) return true;

        try {
            onCommand(sender, label, args);
        } catch (Exception e) {
            Logger.exception("Error executing command: " + name, e);
            if (sender instanceof Player) {
                sender.sendMessage("§cAn error occurred while executing this command. Please check the console.");
            } else {
                Logger.severe("An error occurred while executing command: " + name + ". Check console for details.");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, @NonNull String[] args) {
        if (!isValid) return new ArrayList<>();

        List<String> completions = new ArrayList<>();
        Player player = (sender instanceof Player) ? (Player) sender : null;

        if (args.length == 1) {
            subcommands.stream()
                    .filter(subcommand -> subcommand.startsWith(args[0]) && (player != null && hasPermission(player, permission + "." + subcommand)))
                    .forEach(completions::add);
        }
        return completions;
    }

    /**
     * Checks if a player has the specified permission.
     *
     * @param player     the player whose permissions are being checked
     * @param permission the specific permission to check for
     * @return {@code true} if the player has the specified permission or meets the conditions
     * outlined above; {@code false} otherwise
     */
    protected boolean hasPermission(Player player, String permission) {
        if (player == null) {
            return true;
        }
        if (permission == null || permission.isEmpty()) {
            Logger.severe("Permissions haven't been set. Make sure to initialize them correctly.");
            return false;
        }
        if (player.hasPermission("vanish.*")) {
            return true;
        }
        if (!player.hasPermission(permission)) {
            player.sendMessage(Language.NO_PERMISSION.getMessageWithColor());
            return false;
        }
        return true;
    }

    /**
     * Retrieves a subcommand from the given arguments.
     *
     * @param args the array of arguments from which to extract the subcommand
     * @return the matched subcommand if found; otherwise, an empty string
     */
    private String getSubcommand(String[] args) {
        if (args == null || args.length == 0 || subcommands.isEmpty()) {
            return "";
        }

        for (String subcommand : subcommands) {
            if (subcommand.equalsIgnoreCase(args[0])) {
                return subcommand;
            }
        }
        return "";
    }

    /**
     * Extracts the arguments for the subcommand from the given arguments.
     *
     * @param args the array of arguments from which to extract the subcommand arguments
     * @return a new array containing the subcommand arguments; if there are no arguments,
     * returns an empty array
     */
    private String[] getSubcommandArgs(String[] args) {
        if (args == null || args.length == 0 || subcommands.isEmpty()) {
            return new String[0];
        }

        String[] subcommandArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subcommandArgs, 0, subcommandArgs.length);
        return subcommandArgs;
    }
}
