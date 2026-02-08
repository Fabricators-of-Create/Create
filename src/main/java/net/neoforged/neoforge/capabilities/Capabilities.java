package net.neoforged.neoforge.capabilities;

public final class Capabilities {
	private Capabilities() {
	}

	public static final class ItemHandler {
		public static final Object BLOCK = new Object();
		public static final Object ITEM = new Object();

		private ItemHandler() {
		}
	}

	public static final class FluidHandler {
		public static final Object BLOCK = new Object();
		public static final Object ITEM = new Object();

		private FluidHandler() {
		}
	}
}
