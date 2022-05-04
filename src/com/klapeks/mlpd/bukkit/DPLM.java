package com.klapeks.mlpd.bukkit;

import com.klapeks.coserver.dFunctions;
import com.klapeks.mlpd.api.OftenNull;
import com.klapeks.mlpd.api.uArrayMap;

public class DPLM {

	public static uArrayMap<String, String> pluginenabled = new uArrayMap<>();
	public static uArrayMap<String, String> pluginloading = new uArrayMap<>();
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
			});
		}
		default void async_download(String plugin) {
			dFunctions.scheduleAsync(new Runnable() {
				@Override public void run() { download(plugin); }
			});
		}
		
		default void async_using_cfgs(String folder_with_configs, @OftenNull String subfolder) {
			dFunctions.scheduleAsync(new Runnable() {
				@Override public void run() { using_cfgs(folder_with_configs, subfolder); }
			});
		}
		default void async_download_cfgs(String folder_with_configs, @OftenNull String subfolder) {
			dFunctions.scheduleAsync(new Runnable() {
				@Override public void run() { download_cfgs(folder_with_configs, subfolder); }
			});
		}
		
	}
}
