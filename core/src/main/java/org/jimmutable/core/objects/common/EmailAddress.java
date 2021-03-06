package org.jimmutable.core.objects.common;

import org.apache.commons.validator.routines.EmailValidator;
import org.jimmutable.core.exceptions.ValidationException;
import org.jimmutable.core.objects.Stringable;
import org.jimmutable.core.utils.Validator;

public class EmailAddress extends Stringable
{
	static public final MyConverter CONVERTER = new MyConverter();
	
	public EmailAddress(String code)
	{
		super(code);
	}

	
	public void normalize() 
	{
		normalizeTrim();
		normalizeLowerCase();
	}

	
	public void validate() 
	{
		Validator.notNull(getSimpleValue());
		
		if ( !EmailValidator.getInstance().isValid(getSimpleValue()) ) 
			throw new ValidationException(String.format("Invalid email address %s", getSimpleValue()));
	}
	

	
	static public class MyConverter extends Stringable.Converter<EmailAddress>
	{
		public EmailAddress fromString(String str, EmailAddress default_value)
		{
			try
			{
				return new EmailAddress(str);
			}
			catch(Exception e)
			{
				return default_value;
			}
		}
	}
}
