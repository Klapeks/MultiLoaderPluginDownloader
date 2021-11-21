package com.klapeks.mlpd.bungee;

import java.io.File;
import java.io.FileWriter;

import com.klapeks.coserver.IMLPack;
import com.klapeks.mlpd.api.lFunctions;

import net.md_5.bungee.api.plugin.Plugin;

public class MLPack implements IMLPack<Plugin> {

	static File folder = null;
	
	@Override
	public void init(Plugin plugin) {
		folder = new File("MLPD_plugins"+File.separator + "read me.txt");
		if (!folder.exists()) {
			try {
				folder.getParentFile().mkdirs();
				folder.createNewFile();
				FileWriter fw = new FileWriter(folder);
				fw.write("Create here some folder and put plugins here.\n");
				fw.write("In bukkit/spigot in configuration file specify the path to the plugin");
				fw.write("Read more at https://www.spigotmc.org/resources/multiloaderplugindownloader.95247/");
				fw.flush();
				fw.close();
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}
		folder = new File("MLPD_plugins");
	}

	@Override
	public void load(Plugin plugin) {
		lFunctions.log("§3MultiLoaderPluginDownloader is loading");
		MLPDServer.__init__();
	}

	@Override
	public void enable(Plugin plugin) {
		lFunctions.log("§aMultiLoaderPluginDownloader is enabling");
	}

	@Override
	public void disable(Plugin plugin) {
		lFunctions.log("§cMultiLoaderPluginDownloader is disabling");
	}

}
