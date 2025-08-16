package com.blobstorage.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.blobstorage.entity.Files;

import java.util.List;

public interface FilesRepository extends JpaRepository<Files, Long> {
    List<Files> findByFolderId(Long folderId);
}
