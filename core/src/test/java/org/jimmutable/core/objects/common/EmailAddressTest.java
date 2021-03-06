package org.jimmutable.core.objects.common;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class EmailAddressTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public EmailAddressTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( EmailAddressTest.class );
    }

    
    public void testApp()
    {
    	testStringConvert("foo.bar@gmail.com", new EmailAddress("foo.bar@gmail.com"));
    	testStringConvert(" FOO.bar@gmail.COM  ", new EmailAddress("foo.bar@gmail.com"));
    	
    	testConstructorFailure("foo");
    	testConstructorFailure("google.com");
    	testConstructorFailure("foo@google");
    	testConstructorFailure("foo@.google.com");
    }
    
    private void testStringConvert(String str, EmailAddress expected_result)
    {
    	EmailAddress result = EmailAddress.CONVERTER.fromString(str, null);

 	   assertEquals(result, expected_result);
    }

    private void testConstructorFailure(String str)
    {
 	   try
 	   {
 		  EmailAddress result = new EmailAddress(str);
 		  fail();
 	   }
 	   catch(Exception e)
 	   {
 	   }
    }
}