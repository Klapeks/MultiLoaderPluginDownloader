package com.klapeks.mlpd.bukkit;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.klapeks.mlpd.api.MLPD;
import com.klapeks.mlpd.api.lFunctions;
import com.klapeks.mlpd.api.MLPD.PluginFolder;

public class BukkitPluginConfigutaion {
	
	static final String fs = File.separator;
	
	static boolean autoPluginConfiguration = true;
	
	static void __init__() {
		try {
			File file = new File("plugins" + fs + "MultiLoaderPluginDownloader" + fs + "cfglist.yml");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
				FileWriter fw = new FileWriter(file);
				f(fw, "This is a list of folder with configuration that");
				f(fw, "will be automatically downloaded if out of date");
				f(fw);
				f(fw, "Example:");
				f(fw);
				f(fw, "folder1:");
				f(fw, "- cfg_folder1");
				f(fw, "- cfg_folder2");
				f(fw, "- cfg_folder3");
				f(fw, "- cfg_folder4");
				f(fw, "folder2:");
				f(fw, "- cfg_folder5");
				f(fw, "- cfg_folder6");
				f(fw, "folder3: [cfg_folder7, cfg_folder8, cfg_folder9]");

				fw.write("\n\n");
				fw.write("# If true plugin configuration (plugin from list.yml)\n");
				fw.write("# will be automatically downloaded\n");
				fw.write("autoPluginConfiguration: true\n");
				
				fw.flush();
				fw.close();
			}
			FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
			autoPluginConfiguration = fc.getBoolean("autoPluginConfiguration");
			System.out.println(autoPluginConfiguration);
			for (String folder : fc.getKeys(true)) {
				if (fc.isList(folder) && MLPD.hasFolder(folder)) {
					List<?> list = fc.getList(folder);
					PluginFolder pf = MLPD.from(folder);
					list.forEach(folder_with_configs -> {
						String fwc = folder_with_configs+"";
						if (fwc.contains("$")) {
							Map<String, String> parameters = lFunctions.getAllParameters(fwc, "$");
							String redirect = parameters.containsKey("forcecfgbukkitfolder") ? "plugins" : null;
							pf.using_cfgs(folder_with_configs+"", null, redirect);
						} else {
							pf.using_cfgs(folder_with_configs+"", null);
						}
					});
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
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
