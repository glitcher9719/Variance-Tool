import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;

class ReadWriteExcelFile {

    // data for tables
    private static Vector<Vector<String>> tableData;

    // cost code names
    static LinkedHashSet<Object> ccNames = new LinkedHashSet<Object>();

    // period names
    static LinkedHashSet<Object> periodNames = new LinkedHashSet<Object>();

    // table headers
    private static Vector<String> tableHeaders = new Vector<String>();

    // static counters for the code
    private static int x;
    private static int y ;


    /***
     * Method for overview
     * @return JTable with all of the data
     * @throws IOException - for file not found
     */

    static JTable createTable() throws IOException {
        x = 0;
        y = 0;
        tableData = new Vector<Vector<String>>();
        InputStream ExcelFileToRead = new FileInputStream("src\\main\\resources\\Book1.xlsx");
        XSSFWorkbook  wb = new XSSFWorkbook(ExcelFileToRead);
        XSSFSheet sheet = wb.getSheetAt(0);
        XSSFRow row;
        XSSFCell cell;

        Iterator rows = sheet.rowIterator();
        XSSFRow tableHeaders = (XSSFRow)rows.next();
        Iterator it = tableHeaders.cellIterator();
        int i = 0;
        while (it.hasNext()) {
            cell=(XSSFCell) it.next();

            if (cell.getCellTypeEnum() == CellType.STRING) {
                ReadWriteExcelFile.tableHeaders.add(i, cell.getStringCellValue());
                i++;
            }

            else if(cell.getCellTypeEnum() == CellType.NUMERIC) {
                ReadWriteExcelFile.tableHeaders.add(i, String.valueOf(cell.getNumericCellValue()));
                i++;
            }

            else if (cell.getCellTypeEnum() == CellType.FORMULA) {
                switch(cell.getCachedFormulaResultTypeEnum()) {

                    case NUMERIC:
                        ReadWriteExcelFile.tableHeaders.add(i, String.valueOf(cell.getNumericCellValue()));
                        i++;

                    case STRING:
                        ReadWriteExcelFile.tableHeaders.add(i, cell.getStringCellValue());
                        i++;

                }
            }
        }

        while (rows.hasNext()) {
            row=(XSSFRow) rows.next();
            Iterator cells = row.cellIterator();
            Vector<String> currentRow = new Vector<String>();

            while (cells.hasNext()) {

                cell=(XSSFCell) cells.next();

                if (cell.getCellTypeEnum() == CellType.STRING) {

                    currentRow.add(x, cell.getStringCellValue());
                    x++;

                }

                else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                    currentRow.add(x, String.valueOf(cell.getNumericCellValue()));
                    x++;

                }

                else if (cell.getCellTypeEnum() == CellType.FORMULA)
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

        // Calculating Variance
        ReadWriteExcelFile.tableHeaders.add(7, "Variance");
        for (int s = 0; s< y; s++) {
            double budget = Double.parseDouble(tableData.get(s).get(5));
            double actual = Double.parseDouble(tableData.get(s).get(6));
            double variance = Math.round(budget-actual);
            tableData.get(s).add(7, String.valueOf(variance));
        }

        // generate cost code names set
        for (int a = 0; a< y; a++) {
            ccNames.add(tableData.get(a).get(0));
        }

        // generate period names set
        for (int a = 0; a< y; a++) {
            periodNames.add(tableData.get(a).get(2));
        }

        return new JTable(tableData, ReadWriteExcelFile.tableHeaders);

    }

    static JTable createSpecificTable(Object costCode, Object period) {

        // Sort each vector to match cost code and period parameters
        Vector<Vector<String>> sortedVector = new Vector<Vector<String>>();
        for (int a = 0; a< y; a++) {
            String x = tableData.get(a).get(0);
            String y = tableData.get(a).get(2);
            if (x.equals(costCode.toString()) && y.equals(period.toString())) {
                sortedVector.add(tableData.get(a));
            }
        }

        return new JTable(sortedVector, tableHeaders);

    }
}