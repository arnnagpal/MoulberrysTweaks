package com.moulberry.moulberrystweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

public class PackFolderWatcher {

    private static WatchService watcher = null;
    private static boolean triedToInitialize = false;

    private static void initialize() {
        triedToInitialize = true;

        Path packDirectory = Minecraft.getInstance().getResourcePackDirectory();
        try {
            watcher = packDirectory.getFileSystem().newWatchService();

            Files.walkFileTree(packDirectory, new FileVisitor<>() {
                @Override
                public @NotNull FileVisitResult preVisitDirectory(Path path, @NotNull BasicFileAttributes basicFileAttributes) {
                    watchDir(path);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public @NotNull FileVisitResult visitFile(Path path, @NotNull BasicFileAttributes basicFileAttributes) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public @NotNull FileVisitResult visitFileFailed(Path path, @NotNull IOException e) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public @NotNull FileVisitResult postVisitDirectory(Path path, @Nullable IOException e) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {
        }
    }

    private static void watchDir(Path path) {
        try {
            path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException ignored) {}
    }

    public static void tick() {
        if (!MoulberrysTweaks.config.resourcePack.automaticPackReload) {
            if (watcher != null) {
                try {
                    watcher.close();
                } catch (IOException ignored) {}
                watcher = null;
                triedToInitialize = false;
            }
            return;
        }

        if (!triedToInitialize) {
            initialize();
        }
        if (watcher == null) {
            return;
        }

        boolean changeOccurred = false;

        WatchKey watchKey;
        while ((watchKey = watcher.poll()) != null) {
            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                changeOccurred = true;
                if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    Path packDirectory = Minecraft.getInstance().getResourcePackDirectory();
                    Path path = packDirectory.resolve((Path)watchEvent.context());
                    if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                        watchDir(path);
                    }
                }
            }

            watchKey.reset();
        }

        if (changeOccurred) {
            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen instanceof PackSelectionScreen) {
                // Pack will be reloaded when we exit the screen anyways, so
                // reloading it ourselves is unnecessary
                return;
            }
            Minecraft.getInstance().reloadResourcePacks();
        }
    }

}
