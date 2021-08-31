package com.klapeks.mlpd.bungee;

import net.md_5.bungee.api.plugin.Plugin;

public class MainBungee extends Plugin {

	private static MLPack bungee = new MLPack();

	public MainBungee() {
		bungee.init(this);
	}
	
	@Override
	public void onLoad() {
		bungee.load(this);
	}
	
	
	@Override
	public void onEnable() {
		bungee.enable(this);
	}
	
	@Override
	public void onDisable() {
		bungee.disable(this);
	}
}
