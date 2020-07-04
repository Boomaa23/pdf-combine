package com.boomaa.pdfcombine;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Combine {
    private static final JFrame frame = new JFrame("PDF Combiner");
    private static final PDFMergerUtility merger = new PDFMergerUtility();
    private static JRadioButton memBtn = new JRadioButton("Memory");
    private static JRadioButton tempBtn = new JRadioButton("Temp");

    public static void main(String[] args) {
        Container content = frame.getContentPane();
        content.setLayout(new FlowLayout());

        JFileChooser inPdfChooser = new JFileChooser();
        JFileChooser outPdfChooser = new JFileChooser();

        inPdfChooser.setAcceptAllFileFilterUsed(false);
        inPdfChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().substring(f.getName().lastIndexOf('.') + 1).toLowerCase().equals("pdf");
            }

            @Override
            public String getDescription() {
                return null;
            }
        });

        JButton addPDF = new JButton("Add PDF");
        addPDF.addActionListener((e) -> {
            if (inPdfChooser.showOpenDialog(addPDF) == JFileChooser.APPROVE_OPTION) {
                try {
                    merger.addSource(inPdfChooser.getSelectedFile());
                } catch (FileNotFoundException e0) {
                    e0.printStackTrace();
                }
            }
        });

        JButton savePDF = new JButton("Save");
        savePDF.addActionListener((e) -> {
            if (outPdfChooser.showSaveDialog(savePDF) == JFileChooser.APPROVE_OPTION) {
                merger.setDestinationFileName(outPdfChooser.getSelectedFile().getPath());
                try {
                    merger.mergeDocuments(tempBtn.isSelected() ? MemoryUsageSetting.setupTempFileOnly() : MemoryUsageSetting.setupMainMemoryOnly());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        ButtonGroup pdfBoxMem = new ButtonGroup();
        pdfBoxMem.add(memBtn);
        pdfBoxMem.add(tempBtn);
        memBtn.setSelected(true);

        content.add(new JLabel("       PDF Combiner       "));
        content.add(addPDF);
        content.add(savePDF);
        content.add(memBtn);
        content.add(tempBtn);

        frame.setPreferredSize(new Dimension(200, 120));
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
