package com.aiplus.rag.controller;

import com.aiplus.rag.model.ApiResponse;
import com.aiplus.rag.model.DocumentMetadata;
import com.aiplus.rag.model.DocumentParseResult;
import com.aiplus.rag.service.DocumentParseService;
import com.aiplus.rag.service.VectorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentParseService documentParseService;
    private final VectorizationService vectorizationService;

    @PostMapping("/upload")
    public ApiResponse<DocumentMetadata> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        log.info("收到文件上传请求: {}，大小: {} bytes",
                file.getOriginalFilename(), file.getSize());

        DocumentParseResult parseResult = documentParseService.parse(file);
        DocumentMetadata metadata = vectorizationService.vectorize(parseResult);

        log.info("文档处理完成: {}，段落数: {}", metadata.getFileName(), metadata.getSegmentCount());

        return ApiResponse.ok("文档上传成功", metadata);
    }

    @GetMapping("/list")
    public ApiResponse<List<DocumentMetadata>> listDocuments() {
        List<DocumentMetadata> documents = vectorizationService.listDocuments();
        return ApiResponse.ok(documents);
    }

    @DeleteMapping("/clear")
    public ApiResponse<Void> clearAllDocuments() {
        int totalSegments = vectorizationService.getTotalSegments();
        vectorizationService.clearAll();
        log.info("已清除所有文档数据，共 {} 个段落", totalSegments);
        return ApiResponse.ok("已清除所有文档数据", null);
    }

    @DeleteMapping("/{documentId}")
    public ApiResponse<Void> deleteDocument(@PathVariable String documentId) {
        log.info("收到删除文档请求: {}", documentId);
        boolean deleted = vectorizationService.deleteDocument(documentId);
        if (!deleted) {
            throw new IllegalArgumentException("文档不存在: " + documentId);
        }
        return ApiResponse.ok("文档删除成功", null);
    }
}
