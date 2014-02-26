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
			ObjectNode item = JOM.createObjectNode();
			item.put("username", key);
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
				if (db.exists(account.getUsername())) {
					DB.returnInstance(db);
					return Response.status(Status.PRECONDITION_FAILED)
							.entity("Account already exists").build();
				}
				String stored = JOM.getInstance().writeValueAsString(account);
				db.set(account.getUsername(), stored);
				DB.returnInstance(db);
				
				SenseClient.login();
				if (SenseClient.checkUser(account.getUsername())) {
					SenseClient.createUser(account);
				} else {
					System.err.println("Sense user already exist:"
							+ account.getUsername());
				}
				
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
			SenseClient.deleteUser(account.getUsername());
			db.del(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
		DB.returnInstance(db);
		return Response.noContent().build();
	}
	
	/**
	 * @param username
	 * @return ok
	 */
	@Path("test/{id}")
	@GET
	public Response testSense(@PathParam("id") String username) {
		SenseClient.login();
		SenseClient.checkUser(username);
		
		return Response.ok().build();
	}
}
