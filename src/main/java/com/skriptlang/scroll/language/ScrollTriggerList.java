package com.skriptlang.scroll.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.syst3ms.skriptparser.lang.Trigger;

public class ScrollTriggerList {

	private final List<Trigger> triggers = new ArrayList<Trigger>();

	public List<Trigger> getTriggers() {
		return Collections.unmodifiableList(triggers);
	}

	public void addTriggers(Trigger... triggers) {
		this.triggers.addAll(Arrays.asList(triggers));
	}

}
