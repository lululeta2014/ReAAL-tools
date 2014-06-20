/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.reaal;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;

import org.joda.time.DateTime;
import org.osgi.util.measurement.Measurement;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.MergedRestriction;

import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.scheduling.Scheduler;
import com.almende.eve.scheduling.SimpleSchedulerBuilder;
import com.almende.eve.scheduling.SimpleSchedulerConfig;
import com.almende.eve.transport.Receiver;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * The Class Collector.
 */
public class Collector implements Receiver {
	private ContextPublisher		cpublisher	= null;
	
	private Scheduler				scheduler	= new SimpleSchedulerBuilder()
														.withConfig(
																new SimpleSchedulerConfig())
														.withHandle(
																new SimpleHandler<Receiver>(
																		this))
														.build();
	private HashMap<String, Double>	lastVals	= new HashMap<String, Double>();
	
	/**
	 * Instantiates a new collector.
	 * 
	 * @param context
	 *            the context
	 */
	public Collector(ModuleContext context) {
		ContextProvider myContextProvider = new ContextProvider(
				"http://ontology.universaal.org/Measurement.owl#measurement");
		
		myContextProvider.setType(ContextProviderType.gauge);
		
		ContextEventPattern myContextEventPattern = new ContextEventPattern();
		myContextEventPattern
				.addRestriction(MergedRestriction
						.getAllValuesRestriction(ContextEvent.PROP_RDF_SUBJECT,
								"http://ontology.universaal.org/Measurement.owl#Measurement"));
		myContextProvider
				.setProvidedEvents(new ContextEventPattern[] { myContextEventPattern });
		cpublisher = new DefaultContextPublisher(context, myContextProvider);
		scheduler.schedule("Go!", DateTime.now().plus(1000));
		scheduler.schedule("Go!", DateTime.now().plus(35000));
		scheduler.schedule("Go!", DateTime.now().plus(55000));
	}
	
	/**
	 * Stop.
	 */
	public void stop() {
		cpublisher.close();
	}
	
	/**
	 * Gets the sensor data.
	 * 
	 * @param sensorId
	 *            the sensor id
	 */
	public void getSensorData(String sensorId) {
		AccountBean account = new AccountBean();
		account.setEmail("reaaltest@almende.org");
		account.setPassword("DossierRijnmond15");
		SenseClient.login(account);
		
		String result = SenseClient.getSensorValue(sensorId);
		System.err.println("Result received:" + result);
		try {
			JsonNode tree = JOM.getInstance().readTree(result);
			ArrayNode data = (ArrayNode) tree.get("data");
			ArrayNode goals = (ArrayNode) JOM.getInstance().readTree(
					data.get(0).get("value").asText());
			System.err.println("Goals:" + goals.size());
			Iterator<JsonNode> iter = goals.elements();
			while (iter.hasNext()) {
				JsonNode node = iter.next();
				String id = sensorId + "_" + node.get("id").asInt();
				Double lastVal = lastVals.get(id);
				System.err.println("Checking :" + lastVal + " with:"
						+ data.get(0).get("date").asDouble());
				if (lastVal != null
						&& lastVal >= data.get(0).get("date").asDouble()) {
					// Ignore this value;
				} else {
					System.err.println("Publishing!");
					lastVals.put(id, data.get(0).get("date").asDouble());
					publish(id,
							new Measurement(node.get("progress").asDouble()));
				}
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		return;
	}
	
	/**
	 * Publish.
	 * 
	 * @param sensorId
	 *            the sensor id
	 * @param value
	 *            the value
	 */
	public void publish(String sensorId, Measurement value) {
		try {
			ContextEvent event = ContextEvent
					.constructSimpleEvent(
							"http://ontology.universaal.org/Measurement.owl#Sense_"
									+ sensorId,
							"http://ontology.universaal.org/Measurement.owl#Measurement",
							"http://ontology.universaal.org/Measurement.owl#value",
							new Double(value.getValue()));
			cpublisher.publish(event);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void receive(Object msg, URI senderUrl, String tag) {
		// Scheduler receival (check if other transports are added in the
		// future.
		scheduler.schedule("Go!", DateTime.now().plus(10000));
		getSensorData("540641");
	}
}
