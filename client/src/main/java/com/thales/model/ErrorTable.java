package com.thales.model;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.Data;

import java.io.InputStream;
import java.util.HashMap;

@Data
public class ErrorTable {
    private static ErrorTable instance;

    private final HashMap<String,String> table;

    private ErrorTable(){
        HashMap<String,String> table = new HashMap<>();
        try (
            InputStream fis = ErrorTable.class.getResourceAsStream("/Protocolo de Troca de Mensagens.xlsx");
            Workbook workbook = new XSSFWorkbook(fis);
            ){

            Sheet sheet = workbook.getSheetAt(2);
            for (int rowIndex = 2; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                for (int colIndex = 0; colIndex <= row.getLastCellNum(); colIndex+=4){
                    DataFormatter formatter = new DataFormatter();

                    Cell keyCell = row.getCell(colIndex);
                    Cell valueCell = row.getCell(colIndex+1);
                    if (keyCell == null || valueCell == null){
                        continue;
                    }
                    table.put(
                        formatter.formatCellValue(keyCell), 
                        formatter.formatCellValue(valueCell)
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.table = table;
    }

    public static ErrorTable getInstance() {
        if(instance == null){
            instance = new ErrorTable();
        }
        return instance;
    }
}
