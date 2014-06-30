package edu.lclark.orego.experiment;

import java.io.File;

import edu.lclark.orego.ui.Orego;

/** Defines a constant used to find property files. */
public class PropertyPaths {

	/**
	 * Directory containing, e.g., bin (which contains .class files) and config
	 * (which contains .properties files).
	 */
	public static final String OREGO_ROOT = Orego.class.getProtectionDomain()
			.getCodeSource().getLocation().getFile()
			+ ".." + File.separator;

	private PropertyPaths() {
		// Prevents instantiation
	}

}
