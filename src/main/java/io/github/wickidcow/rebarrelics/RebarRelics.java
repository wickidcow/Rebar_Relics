package io.github.wickidcow.rebarrelics;

import io.github.pylonmc.rebar.addon.RebarAddon;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class RebarRelics extends JavaPlugin implements RebarAddon {

    @Override
    public void onEnable() {
        registerWithRebar();
        saveDefaultConfig();
        RelicItems.initialize(this);
        getServer().getPluginManager().registerEvents(new RelicListener(this), this);
        getServer().getPluginManager().registerEvents(new ClassicTalismansListener(this), this);
        getServer().getPluginManager().registerEvents(new AdvancedRelicListener(this), this);
        getServer().getPluginManager().registerEvents(new MiningTalismansListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatCharmsListener(this), this);
        getServer().getPluginManager().registerEvents(new WhirlwindListener(this), this);
        getServer().getPluginManager().registerEvents(new RecipeStartupAudit(this), this);
        getLogger().info("Loaded unique three-tier talismans, rings, and relic materials. Experimental: test before production use.");
    }

    @Override
    public @NotNull JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public @NotNull Locale getDefaultLanguage() {
        return Locale.ENGLISH;
    }

    @Override
    public @NotNull Material getMaterial() {
        return Material.AMETHYST_SHARD;
    }
}
