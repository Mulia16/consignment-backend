package com.consignment.service.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ExcelHelper {

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final short HEADER_COLOR = IndexedColors.ROYAL_BLUE.getIndex();

    private ExcelHelper() {}

    public static XSSFWorkbook newWorkbook() { return new XSSFWorkbook(); }

    public static CellStyle titleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 13);
        s.setFont(f);
        return s;
    }

    public static CellStyle headerStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(HEADER_COLOR);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }

    public static CellStyle altRowStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    public static CellStyle totalStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    public static Row addTitleRow(Sheet sheet, int rowNum, String title, int colSpan, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, colSpan - 1));
        return row;
    }

    public static Row addMetaRow(Sheet sheet, int rowNum, String text) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(text);
        return row;
    }

    public static Row addHeaderRow(Sheet sheet, int rowNum, List<String> headers, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(style);
        }
        return row;
    }

    public static Row addDataRow(Sheet sheet, int rowNum, List<Object> values, CellStyle altStyle, boolean alt) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < values.size(); i++) {
            Cell cell = row.createCell(i);
            Object v = values.get(i);
            if (v == null) {
                cell.setCellValue("-");
            } else if (v instanceof Number n) {
                cell.setCellValue(n.doubleValue());
            } else if (v instanceof LocalDate d) {
                cell.setCellValue(d.format(DATE_FMT));
            } else {
                cell.setCellValue(v.toString());
            }
            if (alt) cell.setCellStyle(altStyle);
        }
        return row;
    }

    public static void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(width + 512, 15000));
        }
    }

    public static String today() {
        return LocalDate.now(ZoneId.of("Asia/Jakarta")).format(DATE_FMT);
    }
}
