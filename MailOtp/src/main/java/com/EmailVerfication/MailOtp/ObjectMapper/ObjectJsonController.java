// File: com/EmailVerfication/MailOtp/ObjectMapper/ObjectJsonController.java
package com.EmailVerfication.MailOtp.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.EmailVerfication.MailOtp.DTOForTesting.CustomDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper; // Ensure correct import

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data")
public class ObjectJsonController {

	private static final Logger log = LoggerFactory.getLogger(ObjectJsonController.class);

	@Autowired
	private JsonFieldValidator jsonFieldValidator;

	@Autowired
	private ObjectMapper objectMapper; // Inject Spring Boot's configured ObjectMapper

//    @Autowired // Keep UserJsonProcessor if needed for other purposes, but not essential for this validation approach
//    private UserJsonProcessor userJsonProcessor;

	@PostMapping("/validate") // Changed endpoint name for clarity
	public ResponseEntity<?> validateJsonPayload(@RequestBody String jsonInputString) { // Accept raw string first

		if (jsonInputString == null || jsonInputString.trim().isEmpty()) {
			return ResponseEntity.badRequest().body("Request body is empty.");
		}

		try {
			// Step 1: Attempt deserialization into DTO.
			// This implicitly handles/ignores extra fields due to @JsonIgnoreProperties
			// It will fail on fundamental type mismatches (e.g., String for Int).
			CustomDataDto customDataDto = objectMapper.readValue(jsonInputString, CustomDataDto.class);
			log.info("Successfully deserialized JSON to CustomDataDto (extra fields ignored). DTO: {}", customDataDto);

			// Step 2: Deserialize to a Map for detailed validation traversal
			Map<String, Object> jsonDataMap = objectMapper.readValue(jsonInputString,
					new TypeReference<Map<String, Object>>() {
					});
			log.debug("JSON data as Map: {}", jsonDataMap);

			// Step 3: Perform the missing field validation
			List<String> missingFields = jsonFieldValidator.findMissingFields(jsonDataMap, CustomDataDto.class);

			// Step 4: Return result
			if (missingFields.isEmpty()) {
				return ResponseEntity.ok("Validation successful: No missing fields found.");
			} else {
				log.warn("Validation failed. Missing fields: {}", missingFields);
				Map<String, Object> responseMap = new HashMap<>();
				responseMap.put("message", "Validation failed: Required fields are missing.");
				responseMap.put("missingFields", missingFields);

				return ResponseEntity.badRequest().body(responseMap); 
			}

		} catch (JsonProcessingException e) {
			// Error during initial parsing or type conversion
			log.error("Error processing JSON input: {}", e.getMessage());
			return ResponseEntity.badRequest().body("Invalid JSON format or structure: " + e.getMessage());
		} catch (Exception e) {
			// Catch unexpected errors
			log.error("An unexpected error occurred during validation: {}", e.getMessage(), e);
			return ResponseEntity.status(500).body("Internal server error during validation: " + e.getMessage());
		}
	}

	@PostMapping("/test")
	public String testing() {
		return "hello";
	}
}