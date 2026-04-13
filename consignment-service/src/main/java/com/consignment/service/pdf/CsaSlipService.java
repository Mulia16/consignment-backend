package com.consignment.service.pdf;

import com.consignment.service.model.csa.CsaResponse;
import com.consignment.service.service.CsaService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class CsaSlipService {

    private final CsaService csaService;

    public CsaSlipService(CsaService csaService) {
        this.csaService = csaService;
    }

    public byte[] generate(String id) {
        CsaResponse doc = csaService.getById(id);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document pdf = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(pdf, out);
            pdf.open();

            PdfHelper.addTitle(pdf, "CONSIGNMENT STOCK ADJUSTMENT", "Adjustment Note");

            PdfPTable info = PdfHelper.infoTable(1.5f, 2.5f, 1.5f, 2.5f);
            PdfHelper.addInfoRow(info, "Doc No", doc.docNo(), "Status", doc.status());
            PdfHelper.addInfoRow(info, "Company", doc.company(), "Store", doc.store());
            PdfHelper.addInfoRow(info, "Supplier Code", doc.supplierCode(), "Contract", doc.supplierContract());
            PdfHelper.addInfoRow(info, "Adj. Type", doc.transactionType(), "Reason Code", doc.reasonCode());
            PdfHelper.addInfoRow(info, "Released By", doc.releasedBy(), "Released At",
                    doc.releasedAt() != null ? PdfHelper.DATE_FMT.format(doc.releasedAt()) : "-");
            if (doc.remark() != null && !doc.remark().isBlank()) {
                PdfHelper.addInfoRowFull(info, "Remark", doc.remark());
            }
            pdf.add(info);

            pdf.add(new Paragraph("Item Details", PdfHelper.HEADER_FONT));
            PdfPTable items = new PdfPTable(new float[]{0.5f, 2.5f, 1f, 1.5f, 2f});
            items.setWidthPercentage(100);
            items.setSpacingBefore(5);
            PdfHelper.addTableHeader(items, "No", "Item Code", "UOM", "Adj. Qty", "Settlement Decision");
            int n = 1;
            for (var item : doc.items()) {
                PdfHelper.addCell(items, String.valueOf(n++), Element.ALIGN_CENTER);
                PdfHelper.addCell(items, item.itemCode(), Element.ALIGN_LEFT);
                PdfHelper.addCell(items, item.uom(), Element.ALIGN_CENTER);
                PdfHelper.addCell(items, item.qty().toPlainString(), Element.ALIGN_RIGHT);
                PdfHelper.addCell(items, item.settlementDecision(), Element.ALIGN_CENTER);
            }
            pdf.add(items);

            PdfHelper.addSignatureRow(pdf, "Prepared By", "Approved By");
            PdfHelper.addFooterTimestamp(pdf);
            pdf.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSA slip", e);
        }
    }
}
