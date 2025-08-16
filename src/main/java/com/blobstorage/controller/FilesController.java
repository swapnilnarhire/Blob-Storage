package com.blobstorage.controller;

import com.blobstorage.request.RenameFileRequest;
import com.blobstorage.services.FilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FilesController {

    @Autowired
    private FilesService filesService;

    @PostMapping("/upload/{folderId}")
    public ResponseEntity<Object> uploadFile(@PathVariable Long folderId, @RequestParam("file") MultipartFile file) {
        return filesService.uploadFile(folderId, file);
    }

    @GetMapping("/getFilesByFolderId/{folderId}")
    public ResponseEntity<Object> listFilesInFolder(@PathVariable Long folderId) {
        return filesService.listFilesInFolder(folderId);
    }

    @PutMapping("/renameOrMoveFile/{fileId}")
    public ResponseEntity<Object> renameFile(@PathVariable Long fileId, @RequestBody RenameFileRequest renameFileRequest) {
        return filesService.renameFile(fileId, renameFileRequest.getNewName(), renameFileRequest.getNewFolderPath());
    }

    @DeleteMapping("/deleteFile/{fileId}")
    public ResponseEntity<Object> deleteFile(@PathVariable Long fileId) {
        return filesService.deleteFile(fileId);
    }

    @PutMapping("/updateFileById/{fileId}")
    public ResponseEntity<Object> updateFileContent(@PathVariable Long fileId, @RequestParam("file") MultipartFile newFile) {
        return filesService.updateFileContent(fileId, newFile);
    }
    // Unlock file API
    @PutMapping("/lockUnlock/{id}")
    public ResponseEntity<Object> unlockFile(@PathVariable Long id) {
    	return filesService.lockUnlockFile(id);
    }
}
