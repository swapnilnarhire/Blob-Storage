package com.blobstorage.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.blobstorage.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
		// Print error details in the console
		ex.printStackTrace();

		// Use the custom message from the exception if provided
		String errorMessage = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";

		// Create and return the error response with custom message
		ErrorResponse errorResponse = new ErrorResponse("error", errorMessage);
		return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getStatusCode()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
		// Print error details in the console
		ex.printStackTrace();

		// Return a custom message for invalid input
		ErrorResponse errorResponse = new ErrorResponse("error", "Invalid input provided");
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
		// Print error details in the console
		ex.printStackTrace();

		// Return a generic error message for runtime exceptions
		ErrorResponse errorResponse = new ErrorResponse("error", "An error occurred during processing");
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
}
