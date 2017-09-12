package jimmutable_aws.messaging.common_messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.RejectedExecutionException;

import org.jimmutable.cloud.ApplicationId;
import org.jimmutable.cloud.messaging.MessageListener;
import org.jimmutable.cloud.messaging.MessagingDevLocalFileSystem;
import org.jimmutable.cloud.messaging.QueueDefinition;
import org.jimmutable.cloud.messaging.QueueId;
import org.jimmutable.cloud.messaging.StandardMessageOnUpsert;
import org.jimmutable.cloud.messaging.SubscriptionDefinition;
import org.jimmutable.cloud.messaging.TopicDefinition;
import org.jimmutable.cloud.messaging.TopicId;
import org.jimmutable.core.objects.StandardObject;
import org.jimmutable.core.objects.common.Kind;
import org.jimmutable.core.objects.common.ObjectId;
import org.jimmutable.core.serialization.reader.ObjectParseTree;

import junit.framework.TestCase;

public class MessageDevLocalFileSystemTest extends TestCase
{
	static ApplicationId appId;
	static MessagingDevLocalFileSystem messagingdevlocalfilesystem;

	@Override
	protected void setUp()
	{
		appId = new ApplicationId("Development");
		messagingdevlocalfilesystem = new MessagingDevLocalFileSystem();
		ObjectParseTree.registerTypeName(StandardMessageOnUpsert.class);
	}

	public static void testSendAsync() throws InterruptedException
	{
		String mainpath = System.getProperty("user.home") + "/jimmtuable_aws_dev/messaging/development/knights-in-monty-python";
		for ( String queue_application_id : Arrays.asList("lancelot", "galahad") )
		{
			for ( String queue_queue_id : Arrays.asList("queue1", "queue2") )
			{
				String filepath = mainpath + "/" + queue_application_id + "/" + queue_queue_id;
				File f = new File(filepath);
				f.mkdirs();
			}
		}

		assertTrue(messagingdevlocalfilesystem.sendAsync(new TopicDefinition(appId, new TopicId("Knights-in-Monty-Python")), new StandardMessageOnUpsert(new Kind("niii"), new ObjectId(123456789))));
		Thread.sleep(1000);// have it wait a second

		for ( String queue_application_id : Arrays.asList("lancelot", "galahad") )
		{
			for ( String queue_queue_id : Arrays.asList("queue1", "queue2") )
			{
				String filepath = mainpath + "/" + queue_application_id + "/" + queue_queue_id;
				File f = new File(filepath);
				File[] listFiles = f.listFiles();
				assertEquals(1, listFiles.length);// prove that the message got there.
				assertTrue(readFile(listFiles[0]).contains("niii"));

			}
		}
	}

	public static void testSendAsyncWithNoSubfolders() throws InterruptedException
	{
		String mainpath = System.getProperty("user.home") + "/jimmtuable_aws_dev/messaging/development/knights_in_monty_python";

		assertTrue(messagingdevlocalfilesystem.sendAsync(new TopicDefinition(appId, new TopicId("Knights-in-Monty-Python")), new StandardMessageOnUpsert(new Kind("niii"), new ObjectId(123456789))));
		Thread.sleep(1000);// have it wait a second

		String filepath = mainpath;
		File f = new File(filepath);
		assertNull(f.listFiles());// prove that no the message got there.

		for ( String queue_application_id : Arrays.asList("lancelot", "galahad") )
		{
			filepath = mainpath + "/" + queue_application_id;
			f = new File(filepath);
			f.mkdirs();
		}
		assertTrue(messagingdevlocalfilesystem.sendAsync(new TopicDefinition(appId, new TopicId("Knights-in-Monty-Python")), new StandardMessageOnUpsert(new Kind("niii"), new ObjectId(123456789))));
		Thread.sleep(1000);// have it wait a second

		for ( String queue_application_id : Arrays.asList("lancelot", "galahad") )
		{
			filepath = mainpath + "/" + queue_application_id;
			f = new File(filepath);
			assertEquals(0, f.listFiles().length);// prove that no the message got there.
		}
	}

	private static String readFile( File f )
	{
		FileInputStream fis = null;
		byte[] bytesArray = new byte[(int) f.length()];
		try
		{
			fis = new FileInputStream(f);
			fis.read(bytesArray); // read file into bytes[]

		}
		catch ( Exception e )
		{
			fail("Something went wierd when reading the file");
		}
		finally
		{
			try
			{
				fis.close();
			}
			catch ( IOException e )
			{
				fail("Something went weird when trying to close the file stream");
			}
		}
		return new String(bytesArray);
	}

	public static void testStartListening() throws InterruptedException
	{
		String mainpath = System.getProperty("user.home") + "/jimmtuable_aws_dev/messaging/development/monty-python-jokes/development/the-holy-grail";
		TestMessageListener listener = new TestMessageListener();

		// start listening with the listener that we want to listen with
		assertTrue(messagingdevlocalfilesystem.startListening(new SubscriptionDefinition(new TopicDefinition(appId, new TopicId("monty-python-jokes")), new QueueDefinition(appId, new QueueId("the-holy-grail"))), listener));
		Thread.sleep(4500);// have it wait a minute so we can setup the listener

		// send the message
		assertTrue(messagingdevlocalfilesystem.sendAsync(new TopicDefinition(appId, new TopicId("monty-python-jokes")), new StandardMessageOnUpsert(new Kind("killer-bunny"), new ObjectId(123456789))));
		Thread.sleep(6000);// have it wait a minute so it can detect changes.

		// make sure that the listener picked up the message
		assertEquals(1, listener.messagesFound);
		assertEquals("killer-bunny", listener.messageContent);

		// make sure we deleted the message after we heard it.
		File f = new File(mainpath);
		assertEquals(0, f.listFiles().length);

		// send a different message
		assertTrue(messagingdevlocalfilesystem.sendAsync(new TopicDefinition(appId, new TopicId("monty-python-jokes")), new StandardMessageOnUpsert(new Kind("niii"), new ObjectId(123456780))));
		Thread.sleep(12000);// have it wait a minute so it can detect changes.

		// make sure that the listener picked up the message
		assertEquals(2, listener.messagesFound);
		assertEquals("niii", listener.messageContent);

		// make sure we deleted the message after we heard it.
		f = new File(mainpath);
		assertEquals(0, f.listFiles().length);

	}

	public static void testSendAllAndShutdown() throws InterruptedException
	{
		String mainpath = System.getProperty("user.home") + "/jimmtuable_aws_dev/messaging/development/monty-python-jokes";
		for ( String queue_application_id : Arrays.asList("lancelot", "galahad") )
		{
			for ( String queue_queue_id : Arrays.asList("queue1", "queue2") )
			{
				String filepath = mainpath + "/" + queue_application_id + "/" + queue_queue_id;
				File f = new File(filepath);
				f.mkdirs();
			}
		}
		for ( int i = 0; i < 10; i++ )
		{
			messagingdevlocalfilesystem.sendAsync(new TopicDefinition(appId, new TopicId("monty-python-jokes")), new StandardMessageOnUpsert(new Kind("Message"), new ObjectId(123456789)));// putting in a bunch of random information
		}
		messagingdevlocalfilesystem.sendAllAndShutdown();
		int[] initialResults = new int[4];
		int i = 0;
		for ( String queue_application_id : Arrays.asList("lancelot", "galahad") )
		{
			for ( String queue_queue_id : Arrays.asList("queue1", "queue2") )
			{
				String filepath = mainpath + "/" + queue_application_id + "/" + queue_queue_id;
				File f = new File(filepath);
				initialResults[i++] = f.listFiles().length;
			}
		}
		Thread.sleep(10000);// have it wait a millisecond to let things catch up
		i = 0;
		for ( String queue_application_id : Arrays.asList("lancelot", "galahad") )
		{
			for ( String queue_queue_id : Arrays.asList("queue1", "queue2") )
			{
				String filepath = mainpath + "/" + queue_application_id + "/" + queue_queue_id;
				File f = new File(filepath);
				assertTrue(initialResults[i++] < f.listFiles().length);
			}
		}

		// try to send messages after shutdown
		boolean error_thrown = false;
		try
		{
			messagingdevlocalfilesystem.sendAsync(new TopicDefinition(appId, new TopicId("monty-python-jokes")), new StandardMessageOnUpsert(new Kind("Message"), new ObjectId(123456789)));// putting in a bunch of random information
		}
		catch ( RejectedExecutionException e )
		{
			error_thrown = true;
		}
		assertTrue(error_thrown);
	}

	@Override
	protected void tearDown()
	{
		String filePathString = System.getProperty("user.home") + "/jimmtuable_aws_dev/messaging";// application id needs to go here
		File f = new File(filePathString);
		if ( f.exists() )
		{
			Path rootPath = Paths.get(filePathString);
			try
			{
				Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}
		}
	}
}

class TestMessageListener implements MessageListener
{
	public String messageContent;
	public int messagesFound = 0;

	@Override
	public void onMessageReceived( @SuppressWarnings("rawtypes") StandardObject message )
	{
		messagesFound = messagesFound+1;
		messageContent = ((StandardMessageOnUpsert) message).getSimpleKind().toString();
	}
}