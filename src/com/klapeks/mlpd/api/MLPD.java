package com.klapeks.mlpd.api;

import java.io.File;
import java.nio.file.Files;

import org.bukkit.Bukkit;

import com.klapeks.coserver.aConfig;
import com.klapeks.coserver.dCoserver;
import com.klapeks.coserver.dFunctions;
import com.klapeks.coserver.dRSA;
import com.klapeks.mlpd.bukkit.BukkitPluginList;

public class MLPD {
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
				} 
			} catch (Throwable t) {
				t.printStackTrace();
			}
			try {//Trying load and enable plugin
				org.bukkit.plugin.Plugin pl = org.bukkit.Bukkit.getServer().getPluginManager().loadPlugin(
						new File("plugins_MLPD" + File.separator + folder + File.separator + plugin + ".jar"));
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
			
			File file = new File("plugins_MLPD" + File.separator + folder + File.separator + plugin + ".jar");
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

		
		public boolean checkNewVersion(String plugin) {
			File file = new File("plugins_MLPD" + File.separator + folder + File.separator + plugin + ".jar");
			return lFunctions.toLong(send("getpluginlastmodified", folder, plugin)) - file.lastModified() >= 1000;
//			return (send("checkpluginnewversion", folder, plugin, (file.exists() ? file.lastModified() : -1)+"")+"").equals("true");
		}
		
		public boolean has(String plugin) {
			return (send("checkplugin", folder, plugin)+"").equals("true");
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
