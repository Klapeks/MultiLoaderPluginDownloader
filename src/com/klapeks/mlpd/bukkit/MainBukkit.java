package com.klapeks.mlpd.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.klapeks.mlpd.api.lFunctions;

public class MainBukkit extends JavaPlugin {
	
	static MainBukkit bukkit;
	
	public MainBukkit() {
		bukkit = this;
		if (!Bukkit.getVersion().contains("1.8")) {
			lFunctions.prefix = "§9[§aM§3L§cP§4D§9]§r ";
		}
	}
	
	@Override
	public void onLoad() {
		BukkitPluginList.__init__();
	}
	
	
	@Override
	public void onEnable() {
		BukkitPluginList.__init2__();
	}
	
	
}
