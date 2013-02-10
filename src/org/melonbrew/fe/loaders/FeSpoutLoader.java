package org.melonbrew.fe.loaders;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.listeners.FeSpoutPlayerListener;

import com.niccholaspage.Metro.base.loader.loaders.SpoutLoader;

public class FeSpoutLoader extends SpoutLoader {
	public void onEnable(){
		Fe fe = new Fe();
		
		setPlugin(fe);
		
		super.onEnable();
		
		new FeSpoutPlayerListener(this, fe);
	}
}
