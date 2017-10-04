package org.jimmutable.core.objects.common;

import org.jimmutable.core.utils.StringableTester;
import org.junit.Test;

import junit.framework.TestCase;

public class OpenGraphObjectIdTest extends TestCase
{
	private StringableTester<OpenGraphObjectId> tester = new StringableTester(new OpenGraphObjectId.MyConverter());

	@Test
	public void testValid()
	{
		tester.assertValid("123456");
	}

	@Test
	public void testInValid()
	{
		tester.assertInvalid("12-3456");
		tester.assertInvalid("someids");
		tester.assertInvalid("");
		tester.assertInvalid("~`!@#$%^&*()_-+={[}]:;");
		tester.assertInvalid("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
	}

}