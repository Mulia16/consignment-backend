package com.consignment.service.pdf;

import com.consignment.service.model.csrv.CsrvResponse;
import com.consignment.service.service.CsrvService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class CsrvSlipService {

    private final CsrvService csrvService;

    public CsrvSlipService(CsrvService csrvService) {
        this.csrvService = csrvService;
    }

    public byte[] generate(String id) {
        CsrvResponse doc = csrvService.getById(id);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document pdf = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(pdf, out);
            pdf.open();

            PdfHelper.addTitle(pdf, "CONSIGNMENT STOCK RECEIVING", "GR Slip");

            PdfPTable info = PdfHelper.infoTable(1.5f, 2.5f, 1.5f, 2.5f);
            PdfHelper.addInfoRow(info, "Doc No", doc.docNo(), "Status", doc.status());
            PdfHelper.addInfoRow(info, "Company", doc.company(), "Receiving Store", doc.receivingStore());
            PdfHelper.addInfoRow(info, "Supplier Code", doc.supplierCode(), "Contract", doc.supplierContract());
            PdfHelper.addInfoRow(info, "Supplier DO No", doc.supplierDoNo(), "Delivery Date",
                    doc.deliveryDate() != null ? doc.deliveryDate().toString() : "-");
            PdfHelper.addInfoRow(info, "Created By", doc.createdBy(), "Released At",
                    doc.releasedAt() != null ? PdfHelper.DATE_FMT.format(doc.releasedAt()) : "-");
            if (doc.remark() != null && !doc.remark().isBlank()) {
                PdfHelper.addInfoRowFull(info, "Remark", doc.remark());
            }
            pdf.add(info);

            pdf.add(new Paragraph("Item Details", PdfHelper.HEADER_FONT));
            PdfPTable items = new PdfPTable(new float[]{0.5f, 2.5f, 1f, 1.5f, 1.5f});
            items.setWidthPercentage(100);
            items.setSpacingBefore(5);
            PdfHelper.addTableHeader(items, "No", "Item Code", "UOM", "Request Qty", "Received Qty");
            int n = 1;
            for (var item : doc.items()) {
                PdfHelper.addCell(items, String.valueOf(n++), Element.ALIGN_CENTER);
                PdfHelper.addCell(items, item.itemCode(), Element.ALIGN_LEFT);
                PdfHelper.addCell(items, "-", Element.ALIGN_CENTER);
                PdfHelper.addCell(items, item.requestQty().toPlainString(), Element.ALIGN_RIGHT);
                PdfHelper.addCell(items, item.receivingQty().toPlainString(), Element.ALIGN_RIGHT);
            }
            pdf.add(items);

            pdf.add(new Paragraph("\nGoods received in good condition.", PdfHelper.SMALL_FONT));
            PdfHelper.addSignatureRow(pdf, "Received By", "Checked By");
            PdfHelper.addFooterTimestamp(pdf);
            pdf.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSRV slip", e);
        }
    }
}
