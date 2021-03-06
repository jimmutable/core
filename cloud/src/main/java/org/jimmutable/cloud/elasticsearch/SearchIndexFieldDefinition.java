package org.jimmutable.cloud.elasticsearch;

import java.util.Objects;

import org.jimmutable.core.objects.StandardImmutableObject;
import org.jimmutable.core.serialization.reader.ObjectParseTree;
import org.jimmutable.core.serialization.reader.ReadAs;
import org.jimmutable.core.serialization.FieldDefinition;
import org.jimmutable.core.serialization.FieldName;
import org.jimmutable.core.serialization.TypeName;
import org.jimmutable.core.serialization.writer.ObjectWriter;
import org.jimmutable.core.utils.Comparison;
import org.jimmutable.core.utils.Validator;

/**
 * A search index field name and data type (mapping)
 * 
 * @author trevorbox
 *
 */
public class SearchIndexFieldDefinition extends StandardImmutableObject<SearchIndexFieldDefinition>
{

	static public final Converter<SearchIndexFieldDefinition> CONVERTER = new Converter<>();

	public static final FieldDefinition.StandardObject FIELD_FIELD_NAME = new FieldDefinition.StandardObject("name", null);
	public static final FieldDefinition.Enum<SearchIndexFieldType> FIELD_SEARCH_INDEX_FIELD_TYPE = new FieldDefinition.Enum<SearchIndexFieldType>("type", null, SearchIndexFieldType.CONVERTER);

	public static final TypeName TYPE_NAME = new TypeName(SearchIndexFieldDefinition.class.getName());

	private FieldName name;
	private SearchIndexFieldType type;

	public SearchIndexFieldDefinition(ObjectParseTree t)
	{
		name = (FieldName) t.getObject(FIELD_FIELD_NAME);
		type = t.getEnum(FIELD_SEARCH_INDEX_FIELD_TYPE);
	}

	public SearchIndexFieldDefinition(FieldName name, SearchIndexFieldType type)
	{
		super();
		this.name = name;
		this.type = type;
		complete();
	}

	/**
	 * The search FieldName
	 * 
	 * @return FieldName
	 */
	public FieldName getSimpleFieldName()
	{
		return name;
	}

	/**
	 * The search field type
	 * 
	 * @return SearchIndexFieldType
	 */
	public SearchIndexFieldType getSimpleType()
	{
		return type;
	}

	@Override
	public int compareTo(SearchIndexFieldDefinition other)
	{
		int ret = Comparison.startCompare();

		Comparison.continueCompare(ret, this.getSimpleFieldName(), other.getSimpleFieldName());
		Comparison.continueCompare(ret, this.getSimpleType(), other.getSimpleType());

		return ret;
	}

	@Override
	public TypeName getTypeName()
	{
		return TYPE_NAME;
	}

	@Override
	public void write(ObjectWriter writer)
	{
		writer.writeObject(FIELD_FIELD_NAME, this.getSimpleFieldName());
		writer.writeEnum(FIELD_SEARCH_INDEX_FIELD_TYPE, this.getSimpleType());
	}

	@Override
	public void freeze()
	{
	}

	@Override
	public void normalize()
	{
	}

	@Override
	public void validate()
	{
		Validator.notNull(this.getSimpleFieldName());
		Validator.notNull(this.getSimpleType());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(name, type);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof SearchIndexFieldDefinition))
			return false;

		SearchIndexFieldDefinition other = (SearchIndexFieldDefinition) obj;

		if (!this.getSimpleFieldName().equals(other.getSimpleFieldName()))
			return false;
		if (!this.getSimpleType().equals(other.getSimpleType()))
			return false;

		return true;
	}

	static private class Converter<S extends SearchIndexFieldDefinition> extends ReadAs
	{
		public SearchIndexFieldDefinition from(FieldName name, SearchIndexFieldType type, SearchIndexFieldDefinition default_value)
		{
			try
			{
				return new SearchIndexFieldDefinition(name, type);
			} catch (Exception e)
			{
				return default_value;
			}
		}

		@Override
		public Object readAs(ObjectParseTree t)
		{
			FieldName name = (FieldName) t.getObject(FIELD_FIELD_NAME);
			SearchIndexFieldType type = t.getEnum(FIELD_SEARCH_INDEX_FIELD_TYPE);
			return from(name, type, null);
		}
	}

}
