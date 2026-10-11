package io.github.sebminecrafter.noteBlockEasy;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class NoteBlockEasy extends JavaPlugin {
    static public Logger logger;
    public Songs songs = null;

    @Override
    public void onEnable() {
        logger = getLogger();

        songs = new Songs(this);
        new NbsConverter(this).convertAll();
        songs.loadAllSongs();

        PluginCommand playsong = getCommand("playsong");
        PluginCommand nbe = getCommand("nbe");
        if (playsong == null || nbe == null) {
            logger.severe("Error registering commands, disabling.");
            this.setEnabled(false);
            return;
        }

        PlaysongCommand ps_com = new PlaysongCommand(this, songs);
        playsong.setExecutor(ps_com);
        playsong.setTabCompleter(ps_com);
        NBECommand nbe_com = new NBECommand(this, songs);
        nbe.setExecutor(nbe_com);
        nbe.setTabCompleter(nbe_com);

        logger.info("NoteBlockEasy started!");
    }

    @Override
    public void onDisable() {
        if (songs != null) songs.saveAllSongs();
        logger.info("NoteBlockEasy stopped!");
    }
}
