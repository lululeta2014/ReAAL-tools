/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.reaal;

import java.io.Serializable;

/**
 * The Class AccountBean.
 */
public class AccountBean implements Serializable {
	private static final long	serialVersionUID	= -782957937560492020L;
	
	/**
	 * Instantiates a new account bean.
	 */
	public AccountBean() {
	}
	
	private String  zprId				= null;
	private String	email				= null;
	private String  senseId				= null;
	private String	password			= null;
	private String	oauthToken			= null;
	private String	oauthVerifier		= null;
	private String	consumerCategory	= "default";
	
	/**
	 * @return ZprId
	 */
	public String getZprId() {
		return zprId;
	}

	/**
	 * @param zprId
	 */
	public void setZprId(String zprId) {
		this.zprId = zprId;
	}

	/**
	 * Gets the username.
	 * 
	 * @return the username
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * Sets the email.
	 * 
	 * @param email
	 *            the new email
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * @return SenseId
	 */
	public String getSenseId() {
		return senseId;
	}

	/**
	 * @param senseId
	 */
	public void setSenseId(String senseId) {
		this.senseId = senseId;
	}

	/**
	 * Gets the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Sets the password.
	 * 
	 * @param password
	 *            the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Gets the oauthToken.
	 * 
	 * @return the oauthToken
	 */
	public String getOauthToken() {
		return oauthToken;
	}
	
	/**
	 * Sets the oauthToken.
	 * 
	 * @param oauthToken
	 *            the new oauthToken
	 */
	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}
	
	/**
	 * Gets the oauthVerifier.
	 * 
	 * @return the oauthVerifier
	 */
	public String getOauthVerifier() {
		return oauthVerifier;
	}
	
	/**
	 * Sets the oauthVerifier.
	 * 
	 * @param oauthVerifier
	 *            the new oauthVerifier
	 */
	public void setOauthVerifier(String oauthVerifier) {
		this.oauthVerifier = oauthVerifier;
	}
	
	/**
	 * Gets the consumerCategory.
	 * 
	 * @return the consumerCategory
	 */
	public String getConsumerCategory() {
		return consumerCategory;
	}
	
	/**
	 * Sets the consumerCategory.
	 * 
	 * @param consumerCategory
	 *            the new consumerCategory
	 */
	public void setConsumerCategory(String consumerCategory) {
		this.consumerCategory = consumerCategory;
	}
	
	/**
	 * Checks whither the Sense account is correctly registered.
	 * 
	 * @return inSense?
	 */
	public boolean isInSense() {
		return !SenseClient.checkUser(email);
	}
	
	/**
	 * Checks whither the Oauth tokens from Sense are known.
	 * 
	 * @return isOauth
	 */
	public boolean isOauth() {
		return (oauthToken != null && oauthVerifier != null);
	}
}
