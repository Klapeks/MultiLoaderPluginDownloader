package com.klapeks.mlpd.bungee;

import java.io.File;
import java.io.FileWriter;

import com.klapeks.mlpd.api.lFunctions;

import net.md_5.bungee.api.plugin.Plugin;

public class MainBungee extends Plugin {

	public MainBungee() {
		MLPDServer.mlpd_folder = new File("MLPD_plugins"+File.separator + "read me.txt");
		if (!MLPDServer.mlpd_folder.exists()) {
			try {
				MLPDServer.mlpd_folder.getParentFile().mkdirs();
				MLPDServer.mlpd_folder.createNewFile();
				FileWriter fw = new FileWriter(MLPDServer.mlpd_folder);
				fw.write("Create here some folder and put plugins here.\n");
				fw.write("In bukkit/spigot in configuration file specify the path to the plugin");
				fw.write("Read more at https://www.spigotmc.org/resources/multiloaderplugindownloader.95247/");
				fw.flush();
				fw.close();
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}
		MLPDServer.mlpd_folder = new File("MLPD_plugins");
	}
	
	@Override
	public void onLoad() {
		lFunctions.log("§3MultiLoaderPluginDownloader is loading");
		MLPDServer.__init__();
	}
	
	
	@Override
	public void onEnable() {
		lFunctions.log("§aMultiLoaderPluginDownloader is enabling");
	}
	
	@Override
	public void onDisable() {
		lFunctions.log("§cMultiLoaderPluginDownloader is disabling");
	}
}
