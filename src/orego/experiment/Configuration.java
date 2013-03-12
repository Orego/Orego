package orego.experiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** Represents an experiment configuration
 * that reads a property file which is separate from the code base.
 * Useful reference: http://www.mkyong.com/java/java-properties-file-examples/
 * @author samstewart
 *
 */
public class Configuration {
	
	private static final String CONFIG_FILE = "configuration.properties";
	
	/** the actual loaded properties */
	private Properties properties;
	
	public Configuration() throws IOException {
		load();
	}
	
	public Configuration(String config_file) throws IOException {
		this.load(config_file);
	}
	
	private void load() throws IOException {
		properties = new Properties();
		
		properties.load(Configuration.class.getResourceAsStream(CONFIG_FILE));
		
	}
	
	private void load(String config_file) throws IOException {
		properties = new Properties();
		
		properties.load(new FileInputStream(config_file));
	}
	
	public File getOregoClasspath() {
		return new File(properties.getProperty("orego.classpath"));
	}
	
	public String getGnuGoCommand() {
		return properties.getProperty("gnugo.command");
	}
	
	public int getGamesPerHost() {
		return Integer.valueOf(properties.getProperty("orego.gamesperhost"));
	}
	
	public int getGamesPerColor() {
		return getGamesPerCondition() / (2 * getHosts().size() * getGamesPerHost());
	}
	
	/** Get the number of games to run per color for parallel*/
	public int getParallelGamesPerColor() {
		// we run the main ClusterPlayer on only one host so we do 
		// not distribute among the hosts (hence 1 instead of getHosts().length)
		return getGamesPerCondition() / (2 * 1 * getGamesPerHost());
	}
	
	/** Get a list of conditions we should run*/
	public List<String> getRunningConditions() {
		ArrayList<String> conditions = new ArrayList<String>();
		
		Set<Object> keys = properties.keySet();
		
		for (Object key : keys) {
			String keyStr = (String) key;
			
			if (keyStr.startsWith("orego.condition")) {
				conditions.add(properties.getProperty(keyStr));
			}
		}
		
		return conditions;
	}
	
	/** 
	 * Gets the total number of games per condition which will be used
	 * to calculate the number of games per color to run.
	 */
	public int getGamesPerCondition() {
		return Integer.valueOf(properties.getProperty("orego.gamespercondition"));
	}
	
	/** Gets the list of hosts to run experiments on*/
	public List<String> getHosts() {
		ArrayList<String> hosts = new ArrayList<String>();
		
		Set<Object> keys = properties.keySet();
		
		for (Object key : keys) {
			String keyStr = (String) key;
			
			if (keyStr.startsWith("orego.host")) {
				hosts.add(properties.getProperty(keyStr));
			}
		}
		
		
		return hosts;
	}
	
	public File getResultsDirectory() {
		return new File(properties.getProperty("experiment.results"));
	}

}
