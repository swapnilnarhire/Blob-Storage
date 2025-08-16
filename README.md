# Blob Storage  

![Java](https://img.shields.io/badge/Java-17-blue)  
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen)  
![MySQL](https://img.shields.io/badge/MySQL-8.x-orange)  

## 📌 Project Overview  
**Blob Storage** is a self-hosted, Spring Boot–based **Media Storage Service** for managing **files and folders** with ease.  

It provides REST APIs for:  
- Uploading and downloading files  
- Organizing files into structured folders  
- Managing metadata in a MySQL database  
- Securing sensitive files with lock/unlock controls  

Designed to be **scalable, lightweight, and easily integrable**, this service is ideal for applications that need:  
- A reliable backend for file handling  
- Centralized media storage for microservices  
- Fast and efficient static file delivery via **Nginx integration**  

> In short, Blob Storage offers a practical alternative to cloud storage solutions by giving developers full control over file storage, metadata, and access — all in their own infrastructure.  

---

## ⚙️ Tech Stack  

- **Backend Framework**: Spring Boot 3.5.4  
- **Language**: Java 17  
- **Database**: MySQL (for metadata persistence)  
- **ORM**: Spring Data JPA  
- **Validation**: Hibernate Validator (via `spring-boot-starter-validation`)  
- **Build Tool**: Maven  
- **Testing**: Spring Boot Starter Test  

---

## 🚀 Getting Started  

### 1. Prerequisites  
Make sure you have installed:  
- **Java 17+**  
- **Maven 3.8+**  
- **MySQL 8.x**  
- **Nginx** (for serving static media files in production)  

### 2. Clone the Repository  
```bash
git clone https://github.com/swapnilnarhire/blob-storage.git
cd blob-storage
```

### 3. Configure Database  
Update your **MySQL credentials** in `src/main/resources/application.properties`:  
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/springboot_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. File Storage Configuration  

The project supports **cross-platform storage paths**.  
Update these properties depending on your OS:  

#### Windows  
```properties
store_BasePath=C:\media\
spring.web.resources.static-locations=file:///C:/media/
BasePath=http://localhost/media/
```

#### macOS  
```properties
store_BasePath=/usr/local/var/www/media/
spring.web.resources.static-locations=file:///usr/local/var/www/media/
BasePath=http://localhost/media/
```

#### Ubuntu/Linux  
```properties
store_BasePath=/usr/share/nginx/html/media/
spring.web.resources.static-locations=file:///usr/share/nginx/html/media/
BasePath=http://localhost/media/
```

> 💡 Ensure that the `store_BasePath` folder exists and Nginx is configured to serve it.

### 5. Run the Application  
```bash
mvn spring-boot:run
```
The service will start at:  
👉 [http://localhost:8080](http://localhost:8080)  

---

## 🗄️ Database Schema  

### Files Table  

| Column      | Type            | Description                              |
|-------------|-----------------|------------------------------------------|
| `id`        | BIGINT (PK)     | Auto-generated unique identifier          |
| `name`      | VARCHAR         | Name of the file                         |
| `path`      | VARCHAR         | Storage path of the file in filesystem    |
| `fileType`  | VARCHAR         | MIME type or file extension              |
| `updateTime`| TIMESTAMP       | Last modified time                       |
| `folder_id` | BIGINT (FK)     | References the parent folder             |
| `locked`    | BOOLEAN         | Indicates if the file is locked (default = true) |

### Folder Table  

| Column        | Type         | Description                           |
|---------------|--------------|---------------------------------------|
| `id`          | BIGINT (PK)  | Auto-generated unique identifier       |
| `name`        | VARCHAR      | Name of the folder                    |
| `path`        | VARCHAR      | Unique filesystem path for the folder |
| `parent_id`   | BIGINT (FK)  | References parent folder (nullable)   |

### Entity Relationships  
- One Folder → Many Files  
- One Folder → Many Subfolders (recursive hierarchy)  

---

## 📂 File Management APIs  

Base Path: `/api/files`  

### 1. Upload File  
**POST** `/upload/{folderId}`  

### 2. List Files in a Folder  
**GET** `/getFilesByFolderId/{folderId}`  

### 3. Rename or Move File  
**PUT** `/renameOrMoveFile/{fileId}`  

### 4. Delete File  
**DELETE** `/deleteFile/{fileId}`  

### 5. Update File Content  
**PUT** `/updateFileById/{fileId}`  

### 6. Lock / Unlock File  
**PUT** `/lockUnlock/{id}`  

---

## 📁 Folder Management APIs  

Base Path: `/api/folders`  

### 1. Create Folder  
**POST** `/addFolder`  

### 2. Get Folder by ID  
**GET** `/getFolderById/{id}`  

### 3. Get All Folders  
**GET** `/getAllFolders`  

### 4. Get Subfolders by Parent Folder ID  
**GET** `/getParentFolders?parentFolderId={id}`  

### 5. Rename Folder  
**PUT** `/renameFolder/{id}`  

### 6. Delete Folder  
**DELETE** `/deleteFolder/{id}`  

---

## 🚀 Running the Application  

### Run with Maven  
```bash
mvn spring-boot:run
```

### Run JAR directly  
```bash
mvn clean package
java -jar target/blob-storage-0.0.1-SNAPSHOT.jar
```

---

## ⚡ Nginx Configuration  

### Windows Example (`C:\nginx\conf\nginx.conf`)  
```nginx
location /media/ {
    alias C:/media/;
    autoindex on;
}
```

### macOS Example  
```nginx
location /media/ {
    alias /usr/local/var/www/media/;
    autoindex on;
}
```

### Ubuntu/Linux Example  
```nginx
location /media/ {
    alias /usr/share/nginx/html/media/;
    autoindex on;
}
```

---

## 📂 Postman Collection  

👉 [Download Postman Collection](postman/Blob-Storage-v0.1.postman_collection.json)  

## 📖 Documentation  
- [ER Diagram](docs/ERD.md)  
- [Upload/Download Flow](docs/Sequence.md)  

## 📜 License  
This project is licensed under the [MIT License](LICENSE).  

## 🤝 Contributing  
Contributions are welcome! 🎉  

## 🔮 Future Enhancements  
- Cloud Storage Integration (AWS S3, Azure Blob, etc.)  
- User Authentication & Permissions  
- File Versioning  
- Previews & Metadata Extraction  
- Large File Support (Chunked Uploads)  

See [CONTRIBUTING.md](CONTRIBUTING.md).  

## 🌍 Community  
Please read our [Code of Conduct](CODE_OF_CONDUCT.md) before contributing.  
