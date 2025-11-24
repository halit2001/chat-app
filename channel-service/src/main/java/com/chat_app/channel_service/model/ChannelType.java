package com.chat_app.channel_service.model;

public enum ChannelType {
    TEXT,
    ANNOUNCEMENT,
    VOICE;

    public static ChannelType convertStringToType(String channelType) {
        if (channelType == null) return null;
        switch (channelType.toUpperCase()) {
            case "TEXT":
                return TEXT;
            case "VOICE":
                return VOICE;
            case "ANNOUNCEMENT":
                return ANNOUNCEMENT;
            default:
                return null;
        }
    }
}
