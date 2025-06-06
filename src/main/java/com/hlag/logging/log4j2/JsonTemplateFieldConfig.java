package com.hlag.logging.log4j2;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper DTO for the JSON template field configuration.
 */
public class JsonTemplateFieldConfig {

    public static final String FIELD_DEFAULT_NAME       = "name";
    public static final String FIELD_DEFAULT_MESSAGE    = "message";
    public static final String FIELD_DEFAULT_STACK      = "extendedStackTrace";
    public static final String FIELD_DEFAULT_COUNT      = "totalFilteredElements";

    @Getter
    @Setter
    private String nameField = FIELD_DEFAULT_NAME;

    @Setter
    @Getter
    private String messageField = FIELD_DEFAULT_MESSAGE;

    @Setter
    @Getter
    private String stackField = FIELD_DEFAULT_STACK;

    @Setter
    @Getter
    private String countField = FIELD_DEFAULT_COUNT;

    @Setter
    @Getter
    private List<String> allowedPackages;

    @Setter
    @Getter
    private List<String> filteredPackages;

    public JsonTemplateFieldConfig() {
        // Default constructor for deserialization
    }

    public JsonTemplateFieldConfig(String nameField, String messageField, String stackField, String countField, List<String> allowedPackages, List<String> filteredPackages) {
        this.nameField = nameField != null ? nameField : FIELD_DEFAULT_NAME;
        this.messageField = messageField != null ? messageField : FIELD_DEFAULT_MESSAGE;
        this.stackField = stackField != null ? stackField : FIELD_DEFAULT_STACK;
        this.countField = countField != null ? countField : FIELD_DEFAULT_COUNT;
        this.allowedPackages = allowedPackages != null ? allowedPackages : Collections.emptyList();
        this.filteredPackages = filteredPackages != null ? filteredPackages : Collections.emptyList();
    }

    public static class Builder {
        private String nameField;
        private String messageField;
        private String stackField;
        private String countField;
        private List<String> allowedPackages;
        private List<String> filteredPackages;

        public Builder nameField(String nameField) {
            this.nameField = nameField;
            return this;
        }

        public Builder messageField(String messageField) {
            this.messageField = messageField;
            return this;
        }

        public Builder stackField(String stackField) {
            this.stackField = stackField;
            return this;
        }

        public Builder countField(String countField) {
            this.countField = countField;
            return this;
        }

        public Builder allowedPackages(List<String> allowedPackages) {
            this.allowedPackages = allowedPackages;
            return this;
        }

        public Builder filteredPackages(List<String> filteredPackages) {
            this.filteredPackages = filteredPackages;
            return this;
        }

        public JsonTemplateFieldConfig build() {
            return new JsonTemplateFieldConfig(
                    nameField,
                    messageField,
                    stackField,
                    countField,
                    allowedPackages,
                    filteredPackages
            );
        }
    }
}
