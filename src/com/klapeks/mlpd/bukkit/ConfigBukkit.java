package com.klapeks.mlpd.bukkit;

import java.io.File;
import java.io.FileWriter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.klapeks.funcs.FileCfgUtils;

public class ConfigBukkit {

	public static boolean updateOnEnable = true;
	public static boolean updateOnDisable = false;
	public static boolean updateConfigsOnEnable = true;
	public static boolean updateConfigsOnDisable = false;

	public static String loadingType = "AFTER_ENABLE";
	
	static final String fs = File.separator;
	
	
	private static FileWriter fw = null;
	private static FileConfiguration config = null;
	
	static void __init() {
		try {
			File file = new File("plugins" + fs + "MultiLoaderPluginDownloader" + fs + "config.yml");
			if (!file.exists()) try {
				file.getParentFile().mkdirs(); 
				file.createNewFile();
				fw = new FileWriter(file);
				fw.write("# Config for Bukkit server side" + "\n");
			} catch (Throwable e) { throw new RuntimeException(e); }
			config = YamlConfiguration.loadConfiguration(file);
			
			if (fw==null) fw = open(file);

			ConfigBukkit.loadingType = g("loadingType", loadingType, 
					"AFTER_ENABLE: Plugins will be updated and loaded after the server was enalbed.",
					"ONLOAD: Plugins will be updated and loaded while server is loading - may cause errors",
					"!works if 'updateOnEnable' is true"
					);
			switch (loadingType) {
			case "ONLOAD": break;
			default: loadingType = "AFTER_ENABLE";
			}

			ConfigBukkit.updateOnEnable = g("updateOnEnable", updateOnEnable, "Plugins will be updated while", "the server is enabling");
			ConfigBukkit.updateOnDisable = g("updateOnDisable", updateOnDisable, "Plugins will be updated while", "the server is shutting down");
			ConfigBukkit.updateConfigsOnEnable = g("updateConfigsOnEnable", updateConfigsOnEnable, "Configs will be updated while", "the server is enabling", 
					"!NOTE: Configs file from 'list.yml' will be", "!   updated if 'updateOnEnable' is true too");
			ConfigBukkit.updateConfigsOnDisable = g("updateConfigsOnDisable", updateConfigsOnDisable, "Configs will be updated while", "the server is shutting down",
					"!NOTE: Configs file from 'list.yml' will be", "!   updated if 'updateOnDisable' is true too");
			
			fw.flush();
			fw.close();
			config = null; fw = null;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	private static FileWriter open(File file) {
		return FileCfgUtils.open(file);
	}
	private static <T> T g(String key, T defaultValue, String... comment) {
		return FileCfgUtils.get_bk(config, fw, key, defaultValue, comment);
	}
//	private static void copyConfig(File to, Function<String, String> placeholders) {
//		try {
//			if (to.exists()) to.delete();
//			else to.getParentFile().mkdirs();
//			to.createNewFile();
//			
//			BufferedReader br = new BufferedReader(new InputStreamReader(MainBungee.bungee.getResourceAsStream("config.yml")));
//			String line = null;
//			FileWriter fw = new FileWriter(to);
//			while ((line=br.readLine())!=null) {
//				fw.append(placeholders.apply(line)+"\r\n");
//			}
//			br.close();
//			fw.flush();
//			fw.close();
//
//		} catch (Throwable e) {
//			throw new RuntimeException(e);
//		}
//	}

}
