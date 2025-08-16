package com.blobstorage.services;

import com.blobstorage.entity.Files;
import com.blobstorage.entity.Folder;
import com.blobstorage.exception.CustomException;
import com.blobstorage.repository.FilesRepository;
import com.blobstorage.repository.FolderRepository;
import com.blobstorage.response.FileResponse;
import com.blobstorage.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilesService {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private Environment env;

    @Autowired
    private FilesRepository filesRepository;

    // 80 MB max size
    private static final long MAX_FILE_SIZE = 80 * 1024 * 1024;

    public ResponseEntity<Object> uploadFile(Long folderId, MultipartFile file) {
        try {
            validateFileSize(file);
            Folder folder = getFolderById(folderId);
            String folderPath = validateAndCreateFolderPath(folder);
            File destinationFile = createFileOnDisk(folderPath, file);

            Files savedFile = saveFileEntity(folder, file, destinationFile);
            return ResponseUtil.success(convertToFileResponse(savedFile), "File uploaded successfully");

        } catch (CustomException ce) {
            return ResponseUtil.error(ce.getMessage(), HttpStatus.valueOf(ce.getStatusCode()));
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Object> listFilesInFolder(Long folderId) {
        List<FileResponse> files = filesRepository.findByFolderId(folderId)
                .stream()
                .map(this::convertToFileResponse)
                .collect(Collectors.toList());
        return ResponseUtil.success(files, "Files retrieved successfully");
    }

    public ResponseEntity<Object> renameFile(Long fileId, String newName, String newFolderPath) {
        try {
            Files file = getFileById(fileId);
            validateFileLock(file);

            File currentFile = getFileFromDisk(file);
            File newFile = renameFileOnDisk(currentFile, newName, newFolderPath);

            updateFileEntity(file, newFile);
            return ResponseUtil.success(convertToFileResponse(file), "File renamed successfully");

        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Object> deleteFile(Long fileId) {
        try {
            Files file = getFileById(fileId);
            validateFileLock(file);
            deleteFileFromDisk(file);
            filesRepository.delete(file);
            return ResponseUtil.success(null, "File deleted successfully");
        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Object> updateFileContent(Long fileId, MultipartFile newFile) {
        try {
            Files existingFile = getFileById(fileId);
            validateFileLock(existingFile);
            validateFileExtension(existingFile, newFile);

            updateFileOnDisk(existingFile, newFile);
            existingFile.setUpdateTime(LocalDateTime.now());
            existingFile.setLocked(true);

            Files updatedFile = filesRepository.save(existingFile);
            return ResponseUtil.success(convertToFileResponse(updatedFile), "File content updated successfully");

        } catch (Exception e) {
            return ResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Object> lockUnlockFile(Long fileId) {
        Files existingFile = getFileById(fileId);
        existingFile.setLocked(!existingFile.isLocked());

        Files updatedFile = filesRepository.save(existingFile);
        String message = updatedFile.isLocked()
                ? "File is Locked successfully"
                : "File is Unlocked successfully";

        return ResponseUtil.success(convertToFileResponse(updatedFile), message);
    }

    // ----------------- Private Helpers -----------------

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException("File size exceeds the maximum allowed limit of 80MB", 400);
        }
    }

    private Folder getFolderById(Long folderId) {
        return folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException("Folder not found", 404));
    }

    private String validateAndCreateFolderPath(Folder folder) {
        String basePath = env.getProperty("store_BasePath");
        if (folder.getPath() == null || folder.getPath().isEmpty()) {
            throw new CustomException("Folder path is not valid", 400);
        }
        String folderPath = basePath + folder.getPath();
        File dir = new File(folderPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new CustomException("Failed to create folder directory", 500);
        }
        return folderPath;
    }

    private File createFileOnDisk(String folderPath, MultipartFile file) throws IOException {
        File destination = new File(folderPath + File.separator + file.getOriginalFilename());
        if (destination.exists()) {
            throw new CustomException("File already exists in the folder", 400);
        }
        try (FileOutputStream fos = new FileOutputStream(destination)) {
            fos.write(file.getBytes());
        }
        return destination;
    }

    private Files saveFileEntity(Folder folder, MultipartFile file, File destinationFile) {
        Files newFile = new Files();
        newFile.setName(file.getOriginalFilename());
        newFile.setPath(folder.getPath() + "/" + file.getOriginalFilename());
        newFile.setFileType(file.getContentType());
        newFile.setUpdateTime(LocalDateTime.now());
        newFile.setFolder(folder);
        return filesRepository.save(newFile);
    }

    private Files getFileById(Long fileId) {
        return filesRepository.findById(fileId)
                .orElseThrow(() -> new CustomException("File not found", 404));
    }

    private File getFileFromDisk(Files file) {
        String fullPath = env.getProperty("store_BasePath") + file.getPath();
        File f = new File(fullPath);
        if (!f.exists()) {
            throw new CustomException("File not found on disk", 404);
        }
        return f;
    }

    private File renameFileOnDisk(File currentFile, String newName, String newFolderPath) {
        String ext = getFileExtension(currentFile.getName());
        String newFileName = ext.isEmpty() ? newName : newName + "." + ext;
        String targetDir = (newFolderPath != null && !newFolderPath.isEmpty())
                ? newFolderPath
                : currentFile.getParent();
        File newFile = new File(targetDir + File.separator + newFileName);

        if (newFile.exists()) {
            throw new CustomException("File with the new name already exists", 400);
        }
        if (!currentFile.renameTo(newFile)) {
            throw new CustomException("Error renaming the file on disk", 500);
        }
        return newFile;
    }

    private void updateFileEntity(Files file, File newFile) {
        String basePath = env.getProperty("store_BasePath");
        String relativePath = newFile.getAbsolutePath().replace(basePath, "").replace("\\", "/");
        file.setName(newFile.getName());
        file.setPath(relativePath);
        file.setUpdateTime(LocalDateTime.now());
        file.setLocked(true);
        filesRepository.save(file);
    }

    private void validateFileLock(Files file) {
        if (file.isLocked()) {
            throw new CustomException("Cannot modify a locked file", 400);
        }
    }

    private void deleteFileFromDisk(Files file) {
        String path = env.getProperty("store_BasePath") + file.getPath();
        File diskFile = new File(path);
        if (diskFile.exists() && !diskFile.delete()) {
            throw new CustomException("Error deleting the file from disk", 500);
        }
    }

    private void validateFileExtension(Files existingFile, MultipartFile newFile) {
        String oldExt = getFileExtension(existingFile.getName());
        String newExt = getFileExtension(newFile.getOriginalFilename());
        if (!oldExt.equalsIgnoreCase(newExt)) {
            throw new CustomException("File extension must be the same as the original file", 400);
        }
    }

    private void updateFileOnDisk(Files existingFile, MultipartFile newFile) throws IOException {
        String path = env.getProperty("store_BasePath") + existingFile.getPath();
        File currentFile = new File(path);
        try (FileOutputStream fos = new FileOutputStream(currentFile)) {
            fos.write(newFile.getBytes());
        }
    }

    private FileResponse convertToFileResponse(Files file) {
        String baseUrl = env.getProperty("BasePath");
        String fileUrl = baseUrl + file.getPath();
        return new FileResponse(file.getId(), file.getName(), file.getFileType(),
                fileUrl, file.getUpdateTime(), file.isLocked());
    }

    private String getFileExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return (idx == -1) ? "" : fileName.substring(idx + 1);
    }
}
