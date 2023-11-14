package com.skriptlang.scroll.commands;

import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public interface CommandRegistrar<T extends CommandSource> {

	//CommandNode.class, "children", "children", Map.class
	final BrigadierCommandRemover remover = new BrigadierCommandRemover();

	public void register(Command command);

	public void unregister(Command command);

	public void sendFeedback(T source, Text feedback);

}
