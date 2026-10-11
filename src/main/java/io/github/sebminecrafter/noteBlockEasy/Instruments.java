package io.github.sebminecrafter.noteBlockEasy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/** Turns instrument into keys that can be used in `playSound()` */
public final class Instruments {
    public static final String DEFAULT = "bass";
    private static final String NAMESPACE = "minecraft";
    private static final Pattern VALID_KEY = Pattern.compile("([a-z0-9_.-]+:)?[a-z0-9_./-]+");

    private static final Map<String, String> BUILT_IN = new LinkedHashMap<>();
    private static final Map<String, String> ALIASES = Map.of(
            "piano", "harp",
            "bass_drum", "basedrum",
            "bassdrum", "basedrum",
            "snare_drum", "snare",
            "sticks", "hat",
            "bass_guitar", "bass"
    );

    static {
        for (String name : List.of(
                "harp", "basedrum", "snare", "hat", "bass", "flute", "bell", "guitar",
                "chime", "xylophone", "iron_xylophone", "cow_bell", "didgeridoo", "bit", "banjo", "pling")) {
            BUILT_IN.put(name, "block.note_block." + name);
        }
    }

    private Instruments() {
    }

    public static List<String> names() {
        return List.copyOf(BUILT_IN.keySet());
    }

    public static String resolve(String instrument) {
        String key = instrument.trim().toLowerCase(Locale.ROOT);
        key = ALIASES.getOrDefault(key, key);

        String builtIn = BUILT_IN.get(key);
        if (builtIn != null) {
            return NAMESPACE + ":" + builtIn;
        }
        return key.indexOf(':') >= 0 ? key : NAMESPACE + ":" + key;
    }

    public static boolean isValid(String instrument) {
        return !instrument.isBlank() && VALID_KEY.matcher(resolve(instrument)).matches();
    }
}
