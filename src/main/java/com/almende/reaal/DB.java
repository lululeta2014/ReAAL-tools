/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.reaal;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * The Class DB.
 */
public class DB {
	private static JedisPool pool = null;
	
	/**
	 * Gets the single instance of DB.
	 * @param id 
	 * 
	 * @return single instance of DB
	 */
	public static Jedis getInstance(int id){
		if (pool == null){
			pool = new JedisPool(new JedisPoolConfig(), "localhost");
		}
		Jedis res = pool.getResource();
		res.select(id);
		return res;
	}
	
	/**
	 * Return instance.
	 * 
	 * @param instance
	 *            the instance
	 */
	public static void returnInstance(Jedis instance){
		if (pool != null && instance != null){
			pool.returnResource(instance);
		}
	}
	
}
