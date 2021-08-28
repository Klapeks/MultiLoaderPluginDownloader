package com.klapeks.mlpd.api;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;

import org.bukkit.Bukkit;

import com.klapeks.coserver.aConfig;
import com.klapeks.coserver.dCoserver;
import com.klapeks.coserver.dFunctions;
import com.klapeks.coserver.dRSA;
import com.klapeks.mlpd.bukkit.BukkitPluginList;

public class MLPD2 {
	private static PluginFolder nullFolder = new PluginFolder(null) {
		public PluginFolder download(String plugin) {
			lFunctions.log("§cPlugin §6'{plugin}'§c can't be downloaded because folder wasn't found!!".replace("{plugin}", plugin));
			return this;
		};
		public boolean has(String plugin) {return false;};
	};
	public static PluginFolder from(String folder) {
		if (hasFolder(folder)) return new PluginFolder(folder);
		lFunctions.log("§cFolder §6'{folder}'§c wasn't found!".replace("{folder}", folder));
		return nullFolder;
	}
	
	public static boolean hasFolder(String folder) {
		return (send("checkfolder", folder)+"").equals("true");
	}

	static uArrayMap<String, String> pluginenabled = new uArrayMap<>();
	public static void _addEnabled(String folder, String plugin) {
		pluginenabled.addIn(folder, plugin);
	}
	public static boolean _isEnabled(String folder, String plugin) {
		return pluginenabled.containsKey(folder) && pluginenabled.get(folder).contains(plugin);
	}
	public static class PluginFolder {
//		private Plugin getPlugin(String plugin) {
//			File file = new File("plugins_MLPD" + File.separator + folder + File.separator + plugin + ".jar");
//			if (file.exists()) {
//				try {
//					Plugin pl = Bukkit.getServer().getPluginManager().loadPlugin(file);
//					pl.onLoad();
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//			}
//		}
		
		String folder;
		public PluginFolder(String folder) {
			if (folder==null) return;
			this.folder = folder.replace(File.separator, "/");
		}
		
		public boolean localContains(String plugin) {
			return new File("plugins_MLPD" + File.separator + folder + File.separator + plugin + ".jar").exists();
		}
		
		public PluginFolder using(String plugin) {
			if (_isEnabled(folder, plugin)) {
				lFunctions.log("§6Plugin '{plugin}' is already enabled and will not be enabled again".replace("{plugin}", plugin));
				return this;
			}
			try {
				if (has(plugin) && checkNewVersion(plugin)) {
					download(plugin);
				} else {
					lFunctions.log("§aThe latest version of '{plugin}' is already installed".replace("{plugin}", plugin));
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
			try {//Trying load and enable plugin
				org.bukkit.plugin.Plugin pl = org.bukkit.Bukkit.getServer().getPluginManager().loadPlugin(
						new File("plugins_MLPD" + File.separator + folder.replace("/", File.separator) + File.separator + plugin + ".jar"));
				pl.onLoad();
				if (!BukkitPluginList.isStartup) {
					org.bukkit.Bukkit.getServer().getPluginManager().enablePlugin(pl);
					_addEnabled(folder, plugin);
				} else {
					BukkitPluginList.needsToBeEnabled.put(folder+",,,"+plugin, pl);
				}
			} catch (Throwable t) {
				if (BukkitPluginList.DISABLE_BUKKIT_ON_PLUGIN_ERROR) {
					Bukkit.shutdown();
					return nullFolder;
				}
				t.printStackTrace();
			}
			return this;
		}
		public PluginFolder using_async(String plugin) {
			dFunctions.scheduleAsync(new Runnable() {
				@Override
				public void run() {
					using(plugin);
				}
			}, 0);
			return this;
		}
		
		public PluginFolder download_async(String plugin) {
			dFunctions.scheduleAsync(new Runnable() {
				@Override
				public void run() {
					download(plugin);
				}
			}, 0);
			return this;
		}
		
		public PluginFolder download(String plugin) {
			if (!has(plugin)) {
				lFunctions.log("§cPlugin §6'{plugin}'§c wasn't found!".replace("{plugin}", plugin));
				return this;
			}
			//Prepare to plugin downloading
			String secretPsw = sendLarge("startplugindownloading", folder, plugin);
			int size = lFunctions.toInt(secretPsw.split(" ")[0]);
			if (secretPsw.equals("-1") || size==-1) {
				closeLarge();
				lFunctions.log("§cPlugin §6'{plugin}'§c was found but no?".replace("{plugin}", plugin));
				return this;
			}
			secretPsw = secretPsw.replaceFirst(secretPsw.split(" ")[0]+" ", "");
			
			File file = new File("plugins_MLPD" + File.separator + folder.replace("/", File.separator) + File.separator + plugin.replace("/", File.separator) + ".jar");
			try {//Downloading plugin
				if (file.exists()) file.delete();
				file.getParentFile().mkdirs();
				String g = "";
				int old_proc = 0, new_one = 0;
				
				for (int i = 0; i < size; i++) {
					g = sendLarge("downloadpluginstage", secretPsw, i+"");
					if (g==null || g.equals("null")) {
						lFunctions.log("§cSomething went wrong. On iterator: " + i);
						return this;
					}
					Files.write(file.toPath(), dRSA.base64_decode_byte(g), 
							java.nio.file.StandardOpenOption.CREATE,
							java.nio.file.StandardOpenOption.WRITE,
							java.nio.file.StandardOpenOption.APPEND);
					new_one = i*100 / size;
					if (new_one - old_proc >= 10 || i==0) {
						lFunctions.log("§6Downloading plugin '{plugin}', iterator {i}... ".replace("{plugin}", plugin).replace("{i}", i+"") + new_one);
						old_proc = new_one;
					}
				}
				file.setLastModified(lFunctions.toLong(send("getpluginlastmodified", folder, plugin)));
				lFunctions.log("§6Plugin was downloaded");
			} catch (Throwable t) {
				lFunctions.log("§cError with plugin downloading");
				file.delete();
				t.printStackTrace();
			}
			send("mbneedclearcash", secretPsw);
			closeLarge();
			return this;
		}


		//FOLDER WITH CONFIGURATION
		public PluginFolder using_cfgs_async(String folder_with_configs) {
			dFunctions.scheduleAsync(new Runnable() {
				@Override
				public void run() {
					using_cfgs(folder_with_configs);
				}
			}, 0);
			return this;
		}
		
		public PluginFolder download_cfgs_async(String folder_with_configs) {
			dFunctions.scheduleAsync(new Runnable() {
				@Override
				public void run() {
					download_cfgs(folder_with_configs);
				}
			}, 0);
			return this;
		}
		public boolean checkNewVersion(String plugin) {
			File file = new File("plugins_MLPD" + File.separator + folder.replace("/", File.separator) + File.separator + plugin + ".jar");
			if (!file.exists()) return true;
			return lFunctions.toLong(send("getpluginlastmodified", folder, plugin)) - file.lastModified() >= 1000;
//			return (send("checkpluginnewversion", folder, plugin, (file.exists() ? file.lastModified() : -1)+"")+"").equals("true");
		}
		
		public boolean has(String plugin) {
			return (send("checkplugin", folder, plugin)+"").equals("true");
		}

		
		public PluginFolder using_cfgs(String folder_with_configs) {
			if (!has_cfgs(folder_with_configs)) {
				lFunctions.log("§cFolder with configs §6'{folder_with_configs}'§c wasn't found!".replace("{folder_with_configs}", folder_with_configs));
				return this;
			}
			String[] path$file = (send("getconfiglistoffolder", folder, folder_with_configs)+"").split(",,,,,");
			for (String config : path$file) {
				if (checkConfigNewVersion(folder_with_configs, config)) {
					download_cfg_file(folder_with_configs, config);
				}
			}
			return this;
		}
		
		public PluginFolder download_cfgs(String folder_with_configs) {
			if (!has_cfgs(folder_with_configs)) {
				lFunctions.log("§cFolder with configs §6'{folder_with_configs}'§c wasn't found!".replace("{folder_with_configs}", folder_with_configs));
				return this;
			}
			String[] path$file = (send("getconfiglistoffolder", folder, folder_with_configs)+"").split(",,,,,");
			for (String config : path$file) {
//				lFunctions.log("§eConfig: " + folder_with_configs + "  -   " + config);
				download_cfg_file(folder_with_configs, config);
			}
			return this;
		}
		
		public PluginFolder download_cfg_file(String folder_with_configs, String config) {
			if (!has_cfg_file(folder_with_configs, config)) {
				lFunctions.log("§cConfig §6'{config}'§c in §6{folder_with_configs}§c wasn't found!".replace("{config}", config).replace("{folder_with_configs}", folder_with_configs));
				return this;
			}
			//Prepare to config downloading
			String secretPsw = sendLarge("startdownloadconfig", folder, folder_with_configs, config);
			int size = lFunctions.toInt(secretPsw.split(" ")[0]);
			if (secretPsw.equals("-1") || size==-1) {
				closeLarge();
				lFunctions.log("§cConfig §6'{config}'§c was found but no?".replace("{config}", folder_with_configs+"/"+config));
				return this;
			}
			secretPsw = secretPsw.replaceFirst(secretPsw.split(" ")[0]+" ", "");
			
			File file = new File("plugins_MLPD" + File.separator + folder.replace("/", File.separator) + File.separator + folder_with_configs.replace("/", File.separator) + File.separator + config.replace("/", File.separator));
			try {//Downloading config
				if (file.exists()) file.delete();
				file.getParentFile().mkdirs();
				String g = "";
				int old_proc = 0, new_one = 0;
				
				for (int i = 0; i < size; i++) {
					g = sendLarge("downloadconfigstage", secretPsw, i+"");
					if (g==null || g.equals("null")) {
						lFunctions.log("§cSomething went wrong. On iterator: " + i);
						return this;
					}
					Files.write(file.toPath(), dRSA.base64_decode_byte(g), 
							java.nio.file.StandardOpenOption.CREATE,
							java.nio.file.StandardOpenOption.WRITE,
							java.nio.file.StandardOpenOption.APPEND);
					new_one = i*100 / size;
					if (new_one - old_proc >= 10 || i==0) {
						lFunctions.log("§6Downloading config '{config}', iterator {i}... ".replace("{config}", folder_with_configs+"/"+config).replace("{i}", i+"") + new_one);
						old_proc = new_one;
					}
				}
				file.setLastModified(lFunctions.toLong(send("getconfiglastmodified", folder, folder_with_configs, config)));
				lFunctions.log("§6Config was downloaded");
			} catch (Throwable t) {
				lFunctions.log("§cError with config downloading");
				file.delete();
				t.printStackTrace();
			}
			send("mbneedclearconfigcash", secretPsw);
			closeLarge();
			return this;
		}
		
		public boolean has_cfg_file(String folder_with_configs, String config) {
			return (send("checkconfiginfolderwithconfigs", folder, folder_with_configs, config)+"").equals("true");
		}
		
		public boolean has_cfgs(String folder_with_configs) {
			return (send("checkfolderwithconfigs", folder, folder_with_configs)+"").equals("true");
		}

		public boolean checkConfigNewVersion(String folder_with_configs, String config) {
			File file = new File("plugins_MLPD" + File.separator + folder.replace("/", File.separator) + File.separator + 
					folder_with_configs.replace("/", File.separator) + File.separator + config.replace("/", File.separator));
			if (!file.exists()) return true;
			return lFunctions.toLong(send("getconfiglastmodified", folder, folder_with_configs, config)) - file.lastModified() >= 1000;
//			return (send("checkpluginnewversion", folder, plugin, (file.exists() ? file.lastModified() : -1)+"")+"").equals("true");
		}
	}
	
	private static String send(String cmd, String... args) {
		cmd = dRSA.base64_encode(cmd);
		for (String arg : args) {
			cmd += " " + dRSA.base64_encode(arg);
		}
		if (aConfig.useSecurity) {
			return dCoserver.securitySend(aConfig.bukkit.ip, aConfig.bukkit.port, "multiloaderplugindownloader " + cmd, false);
		} else {
			return dCoserver.send(aConfig.bukkit.ip, aConfig.bukkit.port, "multiloaderplugindownloader " + cmd, false);
		}
	}
	
	private static String sendLarge(String cmd, String... args) {
		cmd = dRSA.base64_encode(cmd);
		for (String arg : args) {
			cmd += " " + dRSA.base64_encode(arg);
		}
		if (aConfig.useSecurity) {
			return dCoserver.securitySend(aConfig.bukkit.ip, aConfig.bukkit.port, "multiloaderplugindownloader " + cmd, true);
		} else {
			return dCoserver.send(aConfig.bukkit.ip, aConfig.bukkit.port, "multiloaderplugindownloader " + cmd, true);
		}
	}
	private static void closeLarge() {
		dCoserver.closeLarge(aConfig.bukkit.ip, aConfig.bukkit.port);
	}
}
