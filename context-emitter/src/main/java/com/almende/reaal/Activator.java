/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.reaal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;

/**
 * The Class Activator.
 */
public class Activator implements BundleActivator {
	
	private Collector				collector		= null;
	
	public void start(BundleContext bcontext) throws Exception {
		ModuleContext context = uAALBundleContainer.THE_CONTAINER
				.registerModule(new Object[] { bcontext });
		collector = new Collector(context);
	}
	
	public void stop(BundleContext arg0) throws Exception {
		collector.stop();
	}

}
