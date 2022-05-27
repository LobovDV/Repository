package com.bookshop.BookShopApp.services;

import com.bookshop.BookShopApp.data.BookFileRepository;
import com.bookshop.BookShopApp.errors.UploadFileException;
import com.bookshop.BookShopApp.structure.book.file.BookFile;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;


@Service
public class ResourceStorage {

    @Value("${upload.path}")
    String uploadPath;

    @Value("${download.path}")
    String downloadPath;

    private final BookFileRepository bookFileRepository;

    @Autowired
    public ResourceStorage(BookFileRepository bookFileRepository) {
        this.bookFileRepository = bookFileRepository;
    }


    public String saveNewBookImage(MultipartFile file, String slug) throws IOException, UploadFileException {

        String resourceURI=null;
        if(!file.isEmpty()){
            try {
                if (!new File(uploadPath).exists()) {
                    Files.createDirectories(Paths.get(uploadPath));
                    Logger.getLogger(this.getClass().getSimpleName()).info("created image folder in " + uploadPath);
                }

                String fileName = slug + "." + FilenameUtils.getExtension(file.getOriginalFilename());
                Path path = Paths.get(uploadPath, fileName);
                resourceURI = "/book-covers/" + fileName;
                file.transferTo(path); //uploading user file here
                Logger.getLogger(this.getClass().getSimpleName()).info(fileName + "uploaded OK!");
            } catch(Exception e) {
                throw new UploadFileException(e.getLocalizedMessage(), slug);
            }
        }

        return resourceURI;
    }

    public Path getBookFilePath(String hash) {
        BookFile bookFile = bookFileRepository.findBookFileByHash(hash);
        return Paths.get(bookFile.getPath());
    }

    public Path getCompleteBookFilePath(String hash) {
        BookFile bookFile = bookFileRepository.findBookFileByHash(hash);
        return Paths.get(downloadPath, bookFile.getPath());
    }


    public MediaType getBookFileMime(String hash) {
        BookFile bookFile = bookFileRepository.findBookFileByHash(hash);
        String mimeType =
                URLConnection.guessContentTypeFromName(Paths.get(bookFile.getPath()).getFileName().toString());

        if (mimeType != null) {
            return MediaType.parseMediaType(mimeType);
        }else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    public byte[] getBookFileByteArray(String hash) throws IOException {
        BookFile bookFile = bookFileRepository.findBookFileByHash(hash);
        Path path = Paths.get(downloadPath, bookFile.getPath());
       return Files.readAllBytes(path);
    }

}
