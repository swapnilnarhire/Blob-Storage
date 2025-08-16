package com.blobstorage.response;

import java.time.LocalDateTime;

public class FileResponse {

    private Long id;
    private String name;
    private String fileType;
    private String path;
    private LocalDateTime updateTime;
    private boolean locked;

    // Constructor
    public FileResponse(Long id, String name, String fileType, String path, LocalDateTime updateTime,boolean locked) {
        this.id = id;
        this.name = name;
        this.fileType = fileType;
        this.path = path;
        this.updateTime = updateTime;
        this.locked=locked;
    }

	public FileResponse() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public LocalDateTime getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(LocalDateTime updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	@Override
	public String toString() {
		return "FileResponse [id=" + id + ", name=" + name + ", fileType=" + fileType + ", path=" + path
				+ ", updateTime=" + updateTime + ", locked=" + locked + "]";
	}

  
}
