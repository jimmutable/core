package org.jimmutable.cloud.elasticsearch;

import org.jimmutable.core.objects.StandardEnum;
import org.jimmutable.core.utils.Normalizer;
import org.jimmutable.core.utils.Validator;

/**
 * Search index field datatypes
 * 
 * @author trevorbox
 *
 */
public enum SearchIndexFieldType implements StandardEnum
{
	ATOM("atom", "keyword"),
	TEXT("text", "text"),
	LONG("long", "long"),
	BOOLEAN("boolean", "boolean"),
	FLOAT("float", "float"),
	DAY("day", "date"),
	INSTANT("instant", "text"),
	TIMEOFDAY("timeofday", "text");

	static public final MyConverter CONVERTER = new MyConverter();

	private String code;
	private String search_type;

	private SearchIndexFieldType(String code, String search_type)
	{
		Validator.notNull(code);
		Validator.notNull(search_type);
		this.code = Normalizer.lowerCase(code);
		this.search_type = Normalizer.lowerCase(search_type);
	}

	/**
	 * The enum String representation
	 */
	public String getSimpleCode()
	{
		return code;
	}

	/**
	 * The enum String representation
	 */
	public String toString()
	{
		return code;
	}
	
	public String getSimpleSearchType()
	{
		return search_type;
	}

	/**
	 * Used for converting Strings to SearchIndexFieldType
	 * 
	 * @author trevorbox
	 *
	 */
	static public class MyConverter extends StandardEnum.Converter<SearchIndexFieldType>
	{
		public SearchIndexFieldType fromCode(String code, SearchIndexFieldType default_value)
		{
			if (code == null)
				return default_value;

			for (SearchIndexFieldType t : SearchIndexFieldType.values())
			{
				if (t.getSimpleCode().equalsIgnoreCase(code))
					return t;
				if (t.getSimpleSearchType().equalsIgnoreCase(code))
					return t;
			}

			return default_value;
		}
	}
}
