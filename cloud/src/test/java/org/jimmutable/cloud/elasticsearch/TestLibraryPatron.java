package org.jimmutable.cloud.elasticsearch;

import org.jimmutable.cloud.elasticsearch.IndexDefinition;
import org.jimmutable.cloud.elasticsearch.Indexable;
import org.jimmutable.cloud.elasticsearch.SearchDocumentId;
import org.jimmutable.cloud.elasticsearch.SearchDocumentWriter;
import org.jimmutable.cloud.storage.Storable;
import org.jimmutable.cloud.storage.StorageKey;
import org.jimmutable.core.objects.StandardImmutableObject;
import org.jimmutable.core.objects.common.Day;
import org.jimmutable.core.objects.common.Kind;
import org.jimmutable.core.objects.common.ObjectId;
import org.jimmutable.core.serialization.FieldDefinition;
import org.jimmutable.core.serialization.Format;
import org.jimmutable.core.serialization.TypeName;
import org.jimmutable.core.serialization.reader.ObjectParseTree;
import org.jimmutable.core.serialization.writer.ObjectWriter;
import org.jimmutable.core.utils.Comparison;
import org.jimmutable.core.utils.Validator;

final public class TestLibraryPatron extends StandardImmutableObject<TestLibraryPatron> implements Indexable, Storable
{
	static public final TypeName TYPE_NAME = new TypeName("TestLibraryPatron");

	static public final FieldDefinition.Stringable<ObjectId> FIELD_OBJECT_ID = new FieldDefinition.Stringable("id", null, ObjectId.CONVERTER);
	static public final FieldDefinition.String FIELD_FIRST_NAME = new FieldDefinition.String("first_name", null);
	static public final FieldDefinition.String FIELD_LAST_NAME = new FieldDefinition.String("last_name", null);
	static public final FieldDefinition.Stringable<Day> FIELD_BIRTH_DATE = new FieldDefinition.Stringable("birth_date", null, Day.CONVERTER);
	static public final FieldDefinition.String FIELD_EMAIL_ADDRESS = new FieldDefinition.String("email_address", null);
	static public final FieldDefinition.String FIELD_SSN = new FieldDefinition.String("ssn", null);

	static public final FieldDefinition.Integer FIELD_NUM_BOOKS = new FieldDefinition.Integer("number_of_books_checked_out", -1);
	static public final FieldDefinition.Stringable<StorageKey> FIELD_PICTURE = new FieldDefinition.Stringable<StorageKey>("picture", null, StorageKey.CONVERTER);

	// static public final FieldDefinition.Integer FIELD_NUM_PICTURES = new
	// FieldDefinition.Integer("number_of_pictures", -1);

	// static public final FieldDefinition.Integer FIELD_PICTURE = new
	// FieldDefinition.StandardObject(field_name, unset_value);

	private ObjectId id; // required
	private String first_name; // required
	private String last_name; // required
	private Day birth_date; // optional

	private String email_address; // optional
	private String ssn; // optional
	private Integer number_of_books_checked_out; // required and > 0
	// private Integer number_of_pictures; // required and > 0

	private StorageKey picture; // optional
	private IndexDefinition index;

	public TestLibraryPatron(IndexDefinition index, ObjectId id, String first_name, String last_name, String email_address, String ssn, Day birth_date, int number_of_books_checked_out, StorageKey picture)
	{

		this.index = index;
		this.id = id;
		this.first_name = first_name;
		this.last_name = last_name;
		this.email_address = email_address;
		this.ssn = ssn;
		this.birth_date = birth_date;
		this.number_of_books_checked_out = number_of_books_checked_out;
		this.picture = picture;
	}

	public TestLibraryPatron(ObjectParseTree t)
	{
		this.id = t.getStringable(FIELD_OBJECT_ID);
		this.first_name = t.getString(FIELD_FIRST_NAME);
		this.last_name = t.getString(FIELD_LAST_NAME);
		this.birth_date = t.getStringable(FIELD_BIRTH_DATE);
		this.email_address = t.getString(FIELD_EMAIL_ADDRESS);
		this.ssn = t.getString(FIELD_SSN);
		this.number_of_books_checked_out = t.getInt(FIELD_NUM_BOOKS);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((birth_date == null) ? 0 : birth_date.hashCode());
		result = prime * result + ((email_address == null) ? 0 : email_address.hashCode());
		result = prime * result + ((first_name == null) ? 0 : first_name.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((last_name == null) ? 0 : last_name.hashCode());
		result = prime * result + ((number_of_books_checked_out == null) ? 0 : number_of_books_checked_out.hashCode());
		result = prime * result + ((ssn == null) ? 0 : ssn.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestLibraryPatron other = (TestLibraryPatron) obj;
		if (birth_date == null)
		{
			if (other.birth_date != null)
				return false;
		} else if (!birth_date.equals(other.birth_date))
			return false;
		if (email_address == null)
		{
			if (other.email_address != null)
				return false;
		} else if (!email_address.equals(other.email_address))
			return false;
		if (first_name == null)
		{
			if (other.first_name != null)
				return false;
		} else if (!first_name.equals(other.first_name))
			return false;
		if (id == null)
		{
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;

		if (last_name == null)
		{
			if (other.last_name != null)
				return false;
		} else if (!last_name.equals(other.last_name))
			return false;
		if (number_of_books_checked_out == null)
		{
			if (other.number_of_books_checked_out != null)
				return false;
		} else if (!number_of_books_checked_out.equals(other.number_of_books_checked_out))
			return false;
		if (ssn == null)
		{
			if (other.ssn != null)
				return false;
		} else if (!ssn.equals(other.ssn))
			return false;
		return true;
	}

	public String getSimpleFirst_name()
	{
		return first_name;
	}

	public String getSimpleLast_name()
	{
		return last_name;
	}

	public Day getOptionalBirth_date()
	{
		return birth_date;
	}

	public String getOptionalEmail_address()
	{
		return email_address;
	}

	public String getOptionalSsn()
	{
		return ssn;
	}

	public Integer getSimpleNumber_of_books_checked_out()
	{
		return number_of_books_checked_out;
	}

	public StorageKey getPicture()
	{
		return picture;
	}

	@Override
	public String serialize(Format format)
	{
		return ObjectWriter.serialize(format, this);
	}

	@Override
	public IndexDefinition getSimpleSearchIndexDefinition()
	{
		return index;
	}

	@Override
	public SearchDocumentId getSimpleSearchDocumentId()
	{
		return new SearchDocumentId(id.getSimpleValue());
	}

	@Override
	public void writeSearchDocument(SearchDocumentWriter writer)
	{
		writer.writeObjectId(FIELD_OBJECT_ID.getSimpleFieldName(), id);
		writer.writeText(FIELD_FIRST_NAME.getSimpleFieldName(), first_name);
		writer.writeText(FIELD_LAST_NAME.getSimpleFieldName(), last_name);
		writer.writeText(FIELD_EMAIL_ADDRESS.getSimpleFieldName(), email_address);
		writer.writeTextWithSubstringMatchingSupport(FIELD_SSN.getSimpleFieldName(), ssn);
		writer.writeDay(FIELD_BIRTH_DATE.getSimpleFieldName(), birth_date);
		writer.writeLong(FIELD_NUM_BOOKS.getSimpleFieldName(), number_of_books_checked_out);
		writer.writeAtom(FIELD_PICTURE.getSimpleFieldName(), picture.getSimpleValue());
	}

	@Override
	public int compareTo(TestLibraryPatron other)
	{
		int ret = Comparison.startCompare();

		ret = Comparison.continueCompare(ret, getSimpleObjectId(), other.getSimpleObjectId());
		ret = Comparison.continueCompare(ret, getSimpleFirst_name(), other.getSimpleFirst_name());
		ret = Comparison.continueCompare(ret, getSimpleLast_name(), other.getSimpleLast_name());
		ret = Comparison.continueCompare(ret, getOptionalEmail_address(), other.getOptionalEmail_address());
		ret = Comparison.continueCompare(ret, getOptionalSsn(), other.getOptionalSsn());
		ret = Comparison.continueCompare(ret, getOptionalBirth_date(), other.getOptionalBirth_date());
		ret = Comparison.continueCompare(ret, getSimpleNumber_of_books_checked_out(), other.getSimpleNumber_of_books_checked_out());
		ret = Comparison.continueCompare(ret, getPicture(), other.getPicture());

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
		writer.writeStringable(FIELD_OBJECT_ID, id);
		writer.writeString(FIELD_FIRST_NAME.getSimpleFieldName(), first_name);
		writer.writeString(FIELD_LAST_NAME.getSimpleFieldName(), last_name);
		writer.writeString(FIELD_EMAIL_ADDRESS.getSimpleFieldName(), email_address);
		writer.writeString(FIELD_SSN.getSimpleFieldName(), ssn);
		writer.writeStringable(FIELD_BIRTH_DATE, birth_date);
		writer.writeInt(FIELD_NUM_BOOKS, number_of_books_checked_out);
		// writer.writeLong(FIELD_NUM_BOOKS.getSimpleFieldName(), number_of_pictures);
		writer.writeStringable(FIELD_PICTURE, picture);
	}

	@Override
	public void freeze()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void normalize()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void validate()
	{
		Validator.notNull(id, first_name, last_name, email_address, number_of_books_checked_out);
		Validator.min(1, number_of_books_checked_out);
	}

	@Override
	public Kind getSimpleKind()
	{
		return new Kind("library-patron");
	}

	@Override
	public ObjectId getSimpleObjectId()
	{
		return id;
	}
}
