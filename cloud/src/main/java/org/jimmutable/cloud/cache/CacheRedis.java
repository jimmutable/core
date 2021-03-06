package org.jimmutable.cloud.cache;

import org.jimmutable.cloud.ApplicationId;
import org.jimmutable.cloud.cache.redis.LowLevelRedisDriver;
import org.jimmutable.cloud.cache.redis.RedisScanOperation;
import org.jimmutable.core.objects.StandardObject;
import org.jimmutable.core.serialization.Format;
import org.jimmutable.core.utils.Validator;

public class CacheRedis extends Cache
{ 
	private LowLevelRedisDriver redis;
	private ApplicationId app;
	
	public CacheRedis(ApplicationId app, LowLevelRedisDriver redis)
	{
		Validator.notNull(app, redis);
		this.redis = redis;
		this.app = app;
	}
	
	public void put( CacheKey key, byte[] data, long max_ttl )
	{
		redis.getSimpleCache().set(app, key, data, max_ttl);
	}

	@Override
	public void put( CacheKey key, String data, long max_ttl )
	{
		redis.getSimpleCache().set(app, key, data, max_ttl);
	}

    @Override
	@SuppressWarnings("rawtypes")
	public void put( CacheKey key, StandardObject data, long max_ttl )
	{
		if ( key == null ) return;
		if ( data == null ) { delete(key); return; }
		
		redis.getSimpleCache().set(app, key, data.serialize(Format.JSON), max_ttl);
	}

	@Override
	public long getRemainingTTL( CacheKey key, long default_value )
	{
		return redis.getSimpleCache().getTTL(app, key, default_value);
	}

	@Override
	public byte[] getBytes( CacheKey key, byte[] default_value )
	{
		return redis.getSimpleCache().getBytes(app, key, default_value);
	}

	@Override
	public String getString( CacheKey key, String default_value )
	{
		return redis.getSimpleCache().getString(app, key, default_value);
	}

    @Override
	@SuppressWarnings("rawtypes")
	public StandardObject getObject( CacheKey key, StandardObject default_value )
	{
		String str = getString(key, null);
		if ( str == null ) return default_value;
		
		try
		{
			return StandardObject.deserialize(str);
		}
		catch(Exception e)
		{
			return default_value;
		}
	}

	@Override
	public void delete( CacheKey key )
	{
		redis.getSimpleCache().delete(app, key);
	}

	@Override
	public void scan( CacheKey prefix, ScanOperation operation )
	{
		Validator.notNull(prefix, operation);
		
		RedisScanOperation low_level_op = new RedisScanOperation()
		{
			public void performOperation(LowLevelRedisDriver driver, CacheKey key)
			{
			    /*
			     * CODEREVIEW
			     * Why do you ignore the LowLevelRedisDriver passed in? Sure,
			     * CacheRedis.this and driver *should* be the same, but splicing
			     * the contract like this will probably lead to a weird bug in the future.
			     *   operation.performOperation(driver.cache(), key)
			     * -JMD
			     */
				operation.performOperation(CacheRedis.this, key);
			}
		};
		
		redis.getSimpleCache().scan(app, prefix, low_level_op);
	}

	@Override
	public boolean exists( CacheKey key )
	{
		return redis.getSimpleCache().exists(app, key);
	}
	
}
