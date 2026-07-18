package com.example.aistudymentorapplication.network;

import java.util.ArrayList;
import java.util.List;

/**
 * GeminiRequest defines the JSON structure for the Google Gemini API.
 * Updated to support generation configuration and conversational history.
 */
public class GeminiRequest {
    public List<Content> contents;
    public Content system_instruction;
    public GenerationConfig generationConfig;

    /**
     * Constructor for multi-turn conversations with history.
     */
    public GeminiRequest(List<com.example.aistudymentorapplication.model.ChatMessage> history, String systemPrompt) {
        this.contents = new ArrayList<>();
        for (com.example.aistudymentorapplication.model.ChatMessage msg : history) {
            String role = "user".equals(msg.getSender()) ? "user" : "model";
            this.contents.add(new Content(role, msg.getMessage()));
        }

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            this.system_instruction = new Content(null, systemPrompt);
        }

        // Improvement #1: Set stable generation config for education
        this.generationConfig = new GenerationConfig();
    }

    public static class Content {
        public String role; 
        public List<Part> parts;

        public Content(String role, String text) {
            this.role = role;
            this.parts = new ArrayList<>();
            this.parts.add(new Part(text));
        }
    }

    public static class Part {
        public String text;
        public Part(String text) {
            this.text = text;
        }
    }

    /**
     * Configuration class to control AI output behavior.
     */
    public static class GenerationConfig {
        public float temperature = 0.4f; // Lower temperature = more factual and consistent
        public int topK = 40;
        public float topP = 0.95f;
        public int maxOutputTokens = 2048; // Ensure long enough answers for explanations
    }
}
