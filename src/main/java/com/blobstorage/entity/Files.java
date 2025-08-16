package com.blobstorage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Files {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private LocalDateTime updateTime;

    @ManyToOne
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @Column(nullable = false)
    private boolean locked = true; // Default value set to true

    // Default constructor
    public Files() {
    }

    // Parameterized constructor
    public Files(Long id, String name, String path, String fileType, LocalDateTime updateTime, Folder folder) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.fileType = fileType;
        this.updateTime = updateTime;
        this.folder = folder;
        this.locked = true; // Ensure locked is true when creating through constructor
    }

    // Getters and Setters
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    // toString
    @Override
    public String toString() {
        return "Files{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", fileType='" + fileType + '\'' +
                ", updateTime=" + updateTime +
                ", folderId=" + (folder != null ? folder.getId() : null) +
                ", locked=" + locked +
                '}';
    }
}
