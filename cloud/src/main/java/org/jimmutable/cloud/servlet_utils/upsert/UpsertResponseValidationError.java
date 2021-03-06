package org.jimmutable.cloud.servlet_utils.upsert;

import java.util.Objects;

import org.jimmutable.cloud.servlet_utils.common_objects.JSONServletResponse;
import org.jimmutable.core.serialization.FieldDefinition;
import org.jimmutable.core.serialization.TypeName;
import org.jimmutable.core.serialization.reader.ObjectParseTree;
import org.jimmutable.core.serialization.writer.ObjectWriter;
import org.jimmutable.core.utils.Comparison;
import org.jimmutable.core.utils.Optional;

/**
 * UpsertResponseValidationError
 * Used for a standardized validation error response to an update/insert request
 * 
 * @author Preston McCumber
 * Sep 1, 2017
 */
public class UpsertResponseValidationError extends JSONServletResponse
{
	static public final TypeName TYPE_NAME = new TypeName(
			"jimmutable.aws.servlet_utils.common_objects.UpsertResponseValidationError");

	public TypeName getTypeName()
	{
		return TYPE_NAME;
	}

	static public final FieldDefinition.String FIELD_ERROR_MESSAGE = new FieldDefinition.String("error_message", null);
	static public final FieldDefinition.String FIELD_NAME = new FieldDefinition.String("field_name", null);

	static public final int HTTP_STATUS_CODE_ERROR = 500;

	private String error_message; // optional
	private String field_name; // optional

	public UpsertResponseValidationError()
	{
	};

	public UpsertResponseValidationError( String message, String field_name )
	{
		this.error_message = message;
		this.field_name = field_name;
		complete();
	}

	public UpsertResponseValidationError( ObjectParseTree t )
	{
		field_name = t.getString(FIELD_NAME);
		error_message = t.getString(FIELD_ERROR_MESSAGE);
	}

	@Override
	public int compareTo( JSONServletResponse obj )
	{
		if ( !(obj instanceof UpsertResponseValidationError) )
			return 0;

		UpsertResponseValidationError other = (UpsertResponseValidationError) obj;
		int ret = Comparison.startCompare();
		ret = Comparison.continueCompare(ret, getOptionalErrorMessage(null), other.getOptionalErrorMessage(null));
		ret = Comparison.continueCompare(ret, getOptionalFieldName(null), other.getOptionalFieldName(null));
		return ret;
	}

	@Override
	public void write( ObjectWriter writer )
	{
		writer.writeString(FIELD_ERROR_MESSAGE, getOptionalErrorMessage(null));
		writer.writeString(FIELD_NAME, getOptionalFieldName(null));
	}

	@Override
	public int getSimpleHTTPResponseCode()
	{
		return HTTP_STATUS_CODE_ERROR;
	}

	public String getOptionalErrorMessage( String default_value )
	{
		return Optional.getOptional(error_message, null, default_value);
	}

	public String getOptionalFieldName( String default_value )
	{
		return Optional.getOptional(field_name, null, default_value);
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
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(getOptionalErrorMessage(null), getOptionalFieldName(null));
	}

	@Override
	public boolean equals( Object obj )
	{
		if ( !(obj instanceof UpsertResponseValidationError) )
			return false;

		UpsertResponseValidationError other = (UpsertResponseValidationError) obj;

		if ( !Objects.equals(getOptionalErrorMessage(null), other.getOptionalErrorMessage(null)) )
			return false;
		if ( !Objects.equals(getOptionalFieldName(null), other.getOptionalFieldName(null)) )
			return false;

		return true;
	}

}
