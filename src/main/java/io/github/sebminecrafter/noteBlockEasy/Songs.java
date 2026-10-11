package io.github.sebminecrafter.noteBlockEasy;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.sebminecrafter.noteBlockEasy.NoteBlockEasy.logger;

public class Songs {
    private final File songsFolder;
    private final List<Song> songs;

    public Songs(JavaPlugin plugin) {
        songs = new ArrayList<>();
        this.songsFolder = new File(plugin.getDataFolder(), "songs");

        if (!songsFolder.exists() && !songsFolder.mkdirs()) {
            throw new IllegalStateException(
                    "Could not create songs folder: " + songsFolder.getAbsolutePath()
            );
        }
    }

    Optional<Song> getSong(String name) {
        return songs.stream().filter(s -> s.name().equalsIgnoreCase(name)).findFirst();
    }

    List<Song> getSongs() {
        return songs;
    }

    List<String> getSongNames() {
        return getSongs().stream().map(Song::name).collect(Collectors.toList());
    }

    public void saveSong(Song song) throws IOException {
        File file = songFile(song.name());

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("name", song.name().replaceAll("[^a-zA-Z0-9._-]", "_"));
        yaml.set("notes", song.notes());

        logger.info("Saving song: " + song.name());

        yaml.save(file);
    }

    public Optional<Song> loadSong(String songName) {
        File file = songFile(songName);
        if (!file.isFile()) {
            return Optional.empty();
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        String name = yaml.getString("name");
        List<Integer> notes = readIntegerList(yaml.getList("notes"));

        if (name == null || notes == null) {
            logger.warning("Could not load song: " + songName);
            return Optional.empty();
        }

        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");

        logger.info("Loaded song: " + songName);

        return Optional.of(new Song(name, notes));
    }

    public void saveAllSongs() {
        logger.info("Saving all songs");

        File[] files = songsFolder.listFiles(
                (dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml")
        );

        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (file.delete()) {
                    logger.info("Removed file for replacement: " + name);
                }
            }
        }

        getSongs().forEach(song -> {
            try {
                saveSong(song);
                logger.info("Saved song: " + song.name());
            } catch (IOException e) {
                logger.severe("Failed to save song: " + song.name() + " !");
                throw new RuntimeException(e);
            }
        });
    }

    public void loadAllSongs() {
        File[] files = songsFolder.listFiles(
                (dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml")
        );

        logger.info("Loading all songs from: " + songsFolder.getPath());

        songs.clear();

        if (files == null || files.length == 0) {
            logger.info("No songs to load");
            return;
        }

        Arrays.stream(files).forEach(
                file -> loadSong(file.getName()).ifPresent(songs::add)
        );
    }

    private File songFile(String songName) {
        // Keep the filename within the song folder.
        String safeName = songName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safeName.isBlank() || safeName.equals(".") || safeName.equals("..")) {
            throw new IllegalArgumentException("Invalid song name");
        }

        return new File(songsFolder, safeName + (safeName.endsWith(".yml") ? "" : ".yml"));
    }

    private List<Integer> readIntegerList(List<?> values) {
        if (values == null) {
            return null;
        }

        List<Integer> result = new ArrayList<>();
        for (Object value : values) {
            if (!(value instanceof Number number)) {
                return null;
            }
            result.add(number.intValue());
        }
        return result;
    }
}
