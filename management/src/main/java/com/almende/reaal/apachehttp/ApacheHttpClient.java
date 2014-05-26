/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.reaal.apachehttp;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
/**
 * The Class ApacheHttpClient.
 */
public final class ApacheHttpClient {
	private static final Logger			LOG			= Logger.getLogger(ApacheHttpClient.class
															.getCanonicalName());
	private static DefaultHttpClient	httpClient	= null;
	static {
		new ApacheHttpClient();
	}
	/**
	 * Instantiates a new apache http client.
	 *
	 */
	private ApacheHttpClient() {
		
		// Allow self-signed SSL certificates:
		final TrustStrategy trustStrategy = new TrustSelfSignedStrategy();
		final X509HostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
		final SchemeRegistry schemeRegistry = SchemeRegistryFactory.createDefault();

		SSLSocketFactory sslSf;
		try {
			sslSf = new SSLSocketFactory(trustStrategy,
					hostnameVerifier);
			final Scheme https = new Scheme("https", 443, sslSf);
			schemeRegistry.register(https);
		} catch (Exception e) {
			LOG.warning("Couldn't init SSL socket, https not supported!");
		}
		
		
		
		// Work with PoolingClientConnectionManager
		final ClientConnectionManager connection = new PoolingClientConnectionManager(
				schemeRegistry);
		
		// Provide eviction thread to clear out stale threads.
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						synchronized (this) {
							wait(5000);
							connection.closeExpiredConnections();
							connection.closeIdleConnections(30,
									TimeUnit.SECONDS);
						}
					}
				} catch (final InterruptedException ex) {
				}
			}
		}).start();
		
		// generate httpclient
		httpClient = new DefaultHttpClient(connection);

		final HttpParams params = httpClient.getParams();
		
		params.setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
		params.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
		params.setParameter(CoreConnectionPNames.TCP_NODELAY, true);
		httpClient.setParams(params);
	}
	
	/**
	 * Gets the default http client.
	 *
	 * @return the default http client
	 */
	public static DefaultHttpClient get() {
		return httpClient;
	}
}
