package com.blr19c.common.code;


import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * 生成excel
 *
 * @author blr
 */
public class ExcelUtil {

    public static int defaultWidth = 15;//默认的单元格长度
    public static Charset defaultCharset = StandardCharsets.UTF_8;
    public static boolean defaultHeadLock = true;//头部单元格是否不可修改
    public static int defaultMargin = 5;//边距
    public static int defaultStartLine = 0;//起始行 0 开始
    public static int defaultBodyLine = 1;//内容行 1 开始
    public static int defaultAmplification = 256;//缩放
    public static int defaultMaxWidth = 100;//单元格最大大小
    public static short defaultHeadColor = Font.COLOR_RED;//头部默认颜色
    public static boolean headIsBold = true;//头部是否为粗体
    public static String uuid = UUID.randomUUID().toString();

    public static XSSFWorkbook getWorkbook() {
        return new XSSFWorkbook();
    }

    public static XSSFSheet getSheet() {
        return getSheet(getWorkbook());
    }

    public static XSSFSheet getSheet(XSSFWorkbook workbook) {
        return workbook.createSheet();
    }

    public static byte[] exportExcel(List<Map<String, Object>> dataList, String[] headData, String[] keyList) throws Exception {
        return exportExcel(dataList, headData, keyList, true);
    }

    /**
     * 生成excel
     *
     * @param dataList   数据
     * @param headData   标题
     * @param keyList    数据列表key
     * @param onlyReader 是否只读
     */
    public static byte[] exportExcel(List<Map<String, Object>> dataList, String[] headData, String[] keyList, boolean onlyReader) throws IOException {
        XSSFSheet sheet = getSheet();
        sheet.setSheetPassword(UUID.randomUUID().toString(), null);
        sheet.enableLocking();
        setDefaultCTSheetProtection(sheet);
        fillingSheet(sheet, dataList, headData, keyList, onlyReader);
        return getByteArray(sheet.getWorkbook());
    }

    public static byte[] exportExcel(List<List<Map<String, Object>>> dataList, String[][] headData, String[][] keyList, boolean onlyReader) throws IOException {
        XSSFWorkbook workbook = getWorkbook();
        for (int i = 0, size = dataList.size(); i < size; i++) {
            XSSFSheet sheet = getSheet(workbook);
            sheet.setSheetPassword(UUID.randomUUID().toString(), null);
            sheet.enableLocking();
            setDefaultCTSheetProtection(sheet);
            fillingSheet(sheet, dataList.get(i), headData[i], keyList[i], onlyReader);
        }
        return getByteArray(workbook);
    }

    private static void setDefaultCTSheetProtection(XSSFSheet sheet) {
        CTSheetProtection sheetProtection = sheet.getCTWorksheet().getSheetProtection();
        sheetProtection.setSelectLockedCells(false);
        sheetProtection.setSelectUnlockedCells(false);
        sheetProtection.setFormatCells(true);
        sheetProtection.setFormatColumns(true);
        sheetProtection.setFormatRows(true);
        sheetProtection.setInsertColumns(true);
        sheetProtection.setInsertRows(false);
        sheetProtection.setInsertHyperlinks(true);
        sheetProtection.setDeleteColumns(true);
        sheetProtection.setDeleteRows(true);
        sheetProtection.setSort(false);
        sheetProtection.setAutoFilter(false);
        sheetProtection.setPivotTables(true);
        sheetProtection.setObjects(true);
        sheetProtection.setScenarios(true);
    }

    private static byte[] getByteArray(Workbook workbook) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();
        return byteArrayOutputStream.toByteArray();
    }

    private static CellStyle getCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("@"));
        return style;
    }

    private static void fillingSheet(Sheet sheet, List<Map<String, Object>> dataList, String[] headData, String[] keyList, boolean onlyReader) {
        setHeaderData(sheet.createRow(defaultStartLine), headData);
        CellStyle style = getCellStyle(sheet.getWorkbook());
        style.setLocked(onlyReader);
        for (int i = 0, size = dataList.size(); i < size; i++) {
            int rowCount = i + defaultBodyLine;
            Row row = sheet.createRow(rowCount);
            Map<String, Object> data = dataList.get(i);
            int maxW = defaultWidth;
            for (int j = 0; j < keyList.length; j++) {
                Cell cell = row.createCell(j);
                String value = getCellValue(MapUtils.getObject(data, keyList[j]));
                int length = getLength(value);
                cell.setCellStyle(style);
                maxW = Math.max(length, maxW);
                if (StringUtils.isBlank(value)) {
                    cell.setBlank();
                } else {
                    cell.setCellValue(value);
                }
            }
            reSetCellSize(sheet, rowCount, maxW);
        }
        sheet.setColumnHidden(headData.length, true);
    }

    private static void setHeaderData(Row row, String[] headData) {
        Sheet sheet = row.getSheet();
        Workbook workbook = sheet.getWorkbook();
        Font font = workbook.createFont();
        font.setBold(headIsBold);
        font.setColor(defaultHeadColor);
        CellStyle headStyle = getCellStyle(workbook);
        headStyle.setLocked(defaultHeadLock);
        headStyle.setFont(font);
        int i = 0;
        for (; i < headData.length; i++) {
            String value = getCellValue(headData[i]);
            Cell cell = row.createCell(i);
            cell.setCellValue(value);
            cell.setCellStyle(headStyle);
            reSetCellSize(sheet, i, getLength(value));
        }
        row.createCell(i).setCellValue(uuid);
    }

    private static void reSetCellSize(Sheet sheet, int columnIndex, int width) {
        sheet.setColumnWidth(columnIndex, Math.min(Math.max(width, defaultWidth) + defaultMargin, defaultMaxWidth) * defaultAmplification);
    }

    private static int getLength(String value) {
        return value.getBytes(defaultCharset).length;
    }

    private static String getCellValue(Object object) {
        if (object == null) return "";
        if (object instanceof Cell) {
            return ((Cell) object).getStringCellValue();
        }
        if (object instanceof Date) {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(((Date) object).toInstant());
        }
        try {
            if (object instanceof Number) {
                return String.valueOf(((Number) object).doubleValue());
            }
            return new BigDecimal(String.valueOf(object)).toString();
        } catch (NumberFormatException e) {
            return new XSSFRichTextString(String.valueOf(object)).getString();
        }
    }
}