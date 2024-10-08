package org.scrolllang.scroll.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import org.scrolllang.scroll.Scroll;
import org.scrolllang.scroll.commands.ScriptCommand.ScrollCommandContext;
import org.scrolllang.scroll.commands.arguments.CommandParameter;
import org.scrolllang.scroll.language.Languaged;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ClientCommandRegistrar implements CommandRegistrar<FabricClientCommandSource>, Languaged {

	private static final ClientCommandRegistrar registrar = new ClientCommandRegistrar();

	static {
		try {
			CommandManager.setClientCommandInitalizer(registrar);
		} catch (IllegalAccessException e) {
			assert false;
		}
	}

	private RootCommandNode<FabricClientCommandSource> root;

	@Override
	public void register(Command command) {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			root = dispatcher.getRoot();
			com.mojang.brigadier.Command<FabricClientCommandSource> execute = new com.mojang.brigadier.Command<>() {
				@Override
				public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
					ScrollCommandContext<FabricClientCommandSource> commandContext = new ScriptCommand.ScrollCommandContext<FabricClientCommandSource>(context, registrar);
					command.fill(commandContext);
					// We still want to call the command context even if the command will be cancelled represented by a negative number. ScriptCommand handles not calling the trigger.
					ScriptCommand.runTriggers(ScriptCommand.getTriggersList(), commandContext);
					return commandContext.getReturnCode();
				}
			};
			LiteralArgumentBuilder<FabricClientCommandSource> mainNode = literal(command.getName());
			if (!command.getParameters().isEmpty()) {
				for (CommandParameter<?> argument : command.getParameters()) {
					mainNode.then(argument(argument.getIdentifier(), TextArgumentType.text(registryAccess)));
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
	public void sendFeedback(FabricClientCommandSource source, Text feedback) {
		source.sendFeedback(feedback);
	}

}
