package com.aiplus.rag.service;

import com.aiplus.rag.exception.FileParseException;
import com.aiplus.rag.model.DocumentParseResult;
import com.aiplus.rag.parser.FileParser;
import com.aiplus.rag.parser.FileParserFactory;
import dev.langchain4j.data.document.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParseService {

    private final FileParserFactory parserFactory;

    public DocumentParseResult parse(MultipartFile file) throws FileParseException {
        Instant start = Instant.now();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new FileParseException("", "文件名不能为空");
        }

        String extension = getExtension(originalFilename);

        log.info("开始解析文件: {}，格式: {}", originalFilename, extension);

        FileParser parser = parserFactory.getParser(extension);

        List<Document> documents;
        try {
            documents = parser.parse(file.getInputStream(), originalFilename);
        } catch (FileParseException e) {
            throw e;
        } catch (Exception e) {
            throw new FileParseException(originalFilename, "解析过程发生异常: " + e.getMessage(), e);
        }

        long elapsedMs = Duration.between(start, Instant.now()).toMillis();

        log.info("文件解析完成: {}，共提取 {} 个段落，耗时 {}ms",
                originalFilename, documents.size(), elapsedMs);

        return DocumentParseResult.builder()
                .fileName(originalFilename)
                .documentId(generateDocumentId(originalFilename))
                .segmentCount(documents.size())
                .documents(documents)
                .elapsedMs(elapsedMs)
                .build();
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1);
        }
        throw new FileParseException(filename, "无法识别文件扩展名");
    }

    private String generateDocumentId(String filename) {
        return "doc_" + System.currentTimeMillis() + "_" +
               filename.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_").hashCode();
    }
}
