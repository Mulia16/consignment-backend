package com.consignment.service.pdf;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.awt.Color;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class PdfHelper {

    public static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("Asia/Jakarta"));
    public static final DateTimeFormatter DATE_ONLY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("Asia/Jakarta"));

    public static final Font TITLE_FONT  = new Font(Font.HELVETICA, 14, Font.BOLD);
    public static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    public static final Font NORMAL_FONT = new Font(Font.HELVETICA, 9,  Font.NORMAL);
    public static final Font SMALL_FONT  = new Font(Font.HELVETICA, 8,  Font.NORMAL);
    public static final Color HEADER_BG  = new Color(220, 220, 220);

    private PdfHelper() {}

    public static void addTitle(Document doc, String title, String subtitle) throws DocumentException {
        Paragraph t = new Paragraph(title, TITLE_FONT);
        t.setAlignment(Element.ALIGN_CENTER);
        doc.add(t);
        if (subtitle != null) {
            Paragraph s = new Paragraph(subtitle, NORMAL_FONT);
            s.setAlignment(Element.ALIGN_CENTER);
            s.setSpacingAfter(12);
            doc.add(s);
        }
    }

    public static PdfPTable infoTable(float... widths) throws DocumentException {
        PdfPTable t = new PdfPTable(widths.length);
        t.setWidthPercentage(100);
        t.setWidths(widths);
        t.setSpacingAfter(12);
        return t;
    }

    public static void addInfoRow(PdfPTable table, String label1, String val1,
                                   String label2, String val2) {
        addLabelCell(table, label1);
        addValueCell(table, val1);
        addLabelCell(table, label2);
        addValueCell(table, val2);
    }

    public static void addInfoRowFull(PdfPTable table, String label, String val) {
        addLabelCell(table, label);
        PdfPCell c = new PdfPCell(new Phrase(nvl(val), NORMAL_FONT));
        c.setColspan(3);
        c.setBorder(Rectangle.BOTTOM);
        c.setPadding(3);
        table.addCell(c);
    }

    private static void addLabelCell(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(nvl(text), HEADER_FONT));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(3);
        table.addCell(c);
    }

    private static void addValueCell(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(nvl(text), NORMAL_FONT));
        c.setBorder(Rectangle.BOTTOM);
        c.setPadding(3);
        table.addCell(c);
    }

    public static void addTableHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    public static void addCell(PdfPTable table, String value, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(nvl(value), NORMAL_FONT));
        cell.setPadding(4);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    public static void addFooterTimestamp(Document doc) throws DocumentException {
        Paragraph footer = new Paragraph("Printed: " + DATE_FMT.format(java.time.Instant.now()), SMALL_FONT);
        footer.setAlignment(Element.ALIGN_RIGHT);
        doc.add(footer);
    }

    public static void addSignatureRow(Document doc, String... signers) throws DocumentException {
        PdfPTable sig = new PdfPTable(signers.length);
        sig.setWidthPercentage(100);
        sig.setSpacingBefore(20);
        for (String signer : signers) {
            PdfPCell c = new PdfPCell();
            c.setBorder(Rectangle.NO_BORDER);
            c.setPadding(5);
            c.addElement(new Paragraph(signer, NORMAL_FONT));
            c.addElement(new Paragraph("\n\n\n________________________", NORMAL_FONT));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            sig.addCell(c);
        }
        doc.add(sig);
    }

    public static String nvl(String v) { return v == null ? "-" : v; }
}
