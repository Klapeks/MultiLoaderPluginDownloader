package com.klapeks.mlpd.bukkit;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.klapeks.coserver.aConfig;
import com.klapeks.mlpd.api.MLPD;
import com.klapeks.mlpd.api.MLPD.PluginFolder;

public class BukkitPluginList {
	
	public static boolean DISABLE_BUKKIT_ON_PLUGIN_ERROR = false;
	
	public static boolean isStartup = false;
	public static Map<String, Plugin> needsToBeEnabled = new HashMap<>();
	
	static final String fs = File.separator;
	
	static void __init__() {
		try {
			isStartup = true;
			File file = new File("plugins" + fs + "MultiLoaderPluginDownloader" + fs + "list.yml");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
				FileWriter fw = new FileWriter(file);
				f(fw, "This is a list of plugins that will be");
				f(fw, "automatically downloaded if out of date");
				f(fw);
				f(fw, "Example:");
				f(fw);
				f(fw, "folder1:");
				f(fw, "- plugin1");
				f(fw, "- plugin2");
				f(fw, "- plugin3 $usesubfolder subfolder1");
				f(fw, "- plugin4");
				f(fw, "folder2:");
				f(fw, "- plugin5");
				f(fw, "- plugin6 $nocfg");
				f(fw, "folder3: [plugin7, plugin8, plugin9]");
				
				fw.flush();
				fw.close();
			}
			FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
			for (String folder : fc.getKeys(true)) {
				if (fc.isList(folder) && MLPD.hasFolder(folder)) {
					List<?> list = fc.getList(folder);
					PluginFolder pf = MLPD.from(folder);
					list.forEach(pl -> {
						String plugin = pl+"";
						if (plugin.contains("$")) {
							String par = plugin.split("\\$")[1];
							plugin = plugin.split("\\$")[0];
							if (plugin.endsWith(" ")) {
								plugin = plugin.substring(0, plugin.length()-1);
							}
							if (par.equals("usecfg")) {
								pf.using_cfgs(plugin, null);
								pf.using(plugin+"");
								return;
							} else if (par.startsWith("usesubfolder ")) {
								par = par.substring("usesubfolder ".length());
								pf.using_cfgs(plugin, par);
								pf.using(plugin+"");
								return;
							} else if (par.equals("nocfg")) {
								pf.using(plugin);
								return;
							}
						}
						if (BukkitPluginConfigutaion.autoPluginConfiguration) pf.using_cfgs(plugin, null);
						pf.using(plugin+"");
					});
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	public static void __init2__() {
		for (String folder$pl : needsToBeEnabled.keySet()) {
			try {
				org.bukkit.Bukkit.getServer().getPluginManager().enablePlugin(needsToBeEnabled.get(folder$pl));
				MLPD._addEnabled(folder$pl.split(",,,")[0], folder$pl.split(",,,")[1]);
			} catch (Throwable t) {
				if (DISABLE_BUKKIT_ON_PLUGIN_ERROR) {
					Bukkit.shutdown();
					return;
				}
				throw new RuntimeException(t);
			}
		}
		isStartup = false;
		needsToBeEnabled.clear();
		needsToBeEnabled = null;
	}
	static void f(FileWriter fw, String comment) {
		try {
			fw.write("# " + comment + "\n");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	static void f(FileWriter fw) {
		f(fw, "");
	}
	
}
