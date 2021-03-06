package org.jimmutable.cloud.servlet_utils.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jimmutable.core.utils.StringableTestingUtils;
import org.junit.Test;

public class SearchFieldIdTest
{
	private StringableTestingUtils<SearchFieldId> tester = new StringableTestingUtils(new SearchFieldId.MyConverter());

	@Test
	public void testConverter()
	{
		SearchFieldId defaulted = SearchFieldId.CONVERTER.fromString(" yEs YES ", null);
		assertEquals(defaulted.getSimpleValue(), "yes yes");
	}

	@Test
	public void inValid()
	{
		assertTrue(tester.isInvalid(null));
	}

	@Test
	public void valid()
	{
		assertTrue(tester.isValid("abb_1924 ", "abb_1924"));
		assertTrue(tester.isValid("abb-1924", "abb-1924"));
		assertTrue(tester.isValid("abB1924", "abb1924"));
		assertTrue(tester.isValid("abb1924", "abb1924"));
	}
}
