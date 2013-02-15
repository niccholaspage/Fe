package org.melonbrew.fe.loaders;

import java.io.IOException;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.melonbrew.fe.Economy_Fe;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.listeners.FeBukkitPlayerListener;
import org.melonbrew.fe.loaders.BukkitMetrics.Graph;
import org.melonbrew.fe.loaders.BukkitMetrics.Plotter;

import com.niccholaspage.Metro.base.loader.loaders.BukkitLoader;

public class FeBukkitLoader extends BukkitLoader {
	private Fe fe;
	
	public void onEnable(){
		fe = new Fe();
		
		setPlugin(fe);
		
		super.onEnable();
		
		if (!isEnabled()){
			return;
		}
		
		new FeBukkitPlayerListener(this, fe);
		
		setupVault();
		
		loadMetrics();
	}
	
	public void onDisable(){
		super.onDisable();

		getServer().getServicesManager().unregisterAll(this);
	}
	
	private void loadMetrics(){
		try {
			BukkitMetrics metrics = new BukkitMetrics(this);
			
			Graph databaseGraph = metrics.createGraph("Database Engine");
			
			databaseGraph.addPlotter(new Plotter(fe.getFeDatabase().getName()){
                public int getValue(){
                    return 1;
                }
            });
			
			Graph defaultHoldings = metrics.createGraph("Default Holdings");
			
			defaultHoldings.addPlotter(new Plotter(fe.getAPI().getDefaultHoldings() + ""){
		        public int getValue(){
		            return 1;
		        }
		    });
			
			Graph maxHoldings = metrics.createGraph("Max Holdings");
			
			String maxHolding = fe.getAPI().getMaxHoldings() + "";
			
			if (fe.getAPI().getMaxHoldings() == -1){
				maxHolding = "Unlimited";
			}
			
			maxHoldings.addPlotter(new Plotter(maxHolding){
		        public int getValue(){
		            return 1;
		        }
		    });
            
            metrics.start();
		} catch (IOException e){
			
		}
	}
	
	private void setupVault(){
		Plugin vault = getServer().getPluginManager().getPlugin("Vault");
		
		if (vault == null){
			return;
		}
		
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		
		if (economyProvider != null){
			getServer().getServicesManager().unregister(economyProvider.getProvider());
		}
		
		getServer().getServicesManager().register(Economy.class, new Economy_Fe(fe, this), this, ServicePriority.Highest);
	}
}
