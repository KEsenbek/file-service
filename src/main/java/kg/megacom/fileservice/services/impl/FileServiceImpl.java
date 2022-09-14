package kg.megacom.fileservice.services.impl;

import kg.megacom.fileservice.exceptions.FileStorageException;
import kg.megacom.fileservice.services.FileService;
import kg.megacom.fileservice.utils.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final Path storagePath;

    @Autowired
    public FileServiceImpl(FileStorageProperties fileStorageProperties){
        storagePath = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath()
                .normalize();

        if(!Files.exists(storagePath)){
            try {
                Files.createDirectories(storagePath);
            }catch (IOException e) {
                throw new FileStorageException("Cannot create storage!", e);
            }
        }
    }


    @Override
    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String folder = UUID.randomUUID().toString();

        Path path = storagePath.resolve(folder).resolve(fileName);

        try {
            Files.createDirectories(path);
            Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);

            return folder.concat("/").concat(fileName);
        }catch (IOException e){
            throw new FileStorageException("Cannot storage file!", e);
        }

    }

    @Override
    public Resource getFileByName(String fileName) {

        Path path = storagePath.resolve(fileName).normalize();

        if(!Files.exists(path)){
            throw new RuntimeException("Cannot find file - " + fileName);
        }
        try {
            Resource resource = new UrlResource(path.toUri());
            return resource;
        }catch (MalformedURLException e){
            throw new RuntimeException("Cannot load file!", e);
        }
    }
}
