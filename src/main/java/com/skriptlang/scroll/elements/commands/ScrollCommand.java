package com.skriptlang.scroll.elements.commands;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.word;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.ScrollScriptLoader;
import com.skriptlang.scroll.script.Script;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * The main command class for handling the /scroll command.
 */
public class ScrollCommand {

	private static final boolean HIDE_EXTENSIONS = Scroll.CONFIGURATION.getCommandSection().getBoolean("scroll.commands.hide-extensions", () -> true);
	private static final int PERMISSION_LEVEL = (int) Scroll.CONFIGURATION.getCommandSection().getLong("scroll.commands.permission-level", () -> 4);

	static {
		if (Scroll.CONFIGURATION.getCommandSection().getBoolean("scroll.commands.enabled", () -> true)) {
			CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
				final LiteralCommandNode<ServerCommandSource> scroll = dispatcher.register(literal("sc")
						.requires(source -> !environment.dedicated || source.hasPermissionLevel(PERMISSION_LEVEL))
						.then(literal("reload")
							.then(argument("file", word())
								.suggests(new SuggestionProvider<ServerCommandSource>() {
									@Override
									public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
										return CommandSource.suggestMatching(collectReload(), builder);
									}
								})
								.executes(context -> {
									String argument = context.getArgument("file", String.class);
									switch (argument) {
										case "configuration":
											if (Scroll.CONFIGURATION.reload()) {
												context.getSource().sendMessage(Text.literal(Scroll.language("configuration.reload.success")));
											} else {
												context.getSource().sendMessage(Text.literal(Scroll.languageFormat("scroll.reload.failed", "configuration.toml")));
											}
											break;
										case "languages":
											if (Scroll.LANGUAGE.reload()) {
												context.getSource().sendMessage(Text.literal(Scroll.language("language.reload.success")));
											} else {
												context.getSource().sendMessage(Text.literal(Scroll.languageFormat("scroll.reload.failed", Scroll.LANGUAGE.getLanguage() + ".properties")));
											}
											break;
										default:
											if (ScrollScriptLoader.getScriptByName(argument).orElseThrow().reload()) {
												context.getSource().sendMessage(Text.literal(Scroll.languageFormat("scripts.reload.success", argument)));
											} else {
												context.getSource().sendMessage(Text.literal(Scroll.languageFormat("scripts.reload.failed", argument)));
											}
											break;
									}
									return 0;
								})))
						.executes(context -> {
							context.getSource().sendMessage(Text.literal("No arguments TODO"));
							return 0;
						}));
				dispatcher.register(literal("scroll").redirect(scroll));
			});
		}
	}

	private static Stream<String> collectReload() {
		if (HIDE_EXTENSIONS)
			return Stream.concat(ScrollScriptLoader.getLoadedScripts().stream().map(Script::getSimpleName), Stream.of("configuration", "languages"));
		return Stream.concat(ScrollScriptLoader.getLoadedScripts().stream().map(Script::getFileName), Stream.of("configuration", "languages"));
	}

}
