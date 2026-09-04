package com.example.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ExcelXmlUtil {

    private ExcelXmlUtil() {
    }

    public static void writeWorkbook(OutputStream outputStream, String sheetName, List<String> columns,
            List<List<Object>> rows) throws IOException {
        String safeSheetName = sanitizeSheetName(sheetName);
        StringBuilder xml = new StringBuilder(8192);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<?mso-application progid=\"Excel.Sheet\"?>");
        xml.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"");
        xml.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"");
        xml.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"");
        xml.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">");
        xml.append("<Styles>");
        xml.append("<Style ss:ID=\"Header\"><Font ss:Bold=\"1\"/><Interior ss:Color=\"#DCE6F1\" ss:Pattern=\"Solid\"/></Style>");
        xml.append("</Styles>");
        xml.append("<Worksheet ss:Name=\"").append(escapeXml(safeSheetName)).append("\">");
        xml.append("<Table>");

        xml.append("<Row>");
        for (String column : columns) {
            xml.append("<Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">")
                    .append(escapeXml(column))
                    .append("</Data></Cell>");
        }
        xml.append("</Row>");

        for (List<Object> row : rows) {
            xml.append("<Row>");
            for (Object value : row) {
                appendCell(xml, value);
            }
            xml.append("</Row>");
        }

        xml.append("</Table>");
        xml.append("</Worksheet>");
        xml.append("</Workbook>");

        outputStream.write(xml.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void appendCell(StringBuilder xml, Object value) {
        if (value instanceof Number) {
            xml.append("<Cell><Data ss:Type=\"Number\">")
                    .append(escapeXml(String.valueOf(value)))
                    .append("</Data></Cell>");
            return;
        }

        xml.append("<Cell><Data ss:Type=\"String\">")
                .append(escapeXml(value == null ? "" : String.valueOf(value)))
                .append("</Data></Cell>");
    }

    private static String sanitizeSheetName(String sheetName) {
        String value = sheetName == null ? "Report" : sheetName.trim();
        if (value.isEmpty()) {
            value = "Report";
        }
        value = value.replaceAll("[\\\\/:\\*\\?\\[\\]]", " ");
        return value.length() > 31 ? value.substring(0, 31) : value;
    }

    private static String escapeXml(String value) {
        String text = value == null ? "" : value;
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
