package io.github.fabricators_of_create.porting_lib.event.common;

import java.util.function.Consumer;

public class TagsUpdatedCallback {
	public interface Listener {
		void onTagsUpdated(Object registries);
	}

	public static final Event EVENT = new Event();

	public static class Event {
		public void register(Consumer<Object> listener) {
		}
	}
}
