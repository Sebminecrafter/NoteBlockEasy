package io.github.sebminecrafter.noteBlockEasy;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SongPlayer {
    private static final float VOLUME = 1f;

    /**
     * A note and the sound key of the instrument it is played with.
     */
    private record PlayedNote(String sound, int note) {
    }

    /**
     * Notes that sound together, followed by a wait (in steps) until the next frame.
     */
    private record Frame(List<PlayedNote> notes, int delay) {
    }

    private final Plugin plugin;
    private final Logger logger;
    private final Song song;
    private final List<Frame> frames;
    private final long ticksPerStep;

    public SongPlayer(Plugin plugin, Song song) {
        this(plugin, song, 2); // 2 ticks = 1 redstone tick, normal not block speed
    }

    public SongPlayer(Plugin plugin, Song song, long ticksPerStep) {
        if (ticksPerStep < 1) {
            throw new IllegalArgumentException("ticksPerStep must be >= 1");
        }
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.song = song;
        this.ticksPerStep = ticksPerStep;
        this.frames = compile(song);
    }

    /**
     * Starts playing the song, cancels if world is {@code null}
     */
    public BukkitTask playSongAt(Location location) {
        World world = location.getWorld();
        if (world == null) {
            logger.warning("Cancelled playing '" + song.name() + "': world is null.");
            return null;
        }

        final Location target = location.clone(); // protect against the caller mutating it
        logger.info(() -> "Playing '%s' at %.1f %.1f %.1f in world %s".formatted(
                song.name(), target.getX(), target.getY(), target.getZ(), world.getName()));

        return new BukkitRunnable() {
            private int index = 0;
            private int remaining = 0; // steps left to wait before the next frame

            @Override
            public void run() {
                if (remaining > 0) {
                    remaining--;
                    return;
                }
                if (index >= frames.size()) {
                    cancel();
                    return;
                }
                Frame frame = frames.get(index++);
                for (PlayedNote played : frame.notes()) {
                    world.playSound(target, played.sound(), VOLUME, pitch(played.note()));
                }
                remaining = Math.max(0, frame.delay() - 1);
            }
        }.runTaskTimer(plugin, 0L, ticksPerStep);
    }

    /**
     * Each frame represents one step of the song
     */
    private static List<Frame> compile(Song song) {
        List<Frame> frames = new ArrayList<>();
        List<PlayedNote> pending = new ArrayList<>();
        String sound = Instruments.resolve(song.instrument());

        for (Object value : song.notes()) {
            if (value instanceof String instrument) {
                sound = Instruments.resolve(instrument);
            } else if (value instanceof Number number) {
                int n = number.intValue();
                if (n >= 0) {
                    pending.add(new PlayedNote(sound, n));
                } else {
                    frames.add(new Frame(List.copyOf(pending), -n));
                    pending.clear();
                }
            }
        }
        if (!pending.isEmpty()) {
            frames.add(new Frame(List.copyOf(pending), 0));
        }
        return frames;
    }

    /**
     * Note block pitch (0-24)
     */
    private static float pitch(int note) {
        return (float) Math.pow(2, (note - 12) / 12.0);
    }
}
