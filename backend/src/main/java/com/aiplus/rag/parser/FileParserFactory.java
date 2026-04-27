package com.aiplus.rag.parser;

import com.aiplus.rag.exception.FileParseException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FileParserFactory {

    @Autowired
    private List<FileParser> parsers;

    private Map<String, FileParser> parserMap;

    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(
            "pdf", "docx", "txt", "md", "markdown"
    );

    @PostConstruct
    public void init() {
        parserMap = parsers.stream()
                .collect(Collectors.toMap(
                        parser -> parser.getClass().getSimpleName().replace("FileParser", "").toLowerCase(),
                        Function.identity()
                ));
    }

    public FileParser getParser(String fileExtension) {
        if (fileExtension == null || fileExtension.isBlank()) {
            throw new FileParseException("", "文件扩展名不能为空");
        }

        String ext = fileExtension.toLowerCase().replace(".", "");

        for (FileParser parser : parsers) {
            if (parser.supports(ext)) {
                return parser;
            }
        }

        throw new FileParseException("",
                String.format("不支持的文件格式: .%s，支持的格式: %s",
                        ext, String.join(", ", SUPPORTED_EXTENSIONS)));
    }

    public List<String> getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS;
    }

    public boolean isSupported(String fileExtension) {
        if (fileExtension == null) return false;
        String ext = fileExtension.toLowerCase().replace(".", "");
        return SUPPORTED_EXTENSIONS.contains(ext);
    }
}
