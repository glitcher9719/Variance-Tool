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

class DataImport {

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
                DataImport.tableHeaders.add(i, cell.getStringCellValue());
                i++;
            }

            else if(cell.getCellTypeEnum() == CellType.NUMERIC) {
                DataImport.tableHeaders.add(i, String.valueOf(cell.getNumericCellValue()));
                i++;
            }

            else if (cell.getCellTypeEnum() == CellType.FORMULA) {
                switch(cell.getCachedFormulaResultTypeEnum()) {

                    case NUMERIC:
                        DataImport.tableHeaders.add(i, String.valueOf(cell.getNumericCellValue()));
                        i++;

                    case STRING:
                        DataImport.tableHeaders.add(i, cell.getStringCellValue());
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
        DataImport.tableHeaders.add(7, "Variance");
        for (int s = 0; s< y; s++) {
            double budget = Double.parseDouble(tableData.get(s).get(5));
            double actual = Double.parseDouble(tableData.get(s).get(6));
            double variance = Math.round(budget-actual);
            tableData.get(s).add(7, String.valueOf(variance));
        }

        // Calculating YTD
        DataImport.tableHeaders.add(8, "Budget YTD");
        DataImport.tableHeaders.add(9, "Actual YTD");
        DataImport.tableHeaders.add(10, "Variance YTD");
        double budgetYTD = 0;
        double actualYTD = 0;
        Object currentCostCode = tableData.get(0).get(0);
        for (int s = 0; s< y; s++) {
            if (currentCostCode.equals(tableData.get(s).get(0))) {

                double currentBudget = Double.parseDouble(tableData.get(s).get(5));
                budgetYTD += currentBudget;
                tableData.get(s).add(8, String.valueOf(budgetYTD));

                double currentActual = Double.parseDouble(tableData.get(s).get(6));
                actualYTD += currentActual;
                tableData.get(s).add(9, String.valueOf(actualYTD));

                double varianceYTD = budgetYTD - actualYTD;
                tableData.get(s).add(10, String.valueOf(varianceYTD));
            }

            else {
                currentCostCode = tableData.get(s).get(0);
                budgetYTD = 0;
                actualYTD = 0;

                double currentBudget = Double.parseDouble(tableData.get(s).get(5));
                budgetYTD += currentBudget;
                tableData.get(s).add(8, String.valueOf(budgetYTD));

                double currentActual = Double.parseDouble(tableData.get(s).get(6));
                actualYTD += currentActual;
                tableData.get(s).add(9, String.valueOf(actualYTD));

                double varianceYTD = budgetYTD - actualYTD;
                tableData.get(s).add(10, String.valueOf(varianceYTD));
            }
        }

        // generate cost code names set
        for (int a = 0; a< y; a++) {
            ccNames.add(tableData.get(a).get(0));
        }

        // generate period names set
        for (int a = 0; a< y; a++) {
            periodNames.add(tableData.get(a).get(2));
        }

        return new JTable(tableData, DataImport.tableHeaders);

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

        // Totals of PAY, NON PAY, INCOME and GRAND TOTAL
        //TODO: Create the vectors with the data calculated and add them to the specific table
        //TODO: Find a way to solve the duplicate code used to calculate the rows

        //PAY

        double totalPayYTDBudget = 0;
        double totalPayYTDActual = 0;
        double totalPayYTDVariance = 0;

        double totalPayBudget = 0;
        double totalPayActual = 0;
        double totalPayVariance = 0;


        double totalPayWTEBudget = 0;
        double totalPayWTEContracted = 0;
        double totalPayWTEWorked = 0;

        //NON PAY

        double totalNonPayYTDBudget = 0;
        double totalNonPayYTDActual = 0;
        double totalNonPayYTDVariance = 0;

        double totalNonPayBudget = 0;
        double totalNonPayActual = 0;
        double totalNonPayVariance = 0;


        double totalNonPayWTEBudget = 0;
        double totalNonPayWTEContracted = 0;
        double totalNonPayWTEWorked = 0;

        //Income

        double totalIncomeYTDBudget = 0;
        double totalIncomeYTDActual = 0;
        double totalIncomeYTDVariance = 0;

        double totalIncomeBudget = 0;
        double totalIncomeActual = 0;
        double totalIncomeVariance = 0;


        double totalIncomeWTEBudget = 0;
        double totalIncomeWTEContracted = 0;
        double totalIncomeWTEWorked = 0;


        for (int x = 0; x<sortedVector.size(); x++) {
            if (sortedVector.get(x).get(26).equals("Pay")) {
                totalPayBudget += Double.parseDouble(sortedVector.get(x).get(5));
                totalPayActual += Double.parseDouble(sortedVector.get(x).get(6));
                totalPayVariance += Double.parseDouble(sortedVector.get(x).get(7));
                totalPayYTDBudget += Double.parseDouble(sortedVector.get(x).get(8));
                totalPayYTDActual += Double.parseDouble(sortedVector.get(x).get(9));
                totalPayYTDVariance += Double.parseDouble(sortedVector.get(x).get(10));
                totalPayWTEBudget += Double.parseDouble(sortedVector.get(x).get(11));
                totalPayWTEContracted += Double.parseDouble(sortedVector.get(x).get(12));
                totalPayWTEWorked += Double.parseDouble(sortedVector.get(x).get(13));
            }

            if (sortedVector.get(x).get(26).equals("Non Pay")) {
                totalNonPayBudget += Double.parseDouble(sortedVector.get(x).get(5));
                totalNonPayActual += Double.parseDouble(sortedVector.get(x).get(6));
                totalNonPayVariance += Double.parseDouble(sortedVector.get(x).get(7));
                totalNonPayYTDBudget += Double.parseDouble(sortedVector.get(x).get(8));
                totalNonPayYTDActual += Double.parseDouble(sortedVector.get(x).get(9));
                totalNonPayYTDVariance += Double.parseDouble(sortedVector.get(x).get(10));
                totalNonPayWTEBudget += Double.parseDouble(sortedVector.get(x).get(11));
                totalNonPayWTEContracted += Double.parseDouble(sortedVector.get(x).get(12));
                totalNonPayWTEWorked += Double.parseDouble(sortedVector.get(x).get(13));
            }

            if (sortedVector.get(x).get(26).equals("Income")) {
                totalIncomeBudget += Double.parseDouble(sortedVector.get(x).get(5));
                totalIncomeActual += Double.parseDouble(sortedVector.get(x).get(6));
                totalIncomeVariance += Double.parseDouble(sortedVector.get(x).get(7));
                totalIncomeYTDBudget += Double.parseDouble(sortedVector.get(x).get(8));
                totalIncomeYTDActual += Double.parseDouble(sortedVector.get(x).get(9));
                totalIncomeYTDVariance += Double.parseDouble(sortedVector.get(x).get(10));
                totalIncomeWTEBudget += Double.parseDouble(sortedVector.get(x).get(11));
                totalIncomeWTEContracted += Double.parseDouble(sortedVector.get(x).get(12));
                totalIncomeWTEWorked += Double.parseDouble(sortedVector.get(x).get(13));
            }
        }

        //GrandTotal

        double totalGrandTotalYTDBudget = totalPayYTDBudget + totalNonPayYTDBudget + totalIncomeYTDBudget;
        double totalGrandTotalYTDActual = totalPayYTDActual + totalNonPayYTDActual + totalIncomeYTDActual;
        double totalGrandTotalYTDVariance = totalPayYTDVariance + totalNonPayYTDVariance + totalIncomeYTDVariance;

        double totalGrandTotalBudget = totalPayBudget + totalNonPayBudget + totalIncomeBudget;
        double totalGrandTotalActual = totalPayActual + totalNonPayActual + totalIncomeActual;
        double totalGrandTotalVariance = totalPayVariance + totalNonPayVariance + totalIncomeVariance;

        double totalGrandTotalWTEBudget = totalPayWTEBudget + totalNonPayWTEBudget + totalIncomeWTEBudget;
        double totalGrandTotalWTEContracted = totalPayWTEContracted + totalNonPayWTEContracted + totalIncomeWTEContracted;
        double totalGrandTotalWTEWorked = totalPayWTEWorked + totalNonPayWTEWorked + totalIncomeWTEWorked;

        Vector<Object> pay = new Vector<Object>();
        Vector<Object> nonpay = new Vector<Object>();
        Vector<Object> income = new Vector<Object>();
        Vector<Object> grandtotal = new Vector<Object>();

        return new JTable(sortedVector, tableHeaders);

    }
}