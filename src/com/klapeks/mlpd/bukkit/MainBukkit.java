package com.klapeks.mlpd.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.klapeks.mlpd.api.lFunctions;

public class MainBukkit extends JavaPlugin {
	
	public MainBukkit() {
		if (!Bukkit.getVersion().contains("1.8")) {
			lFunctions.prefix = "§9[§aM§3L§cP§4D§9]§r ";
		}
	}
	
	@Override
	public void onLoad() {
		ConfigBukkit.__init();
		lFunctions.log("§3MultiLoaderPluginDownloader is loading");
		if (ConfigBukkit.loadingType.equals("ONLOAD")) {
			BukkitPluginConfigutaion.__init__();
			BukkitPluginList.__init__();
		}
	}
	
	@Override
	public void onEnable() {
		int time = 0;
		if (ConfigBukkit.loadingType.equals("AFTER_ENABLE")) {
			BukkitPluginConfigutaion.__init__();
			BukkitPluginList.__init__();
			time = 1;
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				lFunctions.log("§aMultiLoaderPluginDownloader is enabling");
				BukkitPluginList.__init2__();
			}
		}, time);
	}
	
	@Override
	public void onDisable() {
		lFunctions.log("§cMultiLoaderPluginDownloader is disabling");
		BukkitPluginList.__disable__();
		BukkitPluginConfigutaion.__disable__();
	}
	
}
