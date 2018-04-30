import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;

public class ReadWriteExcelFile {

    public static void readXLSFile() throws IOException
    {
        InputStream ExcelFileToRead = new FileInputStream("C:/Test.xls");
        HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToRead);

        HSSFSheet sheet=wb.getSheetAt(0);
        HSSFRow row;
        HSSFCell cell;

        Iterator rows = sheet.rowIterator();

        while (rows.hasNext())
        {
            row=(HSSFRow) rows.next();
            Iterator cells = row.cellIterator();

            while (cells.hasNext())
            {
                cell=(HSSFCell) cells.next();

                if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING)
                {
                    System.out.print(cell.getStringCellValue()+" ");
                }
                else if(cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)
                {
                    System.out.print(cell.getNumericCellValue()+" ");
                }
                else
                {
                    //U Can Handel Boolean, Formula, Errors
                }
            }
            System.out.println();
        }

    }

    public static void writeXLSFile() throws IOException {

        String excelFileName = "C:/Test.xls";//name of excel file

        String sheetName = "Sheet1";//name of sheet

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(sheetName) ;

        //iterating r number of rows
        for (int r=0;r < 5; r++ )
        {
            HSSFRow row = sheet.createRow(r);

            //iterating c number of columns
            for (int c=0;c < 5; c++ )
            {
                HSSFCell cell = row.createCell(c);

                cell.setCellValue("Cell "+r+" "+c);
            }
        }

        FileOutputStream fileOut = new FileOutputStream(excelFileName);

        //write this workbook to an Outputstream.
        wb.write(fileOut);
        fileOut.flush();
        fileOut.close();
    }

    public static JTable readXLSXFile() throws IOException
    {

        InputStream ExcelFileToRead = new FileInputStream("C:\\Users\\Dan\\Downloads\\Book1.xlsx");
        XSSFWorkbook  wb = new XSSFWorkbook(ExcelFileToRead);

        XSSFWorkbook test = new XSSFWorkbook();

        XSSFSheet sheet = wb.getSheetAt(0);
        XSSFRow row;
        XSSFCell cell;

        Iterator rows = sheet.rowIterator();
        XSSFRow tableHeaders = (XSSFRow)rows.next();
        Iterator it = tableHeaders.cellIterator();
        String[] columnNames = new String[23];
        int i = 0;
        while (it.hasNext()) {
            cell=(XSSFCell) it.next();

            if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING)
            {
                columnNames[i] = cell.getStringCellValue();
                i++;
            }
            else if(cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC)
            {
                columnNames[i] = String.valueOf(cell.getNumericCellValue());
                i++;
            }
            else if (cell.getCellType() == XSSFCell.CELL_TYPE_FORMULA)
            {
                switch(cell.getCachedFormulaResultType()) {

                    case Cell.CELL_TYPE_NUMERIC:
                        columnNames[i] = String.valueOf(cell.getNumericCellValue());
                        i++;

                    case Cell.CELL_TYPE_STRING:
                        columnNames[i] = cell.getStringCellValue();
                        i++;

                }
            }
        }

        int x = 0;
        int y = 0;
        Vector<Vector<Object>> tableData = new Vector<Vector<Object>>();

        while (rows.hasNext())
        {
            row=(XSSFRow) rows.next();
            Iterator cells = row.cellIterator();
            Vector<Object> currentRow = new Vector<Object>();
            while (cells.hasNext())
            {

                cell=(XSSFCell) cells.next();

                if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {

                    currentRow.add(x, cell.getStringCellValue());
                    x++;

                }

                else if(cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
                    currentRow.add(x, String.valueOf(cell.getNumericCellValue()));
                    x++;

                }

                else if (cell.getCellType() == XSSFCell.CELL_TYPE_FORMULA)
                {
                    currentRow.add(x, String.valueOf(cell.getRawValue()));
                    x++;



                }
            }
            tableData.add(y, currentRow);
            y++;
            if (!rows.hasNext()){
                break;
            }
            x = 0;

        }

        Object[][] dataSet = new Object[y][x];
        for (int a = 0; a< y; a++) {
            for (int b = 0; b< x; b++){
                dataSet[a][b] = tableData.get(a).get(b);
            }
        }

        return new JTable(dataSet, columnNames);

    }

    public static void writeXLSXFile() throws IOException {

        String excelFileName = "C:/Test.xlsx";//name of excel file

        String sheetName = "Sheet1";//name of sheet

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet(sheetName) ;

        //iterating r number of rows
        for (int r=0;r < 5; r++ )
        {
            XSSFRow row = sheet.createRow(r);

            //iterating c number of columns
            for (int c=0;c < 5; c++ )
            {
                XSSFCell cell = row.createCell(c);

                cell.setCellValue("Cell "+r+" "+c);
            }
        }

        FileOutputStream fileOut = new FileOutputStream(excelFileName);

        //write this workbook to an Outputstream.
        wb.write(fileOut);
        fileOut.flush();
        fileOut.close();
    }

    public static void main(String[] args) throws IOException {

       /* writeXLSFile();
        readXLSFile();

        writeXLSXFile();*/
        readXLSXFile();

    }

}