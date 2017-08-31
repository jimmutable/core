package org.jimmutable.aws.servlet_utils.upsert;

import org.jimmutable.core.objects.Builder;
import org.jimmutable.core.objects.StandardObject;
import org.jimmutable.core.serialization.JimmutableTypeNameRegister;
import org.jimmutable.core.serialization.reader.ObjectParseTree;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UpsertResponseOKTest extends TestCase
{
	public UpsertResponseOKTest(String testName)
	{
		super(testName);
	}
	
    public static Test suite()
    {
		JimmutableTypeNameRegister.registerAllTypes();
		ObjectParseTree.registerTypeName(UpsertResponseOK.class);
        return new TestSuite( UpsertResponseOKTest.class );
    }
    
	public void testUpsertResponseOKTest()
	{
		UpsertResponseOK result = null;
		try
		{
			result = new UpsertResponseOK();

		} catch(Exception e) {
			assert(false);
		}

		assert(result.getSimpleHTTPResponseCode() == 200);
		assert(result.getOptionalMessage(null) == null);
		
		UpsertResponseOK data_object = new UpsertResponseOK();
		result = new UpsertResponseOK("Test Message", data_object );
		assert(result.getOptionalMessage(null).equals("Test Message"));
		assert(result.getOptionalObject(null) == data_object);
	}

	public void testBuilder()
	{
		Builder builder = new Builder(UpsertResponseOK.TYPE_NAME);
		
		try
		{
			builder.create(null);
		}
		catch(Exception e)
		{
			assert(false);
		}
		
		builder.set(UpsertResponseOK.FIELD_MESSAGE, "Test Message");
		UpsertResponseOK data_object = new UpsertResponseOK();
		builder.set(UpsertResponseOK.FIELD_OBJECT, data_object);
		builder.create(null);
	}
	 
	public void testSerialization()
	{
		String obj_string = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s"
			     , "{"
			     , "  \"type_hint\" : \"jimmutable.aws.servlet_utils.common_objects.UpsertResponseOK\","
			     , "  \"message\" : \"Test Message\" ,"
			     , "  \"object\" : "
			     , "{"
			     , "  \"type_hint\" : \"jimmutable.aws.servlet_utils.common_objects.UpsertResponseOK\", "
			     , "   \"message\" : \"Nested test message\" "
			     , "}"
			     , "}"
			);

		UpsertResponseOK obj = (UpsertResponseOK)StandardObject.deserialize(obj_string);
		
		assert( obj.getOptionalMessage(null).equals("Test Message") );
		UpsertResponseOK optionalObject = (UpsertResponseOK) obj.getOptionalObject(null);
		assert( optionalObject.getOptionalMessage(null).equals("Nested test message") );
	}
}
