package net.neoforged.neoforge.client.model.data;

import java.util.HashMap;
import java.util.Map;

public class ModelData {
	public static final ModelData EMPTY = new ModelData(Map.of());

	private final Map<ModelProperty<?>, Object> values;

	public ModelData(Map<ModelProperty<?>, Object> values) {
		this.values = values;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(ModelProperty<T> property) {
		return (T) values.get(property);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final Map<ModelProperty<?>, Object> values = new HashMap<>();

		public <T> Builder with(ModelProperty<T> property, T value) {
			values.put(property, value);
			return this;
		}

		public ModelData build() {
			return new ModelData(new HashMap<>(values));
		}
	}
}
