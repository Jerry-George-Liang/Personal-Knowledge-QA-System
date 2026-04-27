package com.aiplus.rag.parser;

import dev.langchain4j.data.document.Document;
import java.io.InputStream;
import java.util.List;

public interface FileParser {

    List<Document> parse(InputStream inputStream, String fileName) throws Exception;

    boolean supports(String fileExtension);
}
