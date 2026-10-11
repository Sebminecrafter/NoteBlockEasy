package io.github.sebminecrafter.noteBlockEasy;

import java.util.List;

/**
 * A song as stored in YAML.
 * <p>
 * {@code notes} is a flat list: a value {@code >= 0} is a note (0-24 note block clicks) that
 * sounds immediately and takes no time of its own; a value {@code < 0} waits {@code -value} steps.
 * Several notes in a row therefore play together as a chord.
 */
public record Song(String name, List<Integer> notes) {
}
