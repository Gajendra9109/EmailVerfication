
package com.EmailVerfication.MailOtp.ObjectMapper;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections; // Import Collections
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JsonFieldValidator {

    private static final Logger log = LoggerFactory.getLogger(JsonFieldValidator.class);

    /**
     * Finds fields defined in the DTO class hierarchy that are missing in the corresponding JSON data Map.
     *
     * @param jsonData The JSON data represented as a nested Map.
     * @param dtoClass The top-level DTO class to validate against.
     * @return A list of missing field paths (e.g., "header.id", "tData[0].address.street1"). Returns an empty list if no fields are missing.
     */
    public List<String> findMissingFields(Map<String, Object> jsonData, Class<?> dtoClass) {
        if (jsonData == null || dtoClass == null) {
            return Collections.emptyList(); // Or throw an IllegalArgumentException
        }
        List<String> missingFields = new ArrayList<>();
        findMissingFieldsRecursive(jsonData, dtoClass, "", missingFields);
        return missingFields;
    }

    private void findMissingFieldsRecursive(Object currentNode, Class<?> currentDtoClass, String currentPath, List<String> missingFields) {
        if (currentDtoClass == null || currentDtoClass.isPrimitive() || currentDtoClass.getName().startsWith("java.lang")) {
            return; // Stop recursion for primitives or basic Java types
        }

        // Handle cases where the expected DTO part is an object, but JSON provides null or wrong type
        if (!(currentNode instanceof Map)) {
            // If we expect an object (Map in JSON terms) but don't have one,
            // all fields within this part of the DTO are effectively missing *relative to this node*.
            // However, the parent check should have already added the parent field if it was missing entirely.
            // If the parent existed but contained null/wrong type, we log it but don't list *all* sub-fields.
             if (currentNode != null && !currentPath.isEmpty()) {
                 log.warn("Type mismatch or null value at path '{}': Expected an object structure for DTO class '{}', but found type '{}'. Cannot check sub-fields.",
                         currentPath, currentDtoClass.getSimpleName(), currentNode.getClass().getSimpleName());
             } else if (currentNode == null && !currentPath.isEmpty()){
                 log.trace("Null value encountered at path '{}' while expecting DTO class '{}'. The parent field itself might be listed as missing if applicable.", currentPath, currentDtoClass.getSimpleName());
             }
            // Don't proceed further down this path if the structure doesn't match
            return;
        }

        Map<String, Object> currentJsonMap = (Map<String, Object>) currentNode;

        // Iterate through all fields declared in the current DTO class and its superclasses
        for (Field field : getAllFields(currentDtoClass)) {
            if (isIgnorableField(field)) {
                continue; // Skip static, transient, synthetic fields
            }

            String fieldName = field.getName();
            String fieldPath = currentPath.isEmpty() ? fieldName : currentPath + "." + fieldName;
            field.setAccessible(true); // Allow access to private fields

            // 1. Check if the field exists in the current JSON map level
            if (!currentJsonMap.containsKey(fieldName)) {
                missingFields.add(fieldPath);
                // If the field itself is missing, don't recurse further for it
                continue;
            }

            // 2. Field exists, now check its value and recurse if necessary
            Object jsonValue = currentJsonMap.get(fieldName);
            Class<?> fieldType = field.getType();

             // Check for null value in JSON - often acceptable unless specifically required
            if (jsonValue == null) {
                 // You *could* add logic here to report fields that are present but null,
                 // if that's a requirement. For now, we assume null is acceptable if the key exists.
                 log.trace("Field '{}' exists but has a null value.", fieldPath);
                 continue; // Don't recurse into a null value
            }


            // 3. Recurse for nested objects
            if (isCustomUserClass(fieldType) && !fieldType.isEnum()) {
                 if (jsonValue instanceof Map) {
                     findMissingFieldsRecursive(jsonValue, fieldType, fieldPath, missingFields);
                 } else {
                     // Type mismatch: DTO expects object, JSON has something else (e.g., String, List)
                     log.warn("Type mismatch for field '{}': Expected a JSON object for DTO field type '{}', but found '{}'. Cannot validate sub-fields.",
                              fieldPath, fieldType.getSimpleName(), jsonValue.getClass().getSimpleName());
                     // Consider adding fieldPath to missingFields if strict type matching is needed,
                     // but generally, the presence check is the main goal here.
                 }
            }
            // 4. Recurse for Lists/Collections of custom objects
            else if (Collection.class.isAssignableFrom(fieldType)) {
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) genericType;
                    Type listContentType = pt.getActualTypeArguments()[0];

                    if (listContentType instanceof Class && isCustomUserClass((Class<?>) listContentType)) {
                        Class<?> listElementDtoClass = (Class<?>) listContentType;

                        if (jsonValue instanceof List) {
                            List<?> jsonList = (List<?>) jsonValue;
                            for (int i = 0; i < jsonList.size(); i++) {
                                Object listItem = jsonList.get(i);
                                String listItemPath = fieldPath + "[" + i + "]";
                                if (listItem instanceof Map) {
                                    // Validate each object within the JSON list against the DTO list element type
                                    findMissingFieldsRecursive(listItem, listElementDtoClass, listItemPath, missingFields);
                                } else {
                                     log.warn("Type mismatch in list '{}': Expected a JSON object at index {} for DTO type '{}', but found '{}'. Cannot validate item sub-fields.",
                                              fieldPath, i, listElementDtoClass.getSimpleName(), (listItem == null ? "null" : listItem.getClass().getSimpleName()));
                                }
                            }
                        } else {
                            // Type mismatch: DTO expects a List, JSON has something else
                             log.warn("Type mismatch for field '{}': Expected a JSON array for DTO field type '{}', but found '{}'. Cannot validate list items.",
                                      fieldPath, fieldType.getSimpleName(), jsonValue.getClass().getSimpleName());
                        }
                    }
                    // Else: It's a list of Strings, Numbers, etc. - only the presence of the list field itself matters, which we've already checked.
                }
            }
            // Else: Field is a primitive, String, Enum, etc. The presence check is sufficient.
        }
    }

    // Helper to get all fields including inherited ones (excluding Object class)
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            Collections.addAll(fields, clazz.getDeclaredFields());
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    // Helper to identify fields to skip during validation
    private boolean isIgnorableField(Field field) {
        return java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
               java.lang.reflect.Modifier.isTransient(field.getModifiers()) ||
               field.isSynthetic();
    }

    // Helper to check if a class is one of our custom DTOs/Entities
    // Adjust the package name check as needed for your project structure
    private boolean isCustomUserClass(Class<?> clazz) {
        return !clazz.isPrimitive() &&
               !clazz.isEnum() &&
               !clazz.getName().startsWith("java.") &&
               !clazz.getName().startsWith("javax.") &&
               // Add specific package checks if needed, e.g.:
                clazz.getPackage() != null && clazz.getPackage().getName().startsWith("com.EmailVerfication.MailOtp.DTOForTesting");
    }
}