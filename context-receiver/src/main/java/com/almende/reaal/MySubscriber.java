package com.almende.reaal;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.owl.MergedRestriction;

/**
 * @author ludo
 * 
 */
public class MySubscriber extends ContextSubscriber {
	
	protected MySubscriber(ModuleContext connectingModule) {
		super(connectingModule, getPermanentSubscriptions());
	}
	
	private static ContextEventPattern[] getPermanentSubscriptions() {
		ContextEventPattern myContextEventPattern = new ContextEventPattern();
		myContextEventPattern.addRestriction(MergedRestriction
				.getAllValuesRestriction(ContextEvent.PROP_RDF_SUBJECT,
						"http://ontology.universaal.org/Measurement.owl#Measurement"));
		return new ContextEventPattern[]{myContextEventPattern};
	}
	
	@Override
	public void communicationChannelBroken() {
		System.out.println("Channel Broken!");
	}
	
	@Override
	public void handleContextEvent(ContextEvent event) {
		Double other = (Double) event.getRDFObject();
		System.err.println("Received measurement value:"+other);
	};
}
