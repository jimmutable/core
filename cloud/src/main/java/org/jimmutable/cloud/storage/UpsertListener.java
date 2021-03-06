package org.jimmutable.cloud.storage;

import org.jimmutable.cloud.CloudExecutionEnvironment;
import org.jimmutable.cloud.messaging.StandardMessageOnUpsert;
import org.jimmutable.cloud.messaging.signal.SignalListener;
import org.jimmutable.core.objects.StandardObject;

public class UpsertListener implements SignalListener
{
	@Override
	public void onMessageReceived( StandardObject message )
	{
		if ( message instanceof StandardMessageOnUpsert )
		{
			StandardMessageOnUpsert standard_message = (StandardMessageOnUpsert) message;
			CloudExecutionEnvironment.getSimpleCurrent().getSimpleCache().remove(standard_message.getSimpleKind(), standard_message.getSimpleObjectId());
		}
	}

}