package com.example.aistudymentorapplication.network;

import java.util.ArrayList;
import java.util.List;

public class GeminiRequest {
    public List<Content> contents;
    public Content system_instruction; // Field for system prompt

    public GeminiRequest(String text, String systemPrompt) {
        this.contents = new ArrayList<>();
        this.contents.add(new Content(text));
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            this.system_instruction = new Content(systemPrompt);
        }
    }

    public static class Content {
        public List<Part> parts;

        public Content(String text) {
            this.parts = new ArrayList<>();
            this.parts.add(new Part(text));
        }

        // Builder pattern as requested by the user
        public static class Builder {
            private String textContent;

            public Builder addText(String text) {
                this.textContent = text;
                return this;
            }

            public Content build() {
                return new Content(textContent);
            }
        }
    }

    public static class Part {
        public String text;

        public Part(String text) {
            this.text = text;
        }
    }
}
