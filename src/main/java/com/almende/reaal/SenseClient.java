/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.reaal;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.almende.reaal.apachehttp.ApacheHttpClient;
import com.almende.reaal.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class SenseClient.
 */
public class SenseClient {
	private static final Logger			LOG		= Logger.getLogger(SenseClient.class
														.getName());
	
	private static DefaultHttpClient	client	= ApacheHttpClient.get();
	
	/**
	 * Login.
	 */
	public static void login() {
		AccountBean defaultAccount = new AccountBean();
		defaultAccount.setUsername(Settings.SenseUser);
		defaultAccount.setPassword(Settings.SensePassword);
		login(defaultAccount);
	}
	/**
	 * Login.
	 * @param account 
	 */
	public static void login(AccountBean account) {
		HttpPost httpPost = null;
		try {
			String base = Settings.SenseBaseUrl;
			
			httpPost = new HttpPost(URI.create(base + "/login"));
			ObjectNode params = JOM.createObjectNode();
			params.put("username", account.getUsername());
			params.put("password", DigestUtils.md5Hex(account.getPassword()));
			
			StringEntity entity = new StringEntity(JOM.getInstance()
					.writeValueAsString(params));
			entity.setContentType("application/json");
			
			httpPost.setEntity(entity);
			final HttpResponse webResp = client.execute(httpPost);
			final String result = EntityUtils.toString(webResp.getEntity());
			
			if (webResp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				LOG.warning("Received HTTP Error Status:"
						+ webResp.getStatusLine().getStatusCode() + ":"
						+ webResp.getStatusLine().getReasonPhrase());
				LOG.warning(result);
			} else {
				LOG.info("Logged into Sense.");
			}
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "HTTP roundtrip resulted in exception!", e);
		} finally {
			if (httpPost != null) {
				httpPost.reset();
			}
		}
	}
	
	/**
	 * Check user.
	 * 
	 * @param username
	 *            the username
	 * @return true, if successful
	 */
	public static boolean checkUser(String username) {
		HttpGet httpGet = new HttpGet(URI.create(Settings.SenseBaseUrl
				+ "/users/check/username/" + username));
		
		try {
			final HttpResponse webResp = client.execute(httpGet);
			return webResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
		} catch (ParseException | IOException e) {
			LOG.log(Level.WARNING, "HTTP roundtrip resulted in exception!", e);
		} finally {
			if (httpGet != null) {
				httpGet.reset();
			}
		}
		return false;
	}
	
	/**
	 * Delete user.
	 * 
	 * @param username
	 *            the username
	 */
	public static void deleteUser(String username){
		HttpDelete httpDelete = new HttpDelete(URI.create(Settings.SenseBaseUrl
				+ "/users/" + username));
		
		try {
			final HttpResponse webResp = client.execute(httpDelete);
			String result = EntityUtils.toString(webResp.getEntity());
			if (webResp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				LOG.warning("Received HTTP Error Status:"
						+ webResp.getStatusLine().getStatusCode() + ":"
						+ webResp.getStatusLine().getReasonPhrase());
				LOG.warning(result);
			}
		} catch (ParseException | IOException e) {
			LOG.log(Level.WARNING, "HTTP roundtrip resulted in exception!", e);
		} finally {
			if (httpDelete != null) {
				httpDelete.reset();
			}
		}
	}
	
	/**
	 * Creates the user.
	 * 
	 * @param account
	 *            the account
	 */
	public static void createUser(AccountBean account) {
		HttpPost httpPost = new HttpPost(URI.create(Settings.SenseBaseUrl
				+ "/users.json?disable_mail=1"));
		
		try {
			ObjectNode user = JOM.createObjectNode();
			ObjectNode realUser = JOM.createObjectNode();
			realUser.put("email", account.getUsername()+"@example.com");
			realUser.put("username", account.getUsername());
			realUser.put("password", DigestUtils.md5Hex(account.getPassword()));
			user.put("user", realUser);
			StringEntity entity = new StringEntity(JOM.getInstance()
					.writeValueAsString(user));
			httpPost.setEntity(entity);
			entity.setContentType("application/json");
			final HttpResponse webResp = client.execute(httpPost);
			final String result = EntityUtils.toString(webResp.getEntity());
			
			if (webResp.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
				LOG.warning("Received HTTP Error Status:"
						+ webResp.getStatusLine().getStatusCode() + ":"
						+ webResp.getStatusLine().getReasonPhrase());
				LOG.warning(result);
			}
		} catch (ParseException | IOException e) {
			LOG.log(Level.WARNING, "HTTP roundtrip resulted in exception!", e);
		} finally {
			if (httpPost != null) {
				httpPost.reset();
			}
		}
	}
}
