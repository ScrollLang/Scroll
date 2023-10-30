package com.skriptlang.scroll.commands;

import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.commands.ScriptCommand.CommandContext;
import com.skriptlang.scroll.language.Languaged;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

public class ClientCommandInitalizer implements CommandRegistrar, Languaged {

	static {
		try {
			CommandManager.setClientCommandInitalizer(new ClientCommandInitalizer());
		} catch (IllegalAccessException e) {
			assert false;
		}
	}

	@Override
	public void register(Command command) {
		ClientCommandManager.getActiveDispatcher().register(ClientCommandManager.literal(command.name()).executes(context -> {
			CommandContext commandContext = new ScriptCommand.CommandContext(context.getSource());
			ScriptCommand.runTriggers(ScriptCommand.getTriggersList(), commandContext);
			return commandContext.getReturnCode();
		}));
	}

	@Override
	public void unregister(Command command) {
		remover.removeCommand(command, ClientCommandManager.getActiveDispatcher().getRoot());
		PlayerManager playerManager = Scroll.getMinecraftServer().getPlayerManager();
		for (ServerPlayerEntity player : playerManager.getPlayerList())
			playerManager.sendCommandTree(player);
	}

}
