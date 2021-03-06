package org.jimmutable.gcloud;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ProjectIDTest extends TestCase 
{
	 /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public ProjectIDTest( String testName )
  {
      super( testName );
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite()
  {
      return new TestSuite( ProjectIDTest.class );
  }
  
  public void testProjectID()
  {
	   testStringConvert("some-id", new ProjectId("some-id"));
	   testStringConvert("some-id-1234", new ProjectId("some-id-1234"));
	   testStringConvert("SOME-id", new ProjectId("some-id"));
	   testStringConvert(" SOME-id ", new ProjectId("some-id"));
	   
	   testConstructorFailure(null);
	   testConstructorFailure("");
	   testConstructorFailure(" ");
	   testConstructorFailure("foo_bar");
	   testConstructorFailure(".foo");
	   testConstructorFailure("foo.");
	   testConstructorFailure("foo..bar");
	   testConstructorFailure("foo..bar");
	   testConstructorFailure("foo/bar");
  }

  private void testStringConvert(String str, ProjectId expected_result)
  {
	  ProjectId result = ProjectId.CONVERTER.fromString(str, null);

	   assertEquals(result, expected_result);
  }

  private void testConstructorFailure(String str)
  {
	   try
	   {
		   ProjectId result = new ProjectId(str);
		   assert(false);
	   }
	   catch(Exception e)
	   {
		   assert(true);
	   }
  }
}
