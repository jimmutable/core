package org.jimmutable.cloud.elasticsearch;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jimmutable.cloud.ApplicationId;
import org.jimmutable.cloud.CloudExecutionEnvironment;
import org.jimmutable.cloud.EnvironmentType;
import org.jimmutable.cloud.servlet_utils.common_objects.JSONServletResponse;
import org.jimmutable.cloud.servlet_utils.search.OneSearchResult;
import org.jimmutable.cloud.servlet_utils.search.SearchResponseOK;
import org.jimmutable.cloud.servlet_utils.search.StandardSearchRequest;
import org.jimmutable.cloud.storage.ObjectIdStorageKey;
import org.jimmutable.cloud.storage.Storable;
import org.jimmutable.cloud.storage.StorageKey;
import org.jimmutable.cloud.storage.StorageKeyHandler;
import org.jimmutable.core.exceptions.ValidationException;
import org.jimmutable.core.objects.StandardObject;
import org.jimmutable.core.objects.common.Kind;
import org.jimmutable.core.objects.common.ObjectId;
import org.jimmutable.core.serialization.reader.ObjectParseTree;


/**
 * This class is used to begin the process of re-indexing Kinds in your
 * Environment's Search to match that of what is in Environment's Storage.
 * 
 * It accomplishes this by doing the following. It takes in a set of Kinds that
 * are both Indexable as well as Storable. From there a thread pool is created.
 * In this thread pool, each thread will do two things:
 * 
 * 1.) Go through every Object in Storage and upsert a matching search document.
 * 
 * 2.) Go through all of the Kind's search index and delete any search document
 * that doesn't have a matching version in Storage.
 * 
 * Once that is complete for all Kinds passed in the start() method will
 * complete.
 * 
 * 
 * Since each application that uses Jimmutable Cloud will have a different
 * application ID and TypeNameRegisters, it is required that you extend this
 * class and implement the proper methods to retrieve the right values specific
 * to your application.
 * 
 * @author avery.gonzales
 */
public abstract class ReindexKinds
{
	private static final Logger logger = LogManager.getLogger(ReindexKinds.class);

	public static final int REINDEX_THREAD_POOL_SIZE = 5;
	public static final int MAX_REINDEX_COMPLETION_TIME_MINUTES = 120;

	//If this is being run with an idempotent script, you want this set to true in order for the execution environment to be setup properly
	//Ensure the class you create to extend this has the proper ApplicationId as well as all TypeNameRegisters needed.
	private boolean should_setup_environment; 
	//All the kinds in your application that need to be re-indexed
	private Set<Kind> kinds;
	
	public ReindexKinds(Set<Kind> kinds, boolean should_setup_environment)
	{
		this.kinds = kinds;
		this.should_setup_environment = should_setup_environment;
		validate();
	}
	
	private void validate()
	{
		if(this.kinds == null) throw new ValidationException("Unable to build script, kinds is not set");
	}

	/**
	 * Once ReindexKinds is created this will start the process of going through
	 * every Kind passed in and re-indexing them. If this is run from a script
	 * it is required that you have the EnvironmentType is set in your system
	 * variables for the script in order for this process to begin.
	 */
	public void start() throws ValidationException
	{
		if(shouldSetupEnvironment())
		{
			EnvironmentType environment_type = CloudExecutionEnvironment.getEnvironmentTypeFromSystemProperty(null);
			if(environment_type == null)
			{
				throw new ValidationException("Unable to run script, environment_type is not set in system properties");
			}
			//Need environment type and application id passed in, otherwise the environment won't be setup properly
			CloudExecutionEnvironment.startup(getSimpleApplicationID(), environment_type);
			//Need a method to collect all the possible types
			setupTypeNameRegisters();	
		}

		long start = System.currentTimeMillis();
		logger.info("Reindexing of all Kinds started...");
		ExecutorService executor = Executors.newFixedThreadPool(REINDEX_THREAD_POOL_SIZE);
		
		//Log the kinds it is attempting to reindex
		for(Kind kind : getSimpleKinds())
		{
			try
			{
				executor.submit(new ReindexSingleKind(kind));
			}
			catch(Exception e)
			{
				logger.error("Invalid ReindexSingleKind", e);
			}
		}
		executor.shutdown();
		//TODO For code review: Should we await the termination of the re-indexing for metrics... 
		//Or should this just shutdown and complete on its own time?
		try
		{
			executor.awaitTermination(MAX_REINDEX_COMPLETION_TIME_MINUTES, TimeUnit.MINUTES);
		}
		catch (InterruptedException e)
		{
			logger.error("ExecutorService termination was interrupted", e);
		}
		
		long end = System.currentTimeMillis();
		logger.info("Reindexing of all Kinds finished");
		NumberFormat formatter = new DecimalFormat("#0.00000");
		logger.info("Reindexing of all Kinds execution time was " + formatter.format((end - start) / 1000d) + " seconds");
	}

	/**
	 * Should call a method that sets up all of your applications types through
	 * ObjectParseTree.registerTypeName
	 */
	public abstract void setupTypeNameRegisters();
	
	/**
	 * Should return an ApplicationId identical to your applications
	 * ApplicationId when run normally.
	 */
	public abstract ApplicationId getSimpleApplicationID();
	
	private boolean shouldSetupEnvironment()
	{
		return should_setup_environment;
	}
	
	private Set<Kind> getSimpleKinds()
	{
		return kinds;
	}
}
