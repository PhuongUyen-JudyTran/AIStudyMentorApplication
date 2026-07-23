package com.example.aistudymentorapplication.network;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * GeminiRequest defines the JSON structure for the Google Gemini API.
 */
public class GeminiRequest {
    public List<Content> contents;
    @SerializedName("system_instruction")
    public Content systemInstruction;
    public GenerationConfig generationConfig;

    public GeminiRequest(List<com.example.aistudymentorapplication.model.ChatMessage> history, String systemPrompt) {
        this.contents = new ArrayList<>();
        if (history != null) {
            for (com.example.aistudymentorapplication.model.ChatMessage msg : history) {
                String role = "user".equals(msg.getSender()) ? "user" : "model";
                this.contents.add(new Content(role, msg.getMessage()));
            }
        }

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            this.systemInstruction = new Content(null, systemPrompt);
        }

        this.generationConfig = new GenerationConfig();
    }

    /**
     * Constructor for Quiz generation with Structured Output.
     */
    public GeminiRequest(String prompt, String systemPrompt, boolean useJson) {
        this.contents = new ArrayList<>();
        this.contents.add(new Content("user", prompt));
        
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            this.systemInstruction = new Content(null, systemPrompt);
        }

        this.generationConfig = new GenerationConfig();
        if (useJson) {
            this.generationConfig.responseMimeType = "application/json";
            // We can define the schema here if needed, but for now application/json is often enough 
            // if we provide instructions in the prompt. 
            // However, the user explicitly asked for responseSchema.
            this.generationConfig.responseSchema = createQuizSchema();
        }
    }

    private ResponseSchema createQuizSchema() {
        ResponseSchema schema = new ResponseSchema();
        schema.type = "ARRAY";
        schema.items = new ObjectSchema();
        schema.items.type = "OBJECT";
        schema.items.required = new String[]{"question", "options", "correctIndex", "explanation"};
        schema.items.properties = new PropertiesSchema();
        
        schema.items.properties.question = new PropertySchema("STRING");
        schema.items.properties.options = new PropertySchema("ARRAY", new PropertySchema("STRING"));
        schema.items.properties.correctIndex = new PropertySchema("INTEGER");
        schema.items.properties.explanation = new PropertySchema("STRING");
        
        return schema;
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

    public static class GenerationConfig {
        public float temperature = 0.4f;
        public int topK = 40;
        public float topP = 0.95f;
        public int maxOutputTokens = 2048;
        @SerializedName("response_mime_type")
        public String responseMimeType;
        @SerializedName("response_schema")
        public ResponseSchema responseSchema;
    }

    public static class ResponseSchema {
        public String type;
        public ObjectSchema items;
    }

    public static class ObjectSchema {
        public String type;
        public String[] required;
        public PropertiesSchema properties;
    }

    public static class PropertiesSchema {
        public PropertySchema question;
        public PropertySchema options;
        @SerializedName("correctIndex")
        public PropertySchema correctIndex;
        public PropertySchema explanation;
    }

    public static class PropertySchema {
        public String type;
        public PropertySchema items;

        public PropertySchema(String type) {
            this.type = type;
        }

        public PropertySchema(String type, PropertySchema items) {
            this.type = type;
            this.items = items;
        }
    }
}
