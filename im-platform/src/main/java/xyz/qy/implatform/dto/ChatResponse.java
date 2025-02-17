package xyz.qy.implatform.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private Message message;
        private int index;

        @Data
        public static class Message {
            private String role;
            private String content;
        }
    }
}
