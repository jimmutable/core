package org.jimmutable.aws.s3;

import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.jimmutable.aws.environment.HostName;
import org.jimmutable.aws.environment.HostName.MyConverter;
import org.jimmutable.core.exceptions.ValidationException;
import org.jimmutable.core.objects.Stringable;
import org.jimmutable.core.utils.Validator;

public class S3AbsolutePath extends Stringable
{
	static public final MyConverter CONVERTER = new MyConverter();
	
	private transient S3BucketName bucket_name;
	private transient S3Path path;
	
	public S3AbsolutePath(String code)
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
		
		String[] value = getSimpleValue().split(":");
		
		if ( value.length != 2 )
		{
			throw new ValidationException(String.format("Invalid S3 absolute path name %s, must contain one : to seperate the bucket name from the object path", getSimpleValue()));
		}
		
		// Creating these objects also validates them
		bucket_name = new S3BucketName(value[0]);
		path = new S3Path(value[1]);
		
		// Set the value (normalizes everything)
		setValue(String.format("%s:%s", bucket_name.getSimpleValue(), path.getSimpleValue()));
	}
	
	static public class MyConverter extends Stringable.Converter<S3AbsolutePath>
	{
		public S3AbsolutePath fromString(String str, S3AbsolutePath default_value)
		{
			try
			{
				return new S3AbsolutePath(str);
			}
			catch(Exception e)
			{
				return default_value;
			}
		}
	}
}