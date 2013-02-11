package org.melonbrew.fe.loaders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.listeners.FeSpoutPlayerListener;
import org.spout.api.Spout;

import com.niccholaspage.Metro.base.loader.loaders.SpoutLoader;

public class FeSpoutLoader extends SpoutLoader {
	private Fe fe;

	public void onEnable(){
		File folder = new File(getDataFolder(), "libs");
		
		folder.mkdirs();

		download("http://dl.dropbox.com/u/7102145/libs/sqlite-jdbc.jar", new File(folder, "sqlite-jdbc.jar"));
		download("http://dl.dropbox.com/u/7102145/libs/mysql-connector-java.jar", new File(folder, "mysql-connector-java.jar"));
		
		fe = new Fe();

		setPlugin(fe);

		super.onEnable();
		
		if (!isEnabled()){
			return;
		}

		new FeSpoutPlayerListener(this, fe);
	}
	
	private void addURLs(URL[] urls){
		try {
			ClassLoader cl = ClassLoader.getSystemClassLoader();
			if (cl instanceof URLClassLoader) {
				URLClassLoader ul = (URLClassLoader) cl;
				
				Class<?>[] paraTypes = new Class[1];
				
				paraTypes[0] = URL.class;
				
				Method method = URLClassLoader.class.getDeclaredMethod("addURL", paraTypes);
				
				method.setAccessible(true);
				Object[] args = new Object[1];
				for (int i = 0; i < urls.length; i++) {
					args[0] = urls[i];
					method.invoke(ul, args);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void download(String libURL, File file){
		FileOutputStream fos = null;
		
		try {
			if (!file.exists()){
				file.createNewFile();
				
				Spout.getLogger().info("[Fe] Downloading ".concat(file.getName()));

				URL website = new URL(libURL);
				ReadableByteChannel rbc = Channels.newChannel(website.openStream());
				fos = new FileOutputStream(file);
				fos.getChannel().transferFrom(rbc, 0, 1 << 24);
			}
			
			URL url = file.toURI().toURL();
			
			addURLs(new URL[]{url});

		} catch (Exception e){
			e.printStackTrace();
		} finally {
			if (fos != null){
				try {
					fos.close();
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		}
	}
}
