package com.klapeks.mlpd.bukkit;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.klapeks.mlpd.api.MLPD;
import com.klapeks.mlpd.api.lFunctions;
import com.klapeks.mlpd.api.MLPD.PluginFolder;

public class BukkitPluginList {
	
	public static boolean isStartup = false;
	public static Map<String, Plugin> needsToBeEnabled1 = new LinkedHashMap<>();
	public static Map<String, Plugin> needsToBeEnabled2 = new LinkedHashMap<>();
	
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
			if (!ConfigBukkit.updateOnEnable) {
				for (String folder : fc.getKeys(true)) {
					if (fc.isList(folder)) {
						List<?> list = fc.getList(folder);
						PluginFolder pf = MLPD.getfolder(folder);
						if (pf.isNullFolder()) continue;
						list.forEach(pl -> {
							String plugin = pl + "";
							if (plugin.contains("$")) {
								plugin = plugin.split("\\$")[0];
								if (plugin.endsWith(" ")) plugin = plugin.substring(0, plugin.length()-1);
							}
							pf.enable(plugin);
						});
					}
				}
				return;
			}
			for (String folder : fc.getKeys(true)) {
				if (fc.isList(folder)) {
					List<?> list = fc.getList(folder);
					PluginFolder pf = MLPD.getfolder(folder);
					if (pf.isNullFolder()) continue;
					list.forEach(pl -> {
						String plugin = pl+"";
						boolean usecfg = ConfigBukkit.updateConfigsOnEnable;
						if (plugin.contains("$")) {
							Map<String, String> parameters = lFunctions.getAllParameters(plugin, "$");
//							String par = plugin.split("\\$")[1];
							plugin = plugin.split("\\$")[0];
							if (plugin.endsWith(" ")) plugin = plugin.substring(0, plugin.length()-1);
							__update_config(pf, usecfg, plugin, parameters);
						} else if (usecfg) pf.using_cfgs(plugin, null);
						pf.using(plugin);
					});
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	private static void __update_config(PluginFolder pf, boolean usecfg, String plugin, Map<String, String> parameters) {
		if (parameters.containsKey("usecfg")) {
			usecfg = true;
		} else if (parameters.containsKey("nocfg")) {
			usecfg = false;
		}
		if (usecfg) {
			String subfolder = parameters.containsKey("usesubfolder") ? parameters.get("usesubfolder") : null;
			String redirect = parameters.containsKey("forcecfgbukkitfolder") ? "plugins" : null;
			pf.using_cfgs(plugin, subfolder, redirect);
		}
	}
	static void __disable__() {
		if (!ConfigBukkit.updateOnDisable) return;
		lFunctions.log("§6Trying to disable all plugins");
		File file = new File("plugins" + fs + "MultiLoaderPluginDownloader" + fs + "list.yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		for (String folder : fc.getKeys(true)) {
			if (fc.isList(folder)) {
				List<?> list = fc.getList(folder);
				PluginFolder pf = MLPD.getfolder(folder);
				if (pf.isNullFolder()) continue;
				list.forEach(pl -> {
					boolean usecfg = ConfigBukkit.updateConfigsOnDisable;
					String plugin = pl+"";
					if (plugin.contains("$")) {
						Map<String, String> parameters = lFunctions.getAllParameters(plugin, "$");
						plugin = plugin.split("\\$")[0];
						if (plugin.endsWith(" ")) plugin = plugin.substring(0, plugin.length()-1);
						pf.disable(plugin);
						__update_config(pf, usecfg, plugin, parameters);
					} else if (usecfg) {
						pf.disable(plugin);
						pf.using_cfgs(plugin, null);
					} else {
						pf.disable(plugin);
					}
					pf.update(plugin);
				});
			}
		}
	}
	public static void __init2__() {
		isStartup = false;
		_doEnable(needsToBeEnabled2);
		_doEnable(needsToBeEnabled1);
		needsToBeEnabled1 = null;
		needsToBeEnabled2 = null;
	}
	static void _doEnable(Map<String, Plugin> map) {
		for (String folder$pl : map.keySet()) {
			try {
				org.bukkit.Bukkit.getServer().getPluginManager().enablePlugin(map.get(folder$pl));
				DPLM._addEnabled(folder$pl.split(",,,")[0], folder$pl.split(",,,")[1]);
			} catch (Throwable t) {
				lFunctions.errorDisable();
				throw new RuntimeException(t);
			}
		}
		map.clear();
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
