package com.moulberry.moulberrystweaks;

public class AutoVanishPlayers {

    public static boolean isEnabled = false;
    private static ServerState serverState = ServerState.CLIENT_OR_DEFAULT;
    private static boolean clientState = false;

    public static void setClientState(boolean clientState) {
        if (serverState == ServerState.ON || serverState == ServerState.OFF) {
            serverState = ServerState.CLIENT_OR_DEFAULT;
        }
        AutoVanishPlayers.clientState = clientState;
        calculateIsEnabled();
    }

    public static void setServerState(ServerState serverState) {
        AutoVanishPlayers.serverState = serverState;
        calculateIsEnabled();
    }

    public static ServerState serverState() {
        return AutoVanishPlayers.serverState;
    }

    public static boolean clientState() {
        return AutoVanishPlayers.clientState;
    }

    private static void calculateIsEnabled() {
        AutoVanishPlayers.isEnabled = switch (AutoVanishPlayers.serverState) {
            case CLIENT_OR_DEFAULT -> AutoVanishPlayers.clientState;
            case FORCE_ON, ON -> true;
            case FORCE_OFF, OFF -> false;
        };
    }

}
