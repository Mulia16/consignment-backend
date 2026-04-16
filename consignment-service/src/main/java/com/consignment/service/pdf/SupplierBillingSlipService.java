package com.consignment.service.pdf;

import com.consignment.service.model.billing.SupplierBillingDetailResponse;
import com.consignment.service.model.billing.SupplierBillingResponse;
import com.consignment.service.service.SupplierBillingService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class SupplierBillingSlipService {

    private final SupplierBillingService supplierBillingService;

    public SupplierBillingSlipService(SupplierBillingService supplierBillingService) {
        this.supplierBillingService = supplierBillingService;
    }

    public byte[] generate(String id) {
        SupplierBillingResponse doc = supplierBillingService.getById(id);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document pdf = new Document(PageSize.A4.rotate(), 40, 40, 50, 40);
            PdfWriter.getInstance(pdf, out);
            pdf.open();

            PdfHelper.addTitle(pdf, "SUPPLIER CONSIGNMENT BILLING REQUEST", "PO / GR Summary");

            PdfPTable info = PdfHelper.infoTable(1.5f, 2.5f, 1.5f, 2.5f);
            PdfHelper.addInfoRow(info, "Doc No", doc.docNo(), "Status", doc.status());
            PdfHelper.addInfoRow(info, "Company", doc.company(), "Store", doc.store());
            PdfHelper.addInfoRow(info, "Supplier Code", doc.supplierCode(), "Contract", doc.supplierContract());
            PdfHelper.addInfoRow(info, "Supplier Type", doc.supplierType(), "Period Type", doc.periodType());
            PdfHelper.addInfoRow(info, "Period",
                    (doc.fromDate() != null ? PdfHelper.DATE_ONLY.format(doc.fromDate()) : "-")
                    + " – "
                    + (doc.toDate() != null ? PdfHelper.DATE_ONLY.format(doc.toDate()) : "-"),
                    "CF Decimal", doc.carryForwardDecimal() ? "Yes" : "No");
            PdfHelper.addInfoRow(info, "Created By", doc.createdBy(),
                    "Released At",
                    doc.releasedAt() != null ? PdfHelper.DATE_FMT.format(doc.releasedAt()) : "-");
            pdf.add(info);

            pdf.add(new Paragraph("Item Details", PdfHelper.HEADER_FONT));
            PdfPTable items = new PdfPTable(new float[]{0.4f, 2f, 0.7f, 1.1f, 1.1f, 1f, 1.2f, 1f, 1.3f, 1.5f});
            items.setWidthPercentage(100);
            items.setSpacingBefore(5);
            PdfHelper.addTableHeader(items, "No", "Item Code", "UOM",
                    "Sales Qty", "Return Qty", "BF Qty", "Billing Qty", "CF Qty", "Unit Cost", "Amount");

            BigDecimal grandTotal = BigDecimal.ZERO;
            int n = 1;
            for (SupplierBillingDetailResponse item : doc.details()) {
                PdfHelper.addCell(items, String.valueOf(n++), Element.ALIGN_CENTER);
                PdfHelper.addCell(items, item.itemCode(), Element.ALIGN_LEFT);
                PdfHelper.addCell(items, item.uom(), Element.ALIGN_CENTER);
                PdfHelper.addCell(items, orDash(item.salesQty()), Element.ALIGN_RIGHT);
                PdfHelper.addCell(items, orDash(item.salesReturnQty()), Element.ALIGN_RIGHT);
                PdfHelper.addCell(items, orDash(item.bfQty()), Element.ALIGN_RIGHT);
                PdfHelper.addCell(items, orDash(item.billingQty()), Element.ALIGN_RIGHT);
                PdfHelper.addCell(items, orDash(item.cfQty()), Element.ALIGN_RIGHT);
                PdfHelper.addCell(items, orDash(item.unitCost()), Element.ALIGN_RIGHT);
                PdfHelper.addCell(items, orDash(item.totalCost()), Element.ALIGN_RIGHT);
                if (item.totalCost() != null) grandTotal = grandTotal.add(item.totalCost());
            }

            // Total row
            PdfPCell totalLabel = new PdfPCell(new Phrase("GRAND TOTAL", PdfHelper.HEADER_FONT));
            totalLabel.setColspan(9);
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabel.setPadding(5);
            items.addCell(totalLabel);
            PdfHelper.addCell(items, grandTotal.toPlainString(), Element.ALIGN_RIGHT);
            pdf.add(items);

            PdfHelper.addSignatureRow(pdf, "Prepared By", "Authorized By");
            PdfHelper.addFooterTimestamp(pdf);
            pdf.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Supplier Billing slip", e);
        }
    }

    private static String orDash(BigDecimal v) {
        return v != null ? v.toPlainString() : "-";
    }
}
