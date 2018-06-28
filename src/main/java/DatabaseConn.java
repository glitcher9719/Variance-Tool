import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Vector;

class DatabaseConn {
    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/experimental-db?useSSL=false";
    static Vector<Vector<Object>> databaseEntries = new Vector<Vector<Object>>();
    static Vector<String> hd = new Vector<String>();

    static Vector<Vector<String>> tableData;

    // cost code names
    static LinkedHashSet<Object> ccNames = new LinkedHashSet<Object>();

    // period names
    static LinkedHashSet<Object> periodNames = new LinkedHashSet<Object>();

    // table headers
    private static Vector<String> tableHeadersExcel = new Vector<String>();

    // department description

    static String name;

    private static String roundOffTo2DecPlaces(double val)
    {
        return String.format("%.2f", val).replace(',','.');
    }

    static JTable generateDataFromDB() throws ClassNotFoundException {
        Connection conn;
        Statement stmt;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,"dan","ParolaMea123");
            stmt = conn.createStatement();
            String dataSQL;
            dataSQL = "SELECT * FROM data;";
            ResultSet rs = stmt.executeQuery(dataSQL);
            ResultSetMetaData headers = rs.getMetaData();
            int columnCount = headers.getColumnCount();
            for (int i = 1; i<= columnCount; i++) {
                hd.add(headers.getColumnName(i));
            }

            while(rs.next()) {
                Vector<Object> element = new Vector<Object >();
                String uniqueKey = rs.getString("Unique Key");
                String costCentre = rs.getString("Cost Centre");
                String expenseHead = rs.getString("Expense Header");
                int periodAndMonth = rs.getInt("Period and Month");
                int month = rs.getInt("Month");
                int year = rs.getInt("Year");
                double budget = rs.getDouble("Budget");
                double actuals = rs.getDouble("Actuals");
                double variance = rs.getDouble("Variance");
                double budgetYTD = rs.getDouble("Budget YTD");
                double actualsYTD = rs.getDouble("Actual YTD");
                double varianceYTD = rs.getDouble("VarianceYTD");
                double WTEBud = rs.getDouble("WTE Bud");
                double WTECon = rs.getDouble("WTE Con");
                double WTEWork = rs.getDouble("WTE Work");
                double WTEPaid = rs.getDouble("WTE Paid");
                String department = rs.getString("Department");
                String group = rs.getString("Group");
                String division = rs.getString("Division");
                String CDG = rs.getString("CDG");
                String service = rs.getString("Service");
                String nationalSpecialty = rs.getString("National Specialty");
                String name = rs.getString("Name");
                String investigationLimit = rs.getString("Investigation Limit");
                String expenseDescription = rs.getString("Expense Description");
                String expenseGrouping = rs.getString("Expense Grouping");
                String expenseType = rs.getString("Expense Type");
                element.add(uniqueKey);
                element.add(costCentre);
                element.add(expenseHead);
                element.add(periodAndMonth);
                element.add(month);
                element.add(year);
                element.add(budget);
                element.add(actuals);
                element.add(variance);
                element.add(budgetYTD);
                element.add(actualsYTD);
                element.add(varianceYTD);
                element.add(WTEBud);
                element.add(WTECon);
                element.add(WTEWork);
                element.add(WTEPaid);
                element.add(department);
                element.add(group);
                element.add(division);
                element.add(CDG);
                element.add(service);
                element.add(nationalSpecialty);
                element.add(name);
                element.add(investigationLimit);
                element.add(expenseDescription);
                element.add(expenseGrouping);
                element.add(expenseType);
                databaseEntries.add(element);
            }

            //STEP 6: Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        }

        catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }

        // generating lists
        for (Vector<Object> databaseEntry : databaseEntries) {
            ccNames.add(databaseEntry.get(1));
            periodNames.add(databaseEntry.get(3));
        }

        return new JTable(databaseEntries, hd);
    }

    static void importSpreadsheet(String path) throws IOException {
        int x = 0;
        int y = 0;
        tableData = new Vector<Vector<String>>();
        InputStream ExcelFileToRead = new FileInputStream(path);
        XSSFWorkbook wb = new XSSFWorkbook(ExcelFileToRead);
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
                tableHeadersExcel.add(i, cell.getStringCellValue());
                i++;
            }

            else if(cell.getCellTypeEnum() == CellType.NUMERIC) {
                tableHeadersExcel.add(i, String.valueOf(cell.getNumericCellValue()));
                i++;
            }

            else if (cell.getCellTypeEnum() == CellType.FORMULA) {
                switch(cell.getCachedFormulaResultTypeEnum()) {

                    case NUMERIC:
                        tableHeadersExcel.add(i, String.valueOf(cell.getNumericCellValue()));
                        i++;

                    case STRING:
                        tableHeadersExcel.add(i, cell.getStringCellValue());
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
        tableHeadersExcel.add(7, "Variance");
        for (int s = 0; s< y; s++) {
            double budget = Double.parseDouble(tableData.get(s).get(5));
            double actual = Double.parseDouble(tableData.get(s).get(6));
            double variance = budget-actual;
            tableData.get(s).add(7, roundOffTo2DecPlaces(variance));
        }

        // Calculating YTD
        tableHeadersExcel.add(8, "Budget YTD");
        tableHeadersExcel.add(9, "Actual YTD");
        tableHeadersExcel.add(10, "Variance YTD");


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
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,"dan","ParolaMea123");
            stmt = conn.createStatement();
            String checkSQL;
            checkSQL = "SELECT `Unique Key` FROM data;";
            ResultSet rs = stmt.executeQuery(checkSQL);

            //STEP 5: Extract data from result set

            Vector<String> uniqueKeys = new Vector<String >();
            while(rs.next()) {
                if (uniqueKeys.contains(rs.getString("Unique Key"))) {
                    throw new Exception("Invalid entries in database, unique keys should not contain duplicates!");
                }

                else {
                    uniqueKeys.add(rs.getString("Unique Key"));
                }
            }


            for (Vector<String> k : tableData) {
                if (uniqueKeys.contains(k.get(15))){
                    String updateSQL =  "UPDATE data " +
                                        "SET `Cost Centre` = '" + k.get(0) +
                                        "', `Expense Header` = '" + k.get(1) +
                                        "', `Period and Month` = '" + k.get(2) +
                                        "', `Month` = '" + k.get(3) +
                                        "', `Year` = '" + k.get(4) +
                                        "', `Budget` = '" + k.get(5) +
                                        "', `Actuals` = '" + k.get(6) +
                                        "', `Variance` = '" + k.get(7) +
                                        "', `Budget YTD` = '" + k.get(8) +
                                        "', `Actual YTD` = '" + k.get(9) +
                                        "', `VarianceYTD` = '" + k.get(10) +
                                        "', `WTE Bud` = '" + k.get(11) +
                                        "', `WTE Con` = '" + k.get(12) +
                                        "', `WTE Work` = '" + k.get(13) +
                                        "', `WTE Paid` = '" + k.get(14) +
                                        "', `Department` = '" + k.get(16) +
                                        "', `Group` = '" + k.get(17) +
                                        "', `Division` = '" + k.get(18) +
                                        "', `CDG` = '" + k.get(19) +
                                        "', `Service` = '" + k.get(20) +
                                        "', `National Specialty` = '" + k.get(21) +
                                        "', `Name` = '" + k.get(22) +
                                        "', `Investigation Limit` = '" + k.get(23) +
                                        "', `Expense Description` = '" + k.get(24) +
                                        "', `Expense Grouping` = '" + k.get(25) +
                                        "', `Expense Type` = '" + k.get(26) +
                                        "'WHERE `Unique Key` = '" + k.get(15) + "';";
                                        stmt.executeUpdate(updateSQL);
                }

                else {
                    String insertSQL = "INSERT INTO data VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                    PreparedStatement preparedStatement = conn.prepareStatement(insertSQL);
                    preparedStatement.setString(1, k.get(15));
                    preparedStatement.setString(2, k.get(0));
                    preparedStatement.setString(3, k.get(1));
                    preparedStatement.setInt(4, Integer.parseInt(k.get(2)));
                    preparedStatement.setInt(5, Integer.parseInt(k.get(3)));
                    preparedStatement.setInt(6, Integer.parseInt(k.get(4)));
                    preparedStatement.setDouble(7, Double.parseDouble(k.get(5)));
                    preparedStatement.setDouble(8, Double.parseDouble(k.get(6)));
                    preparedStatement.setDouble(9, Double.parseDouble(k.get(7)));
                    preparedStatement.setDouble(10, Double.parseDouble(k.get(8)));
                    preparedStatement.setDouble(11, Double.parseDouble(k.get(9)));
                    preparedStatement.setDouble(12, Double.parseDouble(k.get(10)));
                    preparedStatement.setDouble(13, Double.parseDouble(k.get(11)));
                    preparedStatement.setDouble(14, Double.parseDouble(k.get(12)));
                    preparedStatement.setDouble(15, Double.parseDouble(k.get(13)));
                    preparedStatement.setDouble(16, Double.parseDouble(k.get(14)));
                    preparedStatement.setString(17, k.get(16));
                    preparedStatement.setString(18, k.get(17));
                    preparedStatement.setString(19, k.get(18));
                    preparedStatement.setString(20, k.get(19));
                    preparedStatement.setString(21, k.get(20));
                    preparedStatement.setString(22, k.get(21));
                    preparedStatement.setString(23, k.get(22));
                    preparedStatement.setString(24, k.get(23));
                    preparedStatement.setString(25, k.get(24));
                    preparedStatement.setString(26, k.get(25));
                    preparedStatement.setString(27, k.get(26));
                    preparedStatement.executeUpdate();
                }
            }
            //STEP 6: Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        }

        catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }

        catch(Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }

        finally {
            //finally block used to close resources


            try {
                if(conn!=null) conn.close();
            }

            catch(SQLException se) {
                se.printStackTrace();
            }
        }

    }

    static JTable createSpecificTable(Object costCode, Object period) {

        // Sort each vector to match cost code and period parameters
        Vector<Vector<Object>> sortedVector = new Vector<Vector<Object>>();
        for (int a = 0; a < databaseEntries.size(); a++) {
            Object x = DatabaseConn.databaseEntries.get(a).get(1);
            Object y = DatabaseConn.databaseEntries.get(a).get(3);
            if (x.equals(costCode) && y.equals(period)) {
                Vector<Object> tableVector = new Vector<Object>(DatabaseConn.databaseEntries.get(a));
                name = tableVector.get(16).toString();
                tableVector.remove(0);
                tableVector.remove(14);
                tableVector.remove(14);
                tableVector.remove(14);
                tableVector.remove(14);
                tableVector.remove(14);
                tableVector.remove(14);
                tableVector.remove(14);
                tableVector.remove(14);
                tableVector.remove(14);
                tableVector.remove(2);
                tableVector.remove(2);
                tableVector.remove(2);

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

            private Vector<Object> getTotal (Total object) {
                Vector<Object> vector = new Vector<Object>();
                vector.add(object.name);
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

        for (Vector<Object> aSortedVector : sortedVector) {
            Total varTotal;

            if (aSortedVector.get(13).equals("Pay")) {
                varTotal = pay;
                payCounter++;
            } else if (aSortedVector.get(13).equals("Non Pay")) {

                nonPayCounter++;
                varTotal = nonPay;

            } else {
                incomeCounter++;
                varTotal = income;
            }

            varTotal.budgetAdd((Double) aSortedVector.get(2));
            varTotal.actualAdd((Double) aSortedVector.get(3));
            varTotal.varianceAdd((Double) aSortedVector.get(4));
            varTotal.YTDBudgetAdd((Double) aSortedVector.get(5));
            varTotal.YTDActualAdd((Double) aSortedVector.get(6));
            varTotal.YTDVarianceAdd((Double) aSortedVector.get(7));
            varTotal.WTEBudgetAdd((Double) aSortedVector.get(8));
            varTotal.WTEContractedAdd((Double) aSortedVector.get(9));
            varTotal.WTEWorkedAdd((Double) aSortedVector.get(10));
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

        Vector<Object> payVector = pay.getTotal(pay);

        Vector<Object> nonPayVector = nonPay.getTotal(nonPay);

        Vector<Object> incomeVector = income.getTotal(income);

        Vector<Object> grandTotalVector = grandTotal.getTotal(grandTotal);

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

        Vector<String> tableHeadersTruncated = new Vector<String>(DatabaseConn.hd);
        tableHeadersTruncated.remove(0);
        tableHeadersTruncated.remove(14);
        tableHeadersTruncated.remove(14);
        tableHeadersTruncated.remove(14);
        tableHeadersTruncated.remove(14);
        tableHeadersTruncated.remove(14);
        tableHeadersTruncated.remove(14);
        tableHeadersTruncated.remove(14);
        tableHeadersTruncated.remove(14);
        tableHeadersTruncated.remove(14);
        tableHeadersTruncated.remove(2);
        tableHeadersTruncated.remove(2);
        tableHeadersTruncated.remove(2);


        return new JTable(sortedVector, tableHeadersTruncated);

    }
}
