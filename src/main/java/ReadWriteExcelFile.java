import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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

class ReadWriteExcelFile {

    private static Vector<Vector<Object>> tableData = new Vector<Vector<Object>>();
    private static String[] columnNames = new String[23];
    private static Vector<Object> costCodeNames;
    private static Object[] ccNames;
    static String[] ccFinal;
    private static int x = 0;
    private static int y = 0;


    /***
     * Method for overview
     * @return JTable with all of the data
     * @throws IOException - for file not found
     */
    static JTable createTable() throws IOException {

        InputStream ExcelFileToRead = new FileInputStream("C:\\Users\\Dan\\Downloads\\Book1.xlsx");
        XSSFWorkbook  wb = new XSSFWorkbook(ExcelFileToRead);

        XSSFWorkbook test = new XSSFWorkbook();

        XSSFSheet sheet = wb.getSheetAt(0);
        XSSFRow row;
        XSSFCell cell;

        Iterator rows = sheet.rowIterator();
        XSSFRow tableHeaders = (XSSFRow)rows.next();
        Iterator it = tableHeaders.cellIterator();
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

        // Converting a vector of vectors which contain all elements from imported data into a double-dim array for
        // creating a new JPanel
        Object[][] dataSet = new Object[y][x];
        for (int a = 0; a< y; a++) {
            for (int b = 0; b< x; b++){
                dataSet[a][b] = tableData.get(a).get(b);
            }
        }

        return new JTable(dataSet, columnNames);

    }

    static JTable createSpecificTable(String costCode) {

        // Separate each vector in vector of vectors with the same cost code
        Object currentCostCode = "";
        HashMap<Object, Vector<Vector<Object>>> costCodeMap = new HashMap<Object, Vector<Vector<Object>>>();
        Vector<Vector<Object>> vector = new Vector<Vector<Object>>();
        for (int a = 0; a< y; a++) {
            if (!(tableData.get(a).get(0).toString().equals(currentCostCode))) {
                costCodeMap.put(currentCostCode, vector);
                vector = new Vector<Vector<Object>>();
                costCodeNames.add(currentCostCode);
                currentCostCode = tableData.get(a).get(0);
                vector.add(tableData.get(a));
            }

            else {
                vector.add(tableData.get(a));
            }
        }
        costCodeMap.remove("");
        costCodeNames.remove(0);
        ccNames =  costCodeNames.toArray();
        ccFinal = new String[ccNames.length-1];
        int i = 0;
        for (Object x: ccNames) {
            ccFinal[i] = x.toString();
            i++;
        }
        Vector<Vector<Object>> vectorCostCode = costCodeMap.get(costCode);
        Object[][] array = new Object[vectorCostCode.size()][vectorCostCode.get(0).size()];
        for (int a = 0; a< vectorCostCode.size(); a++) {
            for (int b = 0; b< vectorCostCode.get(0).size(); b++){
                array[a][b] = tableData.get(a).get(b);
            }
        }
        return new JTable(array, columnNames);

    }
}