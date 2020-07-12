package com.boomaa.pdfcombine;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;

public class PDFEntry {
    private final File file;
    private Pages pages;

    public PDFEntry(File file) {
        this.file = file;
        try {
            this.pages = new Pages(PDDocument.load(file).getNumberOfPages());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return file;
    }

    public Pages getPages() {
        return pages;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
