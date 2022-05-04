package com.klapeks.mlpd.bungee;

import java.io.File;
import java.io.FileWriter;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.klapeks.mlpd.api.lFunctions;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

@Plugin(id="mlpd")
public class MainVelocity {
	
	@Inject
    public MainVelocity(ProxyServer server, Logger logger) {
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

		lFunctions.log("§3MultiLoaderPluginDownloader is loading");
		MLPDServer.__init__();
	}
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		lFunctions.log("§aMultiLoaderPluginDownloader is enabling");
	}
	@Subscribe
	public void onProxyShutdown(ProxyShutdownEvent event) {
		lFunctions.log("§cMultiLoaderPluginDownloader is disabling");
	}
}
