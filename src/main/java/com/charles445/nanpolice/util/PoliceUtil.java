package com.charles445.nanpolice.util;

import com.charles445.nanpolice.NaNPolice;
import com.charles445.nanpolice.util.FirstAidUtil;
import com.charles445.nanpolice.util.PotionCoreUtil;
import com.charles445.nanpolice.util.ScalingHealthUtil;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class PoliceUtil 
{
	public static boolean fixHealthAll(MinecraftServer server)
	{
		//Returns if check was clean and no players were glitched
		
		boolean clean = true;
		
		for(EntityPlayerMP player : server.getPlayerList().getPlayers())
		{
			if(!fixHealth(player))
				clean=false;
		}
		
		return clean;
	}
	
	public static boolean fixHealth(EntityPlayerMP player)
	{
		float chealth = player.getHealth();
		
		if(!Float.isFinite(chealth))
		{
			int health = 20;
			NaNPolice.logger.info("Attempting to fix health of "+player.getName());
			
			//Vanilla Minecraft
			player.setHealth(health);
			player.setAbsorptionAmount(0);
			
			//Scaling Health
			if(!ScalingHealthUtil.setScalingHealth(player, health))
				NaNPolice.logger.info("ScalingHealthUtil reflection failed");
			
			//Potion Core (This fix might not be necessary)
			PotionCoreUtil.setPotionCoreHealth(player, health);
			
			//First Aid
			if(!FirstAidUtil.fixFirstAidNaN(player))
				NaNPolice.logger.info("FirstAidUtil reflection failed");
			
			//Set this if it matters (probably doesn't)
			player.setPlayerHealthUpdated();
			
			return false;
		}
		return true;
	}
}
