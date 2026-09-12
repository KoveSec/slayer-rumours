package com.slayerrumeurs.task;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Slayer assignment and completion chat. Complements varp/DB game-state detection.
 */
public final class SlayerChatParser
{
	public enum Kind
	{
		NONE,
		ASSIGNED,
		COMPLETED
	}

	public static final class Result
	{
		private final Kind kind;
		private final String rawTaskName;
		private final Integer remaining;
		private final String locationHint;

		Result(Kind kind, String rawTaskName, Integer remaining, String locationHint)
		{
			this.kind = kind;
			this.rawTaskName = rawTaskName;
			this.remaining = remaining;
			this.locationHint = locationHint;
		}

		public Kind getKind()
		{
			return kind;
		}

		public String getRawTaskName()
		{
			return rawTaskName;
		}

		public Integer getRemaining()
		{
			return remaining;
		}

		public String getLocationHint()
		{
			return locationHint;
		}

		public static Result none()
		{
			return new Result(Kind.NONE, null, null, null);
		}
	}

	private static final Pattern COMPLETED = Pattern.compile(
		"(?i)^(?:you've completed your task|your slayer assignment is now complete|"
			+ "your slayer task has been reset|your slayer task has been cancelled|"
			+ "your task has been cancelled|you no longer have a slayer task).*");

	private static final Pattern KONAR = Pattern.compile(
		"(?i)^you are to bring balance to (?:([\\d,]+) )?(.+?)(?: in (?:the )?(.+))?\\.?$");

	private static final Pattern NEW_TASK = Pattern.compile(
		"(?i)^your new task is to kill (?:([\\d,]+) )?(.+?)\\.?$");

	private static final Pattern ASSIGNED = Pattern.compile(
		"(?i)^you're assigned to kill (?:([\\d,]+) )?(.+?)(?:; only ([\\d,]+) more to go)?\\.?$");

	private static final Pattern CURRENT = Pattern.compile(
		"(?i)^you're currently assigned to kill (.+?); only ([\\d,]+) more to go\\.?$");

	private SlayerChatParser()
	{
	}

	public static Result parse(String message)
	{
		if (message == null || message.isEmpty())
		{
			return Result.none();
		}

		String cleaned = message.replace('\u00A0', ' ').trim();
		if (cleaned.isEmpty())
		{
			return Result.none();
		}

		if (COMPLETED.matcher(cleaned).matches())
		{
			return new Result(Kind.COMPLETED, null, 0, null);
		}

		Matcher current = CURRENT.matcher(cleaned);
		if (current.matches())
		{
			return new Result(Kind.ASSIGNED, tidyName(current.group(1)), parseCount(current.group(2)), null);
		}

		Matcher assigned = ASSIGNED.matcher(cleaned);
		if (assigned.matches())
		{
			Integer remaining = parseCount(assigned.group(3));
			if (remaining == null)
			{
				remaining = parseCount(assigned.group(1));
			}
			return new Result(Kind.ASSIGNED, tidyName(assigned.group(2)), remaining, null);
		}

		Matcher neu = NEW_TASK.matcher(cleaned);
		if (neu.matches())
		{
			return new Result(Kind.ASSIGNED, tidyName(neu.group(2)), parseCount(neu.group(1)), null);
		}

		Matcher konar = KONAR.matcher(cleaned);
		if (konar.matches())
		{
			return new Result(Kind.ASSIGNED, tidyName(konar.group(2)), parseCount(konar.group(1)), tidyName(konar.group(3)));
		}

		return Result.none();
	}

	private static Integer parseCount(String raw)
	{
		if (raw == null || raw.isEmpty())
		{
			return null;
		}
		try
		{
			return Integer.parseInt(raw.replace(",", ""));
		}
		catch (NumberFormatException ex)
		{
			return null;
		}
	}

	private static String tidyName(String raw)
	{
		if (raw == null)
		{
			return null;
		}
		String value = raw.replace('\u00A0', ' ').trim();
		if (value.endsWith("."))
		{
			value = value.substring(0, value.length() - 1).trim();
		}
		if (value.isEmpty())
		{
			return null;
		}
		return value;
	}

	static String displayCase(String name)
	{
		if (name == null || name.isEmpty())
		{
			return name;
		}
		return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
	}
}
