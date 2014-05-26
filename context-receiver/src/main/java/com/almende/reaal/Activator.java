/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.reaal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.context.ContextSubscriber;

/**
 * The Class Activator.
 */
public class Activator implements BundleActivator {
	private static final String VERSION="1";
	/**
	 * The osgi context.
	 */
	public static BundleContext				osgiContext	= null;
	
	/**
	 * The context.
	 */
	public static ModuleContext				context		= null;
	
	/**
	 * 
	 */
	public static ContextSubscriber			csubscriber	= null;
	
	public void start(BundleContext bcontext) throws Exception {
		Activator.osgiContext = bcontext;
		Activator.context = uAALBundleContainer.THE_CONTAINER
				.registerModule(new Object[] { bcontext });
		
		System.out.println("Starting receiver!"+VERSION);
		csubscriber = new MySubscriber(context);
		
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		csubscriber.close();
	}
}
