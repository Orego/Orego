package orego.experiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
	
	public Configuration() {
		
	}
	
	public void load() throws IOException {
		properties = new Properties();
		
		properties.load(new FileInputStream(CONFIG_FILE));
		
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
		return getGamesPerCondition() / (2 * getHosts().length * getGamesPerHost());
	}
	
	public int getParallelGamesPerColor() {
		// we run the main ClusterPlayer on only one host so we do 
		// not distribute among the hosts (hence 1 instead of getHosts().length)
		return getGamesPerCondition() / (2 * 1 * getGamesPerHost());
	}
	
	public String[] getRunningConditions() {
		
	}
	
	public int getGamesPerCondition() {
		return Integer.valueOf(properties.getProperty("orego.gamespercond"));
	}
	
	public String[] getHosts() {
		Set<String> keys = (Set<String>)properties.keySet();
		
		for
	}
	
	public File getResultsDirectory() {
		return new File(properties.getProperty("experiment.results"));
	}

}
