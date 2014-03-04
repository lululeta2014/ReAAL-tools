/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.reaal;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import redis.clients.jedis.Jedis;

import com.almende.reaal.jackson.JOM;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class RestServlet.
 */
@Path("/")
public class RestServlet {
	private static final int	DBID	= 1;
	private static final String	EMAILS	= "emailaddresses";
	
	/**
	 * List accounts.
	 * 
	 * @return the response
	 */
	@GET
	@Produces("application/json")
	public Response listAccounts() {
		Jedis db = DB.getInstance(DBID);
		Set<String> keys = db.keys("*");
		DB.returnInstance(db);
		
		ArrayNode result = JOM.createArrayNode();
		for (String key : keys) {
			if (key.equals(EMAILS)){
				continue;
			}
			ObjectNode item = JOM.createObjectNode();
			item.put("zprId", key);
			result.add(item);
		}
		return Response.ok(result.toString()).build();
	}
	
	/**
	 * Creates the account.
	 * 
	 * @param json
	 *            the json
	 * @return the response
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response createAccount(String json) {
		Jedis db = DB.getInstance(DBID);
		try {
			AccountBean account = JOM.getInstance().readValue(json,
					AccountBean.class);
			
			if (account != null) {
				if (db.exists(account.getZprId())) {
					DB.returnInstance(db);
					return Response
							.status(Status.PRECONDITION_FAILED)
							.entity("Account with ZPR username: "
									+ account.getZprId() + " already exists")
							.build();
				}
				if (db.sismember(EMAILS,account.getEmail())) {
					DB.returnInstance(db);
					return Response
							.status(Status.PRECONDITION_FAILED)
							.entity("Account with email adres: "
									+ account.getEmail() + " already exists")
							.build();
				}
				account.setPassword(PasswordGenerator.gen());
				SenseClient.login();
				if (SenseClient.checkUser(account.getEmail())) {
					SenseClient.createUser(account);
					SenseClient.addToDomain(account);
				} else {
					System.err.println("Sense user already exist:"
							+ account.getEmail());
				}
				String stored = JOM.getInstance().writeValueAsString(account);
				db.set(account.getZprId(), stored);
				db.sadd(EMAILS, account.getEmail());
				DB.returnInstance(db);
				
				return Response.ok(
						JOM.getInstance().writeValueAsString(account)).build();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		DB.returnInstance(db);
		return Response.status(Status.BAD_REQUEST).entity("Invalid JSON given")
				.build();
	}
	
	/**
	 * Gets the account.
	 * GET /reaal/{id}
	 * 
	 * @param id
	 *            the id
	 * @return the account
	 */
	@Path("{id}")
	@Produces("application/json")
	@GET
	public Response getAccount(@PathParam("id") String id) {
		Jedis db = DB.getInstance(DBID);
		try {
			if (!db.exists(id)) {
				DB.returnInstance(db);
				return Response.status(Status.NOT_FOUND)
						.entity("Account '" + id + "' doesn't exist").build();
			}
			AccountBean account = JOM.getInstance().readValue(db.get(id),
					AccountBean.class);
			if (account != null) {
				DB.returnInstance(db);
				return Response.ok(
						JOM.getInstance().writeValueAsString(account)).build();
			} else {
				DB.returnInstance(db);
				return Response.status(Status.NOT_FOUND)
						.entity("Account '" + id + "' couldn't be read.")
						.build();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		DB.returnInstance(db);
		return Response.noContent().build();
	}
	
	/**
	 * Delete an account.
	 * DELETE /reaal/{id}
	 * 
	 * @param id
	 *            the id
	 * @return the response
	 */
	@Path("{id}")
	@DELETE
	public Response deleteAccount(@PathParam("id") String id) {
		Jedis db = DB.getInstance(DBID);
		
		if (!db.exists(id)) {
			DB.returnInstance(db);
			return Response.status(Status.NOT_FOUND)
					.entity("Account '" + id + "' doesn't exist").build();
		}
		try {
			AccountBean account = JOM.getInstance().readValue(db.get(id),
					AccountBean.class);
			if (account == null) {
				DB.returnInstance(db);
				return Response.status(Status.NOT_FOUND)
						.entity("Account '" + id + "' couldn't be read.")
						.build();
			}
			SenseClient.login(account);
			SenseClient.deleteUser(account.getSenseId());
			db.del(id);
			db.srem(EMAILS, account.getEmail());
		} catch (IOException e) {
			e.printStackTrace();
		}
		DB.returnInstance(db);
		return Response.noContent().build();
	}
	
	/**
	 * @param id
	 * @return account
	 */
	@Path("{id}")
	@PUT
	public Response retrySenseAccount(@PathParam("id") String id) {
		Jedis db = DB.getInstance(DBID);
		
		if (!db.exists(id)) {
			DB.returnInstance(db);
			return Response.status(Status.NOT_FOUND)
					.entity("Account '" + id + "' doesn't exist").build();
		}
		try {
			AccountBean account = JOM.getInstance().readValue(db.get(id),
					AccountBean.class);
			if (account == null) {
				DB.returnInstance(db);
				return Response.status(Status.NOT_FOUND)
						.entity("Account '" + id + "' couldn't be read.")
						.build();
			}
			SenseClient.login();
			SenseClient.createUser(account);
			SenseClient.addToDomain(account);
			
			String stored = JOM.getInstance().writeValueAsString(account);
			db.set(account.getZprId(), stored);
			DB.returnInstance(db);
			
			return Response.ok(JOM.getInstance().writeValueAsString(account))
					.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		DB.returnInstance(db);
		return Response.noContent().build();
	}
	
}
