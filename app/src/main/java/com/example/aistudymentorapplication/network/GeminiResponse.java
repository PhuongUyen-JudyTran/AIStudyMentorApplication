package com.example.aistudymentorapplication.network;

import java.util.List;

public class GeminiResponse {
    public List<Candidate> candidates;

    public static class Candidate {
        public Content content;
    }

    public static class Content {
        public List<Part> parts;
    }

    public static class Part {
        public String text;
    }

    public String getText() {
        if (candidates != null && !candidates.isEmpty() &&
            candidates.get(0).content != null &&
            candidates.get(0).content.parts != null) {
            
            StringBuilder fullText = new StringBuilder();
            for (Part part : candidates.get(0).content.parts) {
                if (part.text != null) {
                    fullText.append(part.text);
                }
            }
            return fullText.toString().isEmpty() ? "No response from AI." : fullText.toString();
        }
        return "No response from AI.";
    }
}
