package com.moulberry.moulberrystweaks.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

import java.util.ArrayList;
import java.util.List;

public class PacketViewerWidget extends FloatingTextWidget {

    private long lastPacketTime = 0;

    public PacketViewerWidget() {
        super("Packet Viewer");
        List<FormattedText> list = new ArrayList<>();
        list.add(Component.literal("Logging inventory-related packets...").withStyle(ChatFormatting.GRAY));
        this.setLines(list);
    }

    public void addPacket(boolean outgoing, Packet<?> packet) {
        StringBuilder builder = new StringBuilder();

        if (outgoing) {
            builder.append("=> ");
        } else {
            builder.append("<= ");
        }

        MutableComponent extra = null;
        int splitExtra = 0;

        if (packet instanceof ServerboundContainerClickPacket containerClick) {
            builder.append("ContainerClick\n  ");
            builder.append("ContainerId=").append(containerClick.containerId()).append("  ");
            builder.append("StateId=").append(containerClick.stateId()).append("  ");
            builder.append("SlotNum=").append(containerClick.slotNum()).append("  ");
            builder.append("ClickType=").append(containerClick.clickType()).append("  ");
        } else if (packet instanceof ServerboundContainerClosePacket containerClose) {
            builder.append("ContainerClose\n  ");
            builder.append("ContainerId=").append(containerClose.getContainerId()).append("  ");
        } else if (packet instanceof ClientboundContainerSetSlotPacket containerSetSlot) {
            builder.append("ContainerSetSlot\n  ");
            builder.append("ContainerId=").append(containerSetSlot.getContainerId()).append("  ");
            builder.append("StateId=").append(containerSetSlot.getStateId()).append("  ");
            builder.append("Slot=").append(containerSetSlot.getSlot()).append("  ");

            extra = Component.literal("ItemStack").withStyle(Style.EMPTY.withUnderlined(true).withHoverEvent(new HoverEvent.ShowItem(containerSetSlot.getItem().copy())));
        } else if (packet instanceof ClientboundContainerSetContentPacket containerSetContent) {
            builder.append("ContainerSetContent\n  ");
            builder.append("ContainerId=").append(containerSetContent.containerId()).append("  ");
            builder.append("StateId=").append(containerSetContent.stateId()).append("  ");
            builder.append("Size=").append(containerSetContent.items().size()).append("  ");

            MutableComponent contents = Component.empty();
            for (int i = 0; i < containerSetContent.items().size(); i++) {
                contents.append(Component.literal(String.valueOf(i)).withStyle(Style.EMPTY.withUnderlined(true).withHoverEvent(new HoverEvent.ShowItem(containerSetContent.items().get(i).copy()))));
                if (i != containerSetContent.items().size() - 1) {
                    contents.append(",");
                }
            }
            extra = contents;
            splitExtra = 256;
        } else if (packet instanceof ClientboundContainerClosePacket containerClose) {
            builder.append("ContainerClose\n  ");
            builder.append("ContainerId=").append(containerClose.getContainerId()).append("  ");
        } else if (packet instanceof ClientboundOpenScreenPacket openScreen) {
            builder.append("OpenScreen\n  ");
            builder.append("ContainerId=").append(openScreen.getContainerId()).append("  ");
            builder.append("Type=").append(BuiltInRegistries.MENU.getKey(openScreen.getType())).append("  ");

            extra = Component.literal("Title").withStyle(Style.EMPTY.withUnderlined(true).withHoverEvent(new HoverEvent.ShowText(openScreen.getTitle())));
        } else {
            return;
        }


        MutableComponent component = Component.literal(builder.toString());

        if (outgoing) {
            component.withColor(0x99ff99);
        } else {
            component.withColor(0xff9999);
        }

        if (extra != null && splitExtra == 0) {
            component.append(extra);
        }

        List<FormattedText> lines = this.font.getSplitter().splitLines(component, 512, Style.EMPTY);

        if (extra != null && splitExtra != 0) {
            if (outgoing) {
                extra.withColor(0x99ff99);
            } else {
                extra.withColor(0xff9999);
            }
            for (FormattedText line : this.font.getSplitter().splitLines(extra, splitExtra, Style.EMPTY)) {
                lines.add(FormattedText.composite(Component.literal("  "), line));
            }
        }

        var currentLines = this.getLines();
        if (currentLines == null || currentLines.isEmpty()) {
            lastPacketTime = System.currentTimeMillis();
        } else {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastPacketTime > 500) {
                lines.add(0, Component.literal("...").withStyle(ChatFormatting.GRAY));
            }
            lastPacketTime = currentTime;
        }

        this.addLines(lines);
    }

}
