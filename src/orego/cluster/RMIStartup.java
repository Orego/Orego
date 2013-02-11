package orego.cluster;

import java.io.File;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileWriter;

/** 
 * This is a utility class that handles things like file paths and security
 * needed to start RMI successfully.
 */
public class RMIStartup {
	/** 
	 * This method tells RMI where to find the codebase to serve and
	 * sets security policy from the given file.
	 */
	public static void configureRmi(Class<?> classToServe, String policyFileName) {
		// Tell RMI where to find the class files
		if(classToServe != null) {
			System.setProperty("java.rmi.server.codebase", 
				classToServe.getProtectionDomain().getCodeSource().getLocation().toString());
		}
		
		// Configure the security policy
		System.setProperty("java.security.policy", getLocationOfPolicyFile(policyFileName));
		
		if(System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
	}
	
	/** Copies the security policy file from the jar
	 *  into a temporary directory and returns the location of the copy. 
	 */
    private static String getLocationOfPolicyFile(String policyFileName) {
        try {
            File tempFile = File.createTempFile("rmi-base", ".policy");
            InputStream is = RMIStartup.class.getResourceAsStream(policyFileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            int read = 0;
            while((read = is.read()) != -1) {
                writer.write(read);
            }
            writer.close();
            tempFile.deleteOnExit();
            return tempFile.getAbsolutePath();
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
