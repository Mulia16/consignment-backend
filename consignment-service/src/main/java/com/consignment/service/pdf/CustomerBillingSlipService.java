package com.consignment.service.pdf;

import com.consignment.service.model.billing.CustomerBillingDetailResponse;
import com.consignment.service.model.billing.CustomerBillingResponse;
import com.consignment.service.service.CustomerBillingService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class CustomerBillingSlipService {

    private final CustomerBillingService customerBillingService;

    public CustomerBillingSlipService(CustomerBillingService customerBillingService) {
        this.customerBillingService = customerBillingService;
    }

    public byte[] generate(String id) {
        CustomerBillingResponse doc = customerBillingService.getById(id);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document pdf = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(pdf, out);
            pdf.open();

            PdfHelper.addTitle(pdf, "CUSTOMER CONSIGNMENT BILLING", "Invoice / TN");

            PdfPTable info = PdfHelper.infoTable(1.5f, 2.5f, 1.5f, 2.5f);
            PdfHelper.addInfoRow(info, "Doc No", doc.docNo(), "Status", doc.status());
            PdfHelper.addInfoRow(info, "Company", doc.company(), "Store", doc.store());
            PdfHelper.addInfoRow(info, "Customer Code", doc.customerCode(), "Branch", doc.customerBranch());
            PdfHelper.addInfoRow(info, "Period Type", doc.periodType(),
                    "Period",
                    (doc.fromDate() != null ? PdfHelper.DATE_ONLY.format(doc.fromDate()) : "-")
                    + " – "
                    + (doc.toDate() != null ? PdfHelper.DATE_ONLY.format(doc.toDate()) : "-"));
            PdfHelper.addInfoRow(info, "Created By", doc.createdBy(),
                    "Released At",
                    doc.releasedAt() != null ? PdfHelper.DATE_FMT.format(doc.releasedAt()) : "-");
            pdf.add(info);

            pdf.add(new Paragraph("Item Details", PdfHelper.HEADER_FONT));
            PdfPTable items = new PdfPTable(new float[]{0.4f, 2f, 0.8f, 1.2f, 1.2f, 1.2f, 1.5f, 1.5f});
            items.setWidthPercentage(100);
            items.setSpacingBefore(5);
            PdfHelper.addTableHeader(items, "No", "Item Code", "UOM",
                    "Sales Qty", "Return Qty", "Billing Qty", "Unit Price", "Amount");

            BigDecimal grandTotal = BigDecimal.ZERO;
            int n = 1;
            for (CustomerBillingDetailResponse item : doc.details()) {
                PdfHelper.addCell(items, String.valueOf(n++), Element.ALIGN_CENTER);
                PdfHelper.addCell(items, item.itemCode(), Element.ALIGN_LEFT);
                PdfHelper.addCell(items, item.uom(), Element.ALIGN_CENTER);
                PdfHelper.addCell(items, orDash(item.salesQty()), Element.ALIGN_RIGHT);
                PdfHelper.addCell(items, orDash(item.returnQty()), Element.ALIGN_RIGHT);
                PdfHelper.addCell(items, orDash(item.billingQty()), Element.ALIGN_RIGHT);
                PdfHelper.addCell(items, orDash(item.unitPrice()), Element.ALIGN_RIGHT);
                PdfHelper.addCell(items, orDash(item.lineAmount()), Element.ALIGN_RIGHT);
                if (item.lineAmount() != null) grandTotal = grandTotal.add(item.lineAmount());
            }

            // Total row
            PdfPCell totalLabel = new PdfPCell(new Phrase("GRAND TOTAL", PdfHelper.HEADER_FONT));
            totalLabel.setColspan(7);
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
            throw new RuntimeException("Failed to generate Customer Billing slip", e);
        }
    }

    private static String orDash(BigDecimal v) {
        return v != null ? v.toPlainString() : "-";
    }
}
