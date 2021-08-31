package com.klapeks.mlpd.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

public class MainBukkit extends JavaPlugin {
	
	private static MLPack plugin = new MLPack();
	
	public MainBukkit() {
		plugin.init(this);
	}
	
	@Override
	public void onLoad() {
		plugin.load(this);
	}
	
	@Override
	public void onEnable() {
		plugin.enable(this);
	}
	
	@Override
	public void onDisable() {
		plugin.disable(this);
	}
	
}
