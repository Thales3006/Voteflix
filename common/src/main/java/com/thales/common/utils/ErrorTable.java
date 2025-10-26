package com.thales.common.utils;

import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.thales.common.model.ErrorStatus;

import lombok.Data;

import java.io.InputStream;
import java.util.HashMap;

@Data
public class ErrorTable {
    private static ErrorTable instance;

    private final HashMap<ErrorStatus,Pair<String,String>> table;

    private ErrorTable(){
        HashMap<ErrorStatus,Pair<String,String>> table = new HashMap<>();
        try (
            InputStream fis = ErrorTable.class.getResourceAsStream("/Protocolo de Troca de Mensagens.xlsx");
            Workbook workbook = new XSSFWorkbook(fis);
            ){

            Sheet sheet = workbook.getSheetAt(2);
            for (int rowIndex = 2; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            int colIndex = 0;
            boolean first = true;
            while (colIndex <= row.getLastCellNum()) {
                DataFormatter formatter = new DataFormatter();

                Cell keyCell = row.getCell(colIndex);
                Cell nameCell = row.getCell(colIndex+1);
                Cell descriptonCell = row.getCell(colIndex+2);
                if (keyCell == null || nameCell == null){
                if (first) {
                    colIndex += 4;
                    first = false;
                } else {
                    colIndex += 3;
                }
                continue;
                }
                table.put(
                ErrorStatus.fromCode(formatter.formatCellValue(keyCell)),
                new Pair<String,String>(formatter.formatCellValue(nameCell), formatter.formatCellValue(descriptonCell))
                );
                if (first) {
                colIndex += 4;
                first = false;
                } else {
                colIndex += 3;
                }
            }
            }

        } catch (Exception e) {
            System.out.println("Failed to load .xlsx file");
        }
        this.table = table;
    }

    public static ErrorTable getInstance() {
        if(instance == null){
            instance = new ErrorTable();
        }
        return instance;
    }

    public Pair<String,String> get(ErrorStatus key){
        return instance.table.get(key);
    }
/* 
    public Pair<String,String> get(String key){
        try {
            return instance.table.get(ErrorType.valueOf(key));
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }*/
}
