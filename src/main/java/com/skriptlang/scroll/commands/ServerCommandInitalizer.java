package com.skriptlang.scroll.commands;

import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.tree.RootCommandNode;
import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.commands.ScriptCommand.CommandContext;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerCommandInitalizer implements CommandRegistrar {

	private RootCommandNode<ServerCommandSource> root;

	@Override
	public void register(Command command) {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			root = dispatcher.getRoot();
			dispatcher.register(literal(command.name()).executes(context -> {
				CommandContext commandContext = new ScriptCommand.CommandContext(context.getSource());
				if (environment.dedicated) { // Only permission checks on the server.
					ServerCommandSource source = context.getSource();
					if (command.permission() >= 0 && !source.hasPermissionLevel(command.permission())) {
						source.sendError(command.permissionMessage());
						commandContext.setReturnCode(-1);
					}
				}
				// We still want to call the command context even if the command will be cancelled represented by a negative number. ScriptCommand handles not calling the trigger.
				ScriptCommand.runTriggers(ScriptCommand.getTriggersList(), commandContext);
				return commandContext.getReturnCode();
			}));
		});
	}

	@Override
	public void unregister(Command command) {
		remover.removeCommand(command, root);
		PlayerManager playerManager = Scroll.getMinecraftServer().getPlayerManager();
		for (ServerPlayerEntity player : playerManager.getPlayerList())
			playerManager.sendCommandTree(player);
	}

//	private static void reloadServerDispatcher(ServerPlayerEntity player) {
//	    MinecraftServer.ResourceManagerHolder rmh = ((MinecraftServerAccessor) player.server).getResourceManagerHolder();
//	    DataPackContents data = rmh.dataPackContents();
//	    DataPackContentsAccessor dataAccessor = (DataPackContentsAccessor) data;
//	    net.minecraft.server.command.CommandManager manager = new net.minecraft.server.command.CommandManager(
//	            player.server.isDedicated() ? net.minecraft.server.command.CommandManager.RegistrationEnvironment.DEDICATED : net.minecraft.server.command.CommandManager.RegistrationEnvironment.INTEGRATED,
//	            dataAccessor.getCommandRegistryAccess()
//	    );
//	    dataAccessor.setCommandManager(manager);
//	    ((FunctionLoaderAccessor) data.getFunctionLoader()).setCommandDispatcher(manager.getDispatcher());
//	    resendCommandTree(player);
//	}

}
