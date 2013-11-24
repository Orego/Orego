package orego.cluster;

import java.io.File;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileWriter;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/** 
 * This is a utility class that handles things like file paths and security
 * needed to start RMI successfully.
 */
public class RMIStartup {
	/** This simple wrapper around LocateRegistry can be used for dependency injection */
	public static class RegistryFactory {
		public Registry getRegistry() throws RemoteException {
			return LocateRegistry.getRegistry();
		}
		
		public Registry getRegistry(String host) throws RemoteException {
			return LocateRegistry.getRegistry(host);
		}
	}
	
	/** The location of the security policy that is used for RMI */
	public static final String SECURITY_POLICY_FILE = "/allow_all.policy";	
	
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
		System.setProperty("java.security.policy", getLocationOfPolicyFile(policyFileName, classToServe));
		
		if(System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
	}
	
	/** Constructs the appropriate policy file*/
	private static String generatePolicyFile(Class<?> classToServe) {
		return String.format(
				"grant codeBase \"%s\" {\n" + 
				"    permission java.security.AllPermission;\n" + 
				"};", classToServe.getProtectionDomain().getCodeSource().getLocation().toString());
	}
	/** Copies the security policy file from the jar
	 *  into a temporary directory and returns the location of the copy. 
	 */
    private static String getLocationOfPolicyFile(String policyFileName, Class<?> classToServe) {
        try {
            File tempFile = File.createTempFile("rmi-base", ".policy");
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(generatePolicyFile(classToServe));
            writer.flush();
            writer.close();
            
            tempFile.deleteOnExit();
            return tempFile.getAbsolutePath();
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
