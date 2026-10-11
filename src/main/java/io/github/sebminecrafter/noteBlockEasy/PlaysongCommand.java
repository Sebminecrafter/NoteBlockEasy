package io.github.sebminecrafter.noteBlockEasy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlaysongCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    private final Songs songs;

    public PlaysongCommand(Plugin plugin, Songs songs) {
        this.plugin = plugin;
        this.songs = songs;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!List.of(1, 2, 4, 5).contains(args.length)) return false;
        String songName = args[0];
        Location loc;
        if (args.length == 1) {
            if (!(sender instanceof Player p)) {
                return false;
            }
            loc = p.getLocation();
        } else if (args.length == 2) {
            Player p = Bukkit.getPlayerExact(args[1]);
            if (p == null) {
                sender.sendMessage(ChatColor.RED + "That player is not online!");
                return true;
            }
            loc = p.getLocation();
        } else {
            World world;
            if (args.length == 5) {
                world = Bukkit.getWorld(args[4]);
                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "That world does not exist.");
                    return true;
                }
            } else if (sender instanceof Player p) {
                world = p.getWorld();
            } else {
                world = Bukkit.getWorlds().getFirst();
            }

            // Relative coords
            Location origin = sender instanceof Player p ? p.getLocation() : new Location(world, 0, 0, 0);

            try {
                double x = parseCoord(args[1], origin.getX());
                double y = parseCoord(args[2], origin.getY());
                double z = parseCoord(args[3], origin.getZ());
                loc = new Location(world, x, y, z);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid coordinates.");
                return true;
            }
        }

        Optional<Song> song = songs.getSong(songName);
        if (song.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "That song does not exist.");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "Playing " + ChatColor.GOLD + song.get().name());
        new SongPlayer(plugin, song.get()).playSongAt(loc);

        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command,
                                      @NonNull String label, String @NonNull [] args) {
        // /playsong <song name> [player | x y z world]

        if (args.length > 2) {
            boolean coordinateMode = looksLikeCoordinate(args[1]) && Bukkit.getPlayerExact(args[1]) == null;
            if (!coordinateMode) return List.of();
        }

        switch (args.length) {
            case 1: // <song name>
                return filter(songs.getSongNames().stream(), args[0]);

            case 2: { // [player] or [x]
                String input = args[1];
                List<String> out = new ArrayList<>();
                if (!looksLikeCoordinate(input)) {
                    Bukkit.matchPlayer(input).stream().map(Player::getName).forEach(out::add);
                }
                if (input.isEmpty()) out.add("~");
                return out;
            }

            case 3: // [y]
            case 4: // [z]
                return args[args.length - 1].isEmpty() ? List.of("~") : List.of();

            case 5: // [world]
                return filter(Bukkit.getWorlds().stream().map(WorldInfo::getName), args[4]);

            default:
                return List.of();
        }
    }

    private static boolean looksLikeCoordinate(String s) {
        if (s.isEmpty()) return false;
        char c = s.charAt(0);
        return Character.isDigit(c) || c == '-' || c == '~';
    }

    private static List<String> filter(Stream<String> options, String input) {
        String lower = input.toLowerCase();
        return options.filter(s -> s.toLowerCase().startsWith(lower))
                .sorted()
                .collect(Collectors.toList());
    }

    private static double parseCoord(String s, double origin) {
        if (s.startsWith("~")) {
            return origin + (s.length() == 1 ? 0 : Double.parseDouble(s.substring(1)));
        }
        return Double.parseDouble(s);
    }
}
