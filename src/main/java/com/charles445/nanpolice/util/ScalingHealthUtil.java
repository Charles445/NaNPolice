package com.charles445.nanpolice.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;

public class ScalingHealthUtil 
{
	public static final String persistedTag = "PlayerPersisted";
	public static final String scalingpersistTag = "scalinghealth_data";
	public static final String scalinghealth_health = "health";
	
	public static boolean setScalingHealth(EntityPlayer player, int health)
	{
		if(Loader.isModLoaded(ModNames.SCALINGHEALTH))
		{
			try
			{
				Class shpdh_clazz = Class.forName("net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler");
				Class pd_clazz = Class.forName("net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler$PlayerData");
				
				Class[] epclass = new Class[] { EntityPlayer.class };
				
				//Get the PlayerData object for the player
				Object pdata = shpdh_clazz.getMethod("get", epclass).invoke(null, player);
				
				//Save the PlayerData to NBT first
				pd_clazz.getMethod("save").invoke(pdata);
				
				//Go find the scaling health float and set it
				final NBTTagCompound data = player.getEntityData();
				if(data.hasKey(ScalingHealthUtil.persistedTag))
				{
					final NBTTagCompound persisted = data.getCompoundTag(ScalingHealthUtil.persistedTag);
					if(persisted.hasKey(ScalingHealthUtil.scalingpersistTag))
					{
						final NBTTagCompound persistentData = persisted.getCompoundTag(ScalingHealthUtil.scalingpersistTag);
						//TODO get the proper enum
						if(persistentData.hasKey(ScalingHealthUtil.scalinghealth_health, 5))
						{
							persistentData.setFloat(ScalingHealthUtil.scalinghealth_health, health);
						}
					}
				}
				
				//Now load it
				pd_clazz.getMethod("load").invoke(pdata);
				
				return true;
			
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
}
