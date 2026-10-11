package io.github.sebminecrafter.noteBlockEasy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class NBECommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final Songs songs;

    public NBECommand(JavaPlugin plugin, Songs songs) {
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
            case "instruments":
                sender.sendMessage(ChatColor.GOLD + "Built-in (vanilla) Instruments:");
                sender.sendMessage(ChatColor.YELLOW + String.join(", ", Instruments.names()));
                return true;
            case "reload":
                new NbsConverter(plugin).convertAll();
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
        // /nbe <list/instruments/reload/stop>
        return args.length > 1 ? List.of() :
               List.of(
                       "list", "instruments", "reload", "stop"
               );
    }
}
