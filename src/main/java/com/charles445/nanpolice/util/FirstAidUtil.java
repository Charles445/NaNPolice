package com.charles445.nanpolice.util;

import java.lang.reflect.Field;
import java.util.Objects;

//import com.charles445.access.AccessFirstAid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Loader;

public class FirstAidUtil 
{
	public static boolean fixFirstAidNaN(EntityPlayer player)
	{
		if(Loader.isModLoaded(ModNames.FIRSTAID))
		{
			try 
			{
				Class cehs_clazz = Class.forName("ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem");
				Class apdm_clazz = Class.forName("ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel");
				Object cehs_instance = cehs_clazz.getField("INSTANCE").get(null);
				
				Object apdm = Objects.requireNonNull(player.getCapability((Capability<Object>)cehs_instance, null));
				
				Object body = apdm_clazz.getField("BODY").get(apdm);
				Object head = apdm_clazz.getField("HEAD").get(apdm);
				Object left_arm = apdm_clazz.getField("LEFT_ARM").get(apdm);
				Object left_foot = apdm_clazz.getField("LEFT_FOOT").get(apdm);
				Object left_leg = apdm_clazz.getField("LEFT_LEG").get(apdm);
				Object right_arm = apdm_clazz.getField("RIGHT_ARM").get(apdm);
				Object right_foot = apdm_clazz.getField("RIGHT_FOOT").get(apdm);
				Object right_leg = apdm_clazz.getField("RIGHT_LEG").get(apdm);
				Class dpart_clazz = body.getClass();
				Field currentHealthField = dpart_clazz.getField("currentHealth");
				currentHealthField.setFloat(body, 6);
				currentHealthField.setFloat(head, 6);
				currentHealthField.setFloat(left_arm, 6);
				currentHealthField.setFloat(left_foot, 6);
				currentHealthField.setFloat(left_leg, 6);
				currentHealthField.setFloat(right_arm, 6);
				currentHealthField.setFloat(right_foot, 6);
				currentHealthField.setFloat(right_leg, 6);
				
				apdm_clazz.getMethod("scheduleResync").invoke(apdm);
				
				return true;
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
}
