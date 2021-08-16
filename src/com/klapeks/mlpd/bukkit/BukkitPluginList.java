package com.klapeks.mlpd.bukkit;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.klapeks.mlpd.api.MLPD;
import com.klapeks.mlpd.api.MLPD.PluginFolder;

public class BukkitPluginList {
	
	public static boolean DISABLE_BUKKIT_ON_PLUGIN_ERROR = false;
	
	public static boolean isStartup = false;
	public static List<Plugin> needsToBeEnabled = new ArrayList<>();
	
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
				f(fw, "- plugin3");
				f(fw, "- plugin4");
				f(fw, "folder2:");
				f(fw, "- plugin5");
				f(fw, "- plugin6");
				f(fw, "folder3: [plugin7, plugin8, plugin9]");
				
				fw.flush();
				fw.close();
			}
			FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
			for (String folder : fc.getKeys(true)) {
				if (fc.isList(folder) && MLPD.hasFolder(folder)) {
					List<?> list = fc.getList(folder);
					PluginFolder pf = MLPD.from(folder);
					list.forEach(plugin -> {
						pf.using(plugin+"");
					});
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	public static void __init2__() {
		for (Plugin pl : needsToBeEnabled) {
			try {
				org.bukkit.Bukkit.getServer().getPluginManager().enablePlugin(pl);
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
