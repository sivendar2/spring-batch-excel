package com.siven.service;

import com.siven.entity.Attachment;
import com.siven.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class AttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    public Attachment save(MultipartFile file) throws IOException {
        Attachment attachment = new Attachment();
        attachment.setFilename(file.getOriginalFilename());
        attachment.setContentType(file.getContentType());
        attachment.setSize(file.getSize());
        attachment.setData(file.getBytes());
        return attachmentRepository.save(attachment);
    }

    public Attachment getFile(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    public List<Attachment> getAllFiles() {
        return attachmentRepository.findAll();
    }
}

