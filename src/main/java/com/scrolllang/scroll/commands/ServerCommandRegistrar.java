package com.scrolllang.scroll.commands;

import static net.minecraft.server.command.CommandManager.*;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import com.scrolllang.scroll.Scroll;
import com.scrolllang.scroll.commands.ScriptCommand.ScrollCommandContext;
import com.scrolllang.scroll.commands.arguments.CommandParameter;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ServerCommandRegistrar implements CommandRegistrar<ServerCommandSource> {

	private static final ServerCommandRegistrar registrar = new ServerCommandRegistrar();

	static {
		try {
			CommandManager.setServerCommandInitalizer(registrar);
		} catch (IllegalAccessException e) {
			assert false;
		}
	}

	private RootCommandNode<ServerCommandSource> root;

	@Override
	public void register(Command command) {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			root = dispatcher.getRoot();
			com.mojang.brigadier.Command<ServerCommandSource> execute = new com.mojang.brigadier.Command<>() {
				@Override
				public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
					ScrollCommandContext<ServerCommandSource> commandContext = new ScriptCommand.ScrollCommandContext<ServerCommandSource>(context, registrar);
					if (environment.dedicated) { // Only permission checks on the server.
						ServerCommandSource source = context.getSource();
						if (command.getPermission() >= 0 && !source.hasPermissionLevel(command.getPermission())) {
							source.sendError(command.getPermissionMessage());
							commandContext.setReturnCode(-1);
						}
					}
					command.fill(commandContext);
					// We still want to call the command context even if the command will be cancelled represented by a negative number. ScriptCommand handles not calling the trigger.
					ScriptCommand.runTriggers(ScriptCommand.getTriggersList(), commandContext);
					return commandContext.getReturnCode();
				}
			};
			LiteralArgumentBuilder<ServerCommandSource> mainNode = literal(command.getName());
			if (!command.getParameters().isEmpty()) {
				for (CommandParameter<?> argument : command.getParameters()) {
					mainNode.then(argument(argument.getIdentifier(), TextArgumentType.text()).executes(execute));
				}
			}
			dispatcher.register(mainNode.executes(execute));
		});
	}

	@Override
	public void unregister(Command command) {
		remover.removeCommand(command, root);
		PlayerManager playerManager = Scroll.getMinecraftServer().getPlayerManager();
		for (ServerPlayerEntity player : playerManager.getPlayerList())
			playerManager.sendCommandTree(player);
	}

	@Override
	public void sendFeedback(ServerCommandSource source, Text feedback) {
		source.sendFeedback(() -> feedback, false);
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
