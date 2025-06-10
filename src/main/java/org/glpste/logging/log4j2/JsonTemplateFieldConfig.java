package org.glpste.logging.log4j2;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper DTO for the JSON template field configuration.
 */
public class JsonTemplateFieldConfig {

    @Getter
    @Setter
    private String nameField = ConfigProperty.NAME.getDefaultValue();

    @Setter
    @Getter
    private String messageField = ConfigProperty.MESSAGE.getDefaultValue();

    @Setter
    @Getter
    private String stackField = ConfigProperty.STACK.getDefaultValue();

    @Setter
    @Getter
    private String countField = ConfigProperty.COUNT.getDefaultValue();

    @Setter
    @Getter
    private List<String> allowedPackages = new ArrayList<>();

    @Setter
    @Getter
    private List<String> filteredPackages = new ArrayList<>();

    /**
     * Default constructor for deserialization purposes.
     * Use {@link #builder()} to create a new instance instead.
     */
    public JsonTemplateFieldConfig() {
        // Default constructor for deserialization
    }

    /**
     * Constructs a new instance of {@link JsonTemplateFieldConfig} with the specified parameters.
     * Null values for fields will be replaced with their respective default values.
     *
     * @param nameField         the field name for the exception name
     * @param messageField      the field name for the exception message
     * @param stackField        the field name for the stack trace
     * @param countField        the field name for the count of filtered elements
     * @param allowedPackages   the list of allowed packages for stack trace filtering
     * @param filteredPackages  the list of filtered packages for stack trace filtering
     */
    public JsonTemplateFieldConfig(String nameField, String messageField, String stackField, String countField, List<String> allowedPackages, List<String> filteredPackages) {
        this.nameField = nameField != null ? nameField : this.nameField;
        this.messageField = messageField != null ? messageField : this.messageField;
        this.stackField = stackField != null ? stackField : this.stackField;
        this.countField = countField != null ? countField : this.countField;

        this.allowedPackages = allowedPackages != null ? allowedPackages : new ArrayList<>();
        this.filteredPackages = filteredPackages != null ? filteredPackages : new ArrayList<>();
    }

    /**
     * Creates a new builder for {@link JsonTemplateFieldConfig}.
     *
     * @return a new instance of {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating instances of {@link JsonTemplateFieldConfig}.
     */
    public static class Builder {
        private String nameField;
        private String messageField;
        private String stackField;
        private String countField;
        private List<String> allowedPackages;
        private List<String> filteredPackages;

        /**
         * Sets the field name for the exception name in the JSON output.
         *
         * @param nameField the field name for the exception name
         * @return this builder instance
         */
        public Builder nameField(String nameField) {
            this.nameField = nameField;
            return this;
        }

        /**
         * Sets the field name for the exception message in the JSON output.
         *
         * @param messageField the field name for the exception message
         * @return this builder instance
         */
        public Builder messageField(String messageField) {
            this.messageField = messageField;
            return this;
        }

        /**
         * Sets the field name for the stack trace in the JSON output.
         *
         * @param stackField the field name for the stack trace
         * @return this builder instance
         */
        public Builder stackField(String stackField) {
            this.stackField = stackField;
            return this;
        }

        /**
         * Sets the field name for the count of filtered stacktrace elements in the JSON output.
         *
         * @param countField the field name for the count of filtered elements
         * @return this builder instance
         */
        public Builder countField(String countField) {
            this.countField = countField;
            return this;
        }

        /**
         * Sets the list of allowed packages for stack trace filtering.
         *
         * @param allowedPackages the list of allowed packages
         * @return this builder instance
         */
        public Builder allowedPackages(List<String> allowedPackages) {
            this.allowedPackages = allowedPackages;
            return this;
        }

        /**
         * Sets the list of filtered packages for stack trace filtering.
         *
         * @param filteredPackages the list of filtered packages
         * @return this builder instance
         */
        public Builder filteredPackages(List<String> filteredPackages) {
            this.filteredPackages = filteredPackages;
            return this;
        }

        /**
         * Builds a new instance of {@link JsonTemplateFieldConfig} with the configured values.
         *
         * @return a new instance of {@link JsonTemplateFieldConfig}
         */
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
