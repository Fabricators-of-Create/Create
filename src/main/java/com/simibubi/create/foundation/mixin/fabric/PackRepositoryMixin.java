package com.simibubi.create.foundation.mixin.fabric;

import java.lang.reflect.Field;
import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fabricators_of_create.porting_lib.extensions.PackRepositoryExtension;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin implements PackRepositoryExtension {

	@Unique
	private static final Field create$sourcesField = findSourcesField();

	@Unique
	private static Field findSourcesField() {
		for (Field field : PackRepository.class.getDeclaredFields()) {
			if (Collection.class.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				return field;
			}
		}
		return null;
	}

	@Unique
	@SuppressWarnings("unchecked")
	private Collection<RepositorySource> create$getSources() {
		if (create$sourcesField == null) {
			throw new IllegalStateException("Could not locate PackRepository sources collection");
		}
		try {
			return (Collection<RepositorySource>) create$sourcesField.get(this);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Failed to access PackRepository sources collection", e);
		}
	}

	@Override
	public void pl$addPackFinder(RepositorySource packFinder) {
		create$getSources().add(packFinder);
	}
}
