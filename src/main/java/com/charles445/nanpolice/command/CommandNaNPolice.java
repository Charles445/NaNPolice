package com.charles445.nanpolice.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.charles445.nanpolice.util.PoliceUtil;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandNaNPolice implements ICommand
{
	//TODO actually use this for something
	
	@Override
	public int compareTo(ICommand arg0) 
	{
		return 0;
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender commandSender) 
	{
		if(commandSender instanceof EntityPlayer) 
		{
			if(!commandSender.canUseCommand(4, this.getName()))
				return false;
		}
		return true;
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) 
	{
		return new ArrayList<String>();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) 
	{
		return false;
	}
	
	@Override
	public String getName() 
	{
		return "nanpolice";
	}

	@Override
	public String getUsage(ICommandSender sender) 
	{
		return "/nanpolice";
	}

	@Override
	public List<String> getAliases() 
	{
		return Arrays.asList("nanpolice");
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
	{
		if(args.length<1)
		{
			commandHelp(server,sender,args);
			return;
		}
		switch(args[0])
		{
			case "fixhealth": commandFixHealth(server,sender,args);return;
			default: commandHelp(server,sender,args);return;
		}
	}
	
	private void inform(ICommandSender sender, String msg)
	{
		sender.sendMessage(new TextComponentString(msg));
	}
	
	private void commandHelp(MinecraftServer server, ICommandSender sender, String[] args)
	{
		//Inform player of available commands
		inform(sender, "/nanpolice fixhealth : Fix invincible zero health players");
	}
	
	private void commandFixHealth(MinecraftServer server, ICommandSender sender, String[] args)
	{
		if(PoliceUtil.fixHealthAll(server))
		{
			inform(sender, "No players with NaN health found!");
		}
		else
		{
			inform(sender, "Found NaN health player(s) and attempted to fix.");
		}
	}
}
