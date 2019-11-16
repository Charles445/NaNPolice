package com.charles445.nanpolice.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;

public class PotionCoreUtil 
{
	public static final String potion_core_health_fix = "Potion Core - Health Fix";
	
	public static void setPotionCoreHealth(EntityPlayer player, int health)
	{
		if(Loader.isModLoaded(ModNames.POTIONCORE))
		{
			final NBTTagCompound data = player.getEntityData();
			
			//TODO get proper enum
			if(data.hasKey(potion_core_health_fix, 5))
			{
				data.setFloat(potion_core_health_fix, health);
			}
		}
	}
}
