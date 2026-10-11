# NoteBlockEasy
A simple note block song player plugin for Bukkit

## Commands
| Command                                              | Description                        |
|------------------------------------------------------|------------------------------------|
| `/playsong <song> [player \| x y z world]`           | Play a song at a player / location |
| `/nbe list`                                          | List loaded songs                  |
| `/nbe instruments`                                   | List the built-in instrument names |
| `/nbe reload`                                        | Reload songs                       |
| `/nbe stop`                                          | Stop all playing songs             |

## Songs
Songs are YAML files in `plugins/NoteBlockEasy/songs/`

Example:
```yaml
name: demo
instrument: harp #optional
notes: [0, 4, 7, -2, flute, 12, -2, "harp", 14]
```

`notes` is a flat list. Each entry is one of:

| Entry                        | Meaning                                                                                         |
|------------------------------|-------------------------------------------------------------------------------------------------|
| positive number (0-24)       | A note (note block clicks). Plays immediately and takes no time. Several in a row form a chord. |
| negative number              | Wait that many steps (1 step = 2 ticks).                                                        |
| a string                     | Switch to that instrument for every note after it, until the next string.                       |

### Instruments
An instrument is a Minecraft sound key, so anything that's in the game or a resource pack can be used.

- **Built-in names**: `harp`, `basedrum`, `snare`, `hat`, `bass`, `flute`, `bell`, `guitar`, `chime`,
  `xylophone`, `iron_xylophone`, `cow_bell`, `didgeridoo`, `bit`, `banjo`, `pling`
  (also `piano` for `harp`, `bass_drum`, `snare_drum`, `sticks` for `hat`).
- **Vanilla sounds**: `block.note_block.guitar`/`minecraft:block.note_block.guitar`
  (any other vanilla sound works too, like `entity.chicken.egg`).
- **Custom sounds** from a resource pack, like `"coolsounds:sound1"`, as defined in the pack's `sounds.json`.
  The player needs the resource pack for these to be audible.

### Songs that fail to load
Any songs that can't load for some reason get moved to `failed` inside the `songs` directory, so they don't get deleted
on the final save during shutdown.
