package org.glpste.logging.log4j2;

import lombok.Getter;

/**
 * Enum representing the configuration properties for the JSON template field configuration.
 * Each enum constant corresponds to a specific configuration property key.
 */
@Getter
public enum ConfigProperty {

    /**
     * The list of packages that are allowed in the stack trace.
     * If set, {@link ConfigProperty#LIST_FILTER} will be ignored.
     */
    LIST_ALLOW("allowedPackages", ""),

    /**
     * The list of packages that are filtered out from the stack trace.
     */
    LIST_FILTER("filteredPackages", ""),

    /**
     * The field name for the exception name in the JSON output.
     */
    NAME("nameField", "name"),

    /**
     * The field name for the exception message in the JSON output.
     */
    MESSAGE("messageField", "message"),

    /**
     * The field name for the stack trace in the JSON output.
     */
    STACK("stackField", "extendedStackTrace"),

    /**
     * The field name for the count of filtered elements in the JSON output.
     */
    COUNT("countField", "totalFilteredElements")
    ;
    
    private final String key;
    private final String defaultValue;

    ConfigProperty(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }
}
