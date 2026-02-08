package com.simibubi.create.foundation.data;

import org.apache.commons.lang3.StringUtils;

import com.simibubi.create.Create;
import com.tterrag.registrate.fabric.BaseLangProvider;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;

import net.minecraft.resources.ResourceLocation;

/**
 * fabric: creates translations for item tags. These are mostly just for EMI.
 */
public class TagLangGen {
	public static void datagen() {
		Create.registrate().addDataGenerator(ProviderType.LANG, TagLangGen::genItemTagLang);
	}

	private static void genItemTagLang(RegistrateLangProvider prov) {
		TagLangHelper common = new TagLangHelper("c", prov);
		common.suffixedCategory("blocks")
				.auto("andesite_alloy")
				.auto("brass")
				.auto("raw_zinc")
				.auto("zinc");
		common.suffixedCategory("ingots")
				.auto("brass")
				.auto("zinc");
		common.suffixedCategory("nuggets")
				.auto("brass")
				.auto("zinc");
		common.suffixedCategory("plates")
				.auto("copper")
				.auto("gold")
				.auto("iron")
				.auto("brass")
				.auto("obsidian");
		common.suffixedCategory("ores")
				.auto("zinc");

		common.prefixedCategory("raw")
				.auto("zinc")
				.auto("ores")
				.auto("zinc_ores");

		common.suffixedCategory("dough")
				.autoRoot()
				.auto("wheat");
		common.suffixedCategory("flour")
				.autoRoot()
				.auto("wheat");

		common.subDir("dusts")
				.auto("obsidian");

		common.prefixedCategory("stripped")
				.auto("logs")
				.auto("wood");

		common.auto("boots");
		common.auto("chestplates");
		common.auto("helmets");

		common.auto("honey_buckets");

		common.auto("chocolate");
		common.auto("honey");
		common.auto("tea");

		TagLangHelper create = new TagLangHelper(Create.ID, prov);
		create.auto("tracks");
		create.subDir("blaze_burner_fuel")
				.plural("regular")
				.plural("special");
		create.plural("casing");
		create.put("contraption_controlled", "Actors");
		create.put("bottomless/allow", "Potentially Infinite Fluids");
		create.auto("diving_fluids");
		create.auto("create_ingots");
		create.auto("crushed_raw_materials");
		create.plural("deployable_drink");
		create.auto("modded_stripped_logs");
		create.auto("modded_stripped_wood");
		create.auto("pressurized_air_sources");
		create.auto("sandpaper");
		create.auto("seats");
		create.auto("sleepers");
		create.subDir("stone_types")
				.ignoreDir("andesite")
				.ignoreDir("asurine")
				.ignoreDir("calcite")
				.ignoreDir("crimsite")
				.ignoreDir("deepslate")
				.ignoreDir("diorite")
				.ignoreDir("dripstone")
				.ignoreDir("granite")
				.ignoreDir("limestone")
				.ignoreDir("ochrum")
				.ignoreDir("scorchia")
				.ignoreDir("scoria")
				.ignoreDir("tuff")
				.ignoreDir("veridium");
		create.subDir("stone_types/galosphere")
				.ignoreDir("allurite")
				.ignoreDir("amethyst")
				.ignoreDir("lumiere");
		create.auto("toolboxes");
		create.auto("valve_handles");
		create.auto("vanilla_stripped_logs");
		create.auto("vanilla_stripped_wood");
		create.subDir("upgrade_aquatic")
				.ignoreDir("coral");
	}

	public record TagLangHelper(String namespace, BaseLangProvider prov) {
		public TagLangHelper auto(String path) {
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
			String key = key(id);
			String name = translate(id);
			prov.add(key, name);
			return this;
		}

		public TagLangHelper put(String path, String translated) {
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
			String key = key(id);
			prov.add(key, translated);
			return this;
		}

		public TagLangHelper plural(String path) {
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
			String key = key(id);
			String name = translate(id) + 's';
			prov.add(key, name);
			return this;
		}

		public SubDirHelper subDir(String dir) {
			return new SubDirHelper(this, dir);
		}

		public CategoryHelper suffixedCategory(String suffix) {
			return new CategoryHelper(this, suffix, true);
		}

		public CategoryHelper prefixedCategory(String prefix) {
			return new CategoryHelper(this, prefix, false);
		}
	}

	// prefix
	public record SubDirHelper(TagLangHelper parent, String dir) {
		public SubDirHelper put(String path, String translated) {
			ResourceLocation id = tagId(path);
			parent.prov.add(key(id), translated);
			return this;
		}

		public SubDirHelper auto(String path) {
			return put(path, translate(tagId(path)));
		}

		public SubDirHelper ignoreDir(String path) {
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(parent.namespace, path);
			return put(path, translate(id));
		}

		public SubDirHelper plural(String path) {
			return put(path, translate(tagId(path)) + 's');
		}

		public SubDirHelper autoRoot() {
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(parent.namespace, dir);
			parent.prov.add(key(id), translate(id));
			return this;
		}

		public ResourceLocation tagId(String path) {
			return ResourceLocation.fromNamespaceAndPath(parent.namespace, dir + '/' + path);
		}
	}

	// suffix
	public record CategoryHelper(TagLangHelper parent, String category, boolean suffix) {
		public CategoryHelper put(String path, String translated) {
			ResourceLocation id = tagId(path);
			parent.prov.add(key(id), translated);
			return this;
		}

		public CategoryHelper auto(String path) {
			return put(path, translate(tagId(path)));
		}

		public CategoryHelper plural(String path) {
			return put(path, translate(tagId(path)) + 's');
		}

		public CategoryHelper autoRoot() {
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(parent.namespace, category);
			parent.prov.add(key(id), translate(id));
			return this;
		}

		public ResourceLocation tagId(String name) {
			String path = suffix ? name + '_' + category : category + '_' + name;
			return ResourceLocation.fromNamespaceAndPath(parent.namespace, path);
		}
	}

	// automagical utils

	public static String key(ResourceLocation tagId) {
		String namespace = tagId.getNamespace();
		String path = tagId.getPath().replace('/', '.');
		return "tag." + namespace + '.' + path;
	}

	public static String translate(ResourceLocation tagId) {
		String path = tagId.getPath();
		String[] split = path.split("/");
		switch (split.length) {
			case 1 -> { // c:stripped_wood
				return toSentence(split); // Stripped Wood
			}
			case 2 -> { // c:nuggets/iron
				String[] switched = new String[] { split[1], split[0] }; // iron, nuggets
				return toSentence(switched); // Iron Nuggets
			}
			default -> throw new IllegalArgumentException("Tags with >1 subdirectory (" + tagId + ") cannot be automatically translated");
		}
	}

	/**
	 * converts snake_case to Proper Formatting, concatenates segments
	 */
	public static String toSentence(String[] segments) {
		// segments: andesite, stone_types
		StringBuilder sentence = new StringBuilder();
		for (String segment : segments) {
			String[] split = segment.split("_"); // stone, types
			for (String word : split) {
				String capitalized = StringUtils.capitalize(word);
				sentence.append(capitalized).append(' ');
			}
		}
		return sentence.toString().trim();
	}
}
