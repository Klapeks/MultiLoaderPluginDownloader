package com.klapeks.mlpd.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.klapeks.coserver.IMLPack;
import com.klapeks.mlpd.api.lFunctions;

public class MLPack implements IMLPack<JavaPlugin> {

	@Override
	public void init(JavaPlugin plugin) {
		if (!Bukkit.getVersion().contains("1.8")) {
			lFunctions.prefix = "§9[§aM§3L§cP§4D§9]§r ";
		}
	}

	@Override
	public void load(JavaPlugin plugin) {
		lFunctions.log("§3MultiLoaderPluginDownloader is loading");
		BukkitPluginConfigutaion.__init__();
		BukkitPluginList.__init__();
	}

	@Override
	public void enable(JavaPlugin plugin) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				lFunctions.log("§aMultiLoaderPluginDownloader is enabling");
				BukkitPluginList.__init2__();
			}
		});
	}

	@Override
	public void disable(JavaPlugin plugin) {
		lFunctions.log("§cMultiLoaderPluginDownloader is disabling");
	}

}
