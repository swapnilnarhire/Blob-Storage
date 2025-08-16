package com.blobstorage.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RenameFileRequest {


	@NotBlank(message="Folder name is required")
	@Pattern(
		    regexp = "^[a-z][a-zA-Z0-9_-]*$",
		    message = "Folder name must start with a lowercase letter (a-z) and can contain uppercase letters (A-Z), numbers, hyphens (-), and underscores (_)."
		)
    private String newName;
    private String newFolderPath;

    // Getters and setters
    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getNewFolderPath() {
        return newFolderPath;
    }

    public void setNewFolderPath(String newFolderPath) {
        this.newFolderPath = newFolderPath;
    }
}
