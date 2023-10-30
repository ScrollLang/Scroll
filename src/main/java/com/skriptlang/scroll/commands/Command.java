package com.skriptlang.scroll.commands;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.commands.ScriptCommand.CommandContext;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.text.Text;

public record Command(String name, List<String> aliases, boolean client, int permission, Text permissionMessage) {

	private static final Text DEFAULT_PERMISSION_MESSAGE = Scroll.getAdventure().toNative(MiniMessage.miniMessage().deserialize(Scroll.language("scripts.commands.no.permissions")));

	public Command(String name) {
		this(name, new ArrayList<>(), false, -1, DEFAULT_PERMISSION_MESSAGE);
	}

	public Command(String name, boolean client) {
		this(name, new ArrayList<>(), client, -1, DEFAULT_PERMISSION_MESSAGE);
	}

	public Command(String name, boolean client, int permission) {
		this(name, new ArrayList<>(), client, permission, DEFAULT_PERMISSION_MESSAGE);
	}

	public Command(String name, boolean client, int permission, @Nullable Text permissionMessage) {
		this(name, new ArrayList<>(), client, permission, permissionMessage != null ? permissionMessage : DEFAULT_PERMISSION_MESSAGE);
	}

	public boolean isClientSided() {
		return client();
	}

	@Override
	public String toString() {
		return "command /" + name();
	}

	public String toString(CommandContext context, boolean debug) {
		return toString();
	}

}
