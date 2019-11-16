package com.charles445.nanpolice;

import java.util.Map;

import javax.annotation.Nullable;

import com.charles445.nanpolice.util.DebugUtil;
import com.charles445.nanpolice.util.PoliceUtil;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventWatcher 
{
	//TODO Optimize? How much processing does this take?
	//TODO Consider CriticalHitEvent
	//TODO Figure out if these can be registered without tricking Caliper, because Caliper is the best
	//TODO Consider a configurable verbosity... even though if an error happens it NEEDS to be acknowledged
	
	@Nullable
	public Highest watcher_highest;
	@Nullable
	public High watcher_high;
	@Nullable
	public Normal watcher_normal;
	@Nullable
	public Low watcher_low;
	@Nullable
	public Lowest watcher_lowest;
	
	private boolean initialized;
	
	public EventWatcher()
	{
		watcher_highest=null;
		watcher_high=null;
		watcher_normal=null;
		watcher_low=null;
		watcher_lowest=null;
		initialized=false;
	}
	
	public void tryInitialize()
	{
		//MinecraftForge.EVENT_BUS.register(this);
		
		if(initialized)
			return;
		
		initialized=true;
		
		boolean do_highest=false;
		boolean do_high=false;
		boolean do_normal=false;
		boolean do_low=false;
		boolean do_lowest=false;
		
		for(String s : ModConfig.eventbus_priorities)
		{
			switch(s.trim().toLowerCase())
			{
				case "highest":do_highest=true;break;
				case "high":do_high=true;break;
				case "normal":do_normal=true;break;
				case "low":do_low=true;break;
				case "lowest":do_lowest=true;break;
				case "default":break; //lol
			}
		}
		
		if(do_highest)
			NaNPolice.logger.info("Registering with HIGHEST priority");
		if(do_high)
			NaNPolice.logger.info("Registering with HIGH priority");
		if(do_normal)
			NaNPolice.logger.info("Registering with NORMAL priority");
		if(do_low)
			NaNPolice.logger.info("Registering with LOW priority");
		if(do_lowest)
			NaNPolice.logger.info("Registering with LOWEST priority");
		NaNPolice.logger.info("NaNPolice will"+(ModConfig.announce_errors?"":" NOT")+" announce errors!");
		
		watcher_highest = new Highest(do_highest);
		watcher_high = new High(do_high);
		watcher_normal = new Normal(do_normal);
		watcher_low = new Low(do_low);
		watcher_lowest = new Lowest(do_lowest);
	}
	
	public class Prioritized
	{
		private boolean announce_errors;
		private boolean autofix_player;
		private boolean autofix_creatures;
		
		public Prioritized(boolean register)
		{
			if(register)
				MinecraftForge.EVENT_BUS.register(this);
			
			announce_errors=ModConfig.announce_errors;
			autofix_player=ModConfig.autofix_player;
			autofix_creatures=ModConfig.autofix_creatures;
		}
		
		private String getPriorityString(EventPriority priority)
		{
			switch(priority)
			{
				case NORMAL: return "NORMAL";
				case HIGHEST: return "HIGHEST";
				case HIGH: return "HIGH";
				case LOW: return "LOW";
				case LOWEST: return "LOWEST";
				default: return "UNKNOWN";
			}
		}
		
		private void complain(String event_name,float amount, EventPriority priority)
		{
			//amount is invalid
			if(Float.isNaN(amount))
			{
				NaNPolice.logger.warn(event_name+" NaN Detected! Priority "+getPriorityString(priority));
			}
			else //is + - Infinity
			{
				NaNPolice.logger.warn(event_name+" Infinity Detected! Priority "+getPriorityString(priority));
			}
			
			if(announce_errors)
				DebugUtil.messageAll(event_name+" failure! Check Log");
		}
		
		private void checkAndFixEventEntity(EntityLivingBase entity)
		{
			if(autofix_player)
			{
				if(entity instanceof EntityPlayerMP)
				{
					NaNPolice.logger.info("Running player autofix");
					//PoliceUtil.fixHealthAll(FMLCommonHandler.instance().getMinecraftServerInstance());
					PoliceUtil.fixHealth((EntityPlayerMP)entity);
					return;
				}
			}
			
			if(autofix_creatures)
			{	
				EntityLivingBase living = (EntityLivingBase)entity;
				if(!Float.isFinite(living.getHealth()))
				{
					NaNPolice.logger.info("Running creature health autofix");
					living.setHealth(living.getMaxHealth());
				}
				if(!Float.isFinite(living.getAbsorptionAmount()))
				{
					NaNPolice.logger.info("Running creature absorption autofix");
					living.setAbsorptionAmount(0.0f);
				}
			}
		}
		
		private void examineDamageSource(DamageSource dsource)
		{
			Entity immediatesource = dsource.getImmediateSource();
			Entity truesource = dsource.getTrueSource();
			
			if(immediatesource!=null)
			{
				NaNPolice.logger.warn("ImmediateSource: "+immediatesource.getClass().getName());
				if(immediatesource instanceof EntityLivingBase)
					examineEntityLivingBase((EntityLivingBase)immediatesource);
			}
			if(truesource!=null)
			{
				NaNPolice.logger.warn("TrueSource: "+truesource.getClass().getName());
				if(truesource instanceof EntityLivingBase)
					examineEntityLivingBase((EntityLivingBase)truesource);
			}
		}
		
		private void examineEntityLivingBase(EntityLivingBase entity)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(" _Name: "+entity.getName());
			sb.append(" _Pos: "+entity.getPosition().toString());
			ItemStack mainhand = entity.getHeldItemMainhand();
			ItemStack offhand = entity.getHeldItemOffhand();
			ItemStack headitem = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
			ItemStack chestitem = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
			ItemStack legsitem = entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
			ItemStack footitem = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
			
			if(!mainhand.isEmpty())
				sb.append(" _MainHand: "+mainhand.getDisplayName());
			
			if(!offhand.isEmpty())
				sb.append(" _OffHand: "+offhand.getDisplayName());
			
			if(!headitem.isEmpty())
				sb.append(" _Head: "+headitem.getDisplayName());
			
			if(!chestitem.isEmpty())
				sb.append(" _Chest: "+chestitem.getDisplayName());
			
			if(!legsitem.isEmpty())
				sb.append(" _Legs: "+legsitem.getDisplayName());
			
			if(!footitem.isEmpty())
				sb.append(" _Feet: "+legsitem.getDisplayName());
			
			NaNPolice.logger.warn(sb.toString());
			
			// Examine Item Stack Followup
			
			if(!mainhand.isEmpty())
			{
				NaNPolice.logger.warn("MainHand: "+mainhand.getUnlocalizedName());
				examineItemStack(mainhand);
			}
			if(!offhand.isEmpty())
			{
				NaNPolice.logger.warn("OffHand: "+offhand.getUnlocalizedName());
				examineItemStack(offhand);
			}
			if(!headitem.isEmpty())
			{
				NaNPolice.logger.warn("Head: "+headitem.getUnlocalizedName());
				examineItemStack(headitem);
			}
			if(!chestitem.isEmpty())
			{
				NaNPolice.logger.warn("Chest: "+chestitem.getUnlocalizedName());
				examineItemStack(chestitem);
			}
			if(!legsitem.isEmpty())
			{
				NaNPolice.logger.warn("Legs: "+legsitem.getUnlocalizedName());
				examineItemStack(legsitem);
			}
			if(!footitem.isEmpty())
			{
				NaNPolice.logger.warn("Feet: "+footitem.getUnlocalizedName());
				examineItemStack(footitem);
			}
		}
		
		private void examineItemStack(ItemStack stack)
		{
			//Is NOT empty and is NOT null
			
			//Okay let's be real it's probably the enchantments
			Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
			for(Enchantment enchant : enchantments.keySet())
			{
				if(enchant==null) //Paranoia 
					continue;
				NaNPolice.logger.warn(enchant.getName()+" "+enchantments.get(enchant)); 
			}
		}
		
		
		//
		// LivingHurtEvent
		//
		public void onLivingHurt(LivingHurtEvent event, EventPriority priority)
		{
			float amount = event.getAmount();
			if(Float.isFinite(amount))
				return;
			
			complain("LivingHurtEvent",amount,priority);
			
			logLivingHurtEvent(event);
			//NaNPolice.logger.warn("Setting LivingHurtEvent amount to 1.0");
			//event.setAmount(1.0f);
			NaNPolice.logger.warn("Cancelling LivingHurtEvent");
			event.setCanceled(true);
			if(event.getEntityLiving()!=null)
				checkAndFixEventEntity(event.getEntityLiving());
		}
		
		private void logLivingHurtEvent(LivingHurtEvent event)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(" _Amount: "+event.getAmount());
			
			EntityLivingBase living = event.getEntityLiving();
			String livingstr = (living==null)?"null":living.getName();
			sb.append(" _EntityLivingBase: "+livingstr);
			
			DamageSource dt = event.getSource();
			String dtstr = (dt==null)?"null":dt.getDamageType();
			sb.append(" _DamageSource: "+dtstr);
			
			NaNPolice.logger.warn(sb.toString());
			
			if(living!=null)
				examineEntityLivingBase(living);
			if(dt!=null)
				examineDamageSource(dt);
		}
		
		//
		// LivingDamageEvent
		//
		public void onLivingDamage(LivingDamageEvent event, EventPriority priority)
		{
			float amount = event.getAmount();
			if(Float.isFinite(amount))
				return;
			
			complain("LivingDamageEvent",amount,priority);
			
			logLivingDamageEvent(event);
			NaNPolice.logger.warn("Cancelling LivingDamageEvent");
			event.setCanceled(true);
			if(event.getEntityLiving()!=null)
				checkAndFixEventEntity(event.getEntityLiving());
			
		}
		
		private void logLivingDamageEvent(LivingDamageEvent event)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(" _Amount: "+event.getAmount());
			EntityLivingBase living = event.getEntityLiving();
			String livingstr = (living==null)?"null":living.getName();
			sb.append(" _EntityLivingBase: "+livingstr);
			
			DamageSource dt = event.getSource();
			String dtstr = (dt==null)?"null":dt.getDamageType();
			sb.append(" _DamageSource: "+dtstr);
			
			NaNPolice.logger.warn(sb.toString());
			
			if(living!=null)
				examineEntityLivingBase(living);
			if(dt!=null)
				examineDamageSource(dt);
		}
		
		//
		// LivingHealEvent
		//
		public void onLivingHeal(LivingHealEvent event, EventPriority priority)
		{
			float amount = event.getAmount();
			if(Float.isFinite(amount))
				return;

			complain("LivingHealEvent",amount,priority);
			
			logLivingHealEvent(event);
			NaNPolice.logger.warn("Cancelling LivingHealEvent");
			event.setCanceled(true);
			if(event.getEntityLiving()!=null)
				checkAndFixEventEntity(event.getEntityLiving());
		}
		
		private void logLivingHealEvent(LivingHealEvent event)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(" _Amount: "+event.getAmount());
			EntityLivingBase living = event.getEntityLiving();
			String livingstr = (living==null)?"null":living.getName();
			sb.append(" _EntityLivingBase: "+livingstr);
			
			NaNPolice.logger.warn(sb.toString());
			
			if(living!=null)
				examineEntityLivingBase(living);
		}
		
		//
		// LivingAttackEvent
		//
		public void onLivingAttack(LivingAttackEvent event, EventPriority priority)
		{
			float amount = event.getAmount();
			if(Float.isFinite(amount))
				return;
			
			complain("LivingAttackEvent",amount,priority);
			
			logLivingAttackEvent(event);
			NaNPolice.logger.warn("Cancelling LivingAttackEvent");
			event.setCanceled(true);
			if(event.getEntityLiving()!=null)
				checkAndFixEventEntity(event.getEntityLiving());
		}
		
		private void logLivingAttackEvent(LivingAttackEvent event)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(" _Amount: "+event.getAmount());
			EntityLivingBase living = event.getEntityLiving();
			String livingstr = (living==null)?"null":living.getName();
			sb.append(" _EntityLivingBase: "+livingstr);
			
			DamageSource dt = event.getSource();
			String dtstr = (dt==null)?"null":dt.getDamageType();
			sb.append(" _DamageSource: "+dtstr);
			
			NaNPolice.logger.warn(sb.toString());
			
			if(living!=null)
				examineEntityLivingBase(living);
			if(dt!=null)
				examineDamageSource(dt);
		}
	}
	
	//
	// Classes
	//
	
	public class Highest extends Prioritized
	{
		/*
		private void tryDebug(LivingHurtEvent event)
		{
			Random random = new Random(System.currentTimeMillis());
			if(random.nextInt(50)==0)
				event.setAmount(Float.NaN);
		}
		*/
		
		public Highest(boolean register)
		{
			super(register);
		}
		
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public void watchLivingHurt(LivingHurtEvent event)
		{
			//tryDebug(event);
			this.onLivingHurt(event, EventPriority.HIGHEST);
		}
		
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public void watchLivingDamage(LivingDamageEvent event)
		{
			this.onLivingDamage(event, EventPriority.HIGHEST);
		}
		
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public void watchLivingHeal(LivingHealEvent event)
		{
			this.onLivingHeal(event, EventPriority.HIGHEST);
		}
		
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public void watchLivingAttack(LivingAttackEvent event)
		{
			this.onLivingAttack(event, EventPriority.HIGHEST);
		}
	}
	
	public class High extends Prioritized
	{
		public High(boolean register)
		{
			super(register);
		}
		
		@SubscribeEvent(priority = EventPriority.HIGH)
		public void watchLivingHurt(LivingHurtEvent event)
		{
			this.onLivingHurt(event, EventPriority.HIGH);
		}
		
		@SubscribeEvent(priority = EventPriority.HIGH)
		public void watchLivingDamage(LivingDamageEvent event)
		{
			this.onLivingDamage(event, EventPriority.HIGH);
		}
		
		@SubscribeEvent(priority = EventPriority.HIGH)
		public void watchLivingHeal(LivingHealEvent event)
		{
			this.onLivingHeal(event, EventPriority.HIGH);
		}
		
		@SubscribeEvent(priority = EventPriority.HIGH)
		public void watchLivingAttack(LivingAttackEvent event)
		{
			this.onLivingAttack(event, EventPriority.HIGH);
		}
	}
	
	public class Normal extends Prioritized
	{
		public Normal(boolean register)
		{
			super(register);
		}
		
		@SubscribeEvent(priority = EventPriority.NORMAL)
		public void watchLivingHurt(LivingHurtEvent event)
		{
			this.onLivingHurt(event, EventPriority.NORMAL);
		}
		
		@SubscribeEvent(priority = EventPriority.NORMAL)
		public void watchLivingDamage(LivingDamageEvent event)
		{
			this.onLivingDamage(event, EventPriority.NORMAL);
		}
		
		@SubscribeEvent(priority = EventPriority.NORMAL)
		public void watchLivingHeal(LivingHealEvent event)
		{
			this.onLivingHeal(event, EventPriority.NORMAL);
		}
		
		@SubscribeEvent(priority = EventPriority.NORMAL)
		public void watchLivingAttack(LivingAttackEvent event)
		{
			this.onLivingAttack(event, EventPriority.NORMAL);
		}
	}
	
	public class Low extends Prioritized
	{
		public Low(boolean register)
		{
			super(register);
		}
		
		@SubscribeEvent(priority = EventPriority.LOW)
		public void watchLivingHurt(LivingHurtEvent event)
		{
			this.onLivingHurt(event, EventPriority.LOW);
		}
		
		@SubscribeEvent(priority = EventPriority.LOW)
		public void watchLivingDamage(LivingDamageEvent event)
		{
			this.onLivingDamage(event, EventPriority.LOW);
		}
		
		@SubscribeEvent(priority = EventPriority.LOW)
		public void watchLivingHeal(LivingHealEvent event)
		{
			this.onLivingHeal(event, EventPriority.LOW);
		}
		
		@SubscribeEvent(priority = EventPriority.LOW)
		public void watchLivingAttack(LivingAttackEvent event)
		{
			this.onLivingAttack(event, EventPriority.LOW);
		}
	}
	
	public class Lowest extends Prioritized
	{
		public Lowest(boolean register)
		{
			super(register);
		}
		
		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void watchLivingHurt(LivingHurtEvent event)
		{
			this.onLivingHurt(event, EventPriority.LOWEST);
		}
		
		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void watchLivingDamage(LivingDamageEvent event)
		{
			this.onLivingDamage(event, EventPriority.LOWEST);
		}
		
		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void watchLivingHeal(LivingHealEvent event)
		{
			this.onLivingHeal(event, EventPriority.LOWEST);
		}
		
		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void watchLivingAttack(LivingAttackEvent event)
		{
			this.onLivingAttack(event, EventPriority.LOWEST);
		}
	}
}
