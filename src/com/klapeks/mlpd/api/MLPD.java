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
		return (send("isexists", folder)+"").equals("true");
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
//		String[] listoffwc(String folder_with_configs, int frac);
		
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
			if (_isEnabled(folder, plugin)) {
				lFunctions.log("§6Plugin '{plugin}' is already enabled and will not be enabled again".replace("{plugin}", plugin));
				return this;
			}
			try {
				if (has(plugin) && hasnewversion(plugin)) {
					download(plugin);
				} else {
					lFunctions.log("§aThe latest version of '{plugin}' is already installed".replace("{plugin}", plugin));
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
			try {//Trying load and enable plugin
				org.bukkit.plugin.Plugin pl = org.bukkit.Bukkit.getServer().getPluginManager().loadPlugin(file(folder, plugin+".jar"));
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
		
		private String plug(String plugin) {
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
			if (!has(plugin)) {
				lFunctions.log("§cPlugin §6'{plugin}'§c wasn't found!".replace("{plugin}", plugin));
				return this;
			}
			//Prepare to plugin downloading
			String secretPsw = sendLarge("startfiledownload", plug(plugin));
			int size = lFunctions.toInt(secretPsw.split(" ")[0]);
			if (secretPsw.equals("-1") || size==-1) {
				closeLarge();
				lFunctions.log("§cPlugin §6'{plugin}'§c was found but no?".replace("{plugin}", plugin));
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
			return (send("isexists", plug(plugin))+"").equals("true");
		}
		
		@Override
		public boolean local_has(String plugin) {
			return file(folder, plugin+".jar").exists();
		}
		
		@Override
		public boolean hasnewversion(String plugin) {
			if (!local_has(plugin)) return true;
			return lFunctions.toLong(send("lastmodified", plug(plugin))) - file(folder, plugin+".jar").lastModified() >= 1000;
		}
		
		
		
		@Override
		public PluginFolder download_config(String folder_with_configs, String config, @OftenNull String subfolder) {
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
				return this;
			}
			secretPsw = secretPsw.replaceFirst(secretPsw.split(" ")[0]+" ", "");
			File file = file(folder, folder_with_configs, config);
			doDownload(file, secretPsw, size);
			file.setLastModified(lFunctions.toLong(send("lastmodified", cf(folder_with_configs, config, subfolder))));
			send("clearcashdata", secretPsw);
			closeLarge();
			return this;
		}
		
		@Override
		public boolean has_config(String folder_with_configs, String config, @OftenNull String subfolder) {
			return (send("isexists", cf(folder_with_configs, config, subfolder))+"").equals("true");
		}
		
		@Override
		public boolean local_has_config(String folder_with_configs, String config) {
			return file(folder, folder_with_configs, config).exists();
		}
		
		@Override
		public boolean hasnewversion_config(String folder_with_configs, String config, @OftenNull String subfolder) {
			if (!local_has_config(folder_with_configs, config)) return true;
			return lFunctions.toLong(send("lastmodified", cf(folder_with_configs, config, subfolder))) - file(folder, folder_with_configs, config).lastModified() >= 1000;
		}
		
		@Override
		public PluginFolder using_cfgs(String folder_with_configs, @OftenNull String subfolder) {
			if (subfolder==null) {
				//Trying check _default folder
				if (has_cfgs(folder_with_configs, "_default")) return using_cfgs(folder_with_configs, "_default");
			}
			if (!has_cfgs(folder_with_configs, subfolder)) {
				lFunctions.log("§cFolder with configs §6'{folder_with_configs}'§c wasn't found!".replace("{folder_with_configs}", folder_with_configs + (subfolder==null?"":("/"+subfolder))));
				if (subfolder!=null && !subfolder.equals("_default")) {
					return using_cfgs(folder_with_configs, "_default");
				}
				return this;
			}
			String[] path$file = (send("getlistoffiles", fd(folder_with_configs, subfolder))+"").split(",,,,,");
			for (String config : path$file) {
				if (hasnewversion_config(folder_with_configs, config, subfolder)) {
					download_config(folder_with_configs, config, subfolder);
				}
			}
			return this;
		}
		
		@Override
		public PluginFolder download_cfgs(String folder_with_configs, @OftenNull String subfolder) {
			if (subfolder==null) {
				//Trying check _default folder
				if (has_cfgs(folder_with_configs, "_default")) return download_cfgs(folder_with_configs, "_default");
			}
			if (!has_cfgs(folder_with_configs, subfolder)) {
				lFunctions.log("§cFolder with configs §6'{folder_with_configs}'§c wasn't found!".replace("{folder_with_configs}", folder_with_configs + (subfolder==null?"":("/"+subfolder))));
				if (subfolder!=null && !subfolder.equals("_default")) {
					return download_cfgs(folder_with_configs, "_default");
				}
				return this;
			}
			String[] path$file = (send("getlistoffiles", fd(folder_with_configs, subfolder))+"").split(",,,,,");
			for (String config : path$file) {
				download_config(folder_with_configs, config, subfolder);
			}
			return this;
		}
		
		@Override
		public boolean has_cfgs(String folder_with_configs, @OftenNull String subfolder) {
			return (send("isexists", fd(folder_with_configs, subfolder))+"").equals("true");
		}
		@Override
		public boolean local_has_cfgs(String folder_with_configs) {
			return file(folder, folder_with_configs).exists();
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
