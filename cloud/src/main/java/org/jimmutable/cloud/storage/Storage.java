package org.jimmutable.cloud.storage;

import org.jimmutable.core.utils.Validator;
import org.apache.logging.log4j.LogManager;
import org.jimmutable.cloud.ApplicationId;
import org.jimmutable.cloud.CloudExecutionEnvironment;
import org.jimmutable.core.objects.common.Kind;
import org.jimmutable.core.serialization.Format;
import org.jimmutable.core.serialization.writer.ObjectWriter;

/**
 *
 * @author andrew.towe This class is the parent of our file storage handlers. It
 *         is designed to handle all updates, insertions, checking of existence,
 *         listing of objects, and deletions.
 */

public abstract class Storage implements IStorage
{

	// Storage instance = null;
	private boolean is_readonly = false;

	protected Storage(boolean is_readOnly)
	{
		this.is_readonly = is_readOnly;
	}

	/**
	 * @param key
	 *            of the Storable object that we are looking for.
	 * @param default_value
	 *            to be returned if object is not found
	 * @return true if object is found, else Default_value
	 */

	public abstract boolean exists(ObjectIdStorageKey key, boolean default_value);

	/**
	 * @param key
	 *            of the Storable Object to Update/Insert
	 * @param bytes
	 *            the contents of the Storable Object
	 * @param hint_content_likely_to_be_compressible
	 * @return true if the Object was updated/inserted, else false
	 */

	public abstract boolean upsert(ObjectIdStorageKey key, byte bytes[], boolean hint_content_likely_to_be_compressible);

	/**
	 * @param key
	 *            key associated with Stored Object you want to retrieve the current
	 *            version of.
	 * @param default_value
	 *            If the object is not found, what would you like returned.
	 * @return Byte array of Stored object if Object was found, otherwise
	 *         default_value
	 */

	public abstract byte[] getCurrentVersion(ObjectIdStorageKey key, byte[] default_value);

	/**
	 * @param key
	 *            StorageKey associated with StorageObject
	 * @return true if Storage Object existed and was deleted, false otherwise
	 */
	public abstract boolean delete(ObjectIdStorageKey key);

	/**
	 * @param kind
	 *            The kind of the storable object you are looking for
	 * @param default_value
	 *            the value you want returned if nothing is found.
	 * @return If any StorageKeys were found, that Collection of objects will be
	 *         returned, Otherwise the Default_value that was passed in.
	 */
	public abstract Iterable<ObjectIdStorageKey> listComplex(Kind kind, Iterable<ObjectIdStorageKey> default_value);

	/**
	 * Retrieves the StorageMetadata associated to this Storable object,
	 * returning the default_value if lookup fails. (Object doesn't exist, or internal error)
	 */
	public StorageMetadata getObjectMetadata(Storable obj, StorageMetadata default_value)
	{
		if (obj == null) return default_value;
		
		return getObjectMetadata(obj.createStorageKey(), default_value);
	}
	
	public boolean upsert(Storable obj, Format format)
	{
		Validator.notNull(obj);
		return upsert(obj.createStorageKey(), ObjectWriter.serialize(format, obj).getBytes(), true);
	}

	public boolean exists(Storable obj, boolean default_value)
	{
		if (obj == null)
			return default_value;

		try
		{
			return exists(obj.createStorageKey(), false);
		} catch (Exception e)
		{
			LogManager.getRootLogger().error("Failure to list object " + obj, e);
			return default_value;
		}
	}

	public boolean delete(Storable obj)
	{
		if (obj == null)
			return false;
		if (isReadOnly())
			return false;

		return delete(obj.createStorageKey());
	}

	public boolean isReadOnly()
	{
		return is_readonly;
	}

}
