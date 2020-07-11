package com.boomaa.pdfcombine;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Combine {
    private static final JFrame frame = new JFrame("PDF Combiner");
    private static final PDFMergerUtility merger = new PDFMergerUtility();
    private static final GridBagConstraints constraints = new GridBagConstraints();
    private static final Map<File, Pages> queuedPdfs = new HashMap<>();
    private static File lastSelectedFile = null;

    public static void main(String[] args) {
        Container content = frame.getContentPane();
        content.setLayout(new GridBagLayout());

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> fileList = new JList<>(model);
        fileList.setVisibleRowCount(-1);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setLayoutOrientation(JList.VERTICAL);
        JScrollPane fileScroll = new JScrollPane(fileList);
        fileScroll.setPreferredSize(new Dimension(120, 180));

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
                return "Adobe PDF Files (*.pdf)";
            }
        });

        JRadioButton memBtn = new JRadioButton("Memory");
        JRadioButton tempBtn = new JRadioButton("Temp");

        JButton savePdf = new JButton("Save");
        savePdf.addActionListener((e) -> {
            if (outPdfChooser.showSaveDialog(savePdf) == JFileChooser.APPROVE_OPTION) {
                try {
                    MemoryUsageSetting setting = tempBtn.isSelected() ? MemoryUsageSetting.setupTempFileOnly() : MemoryUsageSetting.setupMainMemoryOnly();
                    PDDocument outFile = new PDDocument(setting);
                    for (Map.Entry<File, Pages> entry : queuedPdfs.entrySet()) {
                        PDDocument doc = PDDocument.load(entry.getKey(), setting);
                        boolean[] remPages = entry.getValue().remPages();
                        int remCtr = 0;
                        for (int i = 1; i < remPages.length; i++) {
                            if (remPages[i]) {
                                doc.removePage(i - remCtr + 1);
                                remCtr++;
                            }
                        }
                        merger.appendDocument(outFile, doc);
                    }
                    outFile.save(outPdfChooser.getSelectedFile());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton addPdf = new JButton("+");
        addPdf.addActionListener((e) -> {
            if (inPdfChooser.showOpenDialog(addPdf) == JFileChooser.APPROVE_OPTION) {
                try {
                    File chosenFile = inPdfChooser.getSelectedFile();
                    model.add(model.size(), chosenFile.getName());
                    if (lastSelectedFile == null) {
                        lastSelectedFile = chosenFile;
                    }
                    queuedPdfs.put(chosenFile, new Pages(PDDocument.load(chosenFile).getNumberOfPages()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton remPdf = new JButton("-");
        remPdf.addActionListener((e) -> {
            if (fileList.getSelectedIndex() != -1) {
                model.remove(fileList.getSelectedIndex());
            }
        });

        JLabel title = new JLabel("PDF Combiner");
        JLabel pgsLabel = new JLabel("Page Range");
        JTextField pgsInput = new JTextField("", 5);

        fileList.getSelectionModel().addListSelectionListener((e -> {
            if (!pgsInput.getText().isEmpty()) {
                Pages pages = queuedPdfs.get(lastSelectedFile);
                pages.resetPages();
                String[] ranges = pgsInput.getText().split(",");
                for (int i = 0; i < ranges.length; i++) {
                    String cleanStr = ranges[i].trim().replaceAll(",", "");
                    if (!cleanStr.isEmpty()) {
                        if (cleanStr.contains("-")) {
                            int iodash = cleanStr.indexOf("-");
                            pages.addRange(new Pages.Range(
                                    Integer.parseInt(cleanStr.substring(0, iodash)),
                                    Integer.parseInt(cleanStr.substring(iodash + 1)))
                            );
                        } else {
                            pages.addRange(new Pages.Range(Integer.parseInt(cleanStr)));
                        }
                    }
                }
            }

            lastSelectedFile = strListedFile(fileList.getSelectedValue());
            pgsInput.setText(queuedPdfs.get(lastSelectedFile).toString());
        }));

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

        ButtonGroup pdfBoxMem = new ButtonGroup();
        JPanel panelMem = new JPanel();
        pdfBoxMem.add(memBtn);
        pdfBoxMem.add(tempBtn);
        memBtn.setSelected(true);
        panelMem.add(memBtn);
        panelMem.add(tempBtn);

        constraints.anchor = GridBagConstraints.CENTER;
        addConstrainedPanel(title, 0, 0, 3, 1);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        addConstrainedPanel(pgsLabel, 3, 0, 1, 1);
        addConstrainedPanel(panelMem, 0, 1, 3, 1);
        addConstrainedPanel(pgsInput, 3, 1, 1, 1);
        addConstrainedPanel(fileScroll, 0, 2, 3, 5);
        constraints.fill = GridBagConstraints.BOTH;
        addConstrainedPanel(addPdf, 3, 2, 1, 1);
        addConstrainedPanel(remPdf, 3, 3, 1, 1);
        addConstrainedPanel(movePdfUp, 3, 4, 1, 1);
        addConstrainedPanel(movePdfDown, 3, 5, 1, 1);
        addConstrainedPanel(savePdf, 3, 6, 1, 1);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(240, 285));
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

    private static File strListedFile(String name) {
        for (File file : queuedPdfs.keySet()) {
            if (file.getName().equals(name)) {
                return file;
            }
        }
        return null;
    }

    private static void swap(DefaultListModel<String> model, int a, int b) {
        String aStr = model.getElementAt(a);
        String bStr = model.getElementAt(b);
        model.set(a, bStr);
        model.set(b, aStr);
    }
}
