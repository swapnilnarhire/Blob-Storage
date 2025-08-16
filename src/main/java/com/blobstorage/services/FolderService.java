package com.blobstorage.services;

import com.blobstorage.entity.Files;
import com.blobstorage.entity.Folder;
import com.blobstorage.repository.FilesRepository;
import com.blobstorage.repository.FolderRepository;
import com.blobstorage.request.CreateFolderRequest;
import com.blobstorage.response.FolderResponse;
import com.blobstorage.exception.CustomException;
import com.blobstorage.util.ResponseUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FolderService {

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	Environment env;

	@Autowired
	private FilesRepository filesRepository;

	@Value("${store_BasePath}")
	private String storeBasePath;

	public ResponseEntity<Object> createFolder(CreateFolderRequest request) {
		// Validate the name
		if (request.getName() == null || request.getName().isEmpty()) {
			return ResponseUtil.error("Folder name is required.", HttpStatus.BAD_REQUEST);
		}

		String fullPath = env.getProperty("store_BasePath");
		String basePath = "";
		System.out.println("fullPath=================" + fullPath);

		// Check if the parent folder exists and append its name to the full path
		if (request.getParentFolderId() != null) {
			String parentFolderName = folderRepository.findNameById(request.getParentFolderId());

			if (parentFolderName == null) {
				return ResponseUtil.error("Parent folder not found.", HttpStatus.BAD_REQUEST);
			}
			System.out.println("parentFolderName=============================>" + parentFolderName);
			fullPath += parentFolderName + File.separator;
			basePath = parentFolderName + "/";
		}

		fullPath += request.getName();
		basePath += request.getName();
		// Check if the path already exists in DB
		if (folderRepository.existsByPath(fullPath)) {
			return ResponseUtil.error("Folder path already exists.", HttpStatus.BAD_REQUEST);
		}
		// Create the directory on the file system
		File folder = new File(fullPath);

		if (folder.exists()) {
			return ResponseUtil.error("A folder with the same name already exists", HttpStatus.BAD_REQUEST);
		}

		if (!folder.exists() && !folder.mkdirs()) {
			return ResponseUtil.error("Failed to create folder on the file system.", HttpStatus.BAD_REQUEST);
		}

		Folder parentFolder = null;
		if (request.getParentFolderId() != null) {
			parentFolder = folderRepository.findById(request.getParentFolderId())
					.orElseThrow(() -> new CustomException("Parent folder not found.", 400));
		}

		Folder newFolder = new Folder();
		newFolder.setName(request.getName());
		newFolder.setPath(basePath);
		newFolder.setParentFolder(parentFolder);
		Folder savedFolder = folderRepository.save(newFolder);
		// If the folder is a root folder, create predefined subfolders
		if (request.getParentFolderId() == null) {
			createSubFolders(fullPath, basePath, savedFolder);
		}
		return ResponseUtil.success(mapToResponse(savedFolder), "Folder created successfully.");
	}

	private void createSubFolders(String fullPath, String basePath, Folder parentFolder) {
		List<String> subFolderNames = List.of("images", "audios", "videos", "docs");

		Map<String, Folder> createdSubFolders = subFolderNames.parallelStream()
				.map(name -> createAndSaveSubFolder(fullPath, basePath, name, parentFolder))
				.collect(Collectors.toMap(Folder::getName, folder -> folder));

		if (createdSubFolders.containsKey("images")) {
			Folder imagesFolder = createdSubFolders.get("images");
			List<String> imageSubFolders = List.of("slider","icons", "logos", "social-media", "store-logos", "brand-logos",
					"backgrounds");

			imageSubFolders.parallelStream().forEach(name -> createAndSaveSubFolder(fullPath + "/images",
					basePath + "/images", name, imagesFolder));
		}
	}

	private Folder createAndSaveSubFolder(String parentFullPath, String parentBasePath, String name,
			Folder parentFolder) {
		String subFolderPath = parentFullPath + "/" + name;
		String subFolderDbPath = parentBasePath + "/" + name;

		File subFolder = new File(subFolderPath);
		if (!subFolder.exists() && !subFolder.mkdirs()) {
			throw new CustomException("Failed to create subfolder: " + name, 500);
		}

		Folder subFolderEntity = new Folder();
		subFolderEntity.setName(name);
		subFolderEntity.setPath(subFolderDbPath);
		subFolderEntity.setParentFolder(parentFolder);

		return folderRepository.save(subFolderEntity);
	}

	public ResponseEntity<Object> getFolderById(Long id) {
		Folder folder = folderRepository.findById(id)
				.orElseThrow(() -> new CustomException("Folder not found with id: " + id, 400));
		return ResponseUtil.success(mapToResponse(folder), "Folder retrieved successfully.");
	}

	public ResponseEntity<Object> getRootFolders() {
		List<Folder> rootFolders = folderRepository.findAllRootFolders();
		return ResponseUtil.success(rootFolders.stream().map(this::mapToResponse).collect(Collectors.toList()),
				"Root folders retrieved successfully.");
	}

	public ResponseEntity<Object> getFolders() {
		List<Folder> allFolders = folderRepository.findAll();
		return ResponseUtil.success(allFolders.stream().map(this::mapToResponse).collect(Collectors.toList()),
				"All folders retrieved successfully.");
	}

	public ResponseEntity<Object> getFoldersByParentId(Long parentFolderId) {
		List<Folder> folders = parentFolderId == null ? folderRepository.findByParentFolderIdIsNull()
				: folderRepository.findByParentFolderId(parentFolderId);

		if (folders.isEmpty()) {
			return ResponseUtil.success(List.of(), "No folders found.");
		}

		return ResponseUtil.success(folders.stream().map(this::mapToResponse).collect(Collectors.toList()),
				"Folders retrieved successfully.");
	}

	public ResponseEntity<Object> deleteFolder(Long folderId) {
		// Fetch folder from DB
		Folder folder = folderRepository.findById(folderId)
				.orElseThrow(() -> new CustomException("Folder not found with id: " + folderId, 400));

		String fullPath = env.getProperty("store_BasePath");
		if (fullPath == null || fullPath.isEmpty()) {
			return ResponseUtil.error("Storage base path is not configured.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// Construct the absolute folder path
		String currentFolderPath = fullPath + File.separator + folder.getPath();
		System.out.println("Current Folder Path: " + currentFolderPath);

		File currentFolder = new File(currentFolderPath);
		if (!currentFolder.exists() || !currentFolder.isDirectory()) {
			return ResponseUtil.error("Folder not found on disk.", HttpStatus.NOT_FOUND);
		}

		// Ensure folder is empty before deletion
		List<Folder> childFolders = folderRepository.findByParentFolderId(folderId);
		if (!childFolders.isEmpty()) {
			return ResponseUtil.error("Cannot delete folder because it contains subfolders.", HttpStatus.BAD_REQUEST);
		}

		List<Files> filesInFolder = filesRepository.findByFolderId(folderId);
		if (!filesInFolder.isEmpty()) {
			return ResponseUtil.error("Cannot delete folder because it contains files.", HttpStatus.BAD_REQUEST);
		}

		// Delete folder from file system
		boolean isDeleted = currentFolder.delete();
		if (!isDeleted) {
			return ResponseUtil.error("Failed to delete folder from disk.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// Delete folder from DB
		folderRepository.delete(folder);

		return ResponseUtil.success(null, "Folder deleted successfully.");
	}

	public ResponseEntity<Object> renameFolder(Long folderId, String newFolderName) {
		// Fetch folder from DB
		Folder folder = folderRepository.findById(folderId)
				.orElseThrow(() -> new CustomException("Folder not found with id: " + folderId, 400));

		String fullPath = env.getProperty("store_BasePath");
		if (fullPath == null || fullPath.isEmpty()) {
			return ResponseUtil.error("Storage base path is not configured.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// Construct the absolute folder path
		String currentFolderPath = fullPath + File.separator + folder.getPath();
		System.out.println("Current Folder Path: " + currentFolderPath);

		File currentFolder = new File(currentFolderPath);
		if (!currentFolder.exists() || !currentFolder.isDirectory()) {
			return ResponseUtil.error("Folder not found on disk", HttpStatus.NOT_FOUND);
		}

		// Ensure folder is empty before renaming
		File[] subfolders = currentFolder.listFiles(File::isDirectory);
		if (subfolders != null && subfolders.length > 0) {
			return ResponseUtil.error("Cannot rename folder because it contains subfolders", HttpStatus.BAD_REQUEST);
		}

		List<Files> filesInFolder = filesRepository.findByFolderId(folderId);
		if (!filesInFolder.isEmpty()) {
			return ResponseUtil.error("Cannot rename folder because it contains files", HttpStatus.BAD_REQUEST);
		}

		// Create new folder file object with updated name
		File newFolder = new File(currentFolder.getParent() + File.separator + newFolderName);
		if (newFolder.exists()) {
			return ResponseUtil.error("A folder with the new name already exists", HttpStatus.BAD_REQUEST);
		}

		boolean renamed = currentFolder.renameTo(newFolder);
		if (!renamed) {
			return ResponseUtil.error("Error renaming the folder on disk", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// Update folder path in DB (keep relative path)
		String parentPath = folder.getPath().substring(0, folder.getPath().lastIndexOf("/"));
		folder.setPath(parentPath + "/" + newFolderName);
		folder.setName(newFolderName);

		try {
			folderRepository.save(folder);
		} catch (Exception e) {
			return ResponseUtil.error("Error saving renamed folder to the database", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return ResponseUtil.success(null, "Folder renamed successfully.");
	}

	private FolderResponse mapToResponse(Folder folder) {
		FolderResponse response = new FolderResponse();
		String basePath = env.getProperty("BasePath") + folder.getPath();
		response.setId(folder.getId());
		response.setName(folder.getName());
		response.setPath(basePath);
		response.setParentFolderId(folder.getParentFolder() != null ? folder.getParentFolder().getId() : null);

		return response;
	}
}
