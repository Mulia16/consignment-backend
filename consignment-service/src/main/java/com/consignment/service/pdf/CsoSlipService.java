package com.consignment.service.pdf;

import com.consignment.service.model.cso.CsoResponse;
import com.consignment.service.service.CsoService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class CsoSlipService {

    private final CsoService csoService;

    public CsoSlipService(CsoService csoService) {
        this.csoService = csoService;
    }

    public byte[] generate(String id) {
        CsoResponse doc = csoService.getById(id);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document pdf = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(pdf, out);
            pdf.open();

            PdfHelper.addTitle(pdf, "CONSIGNMENT STOCK OUT", "Delivery Order");

            PdfPTable info = PdfHelper.infoTable(1.5f, 2.5f, 1.5f, 2.5f);
            PdfHelper.addInfoRow(info, "Doc No", doc.docNo(), "Status", doc.status());
            PdfHelper.addInfoRow(info, "Company", doc.company(), "Store", doc.store());
            PdfHelper.addInfoRow(info, "Customer Code", doc.customerCode(), "Customer Branch", doc.customerBranch());
            PdfHelper.addInfoRow(info, "Supplier Code", doc.supplierCode(), "Contract", doc.supplierContract());
            PdfHelper.addInfoRow(info, "Shipping Mode", doc.shippingMode(), "Transporter", doc.transporter());
            PdfHelper.addInfoRow(info, "Ship To", doc.shippingTo(), "Delivery Date",
                    doc.deliveryDate() != null ? doc.deliveryDate().toString() : "-");
            if (doc.shippingAddress() != null && !doc.shippingAddress().isBlank()) {
                PdfHelper.addInfoRowFull(info, "Shipping Address", doc.shippingAddress());
            }
            PdfHelper.addInfoRow(info, "Released By", doc.releasedBy(), "Released At",
                    doc.releasedAt() != null ? PdfHelper.DATE_FMT.format(doc.releasedAt()) : "-");
            pdf.add(info);

            pdf.add(new Paragraph("Item Details", PdfHelper.HEADER_FONT));
            PdfPTable items = new PdfPTable(new float[]{0.5f, 2.5f, 1f, 1.5f});
            items.setWidthPercentage(100);
            items.setSpacingBefore(5);
            PdfHelper.addTableHeader(items, "No", "Item Code", "UOM", "Qty");
            int n = 1;
            for (var item : doc.items()) {
                PdfHelper.addCell(items, String.valueOf(n++), Element.ALIGN_CENTER);
                PdfHelper.addCell(items, item.itemCode(), Element.ALIGN_LEFT);
                PdfHelper.addCell(items, item.uom(), Element.ALIGN_CENTER);
                PdfHelper.addCell(items, item.qty().toPlainString(), Element.ALIGN_RIGHT);
            }
            pdf.add(items);

            PdfHelper.addSignatureRow(pdf, "Prepared By", "Driver", "Received By");
            PdfHelper.addFooterTimestamp(pdf);
            pdf.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSO slip", e);
        }
    }
}
