package com.moulberry.moulberrystweaks.config;

import com.moulberry.moulberrystweaks.MoulberrysTweaks;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class MoulberrysTweaksConfig {

    @OptionCaption("moulberrystweaks.option.fast_loading_overlay")
    @OptionDescription("moulberrystweaks.option.fast_loading_overlay.description")
    public boolean fastLoadingOverlay = true;

    @OptionCaption("moulberrystweaks.option.transparent_loading_overlay")
    @OptionDescription("moulberrystweaks.option.transparent_loading_overlay.description")
    public boolean transparentLoadingOverlay = false;

    @OptionCaption("moulberrystweaks.option.automatic_pack_reload")
    @OptionDescription("moulberrystweaks.option.automatic_pack_reload.description")
    public boolean automaticPackReload = true;

    @OptionCaption("moulberrystweaks.option.correct_attack_indicator")
    @OptionDescription("moulberrystweaks.option.correct_attack_indicator.description")
    public boolean correctAttackIndicator = true;

    @OptionCaption("moulberrystweaks.option.log_packet_exceptions")
    @OptionDescription("moulberrystweaks.option.log_packet_exceptions.description")
    public boolean logPacketExceptions = true;

    @OptionCaption("moulberrystweaks.option.ignore_narrator_error")
    @OptionDescription("moulberrystweaks.option.ignore_narrator_error.description")
    public boolean ignoreNarratorError = false;

    @OptionCaption("moulberrystweaks.option.enable_component_widget")
    @OptionDescription("moulberrystweaks.option.enable_component_widget.description")
    public boolean enableComponentWidget = true;

    @OptionCaption("moulberrystweaks.option.enable_packet_debug_widget")
    @OptionDescription("moulberrystweaks.option.enable_packet_debug_widget.description")
    public boolean enablePacketDebugWidget = false;

    @OptionCaption("moulberrystweaks.option.confirm_disconnect")
    @OptionDescription("moulberrystweaks.option.confirm_disconnect.description")
    public boolean confirmDisconnect = false;

    @SuppressWarnings("unchecked")
    public OptionInstance<?>[] createOptionInstances() {
        List<OptionInstance<?>> options = new ArrayList<>();

        for (Field field : MoulberrysTweaksConfig.class.getDeclaredFields()) {
            try {
                // Ignore static & transient fields
                if ((field.getModifiers() & Modifier.STATIC) != 0 || (field.getModifiers() & Modifier.TRANSIENT) != 0) {
                    continue;
                }

                OptionCaption caption = field.getDeclaredAnnotation(OptionCaption.class);
                if (caption == null) {
                    continue;
                }

                OptionInstance.TooltipSupplier tooltipSupplier = OptionInstance.noTooltip();
                OptionDescription description = field.getDeclaredAnnotation(OptionDescription.class);
                if (description != null) {
                    MutableComponent root = Component.empty();
                    root.append(Component.translatable(caption.value()).withStyle(ChatFormatting.YELLOW));
                    root.append(Component.literal("\n"));
                    root.append(Component.translatable(description.value()));

                    tooltipSupplier = OptionInstance.cachedConstantTooltip(root);
                }

                if (field.getType() == boolean.class) {
                    options.add(OptionInstance.createBoolean(caption.value(), tooltipSupplier, field.getBoolean(this), value -> {
                        try {
                            field.set(this, value);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }));
                }
            } catch (Exception e) {
                MoulberrysTweaks.LOGGER.error("Error while trying to convert config field to OptionInstance", e);
            }
        }

        return options.toArray(new OptionInstance[0]);
    }

    public static MoulberrysTweaksConfig tryLoadFromFolder(Path configFolder) {
        Path primary = configFolder.resolve("moulberrystweaks.json");
        Path backup = configFolder.resolve(".moulberrystweaks.json.backup");

        if (Files.exists(primary)) {
            try {
                return load(primary);
            } catch (Exception e) {
                MoulberrysTweaks.LOGGER.error("Failed to load config from {}", primary, e);
            }
        }

        if (Files.exists(backup)) {
            try {
                return load(backup);
            } catch (Exception e) {
                MoulberrysTweaks.LOGGER.error("Failed to load config from {}", backup, e);
            }
        }

        return new MoulberrysTweaksConfig();
    }

    public void saveToDefaultFolder() {
        Path configFolder = FabricLoader.getInstance().getConfigDir().resolve("moulberrystweaks");
        this.saveToFolder(configFolder);
    }

    public synchronized void saveToFolder(Path configFolder) {
        Path primary = configFolder.resolve("moulberrystweaks.json");
        Path backup = configFolder.resolve(".moulberrystweaks.json.backup");

        if (Files.exists(primary)) {
            try {
                // Ensure primary can be loaded before backing it up
                // Don't want to back up a bad config now, do we?
                load(primary);

                // Try to back up the config
                try {
                    Files.move(primary, backup, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException e) {
                    MoulberrysTweaks.LOGGER.error("Failed to backup config", e);
                }
            } catch (Exception ignored) {}
        }

        this.save(primary);
    }

    private static MoulberrysTweaksConfig load(Path path) throws IOException {
        String serialized = Files.readString(path);
        return MoulberrysTweaks.GSON.fromJson(serialized, MoulberrysTweaksConfig.class);
    }

    private void save(Path path) {
        String serialized = MoulberrysTweaks.GSON.toJson(this, MoulberrysTweaksConfig.class);

        try {
            Files.writeString(path, serialized, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE, StandardOpenOption.DSYNC);
        } catch (IOException e) {
            MoulberrysTweaks.LOGGER.error("Failed to save config", e);
        }
    }

}
