package com.lhh.serverbase.utils;

import com.alibaba.excel.util.DateUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;

public class ImportExcelUtils {
    /**
     * @param startrow //开始行号
     * @param startcol //开始列号
     * @param sheetnum //sheet
     * @return list
     */
    public static Map<String, List<List<String>>> readExcel(MultipartFile target, int startrow, int startcol, int sheetnum) {
        Map<String, List<List<String>>> resultMap = new HashMap<>();
        try {
            FileInputStream fi = (FileInputStream) target.getInputStream();
            HSSFWorkbook wb = new HSSFWorkbook(fi);
            int count = wb.getNumberOfSheets();
            for (int k = 0; k < count; k++) {
                HSSFSheet sheet = wb.getSheetAt(k);                    //sheet 从0开始
                String sheetName = sheet.getSheetName();
                List<List<String>> varList = readSheetdataHSSF2(sheet, startrow, startcol, sheetnum);
                resultMap.put(sheetName, varList);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return resultMap;
    }


    public static Map<String, List<Map<String, String>>> readExcel1(MultipartFile target, int startrow, int startcol, int sheetnum, String type) {
        Map<String, List<Map<String, String>>> resultMap = new HashMap<>();
        try {
            FileInputStream fi = (FileInputStream) target.getInputStream();
            if(type.equals(".xlsx")) {
                XSSFWorkbook wb = new XSSFWorkbook(fi);
                int count = wb.getNumberOfSheets();
                for (int k = 0; k < count; k++) {
                    XSSFSheet sheet = wb.getSheetAt(k);                    //sheet 从0开始
                    String sheetName = sheet.getSheetName();
                    List<Map<String, String>> varList = readSheetdataXSSF(sheet, startrow, startcol, sheetnum);
                    resultMap.put(sheetName, varList);
                }
            }else if(type.equals(".xls")){
                HSSFWorkbook wb = new HSSFWorkbook(fi);
                int count = wb.getNumberOfSheets();
                for (int k = 0; k < count; k++) {
                    HSSFSheet sheet = wb.getSheetAt(k);                    //sheet 从0开始
                    String sheetName = sheet.getSheetName();
                    List<Map<String, String>> varList = readSheetdataHSSF(sheet, startrow, startcol, sheetnum);
                    resultMap.put(sheetName, varList);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return resultMap;
    }

    public static List<Map<String, String>> readSheetdataHSSF(HSSFSheet sheet, int startrow, int startcol, int sheetnum) {
        List<Map<String, String>> varList = new ArrayList<>();
        int rowNum = sheet.getLastRowNum() + 1;                    //取得最后一行的行号

        for (int i = startrow; i < rowNum; i++) {
            //行循环开始
            Map<String, String> varpd = new HashMap();
            HSSFRow row = sheet.getRow(i);                            //行
            if (row == null) {
                break;
            }
            int cellNum = row.getLastCellNum();                    //每行的最后一个单元格位置
            if (row.getCell(0) == null) {
                break;
            }
            for (int j = startcol; j < cellNum; j++) {                //列循环开始
                HSSFCell cell = row.getCell(Short.parseShort(j + ""));
                String cellValue = null;
                if (null != cell) {
                    switch (cell.getCellType()) {                    // 判断excel单元格内容的格式，并对其进行转换，以便插入数据库
                        case NUMERIC:
                            //有可能是时间格式
                            if(HSSFDateUtil.isCellDateFormatted(cell)){
                                Date date = cell.getDateCellValue();
                                cellValue = DateUtils.format(date,"yyyy-MM-dd HH:mm:ss");
                            }else{
                                cellValue = new BigDecimal(cell.getNumericCellValue()).toString();
                            }
                            break;
                        case STRING:
                            cellValue = cell.getStringCellValue();
                            break;
                        case FORMULA:
                            cellValue = cell.getNumericCellValue() + "";
                            break;
                        case BLANK:
                            cellValue = "";
                            break;
                        case BOOLEAN:
                            cellValue = String.valueOf(cell.getBooleanCellValue());
                            break;
                        case ERROR:
                            cellValue = String.valueOf(cell.getErrorCellValue());
                            break;
                        default:
                            cellValue = "";
                            break;
                    }
                } else {
                    cellValue = "";
                }

                varpd.put(String.valueOf(j), cellValue);
                varpd = SortUtils.sortMapInteger(varpd);

            }
            varList.add(varpd);
        }
        return varList;
    }

    public static List<List<String>> readSheetdataHSSF2(HSSFSheet sheet, int startrow, int startcol, int sheetnum) {
        List<List<String>> varList = new ArrayList<>();
        int rowNum = sheet.getLastRowNum() + 1;                    //取得最后一行的行号

        for (int i = startrow; i < rowNum; i++) {
            //行循环开始
            List<String> varpd = new ArrayList<>();
            HSSFRow row = sheet.getRow(i);                            //行
            if (row == null) {
                break;
            }
            int cellNum = row.getLastCellNum();                    //每行的最后一个单元格位置
            if (row.getCell(0) == null) {
                break;
            }
            for (int j = startcol; j < cellNum; j++) {                //列循环开始
                HSSFCell cell = row.getCell(Short.parseShort(j + ""));
                String cellValue = null;
                if (null != cell) {
                    switch (cell.getCellType()) {                    // 判断excel单元格内容的格式，并对其进行转换，以便插入数据库
                        case NUMERIC:
                            //有可能是时间格式
                            if(HSSFDateUtil.isCellDateFormatted(cell)){
                                Date date = cell.getDateCellValue();
                                cellValue = DateUtils.format(date,"yyyy-MM-dd HH:mm:ss");
                            }else{
                                cellValue = new BigDecimal(cell.getNumericCellValue()).toString();
                            }
                            break;
                        case STRING:
                            cellValue = cell.getStringCellValue();
                            break;
                        case FORMULA:
                            cellValue = cell.getNumericCellValue() + "";
                            break;
                        case BLANK:
                            cellValue = "";
                            break;
                        case BOOLEAN:
                            cellValue = String.valueOf(cell.getBooleanCellValue());
                            break;
                        case ERROR:
                            cellValue = String.valueOf(cell.getErrorCellValue());
                            break;
                        default:
                            cellValue = "";
                            break;
                    }
                } else {
                    cellValue = "";
                }
                varpd.add(cellValue);
            }
            varList.add(varpd);
        }
        return varList;
    }

    public static List<Map<String, String>> readSheetdataXSSF(XSSFSheet sheet, int startrow, int startcol, int sheetnum) {
        List<Map<String, String>> varList = new ArrayList<>();
        int rowNum = sheet.getLastRowNum() + 1;                    //取得最后一行的行号

        for (int i = startrow; i < rowNum; i++) {                    //行循环开始
            Map<String, String> varpd = new HashMap();
            XSSFRow row = sheet.getRow(i);                            //行
            if (row == null) {
                break;
            }
            int cellNum = row.getLastCellNum();                    //每行的最后一个单元格位置
            if (row.getCell(0) == null) {
                break;
            }
            for (int j = startcol; j < cellNum; j++) {                //列循环开始
                XSSFCell cell = row.getCell(Short.parseShort(j + ""));
                String cellValue = null;
                if (null != cell) {
                    switch (cell.getCellType()) {                    // 判断excel单元格内容的格式，并对其进行转换，以便插入数据库
                        case NUMERIC:
                            cellValue = String.valueOf((double) cell.getNumericCellValue());
                            break;
                        case STRING:
                            cellValue = cell.getStringCellValue();
                            break;
                        case FORMULA:
                            cellValue = cell.getNumericCellValue() + "";
                            break;
                        case BLANK:
                            cellValue = "";
                            break;
                        case BOOLEAN:
                            cellValue = String.valueOf(cell.getBooleanCellValue());
                            break;
                        case ERROR:
                            cellValue = String.valueOf(cell.getErrorCellValue());
                            break;
                        default:
                            cellValue = "";
                            break;
                    }
                } else {
                    cellValue = "";
                }

                varpd.put(String.valueOf(j), cellValue);
                varpd = SortUtils.sortMapInteger(varpd);

            }
            varList.add(varpd);
        }
        return varList;
    }

}
