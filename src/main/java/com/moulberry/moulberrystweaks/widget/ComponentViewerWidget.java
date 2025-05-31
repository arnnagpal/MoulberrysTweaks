package com.moulberry.moulberrystweaks.widget;

import com.google.common.hash.HashCode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.moulberrystweaks.formatting.FormattedSnbtPrinter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.locale.Language;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.HashOps;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ComponentViewerWidget extends FloatingTextWidget {

    public ComponentViewerWidget() {
        super("Component Viewer");
    }

    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            this.close();
        } else {
            List<FormattedText> lines = new ArrayList<>();

            var registryAccess = Minecraft.getInstance().player.registryAccess();
            var ops = RegistryOps.create(NbtOps.INSTANCE, registryAccess);
            var hashOps = registryAccess.createSerializationContext(HashOps.CRC32C_INSTANCE);

            var patch = itemStack.getComponentsPatch();

            Set<DataComponentType<?>> patchedComponents = new HashSet<>();

            boolean addPatchedHeader = true;

            for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
                var componentType = entry.getKey();
                var componentValue = entry.getValue();

                if (addPatchedHeader) {
                    addPatchedHeader = false;
                    lines.add(Component.literal("Patched").withColor(0xFFFFFF).withStyle(ChatFormatting.BOLD));
                    lines.add(FormattedText.EMPTY);
                }

                if (componentValue.isEmpty()) {
                    lines.add(Component.literal(componentType.toString()).withColor(0xE5C17C));
                    lines.add(Component.literal("~Removed~").withStyle(ChatFormatting.RED));
                    lines.add(FormattedText.EMPTY);
                } else {
                    TypedDataComponent<?> component = itemStack.getTyped(componentType);
                    if (component != null) {
                        addLinesForDataComponent(lines, component, hashOps, ops);
                        patchedComponents.add(componentType);
                    }
                }
            }

            boolean addDefaultHeader = !patch.isEmpty();

            for (TypedDataComponent<?> component : itemStack.getComponents()) {
                if (patchedComponents.contains(component.type())) {
                    continue;
                }

                if (addDefaultHeader) {
                    addDefaultHeader = false;
                    lines.add(Component.literal("Default").withColor(0xFFFFFF).withStyle(ChatFormatting.BOLD));
                    lines.add(FormattedText.EMPTY);
                }

                addLinesForDataComponent(lines, component, hashOps, ops);
            }

            this.setLines(lines);
        }
    }

    private void addLinesForDataComponent(List<FormattedText> lines, TypedDataComponent<?> component, RegistryOps<HashCode> hashOps, RegistryOps<Tag> ops) {
        MutableComponent title = Component.empty().append(Component.literal(component.type().toString()).withColor(0xE5C17C));

        HashCode hash = component.encodeValue(hashOps).getOrThrow();
        title.append(Component.literal(" (Hash: " + hash.asInt() + ")").withColor(0x59626F));

        lines.add(title);

        Tag encoded = component.encodeValue(ops).getOrThrow(); // don't throw
        FormattedSnbtPrinter visitor = new FormattedSnbtPrinter();
        Component componentLines = visitor.visit(encoded);

        lines.addAll(this.font.getSplitter().splitLines(componentLines, 512, Style.EMPTY));
        lines.add(FormattedText.EMPTY);
    }

}
