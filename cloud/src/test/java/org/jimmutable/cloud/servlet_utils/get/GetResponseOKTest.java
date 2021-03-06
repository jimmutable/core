package org.jimmutable.cloud.servlet_utils.get;

import static org.junit.Assert.assertTrue;

import org.jimmutable.cloud.StubTest;
import org.jimmutable.core.examples.book.BindingType;
import org.jimmutable.core.examples.book.Book;
import org.jimmutable.core.objects.StandardImmutableObject;
import org.jimmutable.core.objects.StandardObject;
import org.junit.Test;

public class GetResponseOKTest extends StubTest
{

	@Test
	public void testGetResponseOKTest()
	{
		GetResponseOK result = null;
		try
		{
			result = new GetResponseOK(new Book("test title", 50, "100", BindingType.HARD_COVER, "test author"));
			// System.out.println(result.toJavaCode(Format.JSON_PRETTY_PRINT, "obj"));
		}
		catch ( Exception e )
		{
			assertTrue(false);
		}

		assertTrue(result.getSimpleHTTPResponseCode() == GetResponseOK.HTTP_STATUS_CODE_OK);
		assertTrue(result.getSimpleObject() != null);

		StandardImmutableObject<Book> data_object = new Book("test title", 50, "100", BindingType.HARD_COVER, "test author");
		result = new GetResponseOK(data_object);
		assertTrue(result.getSimpleObject() == data_object);
	}

	@Test
	public void testSerialization()
	{
		String obj_string = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s", "{", "  \"type_hint\" : \"jimmutable.aws.servlet_utils.get.GetResponseOK\",", "  \"object\" : {", "    \"type_hint\" : \"jimmutable.examples.Book\",", "    \"title\" : \"TEST TITLE\",", "    \"page_count\" : 50,", "    \"isbn\" : \"100\",", "    \"binding\" : \"hard-cover\",", "    \"authors\" : [ \"test author\" ]", "  }", "}");

		GetResponseOK obj = (GetResponseOK) StandardObject.deserialize(obj_string);

		Book requiredObject = (Book) obj.getSimpleObject();
		assertTrue(requiredObject.getSimpleTitle().equals("TEST TITLE"));
	}
}
