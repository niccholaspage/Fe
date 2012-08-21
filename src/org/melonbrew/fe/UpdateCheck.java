package org.melonbrew.fe;

import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UpdateCheck implements Runnable {
	private final Fe plugin;
	
	public UpdateCheck(Fe plugin){
		this.plugin = plugin;
	}
	
	public void run(){
		String pluginUrlString = "http://dev.bukkit.org/server-mods/fe-economy/files.rss";

		try {
			URL url = new URL(pluginUrlString);

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());

			doc.getDocumentElement().normalize();

			NodeList nodes = doc.getElementsByTagName("item");

			Node firstNode = nodes.item(0);

			if (firstNode.getNodeType() == 1){
				Element firstElement = (Element) firstNode;

				NodeList firstElementTagName = firstElement.getElementsByTagName("title");

				Element firstNameElement = (Element) firstElementTagName.item(0);

				NodeList firstNodes = firstNameElement.getChildNodes();

				String version = firstNodes.item(0).getNodeValue().trim();
				
				double latestVersion = plugin.versionToDouble(version);
				
				plugin.setLatestVersion(latestVersion);
				
				plugin.setLatestVersionString(version);
				
				if (!plugin.isUpdated()){
					plugin.log(Phrase.FE_OUTDATED, version);
				}
			}
		} catch (Exception e){

		}
	}
}
