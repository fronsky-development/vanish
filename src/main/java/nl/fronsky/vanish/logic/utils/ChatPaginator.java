/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Small utility to paginate long chat outputs.
 * For players it uses click-to-next/prev. For console it prints plain text.
 */
public final class ChatPaginator {
    private ChatPaginator() {
    }

    private static String separatorLike(String header) {
        if (header == null || header.isEmpty()) {
            return ChatColor.DARK_GRAY + "<------------------------------->";
        }
        // Strip color codes, keep the visible width.
        String plain = ChatColor.stripColor(header);
        int len = plain.length();
        if (len <= 0) {
            return ChatColor.DARK_GRAY + "<------------------------------->";
        }
        // Fill with dashes but keep the same start/end characters as the header.
        char first = plain.charAt(0);
        char last = plain.charAt(len - 1);
        if (len == 1) {
            return ChatColor.DARK_GRAY + String.valueOf(first);
        }
        String middle = "-".repeat(Math.max(0, len - 2));
        return ChatColor.DARK_GRAY + String.valueOf(first) + middle + last;
    }

    private static String separatorLikeVisible(String visibleHeader) {
        if (visibleHeader == null || visibleHeader.isEmpty()) {
            return ChatColor.DARK_GRAY + "<------------------------------->";
        }
        int len = visibleHeader.length();
        if (len <= 1) {
            return ChatColor.DARK_GRAY + visibleHeader;
        }
        char first = visibleHeader.charAt(0);
        char last = visibleHeader.charAt(len - 1);
        String middle = "-".repeat(Math.max(0, len - 2));
        if (visibleHeader.contains("(") && visibleHeader.contains(")")) {
            middle = "-".repeat(Math.max(0, len - 3));
        }
        return ChatColor.DARK_GRAY + String.valueOf(first) + middle + last;
    }

    private static String visibleHeader(String headerWithColors) {
        if (headerWithColors == null) return "";
        String plain = ChatColor.stripColor(headerWithColors);
        // IMPORTANT: don't trim, because trimming can change the visible length and cause 1-char mismatch.
        return plain;
    }

    public static void send(CommandSender sender,
                            ChatColor accent,
                            String title,
                            List<String> lines,
                            int page,
                            int perPage,
                            String baseCommand) {
        if (lines == null) {
            lines = Collections.emptyList();
        }
        if (perPage <= 0) {
            perPage = 8;
        }
        int totalPages = Math.max(1, (int) Math.ceil(lines.size() / (double) perPage));
        int safePage = Math.min(Math.max(page, 1), totalPages);

        boolean showPageIndicator = totalPages > 1;

        // Premium-ish style: centered-ish title with a longer dash line.
        String pagePart = showPageIndicator ? (ChatColor.DARK_GRAY + " " + ChatColor.GRAY + "(" + safePage + "/" + totalPages + ")") : "";
        String header = ChatColor.DARK_GRAY + "<--------- " + accent + ChatColor.BOLD + title + ChatColor.RESET + pagePart + ChatColor.DARK_GRAY + " --------->";
        sender.sendMessage(header);

        String sep = separatorLikeVisible(visibleHeader(header));

        int start = (safePage - 1) * perPage;
        int end = Math.min(start + perPage, lines.size());

        for (int i = start; i < end; i++) {
            sender.sendMessage(ColorUtil.colorize(lines.get(i)));
        }

        // Only show navigation footer when there is more than 1 page.
        if (totalPages > 1) {
            sendFooter(sender, sep, safePage, totalPages, baseCommand);
        } else {
            sender.sendMessage(sep);
        }
    }

    private static void sendFooter(CommandSender sender,
                                   String separator,
                                   int page,
                                   int totalPages,
                                   String baseCommand) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(separator);
            return;
        }

        boolean hasPrev = page > 1;
        boolean hasNext = page < totalPages;

        if (!hasPrev && !hasNext) {
            player.sendMessage(separator);
            return;
        }

        TextComponent footer = new TextComponent("");

        if (hasPrev) {
            TextComponent prev = new TextComponent(ChatColor.GRAY + "« Prev");
            String cmd = baseCommand + " " + (page - 1);
            prev.setColor(net.md_5.bungee.api.ChatColor.GRAY);
            prev.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text(ChatColor.WHITE + "Go to page " + (page - 1))));
            prev.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
            footer.addExtra(prev);
        }

        if (hasPrev && hasNext) {
            footer.addExtra(new TextComponent(ChatColor.DARK_GRAY + " | "));
        }

        if (hasNext) {
            TextComponent next = new TextComponent(ChatColor.GRAY + "Next »");
            String cmd = baseCommand + " " + (page + 1);
            next.setColor(net.md_5.bungee.api.ChatColor.GRAY);
            next.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text(ChatColor.WHITE + "Go to page " + (page + 1))));
            next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
            footer.addExtra(next);
        }

        player.spigot().sendMessage(footer);
        player.sendMessage(separator);
    }

    public static List<String> toLines(String... lines) {
        List<String> list = new ArrayList<>();
        if (lines == null) return list;
        Collections.addAll(list, lines);
        return list;
    }
}
