package io.github.sebminecrafter.noteBlockEasy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class NBECommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    private final Songs songs;

    public NBECommand(Plugin plugin, Songs songs) {
        this.plugin = plugin;
        this.songs = songs;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (args.length != 1) return false;

        switch (args[0]) {
            case "list":
                sender.sendMessage(ChatColor.GOLD + "Songs:");
                for (String name : songs.getSongNames()) {
                    sender.sendMessage(ChatColor.YELLOW + name);
                }
                return true;
            case "reload":
                songs.loadAllSongs();
                sender.sendMessage(ChatColor.GOLD + "Reloaded songs!");
                return true;
            case "stop":
                sender.sendMessage(ChatColor.GOLD + "Stopping songs!");
                Bukkit.getScheduler().cancelTasks(plugin);
                return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        // /nbe <list/reload>
        return args.length > 1 ? List.of() :
               List.of(
                       "list", "reload", "stop"
               );
    }
}
