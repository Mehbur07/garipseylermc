package dev.mehbur.garipseyler.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.mehbur.garipseyler.GaripSeylerMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Loads and saves {@link ModConfig}. A missing file is created with defaults; a broken file is
 * backed up as {@code .broken} and replaced, so a typo never prevents the game from starting.
 */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(GaripSeylerMod.MOD_ID + ".json");

	private static ModConfig config = new ModConfig();

	private ConfigManager() {
	}

	public static ModConfig get() {
		return config;
	}

	/** Reads the config from disk and writes it back so newly added keys appear in the file. */
	public static void load() {
		ModConfig loaded = null;

		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
				loaded = GSON.fromJson(reader, ModConfig.class);
			} catch (IOException | JsonParseException e) {
				GaripSeylerMod.LOGGER.error("Config file {} is unreadable, using defaults", PATH, e);
				backupBrokenFile();
			}
		}

		if (loaded == null) {
			loaded = new ModConfig();
		}
		loaded.validate();
		config = loaded;
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			GaripSeylerMod.LOGGER.error("Could not save config file {}", PATH, e);
		}
	}

	private static void backupBrokenFile() {
		try {
			Files.copy(PATH, PATH.resolveSibling(PATH.getFileName() + ".broken"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			GaripSeylerMod.LOGGER.error("Could not back up broken config file", e);
		}
	}
}
