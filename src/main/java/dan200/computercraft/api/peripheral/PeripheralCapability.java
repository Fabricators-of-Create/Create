package dan200.computercraft.api.peripheral;

public final class PeripheralCapability {
	private static final Object TOKEN = new Object();

	private PeripheralCapability() {
	}

	public static Object get() {
		return TOKEN;
	}
}
