package com.skriptlang.scroll.commands;

public interface CommandRegistrar {

	//CommandNode.class, "children", "children", Map.class
	final BrigadierCommandRemover remover = new BrigadierCommandRemover();

	public void register(Command command);

	public void unregister(Command command);

}
