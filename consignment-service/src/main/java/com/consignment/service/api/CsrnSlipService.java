package com.consignment.service.api;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.csrn.CsrnResponse;
import com.consignment.service.model.csrn.CsrnResponseDetail;
import com.consignment.service.service.CsrnService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class CsrnSlipService {

    private static final String STATUS_UPDATED = "UPDATED";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("Asia/Jakarta"));

    private final CsrnService csrnService;

    public CsrnSlipService(CsrnService csrnService) {
        this.csrnService = csrnService;
    }

    public byte[] generateSlip(String csrnId) {
        CsrnResponse csrn = csrnService.getById(csrnId);
        if (!STATUS_UPDATED.equalsIgnoreCase(csrn.status())) {
            throw new BusinessRuleViolationException("Print slip is only available for CSRN with status UPDATED");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
            Font smallFont = new Font(Font.HELVETICA, 8, Font.NORMAL);

            // Title
            Paragraph title = new Paragraph("CONSIGNMENT STOCK RETURN NOTE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph subtitle = new Paragraph("CSRN Slip", new Font(Font.HELVETICA, 10, Font.NORMAL));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            doc.add(subtitle);

            // Header info table
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1.5f, 2.5f, 1.5f, 2.5f});
            infoTable.setSpacingAfter(15);

            addInfoRow(infoTable, "Doc No", csrn.docNo(), "Status", csrn.status(), headerFont, normalFont);
            addInfoRow(infoTable, "CSO Doc No", csrn.csoDocNo(), "Company", csrn.company(), headerFont, normalFont);
            addInfoRow(infoTable, "Store", csrn.store(), "Supplier Code", csrn.supplierCode(), headerFont, normalFont);
            addInfoRow(infoTable, "Supplier Contract", csrn.supplierContract(), "Reason Code", nvl(csrn.reasonCode()), headerFont, normalFont);
            addInfoRow(infoTable, "Created By", csrn.createdBy(), "Updated At",
                    csrn.updatedAt() != null ? DATE_FMT.format(csrn.updatedAt()) : "-", headerFont, normalFont);
            if (csrn.remark() != null && !csrn.remark().isBlank()) {
                addInfoRow(infoTable, "Remark", csrn.remark(), "", "", headerFont, normalFont);
            }
            doc.add(infoTable);

            // Items table
            Paragraph itemsTitle = new Paragraph("Item Details", headerFont);
            itemsTitle.setSpacingAfter(5);
            doc.add(itemsTitle);

            PdfPTable itemTable = new PdfPTable(4);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{0.5f, 3f, 1f, 1.5f});

            addTableHeader(itemTable, new String[]{"No", "Item Code", "UOM", "Qty"}, headerFont);

            int no = 1;
            for (CsrnResponseDetail item : csrn.items()) {
                addTableCell(itemTable, String.valueOf(no++), normalFont, Element.ALIGN_CENTER);
                addTableCell(itemTable, item.itemCode(), normalFont, Element.ALIGN_LEFT);
                addTableCell(itemTable, item.uom(), normalFont, Element.ALIGN_CENTER);
                addTableCell(itemTable, item.qty().toPlainString(), normalFont, Element.ALIGN_RIGHT);
            }
            doc.add(itemTable);

            // Footer
            Paragraph footer = new Paragraph("\nPrinted: " + DATE_FMT.format(java.time.Instant.now()), smallFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        } catch (BusinessRuleViolationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSRN slip PDF", e);
        }
    }

    private void addInfoRow(PdfPTable table, String label1, String val1, String label2, String val2,
                            Font labelFont, Font valFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label1, labelFont));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(3);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(nvl(val1), valFont));
        c2.setBorder(Rectangle.BOTTOM);
        c2.setPadding(3);
        table.addCell(c2);

        PdfPCell c3 = new PdfPCell(new Phrase(label2, labelFont));
        c3.setBorder(Rectangle.NO_BORDER);
        c3.setPadding(3);
        table.addCell(c3);

        PdfPCell c4 = new PdfPCell(new Phrase(nvl(val2), valFont));
        c4.setBorder(Rectangle.BOTTOM);
        c4.setPadding(3);
        table.addCell(c4);
    }

    private void addTableHeader(PdfPTable table, String[] headers, Font font) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font));
            cell.setBackgroundColor(new java.awt.Color(220, 220, 220));
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addTableCell(PdfPTable table, String value, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(nvl(value), font));
        cell.setPadding(4);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private String nvl(String value) {
        return value == null ? "-" : value;
    }
}
