package com.example.aistudymentorapplication.network;

import java.util.ArrayList;
import java.util.List;

public class GeminiRequest {
    public List<Content> contents;

    public GeminiRequest(String text) {
        this.contents = new ArrayList<>();
        this.contents.add(new Content(text));
    }

    public static class Content {
        public List<Part> parts;

        public Content(String text) {
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
}
