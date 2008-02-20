package org.woped.server;


import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.woped.core.Constants;
import org.woped.core.utilities.LoggerManager;
import org.woped.server.configuration.PropertyLoader;


/**
 * Singleton Class to handle the Client RMI
 * @author C. Kr�ger
 *
 */
public class ServerLoader {

	 
	
	static public IServer instance = null;
	
	/**
	 * returns the Instance to the remote Service
	 * @return
	 */
	static public IServer getInstance() {
		if (null == instance) {
			try {
				instance = (IServer)Naming.lookup(PropertyLoader.getProperty("rmiURL"));
			} catch (MalformedURLException e) {
				LoggerManager.fatal(Constants.CORE_LOGGER, e.getMessage());
			} catch (RemoteException e) {
				LoggerManager.fatal(Constants.CORE_LOGGER, e.getMessage());
			} catch (NotBoundException e) {
				LoggerManager.fatal(Constants.CORE_LOGGER, e.getMessage());
			} 
		}
		return instance;
	}
	
	
	
}
