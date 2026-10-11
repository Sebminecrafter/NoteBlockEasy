package io.github.sebminecrafter.noteBlockEasy;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.sebminecrafter.noteBlockEasy.NoteBlockEasy.logger;

public class Songs {
    /** Subfolder for songs that fail to load */
    static final String FAILED_FOLDER = "failed";

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
        yaml.set("instrument", song.instrument());
        yaml.set("notes", song.notes());

        logger.info("Saving song: " + song.name());

        yaml.save(file);
    }

    public Optional<Song> loadSong(String songName) {
        File file = songFile(songName);
        if (!file.isFile()) {
            return Optional.empty();
        }
        return loadFile(file);
    }

    /** Loads a song from {@code file}. Returns empty if it is invalid. */
    private Optional<Song> loadFile(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        String name = yaml.getString("name");
        String instrument = yaml.getString("instrument", Instruments.DEFAULT);
        List<Object> notes = readNotes(yaml.getList("notes"));

        if (name == null || notes == null) {
            logger.warning("Could not load song: " + file.getName());
            return Optional.empty();
        }

        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");

        warnAboutInvalidInstruments(name, instrument, notes);

        logger.info("Loaded song: " + file.getName());

        return Optional.of(new Song(name, instrument, notes));
    }

    /**
     * Moves a song file that could not be loaded into the {@link #FAILED_FOLDER} directory.
     */
    private void moveToFailedFolder(File file) {
        Path folder = new File(songsFolder, FAILED_FOLDER).toPath();
        try {
            Files.createDirectories(folder);
            Path target = folder.resolve(file.getName());
            for (int i = 1; Files.exists(target); i++) { // never overwrite an earlier failed file
                target = folder.resolve(i + "_" + file.getName());
            }
            Files.move(file.toPath(), target);
            logger.warning("Moved '" + file.getName() + "' to " + songsFolder.getName() + "/" + FAILED_FOLDER
                    + "/ because it could not be loaded.");
        } catch (IOException e) {
            logger.severe("Could not move '" + file.getName() + "' to the failed folder (" + e.getMessage()
                    + "). It may be deleted when the server stops.");
        }
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

        for (File file : files) {
            Optional<Song> song = loadFile(file);
            if (song.isPresent()) {
                songs.add(song.get());
            } else {
                moveToFailedFolder(file);
            }
        }
    }

    private File songFile(String songName) {
        // Keep the filename within the song folder.
        String safeName = songName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safeName.isBlank() || safeName.equals(".") || safeName.equals("..")) {
            throw new IllegalArgumentException("Invalid song name");
        }

        return new File(songsFolder, safeName + (safeName.endsWith(".yml") ? "" : ".yml"));
    }

    /**
     * Reads the {@code notes} list: numbers become integers (notes and waits) and strings are kept
     * as instrument switches. Returns null if the list is missing or has any other kind of entry.
     */
    static List<Object> readNotes(List<?> values) {
        if (values == null) {
            return null;
        }

        List<Object> result = new ArrayList<>();
        for (Object value : values) {
            if (value instanceof Number number) {
                result.add(number.intValue());
            } else if (value instanceof String instrument && !instrument.isBlank()) {
                result.add(instrument.trim());
            } else {
                return null;
            }
        }
        return result;
    }

    /** Warn on invalid instrument names */
    private static void warnAboutInvalidInstruments(String songName, String defaultInstrument, List<Object> notes) {
        Set<String> used = new LinkedHashSet<>();
        used.add(defaultInstrument);
        for (Object value : notes) {
            if (value instanceof String instrument) {
                used.add(instrument);
            }
        }
        for (String instrument : used) {
            if (!Instruments.isValid(instrument)) {
                logger.warning("Song '" + songName + "' uses an invalid instrument/sound key: '" + instrument + "'");
            }
        }
    }
}
