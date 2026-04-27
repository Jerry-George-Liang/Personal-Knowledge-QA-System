package com.aiplus.rag.exception;

public class FileParseException extends RuntimeException {

    private final String fileName;

    public FileParseException(String fileName, String message) {
        super(String.format("文件解析失败 [%s]: %s", fileName, message));
        this.fileName = fileName;
    }

    public FileParseException(String fileName, String message, Throwable cause) {
        super(String.format("文件解析失败 [%s]: %s", fileName, message), cause);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
