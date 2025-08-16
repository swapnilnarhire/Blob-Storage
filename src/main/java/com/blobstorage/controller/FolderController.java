package com.blobstorage.controller;

import com.blobstorage.exception.CustomException;
import com.blobstorage.request.CreateFolderRequest;
import com.blobstorage.services.FolderService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

	@Autowired
	private FolderService folderService;

	@PostMapping("/addFolder")
	public ResponseEntity<Object> createFolder(@Valid @RequestBody CreateFolderRequest request, BindingResult br) {

	    if (br.hasErrors()) {
	        // Create a message with the error details from BindingResult
	        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
	        br.getAllErrors().forEach(error -> {
	            errorMessage.append(error.getDefaultMessage()).append(" ");
	        });

	        // Throw a custom exception with the error message and a status code (e.g., 400 for bad request)
	        throw new CustomException(errorMessage.toString(), 400);
	    }

	    return folderService.createFolder(request);
	}


	@GetMapping("/getFolderById/{id}")
	public ResponseEntity<Object> getFolderById(@PathVariable Long id) {
		return folderService.getFolderById(id);
	}

	@GetMapping("/getAllFolders")
	public ResponseEntity<Object> getAllFolders() {
		return folderService.getFolders();
	}

	@GetMapping("/getParentFolders")
	public ResponseEntity<Object> getFoldersByParentId(@RequestParam(required = false) Long parentFolderId) {
		return folderService.getFoldersByParentId(parentFolderId);
	}

	@PutMapping("renameFolder/{id}")
	public ResponseEntity<Object> renameFolder(@PathVariable Long id, @RequestBody Map<String, String> request) {
		String newName = request.get("newName");

		if (newName == null || newName.trim().isEmpty()) {
			throw new CustomException("New folder name must be provided", 400);
		}

		return folderService.renameFolder(id, newName);

	}
	
	@DeleteMapping("deleteFolder/{id}")
	public ResponseEntity<Object> deleteFolder(@PathVariable Long id) {
		return folderService.deleteFolder(id);
	}

}
