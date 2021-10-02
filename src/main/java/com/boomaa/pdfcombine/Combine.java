package com.boomaa.pdfcombine;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;

public class Combine {
    private static final JFrame frame = new JFrame("PDF Combiner");
    private static final GridBagConstraints constraints = new GridBagConstraints();
    private static PDFEntry lastSelected = null;

    public static void main(String[] args) {
        Container content = frame.getContentPane();
        content.setLayout(new GridBagLayout());

        DefaultListModel<PDFEntry> model = new DefaultListModel<>();
        JList<PDFEntry> fileList = new JList<>(model);
        fileList.setVisibleRowCount(-1);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setLayoutOrientation(JList.VERTICAL);
        JScrollPane fileScroll = new JScrollPane(fileList);
        fileScroll.setPreferredSize(new Dimension(120, 180));

        JFileChooser inPdfChooser = new JFileChooser();
        JFileChooser outPdfChooser = new JFileChooser();

        inPdfChooser.setMultiSelectionEnabled(true);
        inPdfChooser.setAcceptAllFileFilterUsed(false);
        inPdfChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.toString().endsWith("pdf");
            }

            @Override
            public String getDescription() {
                return "Adobe PDF Files (*.pdf)";
            }
        });
        outPdfChooser.setAcceptAllFileFilterUsed(false);
        outPdfChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return true;
            }

            @Override
            public String getDescription() {
                return "Adobe PDF Files (*.pdf)";
            }
        });

        JRadioButton tempBtn = new JRadioButton("Temp");
        JRadioButton memBtn = new JRadioButton("Memory");

        memBtn.addActionListener((e) -> JOptionPane.showMessageDialog(frame,
                "WARNING: This often does not work.", "Warning", JOptionPane.WARNING_MESSAGE));

        JLabel title = new JLabel("PDF Combiner");
        JLabel pgsLabel = new JLabel("Page Range");
        JTextField pgsInput = new JTextField("", 5);

        JButton savePdf = new JButton("Save");
        savePdf.addActionListener((e) -> {
            if (outPdfChooser.showSaveDialog(savePdf) == JFileChooser.APPROVE_OPTION) {
                try {
                    parsePageRange(pgsInput);
                    MemoryUsageSetting setting = tempBtn.isSelected() ? MemoryUsageSetting.setupTempFileOnly() : MemoryUsageSetting.setupMainMemoryOnly();
                    PDDocument outFile = new PDDocument(setting);
                    for (int k = 0; k < model.size(); k++) {
                        PDFEntry pdf = model.getElementAt(k);
                        PDPageTree doc = PDDocument.load(pdf.getFile(), setting).getPages();
                        boolean[] remPages = pdf.getPages().remPages();
                        for (int i = 0; i < doc.getCount(); i++) {
                            if (!remPages[i + 1]) {
                                outFile.addPage(doc.get(i));
                            }
                        }
                    }
                    String filename = outPdfChooser.getSelectedFile().toString();
                    outFile.save(new File(!filename.endsWith(".pdf") ? filename + ".pdf" : filename));
                    outFile.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton addPdf = new JButton("+");
        addPdf.addActionListener((e) -> {
            if (inPdfChooser.showOpenDialog(addPdf) == JFileChooser.APPROVE_OPTION) {
                for (File entry : inPdfChooser.getSelectedFiles()) {
                    PDFEntry input = new PDFEntry(entry);
                    model.add(model.size(), input);
                    fileList.setSelectedIndex(model.size() - 1);
                    if (lastSelected == null) {
                        lastSelected = input;
                        pgsInput.setText(input.getPages().toString());
                    }
                }
            }
        });

        JButton remPdf = new JButton("-");
        remPdf.addActionListener((e) -> {
            if (fileList.getSelectedIndex() != -1) {
                model.remove(fileList.getSelectedIndex());
            }
        });

        JButton movePdfUp = new JButton("▲");
        movePdfUp.addActionListener(e -> {
            int sel = fileList.getSelectedIndex();
            if (sel - 1 >= 0) {
                swap(model, sel, sel - 1);
                fileList.setSelectedIndex(sel - 1);
            }
        });

        JButton movePdfDown = new JButton("▼");
        movePdfDown.addActionListener(e -> {
            int sel = fileList.getSelectedIndex();
            if (sel + 1 < model.size()) {
                swap(model, sel, sel + 1);
                fileList.setSelectedIndex(sel + 1);
            }
        });

        JButton resetAll = new JButton("↻");
        resetAll.addActionListener(e -> {
            model.clear();
            fileList.removeAll();
            pgsInput.setText("");
        });

        fileList.getSelectionModel().addListSelectionListener((e -> {
            if (fileList.getSelectedIndex() != -1) {
                parsePageRange(pgsInput);
                PDFEntry next = model.get(fileList.getSelectedIndex());
                pgsInput.setText(next.getPages().toString());
                lastSelected = next;
            }
        }));

        ButtonGroup pdfBoxMem = new ButtonGroup();
        JPanel panelMem = new JPanel();
        pdfBoxMem.add(tempBtn);
        pdfBoxMem.add(memBtn);
        tempBtn.setSelected(true);
        panelMem.add(tempBtn);
        panelMem.add(memBtn);

        constraints.anchor = GridBagConstraints.CENTER;
        addConstrainedPanel(title, 0, 0, 3, 1);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        addConstrainedPanel(pgsLabel, 3, 0, 1, 1);
        addConstrainedPanel(panelMem, 0, 1, 3, 1);
        addConstrainedPanel(pgsInput, 3, 1, 1, 1);
        addConstrainedPanel(fileScroll, 0, 2, 3, 6);
        constraints.fill = GridBagConstraints.BOTH;
        addConstrainedPanel(addPdf, 3, 2, 1, 1);
        addConstrainedPanel(remPdf, 3, 3, 1, 1);
        addConstrainedPanel(movePdfUp, 3, 4, 1, 1);
        addConstrainedPanel(movePdfDown, 3, 5, 1, 1);
        addConstrainedPanel(resetAll, 3, 6, 1, 1);
        addConstrainedPanel(savePdf, 3, 7, 1, 1);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(240, 330));
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void addConstrainedPanel(Component comp, int x, int y, int width, int height) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.gridheight = height;
        frame.getContentPane().add(comp, constraints);
    }

    private static void swap(DefaultListModel<PDFEntry> model, int a, int b) {
        PDFEntry aStr = model.getElementAt(a);
        PDFEntry bStr = model.getElementAt(b);
        model.set(a, bStr);
        model.set(b, aStr);
    }

    private static void parsePageRange(JTextField pgsInput) {
        if (lastSelected != null) {
            lastSelected.getPages().resetPages();
            if (!pgsInput.getText().isBlank()) {
                String[] inPages = pgsInput.getText().split(",");
                for (String inPage : inPages) {
                    String cleanStr = inPage.replaceAll(",", "").trim();
                    if (cleanStr.contains("-")) {
                        int iodash = cleanStr.indexOf("-");
                        int start = Integer.parseInt(cleanStr.substring(0, iodash));
                        int end = Integer.parseInt(cleanStr.substring(iodash + 1));
                        lastSelected.getPages().addRange(new Pages.Range(start, end));
                    } else if (!cleanStr.isBlank()) {
                        lastSelected.getPages().addRange(new Pages.Range(Integer.parseInt(cleanStr)));
                    }
                }
            }
        }
    }
}
