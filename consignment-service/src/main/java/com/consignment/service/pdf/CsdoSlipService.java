package com.consignment.service.pdf;

import com.consignment.service.model.csdo.CsdoResponse;
import com.consignment.service.service.CsdoService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class CsdoSlipService {

    private final CsdoService csdoService;

    public CsdoSlipService(CsdoService csdoService) {
        this.csdoService = csdoService;
    }

    public byte[] generate(String id) {
        CsdoResponse doc = csdoService.getById(id);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document pdf = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(pdf, out);
            pdf.open();

            PdfHelper.addTitle(pdf, "CONSIGNMENT DELIVERY ORDER", "CSDO Slip");

            PdfPTable info = PdfHelper.infoTable(1.5f, 2.5f, 1.5f, 2.5f);
            PdfHelper.addInfoRow(info, "Doc No", doc.docNo(), "Status", doc.status());
            PdfHelper.addInfoRow(info, "CSO Doc No", doc.csoDocNo(), "Company", doc.company());
            PdfHelper.addInfoRow(info, "Store", doc.store(), "Customer Code", doc.customerCode());
            PdfHelper.addInfoRow(info, "Customer Branch", doc.customerBranch(), "Shipping Mode", doc.shippingMode());
            PdfHelper.addInfoRow(info, "Transporter", doc.transporter(), "Released At",
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
            throw new RuntimeException("Failed to generate CSDO slip", e);
        }
    }
}
