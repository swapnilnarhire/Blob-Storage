package com.blobstorage.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.blobstorage.entity.Folder;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

	boolean existsByPath(String path);

	// Custom native query to get the folder name by its ID
	@Query(nativeQuery = true, value = "SELECT path FROM folder WHERE id = :id")
	String findNameById(Long id); // Returns the name of the folder by ID

	@Query(nativeQuery = true, value = "SELECT * FROM `folder` WHERE `parent_id` IS NULL")
	List<Folder> findAllRootFolders();

	@Query(nativeQuery = true, value = "SELECT * FROM folder  WHERE parent_id =?1")
	List<Folder> findByParentFolderId(@Param("parentFolderId") Long parentFolderId);

	@Query(nativeQuery = true, value = "SELECT * FROM `folder` WHERE `parent_id` IS NULL")
	List<Folder> findByParentFolderIdIsNull();

	@Query(nativeQuery = true, value = "SELECT * FROM folder WHERE id=?1")
	Folder findFolderById(Long id);

	@Query(nativeQuery = true, value = "SELECT * FROM folder  WHERE f_config_id=?1")
	List<Folder> findByFconfigId(Long id);

}
