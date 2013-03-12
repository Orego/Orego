package orego.experiment;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ConfigurationTest {

	private Configuration config;
	
	@Before
	public void setup() throws Exception {
		config = new Configuration();
	}
	
	@Test
	public void testShouldGetProperClasspath() {
		assertEquals("/Users/samstewart/Documents/workspace/Orego/bin", config.getOregoClasspath().toString());
	}
	
	
	@Test
	public void testShouldGetHosts() {
		List<String> hosts = new ArrayList<String>();
		hosts.add("fido.bw01.lclark.edu");
		hosts.add("n001.bw01.lclark.edu");
		hosts.add("n002.bw01.lclark.edu");
		hosts.add("n003.bw01.lclark.edu");
		hosts.add("n004.bw01.lclark.edu");
		
		List<String> loadedHosts = config.getHosts();
		
		// not enforcing any p
		for (int i = 0; i < hosts.size(); i++) {
			assertTrue(loadedHosts.contains(hosts.get(i)));
		}
	}
	
	@Test
	public void testShouldGetGamesPerColor() {
		int properNonParallel = config.getGamesPerCondition() / (2 * config.getHosts().size() * config.getGamesPerHost());
		
		int properParallel = config.getGamesPerCondition() / (2 * 1 * config.getGamesPerHost());
		
		assertEquals(properNonParallel, config.getGamesPerColor());
		assertEquals(properParallel, config.getParallelGamesPerColor());
	}
	
	@Test
	public void testShouldGetConditions() {
		List<String> conds = new ArrayList<String>();
		
		conds.add("threads=2 msec=2000 book=FusekiBook player=ClusterPlayer");
		conds.add("threads=2 msec=4000 book=FusekiBook player=ClusterPlayer");
		conds.add("threads=2 msec=8000 book=FusekiBook player=ClusterPlayer");
				
		List<String> loadedConds = config.getRunningConditions();
				
		// not enforcing any p
		for (int i = 0; i < conds.size(); i++) {
			assertTrue(loadedConds.contains(conds.get(i)));
		}		
	}
	@Test
	public void testShouldGetGNUPath() {
		assertEquals("/usr/local/bin/gnugo --boardsize 19 --mode gtp --quiet --chinese-rules --capture-all-dead --positional-superko --komi 7.5", config.getGnuGoCommand());
	}
	@Test
	public void testShouldHaveNumberOfGamesPerHost() {
		assertEquals(6, config.getGamesPerHost());
	}
	@Test
	public void testShouldGetProperResultsDirectory() {
		assertEquals("/Users/samstewart/Downloads/orego_results", config.getResultsDirectory().toString());
	}
	
	@Test
	public void testShouldGetNumberOfGamesPerCondition() {
		assertEquals(600, config.getGamesPerCondition());
	}

}
