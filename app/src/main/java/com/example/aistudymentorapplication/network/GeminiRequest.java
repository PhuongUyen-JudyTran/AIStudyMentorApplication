package com.example.aistudymentorapplication.network;

import java.util.ArrayList;
import java.util.List;

/**
 * GeminiRequest defines the JSON structure for the Google Gemini API.
 * Includes support for system instructions and user content with roles.
 */
public class GeminiRequest {
    public List<Content> contents;
    public Content system_instruction;

    public GeminiRequest(String text, String systemPrompt) {
        this.contents = new ArrayList<>();
        // For Gemini API, user messages must have the "user" role
        this.contents.add(new Content("user", text));
        
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            // System instructions should NOT have a role field
            this.system_instruction = new Content(null, systemPrompt);
        }
    }

    public static class Content {
        public String role; // "user" or null for system_instruction
        public List<Part> parts;

        // Constructor for system_instruction (no role) or user content
        public Content(String role, String text) {
            this.role = role;
            this.parts = new ArrayList<>();
            this.parts.add(new Part(text));
        }

        public static class Builder {
            private String textContent;
            private String role;

            public Builder addText(String text) {
                this.textContent = text;
                return this;
            }

            public Builder setRole(String role) {
                this.role = role;
                return this;
            }

            public Content build() {
                return new Content(role, textContent);
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
