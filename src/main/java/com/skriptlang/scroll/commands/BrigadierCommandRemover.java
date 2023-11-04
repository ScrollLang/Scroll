package com.skriptlang.scroll.commands;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

public class BrigadierCommandRemover {

	private VarHandle arguments, children, literals;

	BrigadierCommandRemover() {
		try {
			this.arguments = MethodHandles.privateLookupIn(CommandNode.class, MethodHandles.lookup()).findVarHandle(CommandNode.class, "arguments", Map.class);
			this.children = MethodHandles.privateLookupIn(CommandNode.class, MethodHandles.lookup()).findVarHandle(CommandNode.class, "children", Map.class);
			this.literals = MethodHandles.privateLookupIn(CommandNode.class, MethodHandles.lookup()).findVarHandle(CommandNode.class, "literals", Map.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Nullable
	public Map<String, ?> getArguments(CommandNode<?> node) {
		if (arguments == null)
			return null;
		return (Map<String, ?>) arguments.get(node);
	}

	@Nullable
	public Map<String, ?> getChildren(CommandNode<?> node) {
		if (children == null)
			return null;
		return (Map<String, ?>) children.get(node);
	}

	@Nullable
	public Map<String, ?> getLiterals(CommandNode<?> node) {
		if (literals == null)
			return null;
		return (Map<String, ?>) literals.get(node);
	}

	public void removeCommand(Command command, RootCommandNode<?> root) {
		Map<String, ?> arguments = getArguments(root);
		if (arguments != null)
			remove(command.getName(), arguments);
		Map<String, ?> children = getChildren(root);
		if (children != null)
			remove(command.getName(), children);
		Map<String, ?> literals = getLiterals(root);
		if (literals != null)
			remove(command.getName(), literals);
	}

	private void remove(String name, Map<String, ?> map) {
		Object element = map.get(name);
		if (element != null)
			map.remove(element);
	}

}
