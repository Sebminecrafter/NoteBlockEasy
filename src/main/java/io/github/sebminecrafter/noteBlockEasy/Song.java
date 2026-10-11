package io.github.sebminecrafter.noteBlockEasy;

import java.util.List;

/**
 * A song as stored in YAML.
 * <p>
 * {@code notes} a list of
 * <ul>
 *   <li>integers {@code >= 0}: note block note (0-24), plays instantly.
 *   <li>an integer {@code < 0}: waits {@code -[amount]} steps.
 *   <li>a string: switches the instrument. See {@link Instruments}
 * </ul>
 * @param name       Name of the song
 * @param instrument The instrument at the start of the song
 * @param notes      The notes/actions in the song
 */
public record Song(String name, String instrument, List<Object> notes) {
    public Song {
        if (instrument == null || instrument.isBlank()) {
            instrument = Instruments.DEFAULT;
        }
    }

    public Song(String name, List<Object> notes) {
        this(name, Instruments.DEFAULT, notes);
    }
}
