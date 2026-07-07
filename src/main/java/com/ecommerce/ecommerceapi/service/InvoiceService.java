package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.Order;
import com.ecommerce.ecommerceapi.entity.OrderItem;
import com.ecommerce.ecommerceapi.exception.ResourceNotFoundException;
import com.ecommerce.ecommerceapi.repository.OrderRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceService {

    @Autowired
    private OrderRepository orderRepository;

    public byte[] generateInvoicePdf(Integer orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Font.BOLD);
        Paragraph title = new Paragraph("HOA DON BAN HANG (INVOICE)", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Info Block
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("Ma don hang: " + order.getOrderCode(), boldFont));
        document.add(new Paragraph("Ngay tao: " + order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), normalFont));
        document.add(new Paragraph("Khach hang: " + order.getUser().getUsername(), normalFont));
        document.add(new Paragraph("Dia chi giao hang: " + order.getShippingAddress(), normalFont));
        document.add(new Paragraph("Phuong thuc thanh toan: " + order.getPaymentMethod(), normalFont));
        document.add(new Paragraph("Trang thai: " + order.getStatus().name(), normalFont));
        document.add(Chunk.NEWLINE);

        // Table
        Table table = new Table(3);
        table.setWidth(100);
        table.setWidths(new float[]{60f, 20f, 20f});
        table.setPadding(5);

        Cell cell1 = new Cell(new Paragraph("Ten san pham", boldFont));
        Cell cell2 = new Cell(new Paragraph("So luong", boldFont));
        Cell cell3 = new Cell(new Paragraph("Don gia", boldFont));

        table.addCell(cell1);
        table.addCell(cell2);
        table.addCell(cell3);

        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                String name = item.getProduct().getName();
                if (item.getVariant() != null) {
                    name += " (" + item.getVariant().getName() + ")";
                }
                // Strip accents to ensure no rendering errors in standard PDF fonts
                String cleanName = stripAccents(name);
                table.addCell(new Cell(new Paragraph(cleanName, normalFont)));
                table.addCell(new Cell(new Paragraph(String.valueOf(item.getQuantity()), normalFont)));
                table.addCell(new Cell(new Paragraph(item.getPriceAtPurchase().toString() + " VND", normalFont)));
            }
        }

        document.add(table);

        // Total
        Paragraph total = new Paragraph("Tong thanh toan: " + order.getTotalPrice().toString() + " VND", boldFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(15);
        document.add(total);

        document.close();
        return out.toByteArray();
    }

    private String stripAccents(String s) {
        if (s == null) return "";
        String normalized = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String result = pattern.matcher(normalized).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
        // Filter out non-ASCII to prevent PDF rendering issues
        return result.replaceAll("[^\\x00-\\x7F]", "");
    }
}
