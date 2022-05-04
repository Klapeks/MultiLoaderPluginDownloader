package com.klapeks.mlpd.bungee;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import com.klapeks.coserver.aConfig;
import com.klapeks.coserver.dFunctions;
import com.klapeks.coserver.plugin.bungee.SuperCoServer;
import com.klapeks.funcs.dRSA;
import com.klapeks.mlpd.api.lFunctions;

public class MLPDServer {
	static File mlpd_folder;
	static final String fs = File.separator;
	static final int iqii = 500;
	static HashMap<String, List<String>> filedata = new HashMap<>();
	
	static void __init__() {
		Function<String, String> minihandler = request ->{
			String[] args = doArgs(request);
			switch (args[0]) {
			
			case "isexists": {
				return file(args[1]).exists()+"";
			}
			case "lastmodified": {
				return file(args[1]).lastModified()+"";
			}
			case "getlistoffiles": {
				File file = file(args[1]);
				if (!file.exists()) return "null";
				if (!file.isDirectory()) return "null";
				
				String str = ""; int g = (file+"").length()+1;
				List<File> files = listOfFiles(file);
				for (File f : files) {
					str += ",,,,," + (f+"").substring(g).replace(fs, "/");
				}
				if (str.startsWith(",,,,,")) str = str.replaceFirst(",,,,,", "");
				return str;
				
			}
			
			case "startfiledownload":{
				try {
					File file = file(args[1]);
					dFunctions.debug("§eServer tries download file: " + file);
					if (!file.exists()) return "-1";
					byte[] bytes = Files.readAllBytes(file.toPath());
					dFunctions.debug("§6Encoding {file}... ".replace("{file}", file+"") + bytes.length);
					
					List<String> cash = new ArrayList<>();
					for (int a = 0; a <= bytes.length; a = a + iqii) {
						byte[] na = Arrays.copyOfRange(bytes, a, (a+iqii) > bytes.length ? bytes.length : (a + iqii));
						cash.add(dRSA.base64_encode_byte(na));
					}
					String randpsw = dRSA.generateSecretKey(3);
					filedata.put(randpsw, cash);
					return cash.size() + " " + randpsw;
				} catch (Throwable e) {
					e.printStackTrace();
					return "-1";
				}
			}
			case "downloadstage": {
				String secretPsw = args[1];
				if (!filedata.containsKey(secretPsw)) return "null";
				try {
					String a = filedata.get(secretPsw).get(dFunctions.toInt(args[2]));
					if (a==null) throw new RuntimeException("a is null :(");
					return a;
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return "null";
			}
			case "clearcashdata": {
				String secretPsw = args[1];
				filedata.remove(secretPsw);
				return "ok";
			}
			
			default: {
				lFunctions.log("Unknown request: " + Arrays.toString(args));
				break;
			}
			}
			return "404error";
		};
		if (aConfig.useSecurity) {
			SuperCoServer.BungeeCoserv.addSecurityHandler("multiloaderplugindownloader", minihandler);
		} else {
			SuperCoServer.BungeeCoserv.addHandler("multiloaderplugindownloader", minihandler);
		}
	}
	
	private static File file(String path) {
		return new File(fixPath("MLPD_plugins", path));
	}
	
	private static String fixPath(String... args) {
		String path = "";
		for (String s : args) {
			if (!path.equals("")) path += fs;
			path += s.replace("/", fs);
		}
		return path;
	}
	
	
	private static List<File> listOfFiles(File folder) {
		List<File> f = new ArrayList<>();
		listOfFiles(f, folder);
		return f;
	}
	private static void listOfFiles(List<File> f, File folder) {
		for (File fl : folder.listFiles()) {
			if (fl.isDirectory()) {
				listOfFiles(f, fl);
				continue;
			}
			f.add(fl);
		}
	}
	
	private static String[] doArgs(String req) {
		String[] ss = req.split(" ");
		for (int i = 0; i < ss.length; i++) {
			try { 
				ss[i] = dRSA.base64_decode(ss[i]);
			} 
			catch (Throwable r) {}
		}
		return ss;
	}
}
