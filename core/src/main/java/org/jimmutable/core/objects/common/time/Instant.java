package org.jimmutable.core.objects.common.time;

import java.util.Objects;

import org.jimmutable.core.objects.StandardImmutableObject;
import org.jimmutable.core.objects.common.Day;
import org.jimmutable.core.objects.common.Kind;
import org.jimmutable.core.objects.common.TimezoneID;
import org.jimmutable.core.serialization.FieldDefinition;
import org.jimmutable.core.serialization.TypeName;
import org.jimmutable.core.serialization.reader.ObjectParseTree;
import org.jimmutable.core.serialization.writer.ObjectWriter;
import org.jimmutable.core.utils.Comparison;
import org.jimmutable.core.utils.Validator;
import org.joda.time.DateTime;

public class Instant extends StandardImmutableObject<Instant>
{
	static public final Kind KIND = new Kind("instant");
	static public final TypeName TYPE_NAME = new TypeName("instant");
	static public final FieldDefinition.Long FIELD_MS_FROM_EPOCH = new FieldDefinition.Long("ms_from_epoch", null);

	private long ms_from_epoch; // required, the current instant is Sysmtem.currentTimeMillis()

	/**
	 * the current instant is System.currentTimeMillis()
	 * 
	 * @param ms_from_epoch
	 */
	public Instant()
	{
		//Quality of Life Thing. 
		this.ms_from_epoch = System.currentTimeMillis();
	}
	
	public Instant(long ms_from_epoch)
	{
		this.ms_from_epoch = ms_from_epoch;
	}

	public Instant(ObjectParseTree t)
	{
		this.ms_from_epoch = t.getLong(FIELD_MS_FROM_EPOCH);
	}

	public long getSimpleMillisecondsFromEpoch()
	{
		return ms_from_epoch;
	}

	public long getSimpleSecondsFromEpoch()
	{
		return ms_from_epoch / 1000l;
	}

	public static long toMillisecondsFromEpoch(Day day, TimeOfDay time, TimezoneID timezone, long default_value)
	{
		if (day == null || time == null || timezone == null)
		{
			return default_value;
		}
		try
		{
			return new DateTime(day.getSimpleYear(), day.getSimpleMonthOfYear(), day.getSimpleDayOfMonth(), (int) time.getSimple24hrClockHours(), (int) time.getSimple24hrClockMinutes(), (int) time.getSimple24hrClockSeconds(), (int) time.getSimple24hrClockMilliseconds(), timezone.getSimpleDateTimeZoneValue()).getMillis();
		} catch (Exception e)
		{
			return default_value;
		}
	}

	public Day toDay(TimezoneID timezone)
	{
		return new Day(new DateTime(ms_from_epoch, timezone.getSimpleDateTimeZoneValue()));
	}

	public TimeOfDay toTimeOfDay(TimezoneID timezone)
	{
		DateTime dt = new DateTime(ms_from_epoch, timezone.getSimpleDateTimeZoneValue());
		return new TimeOfDay(TimeOfDay.toMillis(dt.getHourOfDay(), dt.getMinuteOfHour(), dt.getSecondOfMinute(), dt.getMillisOfSecond()));
	}

	/**
	 * Given a Instant we will make a more human readable timestamp string.
	 * 
	 * Generic output as such: MM/DD/YYYY HH:MM [AM/PM] EST
	 * 
	 * @param timezone_id
	 *            the timezone to base our string off of
	 * @param show_timezone
	 *            boolean, when set to true this will show the currently selected
	 *            timezone the time is from
	 * @param default_value
	 *            Return value on creation failure
	 * 
	 * @return A pretty printed timestamp string if possible
	 */
	public String createTimestampString(TimezoneID timezone_id, boolean show_timezone, String default_value)
	{
		StringBuilder timestamp_string_builder = new StringBuilder();
		timestamp_string_builder.append(toDay(timezone_id).getSimpleMonthOfYear());
		timestamp_string_builder.append("/");
		timestamp_string_builder.append(toDay(timezone_id).getSimpleDayOfMonth());
		timestamp_string_builder.append("/");
		timestamp_string_builder.append(toDay(timezone_id).getSimpleYear());
		timestamp_string_builder.append(" ");
		timestamp_string_builder.append(toTimeOfDay(timezone_id).getSimple12hrClockPrettyPrint());
		if (show_timezone)
			timestamp_string_builder.append(" " + timezone_id.getSimpleValue());

		return timestamp_string_builder.toString();
	}

	@Override
	public int compareTo(Instant other)
	{
		int ret = Comparison.startCompare();
		ret = Comparison.continueCompare(ret, getSimpleMillisecondsFromEpoch(), other.getSimpleMillisecondsFromEpoch());
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
		writer.writeLong(FIELD_MS_FROM_EPOCH, ms_from_epoch);
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
		Validator.notNull(ms_from_epoch);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(ms_from_epoch);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Instant))
		{
			return false;
		}

		Instant other = (Instant) obj;

		return Objects.equals(getSimpleMillisecondsFromEpoch(), other.getSimpleMillisecondsFromEpoch());
	}

}
