package com.blobstorage.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateFolderRequest {

	@NotBlank(message="Folder name is required")
	@Pattern(regexp = "^[a-z][a-zA-Z0-9_-]*$",message = "Folder name must start with a lowercase letter (a-z) and can contain uppercase letters (A-Z), numbers, hyphens (-), and underscores (_).")
    private String name;
	
    private Long parentFolderId;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(Long parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

	@Override
	public String toString() {
		return "CreateFolderRequest [name=" + name + ", parentFolderId=" + parentFolderId + "]";
	}
    
    
}
