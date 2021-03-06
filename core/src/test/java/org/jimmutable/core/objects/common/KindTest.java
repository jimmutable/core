package org.jimmutable.core.objects.common;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class KindTest extends TestCase 
{
	 /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public KindTest( String testName )
  {
      super( testName );
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite()
  {
      return new TestSuite( KindTest.class );
  }
  
  public void testBucketName()
  {
	   testStringConvert("foo", new Kind("foo"));
	   testStringConvert("FOO", new Kind("foo"));
	   
	   testStringConvert("Foo-248", new Kind("foo-248"));
	   
	   testConstructorFailure(null);
	   testConstructorFailure("");
	   testConstructorFailure(" ");
	   testConstructorFailure("foo_bar");
	   testConstructorFailure(".foo");
	   testConstructorFailure("foo.");
	   testConstructorFailure("foo.bar");
	   testConstructorFailure("foo..bar");
  }

  private void testStringConvert(String str, Kind expected_result)
  {
	  Kind result = Kind.CONVERTER.fromString(str, null);

	   assertEquals(result, expected_result);
  }

  private void testConstructorFailure(String str)
  {
	   try
	   {
		   Kind result = new Kind(str);
		   System.out.println("Should be invalid (but was not) "+str);
		   fail();
	   }
	   catch(Exception e)
	   {
		   
	   }
  }
}
