package org.jimmutable.cloud.email;

import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jimmutable.core.utils.Validator;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;

/**
 * AWS Simple Email Service
 * 
 * @author trevorbox
 *
 */
public class SESClient implements IEmail
{

	private static final Logger logger = LoggerFactory.getLogger(SESClient.class);

	private AmazonSimpleEmailService client;

	/**
	 * Just set the -Daws.accessKeyId and -Daws.secretKey properties when running
	 * the JVM to pass the id and secret for the client to work.
	 * 
	 * @see <a href=
	 *      "https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html">Working
	 *      with AWS Credentials</a>
	 * 
	 * @return
	 */
	public static AmazonSimpleEmailService getClient()
	{
		return AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_WEST_2).build();
	}

	public SESClient(AmazonSimpleEmailService client)
	{
		this.client = client;
	}

	@Override
	public boolean sendEmail(Email email)
	{
		try
		{
			Validator.notNull(email, "Email");

			Message aws_message = new Message();

			// build the message up.
			{
				aws_message.withSubject(new Content().withData(email.getSimpleSubject()));

				Body aws_body = new Body();
				if (email.hasTextBody())
				{
					aws_body.withText(new Content().withData(email.getOptionalTextBody(null)));
				}
				if (email.hasHtmlBody())
				{
					aws_body.withHtml(new Content().withData(email.getOptionalHtmlBody(null)));
				}

				aws_message.withBody(aws_body);
			}

			SendEmailRequest request = new SendEmailRequest();
			request.withSource(email.getSimpleSource());

			Destination dest = new Destination();

			dest.setToAddresses(email.getSimpleTo().stream().map(e -> e.getSimpleValue()).collect(Collectors.toSet()));

			if (email.hasCc())
			{
				dest.setCcAddresses(email.getOptionalCc(null).stream().map(e -> e.getSimpleValue()).collect(Collectors.toSet()));
			}

			if (email.hasBcc())
			{
				dest.setBccAddresses(email.getOptionalBcc(null).stream().map(e -> e.getSimpleValue()).collect(Collectors.toSet()));
			}

			request.withDestination(dest);
			request.withMessage(aws_message);

			if (email.hasReplyTo())
			{
				request.setReplyToAddresses(email.getOptionalReplyTo(null).stream().map(e -> e.getSimpleValue()).collect(Collectors.toSet()));
			}

			// Send the email.
			SendEmailResult result = client.sendEmail(request);
			logger.info(String.format("Sent an email with id: %s", result.getMessageId()));
			return true;
		} catch (Exception e)
		{
			logger.error("Message rejected!", e);
			return false;
		}
	}

}
