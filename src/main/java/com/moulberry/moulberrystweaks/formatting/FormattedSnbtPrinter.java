package com.moulberry.moulberrystweaks.formatting;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class FormattedSnbtPrinter implements TagVisitor {
    private static final int STRING_RGB = 0x98C379;
    private static final int NUMBER_RGB = 0xD19A66;
    private static final int STRUCTURE_RGB = 0xA6B2C0;
    private static final int FIELD_RGB = 0xC679DD;

    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    private static final String NAME_VALUE_SEPARATOR = String.valueOf(':');
    private static final String ELEMENT_SEPARATOR = String.valueOf(',');
    private static final String LIST_OPEN = "[";
    private static final String LIST_CLOSE = "]";
    private static final String LIST_TYPE_SEPARATOR = ";";
    private static final String STRUCT_OPEN = "{";
    private static final String STRUCT_CLOSE = "}";
    private final String indentation;
    private final int depth;
    private MutableComponent result = Component.empty();

    public FormattedSnbtPrinter() {
        this("  ", 0);
    }

    public FormattedSnbtPrinter(String indentation, int depth) {
        this.indentation = indentation;
        this.depth = depth;
    }

    public MutableComponent visit(Tag tag) {
        tag.accept(this);
        return this.result;
    }

    @Override
    public void visitString(StringTag tag) {
        this.result = Component.literal(StringTag.quoteAndEscape(tag.value())).withColor(STRING_RGB);
    }

    @Override
    public void visitByte(ByteTag tag) {
        this.result = Component.literal(tag.value() + "b").withColor(NUMBER_RGB);
    }

    @Override
    public void visitShort(ShortTag tag) {
        this.result = Component.literal(tag.value() + "s").withColor(NUMBER_RGB);
    }

    @Override
    public void visitInt(IntTag tag) {
        this.result = Component.literal(String.valueOf(tag.value())).withColor(NUMBER_RGB);
    }

    @Override
    public void visitLong(LongTag tag) {
        this.result = Component.literal(tag.value() + "L").withColor(NUMBER_RGB);
    }

    @Override
    public void visitFloat(FloatTag tag) {
        this.result = Component.literal(tag.value() + "f").withColor(NUMBER_RGB);
    }

    @Override
    public void visitDouble(DoubleTag tag) {
        this.result = Component.literal(tag.value() + "d").withColor(NUMBER_RGB);
    }

    @Override
    public void visitByteArray(ByteArrayTag tag) {
        this.result.append(Component.literal("[B;"));

        StringBuilder stringBuilder = new StringBuilder();
        byte[] bs = tag.getAsByteArray();

        for (int i = 0; i < bs.length; i++) {
            stringBuilder.append(" ").append(bs[i]).append("B");
            if (i != bs.length - 1) {
                stringBuilder.append(ELEMENT_SEPARATOR);
            }
        }

        this.result.append(Component.literal(stringBuilder.toString()).withColor(NUMBER_RGB));
        this.result.append(LIST_CLOSE);
        this.result.withColor(STRUCTURE_RGB);
    }

    @Override
    public void visitIntArray(IntArrayTag tag) {
        this.result.append(Component.literal("[I;"));

        StringBuilder stringBuilder = new StringBuilder();
        int[] is = tag.getAsIntArray();

        for (int i = 0; i < is.length; i++) {
            stringBuilder.append(" ").append(is[i]);
            if (i != is.length - 1) {
                stringBuilder.append(ELEMENT_SEPARATOR);
            }
        }

        this.result.append(Component.literal(stringBuilder.toString()).withColor(NUMBER_RGB));
        this.result.append(LIST_CLOSE);
        this.result.withColor(STRUCTURE_RGB);
    }

    @Override
    public void visitLongArray(LongArrayTag tag) {
        this.result.append(Component.literal("[L;"));

        StringBuilder stringBuilder = new StringBuilder();
        long[] ls = tag.getAsLongArray();

        for (int i = 0; i < ls.length; i++) {
            stringBuilder.append(" ").append(ls[i]).append("L");
            if (i != ls.length - 1) {
                stringBuilder.append(ELEMENT_SEPARATOR);
            }
        }

        this.result.append(Component.literal(stringBuilder.toString()).withColor(NUMBER_RGB));
        this.result.append(LIST_CLOSE);
        this.result.withColor(STRUCTURE_RGB);
    }

    @Override
    public void visitList(ListTag tag) {
        if (tag.isEmpty()) {
            this.result = Component.literal("[]").withColor(STRUCTURE_RGB);
        } else {
            this.result.append(LIST_OPEN);

            StringBuilder stringBuilder = new StringBuilder();
            String string = this.indentation;
            if (!string.isEmpty()) {
                stringBuilder.append("\n");
            }

            for (int i = 0; i < tag.size(); i++) {
                stringBuilder.append(Strings.repeat(string, this.depth + 1));

                this.result.append(Component.literal(stringBuilder.toString()));
                stringBuilder = new StringBuilder();

                this.result.append(new FormattedSnbtPrinter(string, this.depth + 1).visit(tag.get(i)));

                if (i != tag.size() - 1) {
                    stringBuilder.append(ELEMENT_SEPARATOR).append(string.isEmpty() ? " " : "\n");
                }
            }

            if (!string.isEmpty()) {
                stringBuilder.append("\n").append(Strings.repeat(string, this.depth));
            }

            stringBuilder.append(LIST_CLOSE);
            this.result.append(Component.literal(stringBuilder.toString()));
            this.result.withColor(STRUCTURE_RGB);
        }
    }

    @Override
    public void visitCompound(CompoundTag tag) {
        if (tag.isEmpty()) {
            this.result = Component.literal("{}").withColor(STRUCTURE_RGB);
        } else {
            this.result.append(STRUCT_OPEN);

            StringBuilder stringBuilder = new StringBuilder();
            String string = this.indentation;
            if (!string.isEmpty()) {
                stringBuilder.append("\n");
            }

            Collection<String> collection = this.getKeys(tag);
            Iterator<String> iterator = collection.iterator();

            while (iterator.hasNext()) {
                String key = iterator.next();
                Tag tag2 = tag.get(key);

                stringBuilder.append(Strings.repeat(string, this.depth + 1));
                this.result.append(Component.literal(stringBuilder.toString()));
                stringBuilder = new StringBuilder();

                this.result.append(Component.literal(handleEscapePretty(key)).withColor(FIELD_RGB));

                this.result.append(NAME_VALUE_SEPARATOR + " ");

                this.result.append(new FormattedSnbtPrinter(string, this.depth + 1).visit(tag2));

                if (iterator.hasNext()) {
                    stringBuilder.append(ELEMENT_SEPARATOR).append(string.isEmpty() ? " " : "\n");
                }
            }

            if (!string.isEmpty()) {
                stringBuilder.append("\n").append(Strings.repeat(string, this.depth));
            }

            stringBuilder.append(STRUCT_CLOSE);
            this.result.append(Component.literal(stringBuilder.toString()));
            this.result.withColor(STRUCTURE_RGB);
        }
    }

    protected List<String> getKeys(CompoundTag tag) {
        List<String> list = Lists.newArrayList();
        list.addAll(tag.keySet());
        Collections.sort(list);

        return list;
    }

    protected static String handleEscapePretty(String text) {
        return SIMPLE_VALUE.matcher(text).matches() ? text : StringTag.quoteAndEscape(text);
    }

    @Override
    public void visitEnd(EndTag tag) {
    }
}
