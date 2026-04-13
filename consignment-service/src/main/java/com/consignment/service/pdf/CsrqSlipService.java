package com.consignment.service.pdf;

import com.consignment.service.model.csrq.CsrqResponse;
import com.consignment.service.service.CsrqService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class CsrqSlipService {

    private final CsrqService csrqService;

    public CsrqSlipService(CsrqService csrqService) {
        this.csrqService = csrqService;
    }

    public byte[] generate(String id) {
        CsrqResponse doc = csrqService.getById(id);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document pdf = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(pdf, out);
            pdf.open();

            PdfHelper.addTitle(pdf, "CONSIGNMENT STOCK REQUEST", "CSRQ Slip");

            PdfPTable info = PdfHelper.infoTable(1.5f, 2.5f, 1.5f, 2.5f);
            PdfHelper.addInfoRow(info, "Doc No", doc.docNo(), "Status", doc.status());
            PdfHelper.addInfoRow(info, "Company", doc.company(), "Store", doc.store());
            PdfHelper.addInfoRow(info, "Supplier Code", doc.supplierCode(), "Contract", doc.supplierContract());
            PdfHelper.addInfoRow(info, "Created By", doc.createdBy(), "Method", doc.createdMethod());
            PdfHelper.addInfoRow(info, "Reference No", doc.referenceNo(), "Released At",
                    doc.releasedAt() != null ? PdfHelper.DATE_FMT.format(doc.releasedAt()) : "-");
            if (doc.notes() != null && !doc.notes().isBlank()) {
                PdfHelper.addInfoRowFull(info, "Notes", doc.notes());
            }
            pdf.add(info);

            pdf.add(new Paragraph("Item Details", PdfHelper.HEADER_FONT));
            PdfPTable items = new PdfPTable(new float[]{0.5f, 2.5f, 1f, 1.5f});
            items.setWidthPercentage(100);
            items.setSpacingBefore(5);
            PdfHelper.addTableHeader(items, "No", "Item Code", "UOM", "Qty Requested");
            int n = 1;
            for (var item : doc.items()) {
                PdfHelper.addCell(items, String.valueOf(n++), Element.ALIGN_CENTER);
                PdfHelper.addCell(items, item.itemCode(), Element.ALIGN_LEFT);
                PdfHelper.addCell(items, item.requestUom(), Element.ALIGN_CENTER);
                PdfHelper.addCell(items, item.requestQty().toPlainString(), Element.ALIGN_RIGHT);
            }
            pdf.add(items);

            pdf.add(new Paragraph("\nPlease deliver within 7 working days.", PdfHelper.SMALL_FONT));
            PdfHelper.addSignatureRow(pdf, "Requested By", "Approved By");
            PdfHelper.addFooterTimestamp(pdf);
            pdf.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSRQ slip", e);
        }
    }
}
