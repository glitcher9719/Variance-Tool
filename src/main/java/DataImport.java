import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sun.reflect.generics.tree.Tree;

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

    // department description

    static String name;

    private static String roundOffTo2DecPlaces(double val)
    {
        return String.format("%.2f", val).replace(',','.');
    }

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

        LinkedHashSet<String> head = new LinkedHashSet<String>();
         // generating lists
         for (int a = 0; a< y; a++) {
             head.add(tableData.get(a).get(0) + tableData.get(a).get(1));
             ccNames.add(tableData.get(a).get(0));
             periodNames.add(tableData.get(a).get(2));
         }

        // Calculating Variance
        DataImport.tableHeaders.add(7, "Variance");
        for (int s = 0; s< y; s++) {
            double budget = Double.parseDouble(tableData.get(s).get(5));
            double actual = Double.parseDouble(tableData.get(s).get(6));
            double variance = budget-actual;
            tableData.get(s).add(7, roundOffTo2DecPlaces(variance));
        }

        // Calculating YTD
        DataImport.tableHeaders.add(8, "Budget YTD");
        DataImport.tableHeaders.add(9, "Actual YTD");
        DataImport.tableHeaders.add(10, "Variance YTD");


        for (String element : head) {
            double budgetYTD = 0;
            double actualYTD = 0;
            for (int s = 0; s < y; s++) {

                double currentBudget = Double.parseDouble(tableData.get(s).get(5));
                double currentActual = Double.parseDouble(tableData.get(s).get(6));

                if (element.equals(tableData.get(s).get(0) + tableData.get(s).get(1))) {

                    budgetYTD += currentBudget;
                    tableData.get(s).add(8, roundOffTo2DecPlaces(budgetYTD));

                    actualYTD += currentActual;
                    tableData.get(s).add(9, roundOffTo2DecPlaces(actualYTD));

                    double varianceYTD = budgetYTD - actualYTD;
                    tableData.get(s).add(10, roundOffTo2DecPlaces(varianceYTD));

                }
            }
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
                Vector<String> tableVector = new Vector<String>(tableData.get(a));
                name = tableVector.get(16);
                tableVector.remove(16);
                tableVector.remove(16);
                tableVector.remove(16);
                tableVector.remove(16);
                tableVector.remove(16);
                tableVector.remove(16);
                tableVector.remove(16);
                tableVector.remove(16);
                tableVector.remove(14);

                sortedVector.add(tableVector);
            }
        }

        // Total class for PAY, NON PAY, INCOME and GRAND TOTAL

        class Total {

            private String name;

            private double budget;
            private double actual;
            private double variance;

            private double YTDBudget;
            private double YTDActual;
            private double YTDVariance;

            private double WTEBudget;
            private double WTEContracted;
            private double WTEWorked;

            private Total(String n) {
                this.name = n;
                this.budget = 0;
                this.actual = 0;
                this.variance = 0;
                this.YTDBudget = 0;
                this.YTDActual = 0;
                this.YTDVariance = 0;
                this.WTEBudget = 0;
                this.WTEContracted = 0;
                this.WTEWorked = 0;
            }

            private void budgetAdd(double b) {
                budget += b;
            }

            private void actualAdd(double a) {
                actual += a;
            }

            private void varianceAdd(double v) {
                variance += v;
            }

            private void YTDBudgetAdd(double yb) {
                YTDBudget += yb;
            }

            private void YTDActualAdd(double ya) {
                YTDActual += ya;
            }

            private void YTDVarianceAdd(double yv) {
                YTDVariance += yv;
            }

            private void WTEBudgetAdd(double wb) {
                WTEBudget += wb;
            }

            private void WTEContractedAdd(double wc) {
                WTEContracted += wc;
            }

            private void WTEWorkedAdd(double ww) {
                WTEWorked += ww;
            }

            private Vector<String> getTotal (Total object) {
                Vector<String> vector = new Vector<String>();
                vector.add(object.name);
                vector.add(null);
                vector.add(null);
                vector.add(null);
                vector.add(null);
                vector.add(roundOffTo2DecPlaces(object.budget));
                vector.add(roundOffTo2DecPlaces(object.actual));
                vector.add(roundOffTo2DecPlaces(object.variance));
                vector.add(roundOffTo2DecPlaces(object.YTDBudget));
                vector.add(roundOffTo2DecPlaces(object.YTDActual));
                vector.add(roundOffTo2DecPlaces(object.YTDVariance));
                vector.add(roundOffTo2DecPlaces(object.WTEBudget));
                vector.add(roundOffTo2DecPlaces(object.WTEContracted));
                vector.add(roundOffTo2DecPlaces(object.WTEWorked));

                return vector;
            }
        }

        Total pay = new Total("PAY");
        Total nonPay = new Total("NON PAY");
        Total income = new Total("INCOME");
        Total grandTotal = new Total("GRAND TOTAL");

        int payCounter = 0;
        int nonPayCounter= 0;
        int incomeCounter = 0;

        for (Vector<String> aSortedVector : sortedVector) {
            Total varTotal;

            if (aSortedVector.get(17).equals("Pay")) {
                varTotal = pay;
                payCounter++;
            } else if (aSortedVector.get(17).equals("Non Pay")) {

                nonPayCounter++;
                varTotal = nonPay;

            } else {
                incomeCounter++;
                varTotal = income;
            }

            varTotal.budgetAdd(Double.parseDouble(aSortedVector.get(5)));
            varTotal.actualAdd(Double.parseDouble(aSortedVector.get(6)));
            varTotal.varianceAdd(Double.parseDouble(aSortedVector.get(7)));
            varTotal.YTDBudgetAdd(Double.parseDouble(aSortedVector.get(8)));
            varTotal.YTDActualAdd(Double.parseDouble(aSortedVector.get(9)));
            varTotal.YTDVarianceAdd(Double.parseDouble(aSortedVector.get(10)));
            varTotal.WTEBudgetAdd(Double.parseDouble(aSortedVector.get(11)));
            varTotal.WTEContractedAdd(Double.parseDouble(aSortedVector.get(12)));
            varTotal.WTEWorkedAdd(Double.parseDouble(aSortedVector.get(13)));
        }

        // Grand total calculation

        grandTotal.budgetAdd(pay.budget + nonPay.budget + income.budget);
        grandTotal.actualAdd(pay.actual + nonPay.actual + income.actual);
        grandTotal.varianceAdd(pay.variance + nonPay.variance + income.variance);
        grandTotal.YTDBudgetAdd(pay.YTDBudget + nonPay.YTDBudget + income.YTDBudget);
        grandTotal.YTDActualAdd(pay.YTDActual + nonPay.YTDActual + income.YTDActual);
        grandTotal.YTDVarianceAdd(pay.YTDVariance + nonPay.YTDVariance + income.YTDVariance);
        grandTotal.WTEBudgetAdd(pay.WTEBudget + nonPay.WTEBudget + income.WTEBudget);
        grandTotal.WTEContractedAdd(pay.WTEContracted + nonPay.WTEContracted + income.WTEContracted);
        grandTotal.WTEWorkedAdd(pay.WTEWorked + nonPay.WTEWorked + income.WTEWorked);

        Vector<String> payVector = pay.getTotal(pay);

        Vector<String> nonPayVector = nonPay.getTotal(nonPay);

        Vector<String> incomeVector = income.getTotal(income);

        Vector<String> grandTotalVector = grandTotal.getTotal(grandTotal);

        // Logic behind totals counters and totals position in table

        if (incomeCounter != 0) {
            sortedVector.add(incomeCounter, incomeVector);
            payCounter += incomeCounter;
            payCounter++;
        }

        else {
            sortedVector.add(incomeVector);
        }

        if (!(payCounter<=incomeCounter+1)) {
            sortedVector.add(payCounter, payVector);
            nonPayCounter+= payCounter;
            nonPayCounter++;
        }

        else {
            sortedVector.add(payVector);
        }

        if (nonPayCounter != 0) {
            sortedVector.add(nonPayCounter, nonPayVector);
        }

        else {
            sortedVector.add(nonPayVector);

        }

        sortedVector.add(grandTotalVector);

        Vector<String> tableHeadersTruncated = new Vector<String>(tableHeaders);
        tableHeadersTruncated.remove(16);
        tableHeadersTruncated.remove(16);
        tableHeadersTruncated.remove(16);
        tableHeadersTruncated.remove(16);
        tableHeadersTruncated.remove(16);
        tableHeadersTruncated.remove(16);
        tableHeadersTruncated.remove(16);
        tableHeadersTruncated.remove(16);
         tableHeadersTruncated.remove(14);

        return new JTable(sortedVector, tableHeadersTruncated);

    }
}