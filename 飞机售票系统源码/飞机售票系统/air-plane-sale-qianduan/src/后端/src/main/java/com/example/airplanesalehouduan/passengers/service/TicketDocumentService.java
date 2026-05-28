package com.example.airplanesalehouduan.passengers.service;

import com.example.airplanesalehouduan.passengers.entity.Ticket;
import com.example.airplanesalehouduan.passengers.other.TicketRepository;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * 机票文档生成服务
 * 使用Apache POI生成Word格式的机票文档
 */
@Service
public class TicketDocumentService {

    @Autowired
    private TicketRepository ticketRepository;

    /**
     * 生成机票文档（Word格式）
     * @param ticketNo 机票号
     * @return 文档字节数组
     */
    public byte[] generateTicketDocument(String ticketNo) throws IOException {
        // 查询机票信息
        Ticket ticket = ticketRepository.findByTicketNo(ticketNo)
                .orElseThrow(() -> new RuntimeException("机票不存在"));

        // 创建Word文档
        XWPFDocument document = new XWPFDocument();

        // 创建标题段落
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText("机票凭证");
        titleRun.setBold(true);
        titleRun.setFontSize(20);
        titleRun.setFontFamily("宋体");

        // 添加空行
        document.createParagraph();

        // 创建表格
        XWPFTable table = document.createTable(10, 2);
        table.setWidth("100%");

        // 设置表格样式（确保行和单元格存在）
        for (int i = 0; i < 10; i++) {
            XWPFTableRow row = getOrCreateRow(table, i);
            // 确保每行至少两个单元格
            while (row.getTableCells().size() < 2) {
                row.createCell();
            }
            row.getCell(0).setWidth("30%");
            row.getCell(1).setWidth("70%");
        }

        // 填充表格内容
        int rowIndex = 0;

        // 机票号
        setCellText(getOrCreateRow(table, rowIndex++), "机票号", ticket.getTicketNo());

        // 订单号
        setCellText(getOrCreateRow(table, rowIndex++), "订单号", ticket.getOrderNo());

        // 乘客姓名
        setCellText(getOrCreateRow(table, rowIndex++), "乘客姓名", ticket.getPassengerName());

        // 身份证号
        setCellText(getOrCreateRow(table, rowIndex++), "身份证号", ticket.getIdCard());

        // 联系电话
        setCellText(getOrCreateRow(table, rowIndex++), "联系电话", ticket.getPhone() != null ? ticket.getPhone() : "");

        // 航班信息
        setCellText(getOrCreateRow(table, rowIndex++), "航班号", ticket.getFlightNo());
        setCellText(getOrCreateRow(table, rowIndex++), "航线", ticket.getRoute());
        setCellText(getOrCreateRow(table, rowIndex++), "出发机场", ticket.getOriginAirport());
        setCellText(getOrCreateRow(table, rowIndex++), "到达机场", ticket.getDestAirport());

        // 时间信息
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String departureTime = ticket.getDepartureTime().format(dateTimeFormatter);
        String arrivalTime = ticket.getArrivalTime().format(dateTimeFormatter);
        setCellText(getOrCreateRow(table, rowIndex++), "起飞时间", departureTime);
        setCellText(getOrCreateRow(table, rowIndex++), "到达时间", arrivalTime);

        // 座位信息
        if (ticket.getSeatNumber() != null && !ticket.getSeatNumber().isEmpty()) {
            setCellText(getOrCreateRow(table, rowIndex++), "座位号", ticket.getSeatNumber());
        }
        if (ticket.getSeatClass() != null && !ticket.getSeatClass().isEmpty()) {
            setCellText(getOrCreateRow(table, rowIndex++), "舱位等级", ticket.getSeatClass());
        }

        // 价格信息
        setCellText(getOrCreateRow(table, rowIndex++), "基础票价", "¥" + ticket.getBasePrice());
        if (ticket.getSeatFee().compareTo(java.math.BigDecimal.ZERO) > 0) {
            setCellText(getOrCreateRow(table, rowIndex++), "座位选择费", "¥" + ticket.getSeatFee());
        }
        if (ticket.getDiscountFee().compareTo(java.math.BigDecimal.ZERO) > 0) {
            setCellText(getOrCreateRow(table, rowIndex++), "优惠金额", "¥" + ticket.getDiscountFee());
        }
        setCellText(getOrCreateRow(table, rowIndex++), "总价", "¥" + ticket.getTotalPrice());

        // 状态
        setCellText(getOrCreateRow(table, rowIndex++), "状态", ticket.getStatus());

        // 创建时间
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdAt = ticket.getCreatedAt().format(dateFormatter);
        setCellText(getOrCreateRow(table, rowIndex++), "出票时间", createdAt);

        // 添加底部说明
        document.createParagraph();
        XWPFParagraph notePara = document.createParagraph();
        notePara.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun noteRun = notePara.createRun();
        noteRun.setText("温馨提示：请妥善保管此机票凭证，出行时请携带有效身份证件。");
        noteRun.setFontSize(10);
        noteRun.setFontFamily("宋体");
        noteRun.setColor("666666");

        // 将文档写入字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.write(outputStream);
        document.close();

        return outputStream.toByteArray();
    }

    /**
     * 设置表格单元格文本
     */
    private void setCellText(XWPFTableRow row, String label, String value) {
        if (row == null) {
            return;
        }

        // Ensure at least two cells exist
        while (row.getTableCells().size() < 2) {
            row.createCell();
        }

        XWPFTableCell labelCell = row.getCell(0);
        // Ensure a paragraph and run exist for the label cell
        XWPFParagraph labelPara = labelCell.getParagraphs().size() > 0 ? labelCell.getParagraphs().get(0) : labelCell.addParagraph();
        XWPFRun labelRun = labelPara.getRuns().size() > 0 ? labelPara.getRuns().get(0) : labelPara.createRun();
        labelRun.setText(label != null ? label : "");
        labelRun.setBold(true);
        labelRun.setFontFamily("宋体");

        XWPFTableCell valueCell = row.getCell(1);
        XWPFParagraph valuePara = valueCell.getParagraphs().size() > 0 ? valueCell.getParagraphs().get(0) : valueCell.addParagraph();
        XWPFRun valueRun = valuePara.getRuns().size() > 0 ? valuePara.getRuns().get(0) : valuePara.createRun();
        valueRun.setText(value != null ? value : "");
        valueRun.setFontFamily("宋体");
    }

    /**
     * 确保表格中存在指定索引的行；如果不存在则创建新行直到存在为止
     */
    private XWPFTableRow getOrCreateRow(XWPFTable table, int index) {
        // Ensure the table has enough rows
        while (table.getNumberOfRows() <= index) {
            table.createRow();
        }

        XWPFTableRow row = table.getRow(index);

        // Ensure the row has at least two cells
        if (row != null) {
            while (row.getTableCells().size() < 2) {
                row.createCell();
            }
        }

        return row;
    }

    /**
     * 根据订单号生成所有机票文档（打包为ZIP）
     * @param orderNo 订单号
     * @return ZIP文件字节数组
     */
    public byte[] generateTicketDocumentsByOrderNo(String orderNo) throws IOException {
        // 查询订单下的所有机票
        java.util.List<Ticket> tickets = ticketRepository.findByOrderNo(orderNo);
        if (tickets.isEmpty()) {
            throw new RuntimeException("订单下没有机票");
        }

        // 创建ZIP文件
        java.util.zip.ZipOutputStream zipOut = null;
        java.io.ByteArrayOutputStream zipBaos = new java.io.ByteArrayOutputStream();

        try {
            zipOut = new java.util.zip.ZipOutputStream(zipBaos);

            // 为每张机票生成文档
            for (Ticket ticket : tickets) {
                byte[] documentBytes = generateTicketDocument(ticket.getTicketNo());
                java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(
                        "机票_" + ticket.getTicketNo() + ".docx");
                zipOut.putNextEntry(entry);
                zipOut.write(documentBytes);
                zipOut.closeEntry();
            }
        } finally {
            if (zipOut != null) {
                zipOut.close();
            }
        }

        return zipBaos.toByteArray();
    }
}

