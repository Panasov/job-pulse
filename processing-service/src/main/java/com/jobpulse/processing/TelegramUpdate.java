package com.jobpulse.processing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUpdate {
    @JsonProperty("update_id")
    private Long updateId;

    @JsonProperty("channel_post")
    private ChannelPost channelPost;

    public Long getUpdateId() { return updateId; }
    public ChannelPost getChannelPost() { return channelPost; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChannelPost {
        @JsonProperty("message_id")
        private Long messageId;
        private String text;
        private Chat chat;

        public Long getMessageId() { return messageId; }
        public String getText() { return text; }
        public Chat getChat() { return chat; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Chat {
        private String username;
        public String getUsername() { return username; }
    }
}