package io.github.syst3ms.skriptparser.event;

import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * The script loading context, which corresponds to running code inside {@code public static void main(String[] args)}
 * in Java.
 */
public class ScriptLoadContext implements TriggerContext {
    private final String[] args;

    public ScriptLoadContext(String[] args) {
        this.args = args;
    }

    @Override
    public String getName() {
        return "main";
    }

    public String[] getArguments() {
        return args;
    }
}
