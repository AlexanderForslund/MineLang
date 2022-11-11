package dev.forslund.mc;
import org.bukkit.plugin.java.JavaPlugin;

public class MineLang extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("MineLang helper is enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MineLang helper is disabled.");
    }
}