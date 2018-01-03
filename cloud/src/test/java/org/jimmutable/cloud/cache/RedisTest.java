package org.jimmutable.cloud.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import org.jimmutable.cloud.ApplicationId;
import org.jimmutable.cloud.StubTest;
import org.jimmutable.cloud.cache.redis.Redis;
import org.junit.Test;

public class RedisTest extends StubTest
{
	private Redis redis;
	private ApplicationId app;
	private boolean is_redis_live = false;

	public RedisTest()
	{	
		app = new ApplicationId("stub");
		redis = new Redis();
		
		is_redis_live = isRedisLive();
	}
	
	@Test
	public void testRedis()
	{ 
		if ( !is_redis_live ) { System.out.println("Redis server not available, skipping redis unit test!"); return; }
		
		// Test the acid string as a string value
		{
			CacheKey key = new CacheKey("acid-string-test://test-acid-string-value");
			
			String acid_string = createAcidString();
			redis.cacheSet(app, key, acid_string, -1); 
			
			String from_cache = redis.cacheGetString(app, key, null);
			
			assert(Objects.equals(acid_string, from_cache));
		}
		
		// Test the get on an empty string
		{
			CacheKey key = new CacheKey("get-unset-test://a-key-that-is-not-set");
			
			String from_cache = redis.cacheGetString(app, key, null);
			
			assert(Objects.equals(from_cache, null)); 
		}
		
		// Test the acid string as a key and a value!
		{
			String acid_string = createAcidString();
			
			CacheKey key = new CacheKey("acid-string-test://"+acid_string);
			
			redis.cacheSet(app, key, acid_string, -1); 
			String from_cache = redis.cacheGetString(app, key, null);
			
			assert(Objects.equals(acid_string, from_cache));
		}
		
		// Test TTL
		{
			CacheKey key = new CacheKey("ttl-test://test-ttl");
			
			redis.cacheSet(app, key, "Hello World", 10_000);
			long value = redis.cacheTTL(app, key, -1);
			
			assert(value > 8_500);
		}
		
		// Test TTL (unset)
		{
			CacheKey key = new CacheKey("ttl-test://test-ttl-unset");
			
			redis.cacheSet(app, key, "Hello World", -1);
			long value = redis.cacheTTL(app, key, -1);
			
			assert(value == -1);
			
			redis.cacheSet(app, key, "Hello World", 0);
			value = redis.cacheTTL(app, key, -1);
			
			assert(value == -1);
		}
		
		// Test delete
		{
			CacheKey key = new CacheKey("ttl-delete://test-delete");
			
			redis.cacheSet(app, key, "Hello World", -1);
			
			String from_cache = redis.cacheGetString(app, key, null);
			assert(Objects.equals(from_cache, "Hello World"));
			
			redis.cacheDelete(app, key);
			
			from_cache = redis.cacheGetString(app, key, null);
			
			assert(Objects.equals(from_cache, null));
			
			// test delete on null data
			redis.cacheSet(app, key, "Hello World", -1);
			from_cache = redis.cacheGetString(app, key, null);
			assert(Objects.equals(from_cache, "Hello World"));
			
			redis.cacheSet(app, key, (String)null, -1);
			from_cache = redis.cacheGetString(app, key, null);
			assert(Objects.equals(from_cache, null));
		}
		
		// Test binary data
		{
			CacheKey key = new CacheKey("binary-data://test-binary-data");
			byte data[] = createRandomBytes(1024*1024);
			
			redis.cacheSet(app, key, data, -1);
			
			byte from_cache[]  =redis.cacheGetBytes(app, key, null);
			
			assert(from_cache != null);
			assert(Arrays.equals(data, from_cache));
		}
		
		// Test scan
		{
			List<CacheKey> scan_test = new ArrayList();
			
			for ( int i = 0; i < 10_000; i++ )
			{
				CacheKey key = new CacheKey("scan-test://"+i);
				scan_test.add(key);
				redis.cacheSet(app, key, ""+i, -1);
			}
			
			AccumulateKeyScanOp op = new AccumulateKeyScanOp();
			
			redis.scan(app, null, new CacheKey("scan-test://"), op);
			
			assert(op.keys.size() == scan_test.size());
			
			assert(op.keys.containsAll(scan_test));
		}
		
		// Test that a key actually expires
		{
			CacheKey key = new CacheKey("ttl-test://test-ttl-expiration");
			
			redis.cacheSet(app, key, "Hello World", 1_000);
			
			String from_cache = redis.cacheGetString(app, key, null);
			assert(Objects.equals(from_cache, "Hello World"));
			
			try { Thread.currentThread().sleep(2000); } catch(Exception e) {}
			
			from_cache = redis.cacheGetString(app, key, null);
			
			assert(Objects.equals(from_cache, null));
		}
		
	}
	
	static private class AccumulateKeyScanOp implements ScanOperation
	{
		private Set<CacheKey> keys = new HashSet();
		
		@Override
		public void performOperation( Cache cache, CacheKey key )
		{
			keys.add(key);
		}
	}
	
	private boolean isRedisLive()
	{
		try
		{
			CacheKey key = new CacheKey("live-test://test-one-key");
			
			redis.cacheSet(app, key, "hello world", -1);
			String get_result = redis.cacheGetString(app, key, null);
			
			return get_result.equalsIgnoreCase("hello world");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	static private byte[] createRandomBytes(int size)
	{
		byte[] ret = new byte[size];
		
		Random r = new Random(); 
		
		r.nextBytes(ret);
			
		return ret;
	} 
	
	static private String createAcidString()
	{
		StringBuilder ret = new StringBuilder();
		
		for ( int i = 0; i < 10_000; i++ )
		{
			ret.append((char)i);
		}
		
		return ret.toString();
	}
}
