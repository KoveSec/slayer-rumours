package com.slayerrumeurs.task;

import java.util.List;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

/**
 * Reads the current Slayer assignment from official game state (varps + cache DB tables).
 * Soft-fails if a lookup is unavailable so chat parsing can still supply the name.
 */
@Slf4j
@Singleton
public class SlayerGameStateReader
{
	private static final int BOSS_TASK_ID = 98;

	public int getRemaining(Client client)
	{
		return client.getVarpValue(VarPlayerID.SLAYER_COUNT);
	}

	public String lookupTaskName(Client client)
	{
		try
		{
			int remaining = getRemaining(client);
			if (remaining <= 0)
			{
				return null;
			}

			int taskId = client.getVarpValue(VarPlayerID.SLAYER_TARGET);
			int taskDBRow;
			if (taskId == BOSS_TASK_ID)
			{
				List<Integer> bossRows = client.getDBRowsByValue(
					DBTableID.SlayerTaskSublist.ID,
					DBTableID.SlayerTaskSublist.COL_TASK_SUBTABLE_ID,
					0,
					client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID));
				if (bossRows == null || bossRows.isEmpty())
				{
					return null;
				}
				Object[] taskField = client.getDBTableField(bossRows.get(0), DBTableID.SlayerTaskSublist.COL_TASK, 0);
				if (taskField == null || taskField.length == 0 || !(taskField[0] instanceof Integer))
				{
					return null;
				}
				taskDBRow = (Integer) taskField[0];
			}
			else
			{
				List<Integer> taskRows = client.getDBRowsByValue(
					DBTableID.SlayerTask.ID,
					DBTableID.SlayerTask.COL_ID,
					0,
					taskId);
				if (taskRows == null || taskRows.isEmpty())
				{
					return null;
				}
				taskDBRow = taskRows.get(0);
			}

			Object[] nameField = client.getDBTableField(taskDBRow, DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0);
			if (nameField == null || nameField.length == 0 || !(nameField[0] instanceof String))
			{
				return null;
			}
			return (String) nameField[0];
		}
		catch (RuntimeException ex)
		{
			log.debug("Unable to resolve slayer task name from game state", ex);
			return null;
		}
	}
}
