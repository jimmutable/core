package org.jimmutable.cloud.servlet_utils.common_objects;

import static org.junit.Assert.assertTrue;

import org.jimmutable.cloud.StubTest;
import org.jimmutable.core.objects.StandardObject;
import org.junit.Test;

public class GeneralResponseOKTest extends StubTest
{

	@Test
	public void testGeneralResponseOK()
	{
		GeneralResponseOK result = null;
		try
		{
			result = new GeneralResponseOK();
		}
		catch ( Exception e )
		{
			assertTrue(false);
		}

		assertTrue(result.getSimpleHTTPResponseCode() == GeneralResponseOK.HTTP_STATUS_CODE_OK);
		assertTrue(result.getOptionalMessage(null) == null);

		result = new GeneralResponseOK("Test Message");
		assertTrue(result.getOptionalMessage(null).equals("Test Message"));
		assertTrue(result.getOptionalMessage("default").equals("Test Message"));

		// System.out.println(result.toJavaCode(Format.JSON_PRETTY_PRINT, "obj"));
	}

	@Test
	public void testSerialization()
	{
		String obj_string = String.format("%s\n%s\n%s\n%s", "{", "  \"type_hint\" : \"jimmutable.aws.servlet_utils.common_objects.GeneralResponseOK\",", "  \"message\" : \"Test Message\"", "}");

		GeneralResponseOK obj = (GeneralResponseOK) StandardObject.deserialize(obj_string);
		assertTrue(obj.getOptionalMessage(null).equals("Test Message"));
	}

}
