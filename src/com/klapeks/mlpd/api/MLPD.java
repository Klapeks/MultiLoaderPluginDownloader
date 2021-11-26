package com.klapeks.mlpd.api;

import java.io.File;
import java.nio.file.Files;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.klapeks.coserver.aConfig;
import com.klapeks.coserver.dCoserver;
import com.klapeks.coserver.dFunctions;
import com.klapeks.funcs.dRSA;
import com.klapeks.mlpd.bukkit.BukkitPluginList;

public class MLPD {
	private static PluginFolder nullFolder = new PluginFolder(null) {
		public PluginFolder download(String plugin) {
			lFunctions.log("§cPlugin §6'{plugin}'§c can't be downloaded because folder wasn't found!!".replace("{plugin}", plugin));
			lFunctions.errorDisable();
			return this;
		};
		public boolean has(String plugin) {return false;};
		public boolean isNullFolder() { return true; }
	};
	public static PluginFolder from(String folder) {
		if (hasFolder(folder)) return new PluginFolder(folder);
		lFunctions.log("§cFolder §6'{folder}'§c wasn't found!".replace("{folder}", folder));
		return nullFolder;
	}
	
	public static boolean hasFolder(String folder) {
		try {
			return (send("isexists", folder)+"").equals("true");
		} catch (Throwable t) {
			if (aConfig.useDebugMsg) t.printStackTrace();
			lFunctions.log("§cFolders: Bungeecord is probably disabled now.");
			lFunctions.errorDisable();
			return true;
		}
	}

	static uArrayMap<String, String> pluginenabled = new uArrayMap<>();
	public static void _addEnabled(String folder, String plugin) {
		pluginenabled.addIn(folder, plugin);
	}
	public static boolean _isEnabled(String folder, String plugin) {
		return pluginenabled.containsKey(folder) && pluginenabled.get(folder).contains(plugin);
	}
	
	public static interface IPF<T> {
		T using(String plugin);
		T download(String plugin);
		boolean has(String plugin);
		boolean local_has(String plugin);
		boolean hasnewversion(String plugin);
		
		T download_config(String folder_with_configs, String config, @OftenNull String subfolder);
		boolean has_config(String folder_with_configs, String config, @OftenNull String subfolder);
		boolean local_has_config(String folder_with_configs, String config);
		boolean hasnewversion_config(String folder_with_configs, String config, @OftenNull String subfolder);
//////		String[] listoffwc(String folder_with_configs, int frac);
		
		T using_cfgs(String folder_with_configs, @OftenNull String subfolder);
		T download_cfgs(String folder_with_configs, @OftenNull String subfolder);
		boolean has_cfgs(String folder_with_configs, @OftenNull String subfolder);
		boolean local_has_cfgs(String folder_with_configs);
		
		
		default void async_using(String plugin) {
			dFunctions.scheduleAsync(new Runnable() {
				@Override public void run() { using(plugin); }
			}, 0);
		}
		default void async_download(String plugin) {
			dFunctions.scheduleAsync(new Runnable() {
				@Override public void run() { download(plugin); }
			}, 0);
		}
		
		default void async_using_cfgs(String folder_with_configs, @OftenNull String subfolder) {
			dFunctions.scheduleAsync(new Runnable() {
				@Override public void run() { using_cfgs(folder_with_configs, subfolder); }
			}, 0);
		}
		default void async_download_cfgs(String folder_with_configs, @OftenNull String subfolder) {
			dFunctions.scheduleAsync(new Runnable() {
				@Override public void run() { download_cfgs(folder_with_configs, subfolder); }
			}, 0);
		}
		
	}
	
	public static class PluginFolder implements IPF<PluginFolder> {
		
		String folder;
		public PluginFolder(String folder) {
			if (folder==null) return;
			this.folder = folder.replace(File.separator, "/");
		}
		@Override
		public PluginFolder using(String plugin) {
			if (plugin.endsWith(".jar")) plugin = plugin.substring(0, plugin.length()-4);
			if (_isEnabled(folder, plugin)) {
				lFunctions.log("§6Plugin '{plugin}' is already enabled and will not be enabled again".replace("{plugin}", plugin));
				return this;
			}
			update(plugin);
			return enable(plugin);
		}
		public PluginFolder enable(String plugin) {
			try {//Trying load and enable plugin
				if (!local_has(plugin)) download(plugin);
				org.bukkit.plugin.Plugin pl = org.bukkit.Bukkit.getServer().getPluginManager().loadPlugin(file(folder, plugin+".jar"));
				pl.onLoad();
				if (!BukkitPluginList.isStartup) {
					org.bukkit.Bukkit.getServer().getPluginManager().enablePlugin(pl);
					_addEnabled(folder, plugin);
				} else {
					BukkitPluginList.needsToBeEnabled.put(folder+",,,"+plugin, pl);
				}
			} catch (Throwable t) {
				lFunctions.errorDisable();
				t.printStackTrace();
			}
			return this;
		}
		public PluginFolder disable(String plugin) {
			return disable(plugin, "plugins_MLPD");
		}
		public PluginFolder disable(String plugin, String path) {
			if (!plugin.endsWith(".jar")) plugin += ".jar";
			File e = null;
			if (path.equals("plugins_MLPD")) {
				e = file(folder, plugin);
			} else {
				e = new File(path + File.separator + plugin);
			}
			String plname = lFunctions.getPluginName(e);
			Plugin pl = Bukkit.getPluginManager().getPlugin(plugin);
			if (pl==null) {
				lFunctions.log("§cPlugin §6" + plname + "§e(" + plugin + ")§c wasn't found or was disabled before this plugin");
				return this;
			}
			lFunctions.log("§6Disabling §6" + plname + "§e(" + plugin + ")§c...");
			Bukkit.getServer().getPluginManager().disablePlugin(pl);
			lFunctions.log("§6Plugin " + plugin + "(" + plname + ")" + "was disabled");
			return this;
		}
		
		public PluginFolder update(String plugin) {
			if (plugin.endsWith(".jar")) plugin = plugin.substring(0, plugin.length()-4);
			try {
				if (has(plugin) && hasnewversion(plugin)) {
					download(plugin);
				} else {
					lFunctions.log("§aThe latest version of '{plugin}' is already installed".replace("{plugin}", plugin));
				}
			} catch (Throwable t) {
				t.printStackTrace();
				lFunctions.log("§cSome error");
			}
			return this;
		}
		
		private String plug(String plugin) {
			if (plugin.endsWith(".jar")) plugin = plugin.substring(0, plugin.length()-4);
			return folder + "/" + plugin.replace(File.separator, "/") + ".jar";
		}
		private String fd(String folder_with_configs, @OftenNull String subfolder) {
			return fixPath(folder, folder_with_configs, subfolder).replace(File.separator, "/");
		}
		private String cf(String folder_with_configs, String config, @OftenNull String subfolder) {
			return fixPath(folder, folder_with_configs, subfolder, config).replace(File.separator, "/");
		}
		
		@Override
		public PluginFolder download(String plugin) {
			if (plugin.endsWith(".jar")) plugin = plugin.substring(0, plugin.length()-4);
			if (!has(plugin)) {
				lFunctions.log("§cPlugin §6'{plugin}'§c wasn't found!".replace("{plugin}", plugin));
				lFunctions.errorDisable();
				return this;
			}
			//Prepare to plugin downloading
			String secretPsw = sendLarge("startfiledownload", plug(plugin));
			int size = lFunctions.toInt(secretPsw.split(" ")[0]);
			if (secretPsw.equals("-1") || size==-1) {
				closeLarge();
				lFunctions.log("§cPlugin §6'{plugin}'§c was found but no?".replace("{plugin}", plugin));
				lFunctions.errorDisable();
				return this;
			}
			secretPsw = secretPsw.replaceFirst(secretPsw.split(" ")[0]+" ", "");

			File file = file(folder, plugin+".jar");
			doDownload(file, secretPsw, size);
			file.setLastModified(lFunctions.toLong(send("lastmodified", plug(plugin))));
			send("clearcashdata", secretPsw);
			closeLarge();
			return this;
		}
		
		@Override
		public boolean has(String plugin) {
			if (plugin.endsWith(".jar")) plugin = plugin.substring(0, plugin.length()-4);
			try {
				return (send("isexists", plug(plugin))+"").equals("true");
			} catch (Throwable t) {
				if (aConfig.useDebugMsg) t.printStackTrace();
				lFunctions.log("§cPlugins: Bungeecord is probably disabled now.");
				lFunctions.errorDisable();
				return false;
			}
		}
		
		@Override
		public boolean local_has(String plugin) {
			if (!plugin.endsWith(".jar")) plugin+=".jar";
			return file(folder, plugin).exists();
		}
		
		@Override
		public boolean hasnewversion(String plugin) {
			if (plugin.endsWith(".jar")) plugin = plugin.substring(0, plugin.length()-4);
			if (!local_has(plugin)) return true;
			return lFunctions.toLong(send("lastmodified", plug(plugin))) - file(folder, plugin+".jar").lastModified() >= 1000;
		}
		
		@Override
		public boolean has_config(String folder_with_configs, String config, @OftenNull String subfolder) {
			try {
				return (send("isexists", cf(folder_with_configs, config, subfolder))+"").equals("true");
			} catch (Throwable t) {
				if (aConfig.useDebugMsg) t.printStackTrace();
				lFunctions.log("§cConfigs: Bungeecord is probably disabled now.");
				lFunctions.errorDisable();
				return false;
			}
		}

		@Override
		public boolean local_has_config(String folder_with_configs, String config) {
			return file(folder, folder_with_configs, config).exists();
		}
		
		public boolean local_has_config(String folder_with_configs, String config, @OftenNull String redirect) {
			if (redirect==null) return local_has_config(folder_with_configs, config);
			return new File(fixPath(redirect, folder_with_configs, config)).exists();
		}

		@Override
		public boolean hasnewversion_config(String folder_with_configs, String config, @OftenNull String subfolder) {
			if (!local_has_config(folder_with_configs, config)) return true;
			return lFunctions.toLong(send("lastmodified", cf(folder_with_configs, config, subfolder))) - file(folder, folder_with_configs, config).lastModified() >= 1000;
		}
		
		public boolean hasnewversion_config(String folder_with_configs, String config, @OftenNull String subfolder, @OftenNull String redirect) {
			if (redirect==null) return hasnewversion_config(folder_with_configs, config, subfolder);
			if (!local_has_config(folder_with_configs, config, redirect)) return true;
			return lFunctions.toLong(send("lastmodified", cf(folder_with_configs, config, subfolder))) - 
					new File(fixPath(redirect, folder_with_configs, config)).lastModified() >= 1000;
		}
		@Override
		public PluginFolder download_config(String folder_with_configs, String config, @OftenNull String subfolder) {
			return download_config(folder_with_configs, config, subfolder, null);
		}
		@Override
		public PluginFolder download_cfgs(String folder_with_configs, @OftenNull String subfolder) {
			return download_cfgs(folder_with_configs, subfolder, null);
		}
		@Override
		public PluginFolder using_cfgs(String folder_with_configs, @OftenNull String subfolder) {
			return using_cfgs(folder_with_configs, subfolder, null);
		}
		public PluginFolder download_config(String folder_with_configs, String config, @OftenNull String subfolder, @OftenNull String redirect) {
			if (!has_config(folder_with_configs, config, subfolder)) {
				lFunctions.log("§cConfig §6'{config}'§c in §6{folder_with_configs}§c wasn't found!".replace("{config}", config).replace("{folder_with_configs}", 
						folder_with_configs + (subfolder==null?"":("/"+subfolder))));
				return this;
			}
			String secretPsw = sendLarge("startfiledownload", cf(folder_with_configs, config, subfolder));
			int size = lFunctions.toInt(secretPsw.split(" ")[0]);
			if (secretPsw.equals("-1") || size==-1) {
				closeLarge();
				lFunctions.log("§cConfig §6'{config}'§c was found but no?".replace("{config}", folder_with_configs+"/"+config));
				lFunctions.errorDisable();
				return this;
			}
			secretPsw = secretPsw.replaceFirst(secretPsw.split(" ")[0]+" ", "");
			File file = redirect==null ? file(folder, folder_with_configs, config)
					: new File(fixPath(redirect, folder_with_configs, config));
			doDownload(file, secretPsw, size);
			file.setLastModified(lFunctions.toLong(send("lastmodified", cf(folder_with_configs, config, subfolder))));
			send("clearcashdata", secretPsw);
			return this;
		}
		
		public PluginFolder download_cfgs(String folder_with_configs, @OftenNull String subfolder, @OftenNull String redirect) {
			if (subfolder==null) {
				//Trying check _default folder
				if (has_cfgs(folder_with_configs, "_default")) return download_cfgs(folder_with_configs, "_default", redirect);
			}
			if (!has_cfgs(folder_with_configs, subfolder)) {
				lFunctions.log("§cFolder with configs §6'{folder_with_configs}'§c wasn't found!".replace("{folder_with_configs}", folder_with_configs + (subfolder==null?"":("/"+subfolder))));
				if (subfolder!=null && !subfolder.equals("_default")) {
					return download_cfgs(folder_with_configs, "_default", redirect);
				}
				return this;
			}
			String[] path$file = (send("getlistoffiles", fd(folder_with_configs, subfolder))+"").split(",,,,,");
			for (String config : path$file) {
				download_config(folder_with_configs, config, subfolder, redirect);
			}
			closeLarge();
			return this;
		}
		public PluginFolder using_cfgs(String folder_with_configs, @OftenNull String subfolder, @OftenNull String redirect) {
			if (subfolder==null) {
				//Trying check _default folder
				if (has_cfgs(folder_with_configs, "_default")) return using_cfgs(folder_with_configs, "_default", redirect);
			}
			if (!has_cfgs(folder_with_configs, subfolder)) {
				lFunctions.log("§cFolder with configs §6'{folder_with_configs}'§c wasn't found!".replace("{folder_with_configs}", folder_with_configs + (subfolder==null?"":("/"+subfolder))));
				if (subfolder!=null && !subfolder.equals("_default")) {
					return using_cfgs(folder_with_configs, "_default", redirect);
				}
				return this;
			}
			try {
				String[] path$file = (send("getlistoffiles", fd(folder_with_configs, subfolder))+"").split(",,,,,");
				for (String config : path$file) {
					if (hasnewversion_config(folder_with_configs, config, subfolder, redirect)) {
						download_config(folder_with_configs, config, subfolder, redirect);
					}
				}
				closeLarge();
			} catch (Throwable t) {
				if (aConfig.useDebugMsg) t.printStackTrace();
				lFunctions.log("§cConfigurations: Bungeecord is probably disabled now.");
				lFunctions.errorDisable();
			}
			return this;
		}
		
		@Override
		public boolean has_cfgs(String folder_with_configs, @OftenNull String subfolder) {
			try {
				return (send("isexists", fd(folder_with_configs, subfolder))+"").equals("true");
			} catch (Throwable t) {
				if (aConfig.useDebugMsg) t.printStackTrace();
				lFunctions.log("§cConfigurations: Bungeecord is probably disabled now.");
				lFunctions.errorDisable();
				return false;
			}
		}
		@Override
		public boolean local_has_cfgs(String folder_with_configs) {
			return file(folder, folder_with_configs).exists();
		}
		public boolean local_has_cfgs(String folder_with_configs, String redirect) {
			if (redirect==null) return local_has_cfgs(folder_with_configs);
			return new File(fixPath(redirect, folder_with_configs)).exists();
		}
		
		

		private static File file(String... args) {
			return new File("plugins_MLPD" + File.separator + fixPath(args));
		}
		
		private static String fixPath(String... args) {
			String path = "";
			for (String s : args) {
				if (s==null||s.equals("null")) continue;
				if (!path.equals("")) path += File.separator;
				path += s.replace("/", File.separator);
			}
			return path;
		}
		
		private void doDownload(File in, String secretPsw, int size) {
			try {//Downloading plugin
				if (in.exists()) in.delete();
				in.getParentFile().mkdirs();
				String g = "";
				int old_proc = 0, new_one = 0;
				
				for (int i = 0; i < size; i++) {
					g = sendLarge("downloadstage", secretPsw, i+"");
					if (g==null || g.equals("null")) {
						lFunctions.log("§cSomething went wrong. On iterator: " + i);
						return;
					}
					Files.write(in.toPath(), dRSA.base64_decode_byte(g), 
							java.nio.file.StandardOpenOption.CREATE,
							java.nio.file.StandardOpenOption.WRITE,
							java.nio.file.StandardOpenOption.APPEND);
					new_one = i*100 / size;
					if (new_one - old_proc >= 10 || i==0) {
						lFunctions.log("§6Downloading '{file}', iterator {i}... ".replace("{file}", in+"").replace("{i}", i+"") + new_one);
						old_proc = new_one;
					}
				}
				lFunctions.log("§6Plugin was downloaded");
			} catch (Throwable t) {
				lFunctions.log("§cError with plugin downloading");
				in.delete();
				t.printStackTrace();
			}
		}

		public boolean isNullFolder() { return false; }
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
