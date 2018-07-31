import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

class DatabaseConn {

    // JDBC driver name and database URL
    final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    final String DB_URL = "jdbc:mysql://localhost:3306/experimental-db?useSSL=false";
    private Vector<Vector<String>> databaseEntries = new Vector<>();
    private Vector<Vector<String>> sortedVector = new Vector<>();
    private Vector<String> hd = new Vector<>();
    LinkedHashSet<Object> ccNames = new LinkedHashSet<>();
    LinkedHashSet<Object> periodNames = new LinkedHashSet<>();
    LinkedHashSet<Object> names = new LinkedHashSet<>();
    LinkedHashSet<Object> divisions = new LinkedHashSet<>();
    LinkedHashSet<Object> CDGs = new LinkedHashSet<>();
    int numberOfRows;
    private DefaultTableModel model;
    DecimalFormat nf = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    private DecimalFormatSymbols symbols = nf.getDecimalFormatSymbols();

    private int rowsCompleted;

    // department description

    String name;

    // flush
    private boolean bol = false;

    private static String roundOffTo2DecPlaces(double val) {
        return String.format("%.2f", val).replace(',', '.');
    }

    JTable generateDataFromDB() throws ClassNotFoundException {
        Connection conn;
        Statement stmt;
        symbols.setCurrencySymbol(""); // Don't use null.
        nf.setDecimalFormatSymbols(symbols);
        try {
            databaseEntries.clear();
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, "dan", "ParolaMea123");
            stmt = conn.createStatement();
            String dataSQL;
            dataSQL = "SELECT * FROM data;";
            ResultSet rs = stmt.executeQuery(dataSQL);
            ccNames.add("ALL");

            if (!bol) {
                ResultSetMetaData headers = rs.getMetaData();
                int columnCount = headers.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    hd.add(headers.getColumnName(i));
                }

                bol = true;
            }

            while (rs.next()) {
                Vector<String> element = new Vector<String>();
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
                String note = rs.getString("Note");
                element.add(uniqueKey);
                element.add(costCentre);
                element.add(expenseHead);
                element.add(String.valueOf(periodAndMonth));
                element.add(String.valueOf(month));
                element.add(String.valueOf(year));
                element.add(nf.format(budget));
                element.add(nf.format(actuals));
                element.add(nf.format(variance));
                element.add(nf.format(budgetYTD));
                element.add(nf.format(actualsYTD));
                element.add(nf.format(varianceYTD));
                element.add(nf.format(WTEBud));
                element.add(nf.format(WTECon));
                element.add(nf.format(WTEWork));
                element.add(nf.format(WTEPaid));
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
                element.add(note);
                ccNames.add(costCentre);
                periodNames.add(String.valueOf(periodAndMonth));
                databaseEntries.add(element);
            }

            //STEP 6: Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        }

        catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }

        return new JTable(databaseEntries, hd);
    }

    void importSpreadsheet(String path) throws IOException {
        long start = System.currentTimeMillis();
        int x = 0;
        int y = 0;
        Vector<Vector<String>> tableData = new Vector<Vector<String>>();
        TreeMap<String, Vector<Double>> head = new TreeMap<String, Vector<Double>>();
        InputStream ExcelFileToRead = new FileInputStream(path);
        XSSFWorkbook wb = new XSSFWorkbook(ExcelFileToRead);
        XSSFSheet sheet = wb.getSheetAt(0);
        numberOfRows = sheet.getPhysicalNumberOfRows();
        XSSFRow row;
        XSSFCell cell;
        Iterator rows = sheet.rowIterator();
        rows.next();
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            Iterator cells = row.cellIterator();
            Vector<String> currentRow = new Vector<String>();

            while (cells.hasNext()) {

                cell = (XSSFCell) cells.next();

                if (cell.getCellTypeEnum() == CellType.STRING) {

                    currentRow.add(x, cell.getStringCellValue());
                    x++;

                } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                    currentRow.add(x, String.valueOf(cell.getNumericCellValue()));
                    x++;

                } else if (cell.getCellTypeEnum() == CellType.FORMULA) {
                    currentRow.add(x, String.valueOf(cell.getRawValue()));
                    x++;

                }
            }

            tableData.add(y, currentRow);

            // Calculating variance
            double budget = Double.parseDouble(tableData.get(y).get(5));
            double actual = Double.parseDouble(tableData.get(y).get(6));
            double variance = budget - actual;
            tableData.get(y).add(7, roundOffTo2DecPlaces(variance));

            // Calculating YTD
            if (head.containsKey(tableData.get(y).get(0) + tableData.get(y).get(1))) {
                Vector<Double> currentValues = head.get(tableData.get(y).get(0) + tableData.get(y).get(1));
                double budgetYTD = currentValues.get(0);
                double actualYTD = currentValues.get(1);
                double varianceYTD = currentValues.get(2);

                double currentBudget = Double.parseDouble(tableData.get(y).get(5));
                double currentActual = Double.parseDouble(tableData.get(y).get(6));

                budgetYTD += currentBudget;
                actualYTD += currentActual;
                varianceYTD += budgetYTD - actualYTD;

                head.get(tableData.get(y).get(0) + tableData.get(y).get(1)).clear();
                head.get(tableData.get(y).get(0) + tableData.get(y).get(1)).add(budgetYTD);
                head.get(tableData.get(y).get(0) + tableData.get(y).get(1)).add(actualYTD);
                head.get(tableData.get(y).get(0) + tableData.get(y).get(1)).add(varianceYTD);

                tableData.get(y).add(8, roundOffTo2DecPlaces(budgetYTD));
                tableData.get(y).add(9, roundOffTo2DecPlaces(actualYTD));
                tableData.get(y).add(10, roundOffTo2DecPlaces(varianceYTD));
            }

            else {
                double budgetYTD = Double.parseDouble(tableData.get(y).get(5));
                double actualYTD = Double.parseDouble(tableData.get(y).get(6));
                double varianceYTD = budgetYTD - actualYTD;
                Vector<Double> newYTD = new Vector<Double>();
                newYTD.add(budgetYTD);
                newYTD.add(actualYTD);
                newYTD.add(varianceYTD);
                head.put(tableData.get(y).get(0) + tableData.get(y).get(1), newYTD);

                tableData.get(y).add(8, roundOffTo2DecPlaces(budgetYTD));
                tableData.get(y).add(9, roundOffTo2DecPlaces(actualYTD));
                tableData.get(y).add(10, roundOffTo2DecPlaces(varianceYTD));
            }

            y++;
            if (!rows.hasNext()) {
                break;
            }
            x = 0;
            rowsCompleted++;
        }
        long process = System.currentTimeMillis();
        System.out.println("Processing time: " + (process - start));
        Connection conn = null;
        Statement stmt;

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, "dan", "ParolaMea123");
            stmt = conn.createStatement();
            String checkSQL;
            checkSQL = "SELECT `Unique Key` FROM data;";
            ResultSet rs = stmt.executeQuery(checkSQL);

            //STEP 5: Extract data from result set

            Vector<String> uniqueKeys = new Vector<String>();
            while (rs.next()) {
                if (uniqueKeys.contains(rs.getString("Unique Key"))) {
                    throw new Exception("Invalid entries in database, unique keys should not contain duplicates!");
                } else {
                    uniqueKeys.add(rs.getString("Unique Key"));
                }
            }

            final int BATCH_SIZE = 1000;
            int currentBatch = 0;

            String insertSQL = "INSERT INTO data VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = conn.prepareStatement(insertSQL);
            conn.setAutoCommit(false);

            for (Vector<String> k : tableData) {
                if (uniqueKeys.contains(k.get(15))) {
                    String updateSQL = "UPDATE data " +
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
                            // TODO: WHY IT THROWS SQLEXCEPTION ????
                            "', `Expense Type` = '" + k.get(26) +
                            "' WHERE `Unique Key` = '" + k.get(15) + "';";
                    stmt.executeUpdate(updateSQL);
                    conn.commit();
                } else {
                    preparedStatement.setString(1, k.get(15));
                    preparedStatement.setString(2, k.get(0));
                    preparedStatement.setString(3, k.get(1));
                    preparedStatement.setInt(4, (int) Math.round(Double.parseDouble(k.get(2))));
                    preparedStatement.setInt(5, (int) Math.round(Double.parseDouble(k.get(3))));
                    preparedStatement.setInt(6, (int) Math.round(Double.parseDouble(k.get(4))));
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
                    preparedStatement.setString(28, null);
                    preparedStatement.executeUpdate();
                    currentBatch++;

                    if (currentBatch >= BATCH_SIZE) {
                        preparedStatement.executeBatch();
                        conn.commit();
                        currentBatch = 0;
                    }
                }
            }

            preparedStatement.executeBatch();
            conn.commit();

            //STEP 6: Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources


            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        long finish = System.currentTimeMillis();
        System.out.println("Database fetching: " + (finish - process));
        System.out.println("Total time: " + (finish - start));

    }


    // TODO: Little BUGS (FIX THEM)
    /***
     *
     * @param vector
     * @return newVector - returns a new vector with totals calculated and added to the collection
     * (uncomment the prints for better understanding of the method)
     * @throws ParseException
     */
    private Vector<Vector<String>> addTotals(Vector<Vector<String>> vector) throws ParseException {

        // Total class for PAY, NON PAY, INCOME, RECHARGE and GRAND TOTAL
        class Total {

            private String name;

            private int index;

            private boolean isInserted;

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
                this.isInserted = false;
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

            private Vector<String> getTotal(Total object) {
                Vector<String> vector = new Vector<>();
                vector.add(null);
                vector.add(object.name);
                vector.add(null);
                vector.add(null);
                vector.add(null);
                vector.add(null);
                vector.add(nf.format(Double.parseDouble(String.valueOf(object.budget))));
                vector.add(nf.format((Double.parseDouble(String.valueOf(object.actual)))));
                vector.add(nf.format((Double.parseDouble(String.valueOf(object.variance)))));
                vector.add(nf.format((Double.parseDouble(String.valueOf(object.YTDBudget)))));
                vector.add(nf.format((Double.parseDouble(String.valueOf(object.YTDActual)))));
                vector.add(nf.format((Double.parseDouble(String.valueOf(object.YTDVariance)))));
                vector.add(nf.format((Double.parseDouble(String.valueOf(object.WTEBudget)))));
                vector.add(nf.format((Double.parseDouble(String.valueOf(object.WTEContracted)))));
                vector.add(nf.format((Double.parseDouble(String.valueOf(object.WTEWorked)))));
                vector.add(null);
                vector.add(null);
                vector.add(null);
                vector.add(null);
                vector.add(null);
                vector.add(null);
                vector.add(null);
                vector.add(null);
                vector.add(null);
                vector.add(null);
                vector.add(null);
                vector.add(null);
                return vector;
            }

            private void clear(){
                this.isInserted = false;
                this.index = 0;
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

            private void setInserted(int counter) {
                this.isInserted = true;
                this.index = counter;
            }

            private boolean isInserted() {
                return isInserted;
            }

            private int getIndex() {
                return index;
            }

        }

        // The totals
        Total income = new Total("INCOME");
        Total pay = new Total("PAY");
        Total nonPay = new Total("NON PAY");
        Total recharge = new Total("RECHARGE");
        Total grandTotal = new Total("GRAND TOTAL");

        // Totals mapped with the strings found in in the vectors
        TreeMap<String, Total> totalTreeMap = new TreeMap<>();
        totalTreeMap.put("Income", income);
        totalTreeMap.put("Pay", pay);
        totalTreeMap.put("Non Pay", nonPay);
        totalTreeMap.put("Recharge", recharge);
        totalTreeMap.put("Grand Total", grandTotal);

        // New vector for inserting the totals at the indexes iterated here
        Vector<Vector<String>> newVector = new Vector<>(vector);

        // Index counter
        int counter = 0;
        int n = 0;

        // Value to add to index after inserting totals (increment by 1 after each total inserted in the new vector)
        int noOfTotals = 0;

        // Starting cost code and expense type
        String currentCode = vector.get(0).get(1);
        String currentType = vector.get(0).get(26);

        // Iterating over each entry in the vector
        for (Vector<String> aVector : vector) {
            // Current iteration cost code and expense type
            String iteratedExpenseType = aVector.get(26);
            String iteratedCode = aVector.get(1);

            // Special case (One cost code)
            if (vector.lastElement() == aVector) {
                counter++;
                Total varTotal = totalTreeMap.get(currentType);
                varTotal.budgetAdd(nf.parse(aVector.get(6)).doubleValue());
                varTotal.actualAdd(nf.parse(aVector.get(7)).doubleValue());
                varTotal.varianceAdd(nf.parse(aVector.get(8)).doubleValue());
                varTotal.YTDBudgetAdd(nf.parse(aVector.get(9)).doubleValue());
                varTotal.YTDActualAdd(nf.parse(aVector.get(10)).doubleValue());
                varTotal.YTDVarianceAdd(nf.parse(aVector.get(11)).doubleValue());
                varTotal.WTEBudgetAdd(nf.parse(aVector.get(12)).doubleValue());
                varTotal.WTEContractedAdd(nf.parse(aVector.get(13)).doubleValue());
                varTotal.WTEWorkedAdd(nf.parse(aVector.get(14)).doubleValue());
                Vector<String> insertVector = varTotal.getTotal(varTotal);
                newVector.insertElementAt(insertVector, counter+noOfTotals);
                varTotal.setInserted(counter+noOfTotals);
                noOfTotals++;
                grandTotal.budgetAdd(pay.budget + nonPay.budget + income.budget + recharge.budget);
                grandTotal.actualAdd(pay.actual + nonPay.actual + income.actual + recharge.actual);
                grandTotal.varianceAdd(pay.variance + nonPay.variance + income.variance + recharge.variance);
                grandTotal.YTDBudgetAdd(pay.YTDBudget + nonPay.YTDBudget + income.YTDBudget + recharge.YTDBudget);
                grandTotal.YTDActualAdd(pay.YTDActual + nonPay.YTDActual + income.YTDActual + recharge.YTDActual);
                grandTotal.YTDVarianceAdd(pay.YTDVariance + nonPay.YTDVariance + income.YTDVariance + recharge.YTDVariance);
                grandTotal.WTEBudgetAdd(pay.WTEBudget + nonPay.WTEBudget + income.WTEBudget + recharge.WTEBudget);
                grandTotal.WTEContractedAdd(pay.WTEContracted + nonPay.WTEContracted + income.WTEContracted + recharge.WTEContracted);
                grandTotal.WTEWorkedAdd(pay.WTEWorked + nonPay.WTEWorked + income.WTEWorked + recharge.WTEWorked);
                Vector<String> grandTotalVector = grandTotal.getTotal(grandTotal);
                newVector.insertElementAt(grandTotalVector, counter+noOfTotals);
                if (!(income.isInserted())) {
                    newVector.insertElementAt(income.getTotal(income), n);
                    income.setInserted(n);
                    pay.index++;
                    nonPay.index++;
                    recharge.index++;
                }

                if (!(pay.isInserted())) {
                    newVector.insertElementAt(pay.getTotal(pay), income.getIndex()+1);
                    pay.setInserted(income.getIndex()+1);
                    nonPay.index++;
                    recharge.index++;
                }

                if (!(nonPay.isInserted())) {
                    newVector.insertElementAt(nonPay.getTotal(nonPay), pay.getIndex()+1);
                    nonPay.setInserted(pay.getIndex()+1);
                    recharge.index++;
                }

                if (!(recharge.isInserted())) {
                    newVector.insertElementAt(recharge.getTotal(recharge), nonPay.getIndex()+1);
                    recharge.setInserted(nonPay.getIndex()+1);
                }
                break;
            }

            // Same cost code case
            if (iteratedCode.equals(currentCode)) {

                // Same expense type case
                if (iteratedExpenseType.equals(currentType)) {
                    /*System.out.println(currentType + " at index: " + counter);
                    System.out.println("Adding value to " + currentType + " total.");*/
                    // Adding values to the total matching the currentType
                    Total varTotal = totalTreeMap.get(currentType);
                    varTotal.budgetAdd(nf.parse(aVector.get(6)).doubleValue());
                    varTotal.actualAdd(nf.parse(aVector.get(7)).doubleValue());
                    varTotal.varianceAdd(nf.parse(aVector.get(8)).doubleValue());
                    varTotal.YTDBudgetAdd(nf.parse(aVector.get(9)).doubleValue());
                    varTotal.YTDActualAdd(nf.parse(aVector.get(10)).doubleValue());
                    varTotal.YTDVarianceAdd(nf.parse(aVector.get(11)).doubleValue());
                    varTotal.WTEBudgetAdd(nf.parse(aVector.get(12)).doubleValue());
                    varTotal.WTEContractedAdd(nf.parse(aVector.get(13)).doubleValue());
                    varTotal.WTEWorkedAdd(nf.parse(aVector.get(14)).doubleValue());
                    counter++;
                }

                // Different expense type case
                else {
                    /*System.out.println("Expense type changed, inserting current vector of " + currentType + " at index " + counter);*/
                    Total insertTotal = totalTreeMap.get(currentType);
                    Vector<String> insertVector = insertTotal.getTotal(insertTotal);
                    newVector.insertElementAt(insertVector, counter+noOfTotals);
                    insertTotal.setInserted(counter+noOfTotals);
                    noOfTotals++;
                    currentType = iteratedExpenseType;
                    /*System.out.println("Type change to " + currentType + " at index: " + counter);
                    System.out.println("Adding value to " + currentType + " instead");*/
                    Total varTotal = totalTreeMap.get(currentType);
                    varTotal.budgetAdd(nf.parse(aVector.get(6)).doubleValue());
                    varTotal.actualAdd(nf.parse(aVector.get(7)).doubleValue());
                    varTotal.varianceAdd(nf.parse(aVector.get(8)).doubleValue());
                    varTotal.YTDBudgetAdd(nf.parse(aVector.get(9)).doubleValue());
                    varTotal.YTDActualAdd(nf.parse(aVector.get(10)).doubleValue());
                    varTotal.YTDVarianceAdd(nf.parse(aVector.get(11)).doubleValue());
                    varTotal.WTEBudgetAdd(nf.parse(aVector.get(12)).doubleValue());
                    varTotal.WTEContractedAdd(nf.parse(aVector.get(13)).doubleValue());
                    varTotal.WTEWorkedAdd(nf.parse(aVector.get(14)).doubleValue());
                    counter++;
                }
            }

            // Different cost code case
            else {
                /*System.out.println("Cost code possible changed, inserting grand total vector at index: " + (counter+1) + "and last vector at index: " + counter);*/
                Total insertTotal = totalTreeMap.get(currentType);
                Vector<String> insertVector = insertTotal.getTotal(insertTotal);
                newVector.insertElementAt(insertVector, counter+noOfTotals);
                insertTotal.setInserted(counter+noOfTotals);
                noOfTotals++;
                grandTotal.budgetAdd(pay.budget + nonPay.budget + income.budget + recharge.budget);
                grandTotal.actualAdd(pay.actual + nonPay.actual + income.actual + recharge.actual);
                grandTotal.varianceAdd(pay.variance + nonPay.variance + income.variance + recharge.variance);
                grandTotal.YTDBudgetAdd(pay.YTDBudget + nonPay.YTDBudget + income.YTDBudget + recharge.YTDBudget);
                grandTotal.YTDActualAdd(pay.YTDActual + nonPay.YTDActual + income.YTDActual + recharge.YTDActual);
                grandTotal.YTDVarianceAdd(pay.YTDVariance + nonPay.YTDVariance + income.YTDVariance + recharge.YTDVariance);
                grandTotal.WTEBudgetAdd(pay.WTEBudget + nonPay.WTEBudget + income.WTEBudget + recharge.WTEBudget);
                grandTotal.WTEContractedAdd(pay.WTEContracted + nonPay.WTEContracted + income.WTEContracted + recharge.WTEContracted);
                grandTotal.WTEWorkedAdd(pay.WTEWorked + nonPay.WTEWorked + income.WTEWorked + recharge.WTEWorked);
                Vector<String> grandTotalVector = grandTotal.getTotal(grandTotal);
                newVector.insertElementAt(grandTotalVector, counter+noOfTotals);
                grandTotal.setInserted(counter+noOfTotals);
                noOfTotals++;
                currentType = iteratedExpenseType;
                if (currentType == null) {
                    break;
                }
              /*  System.out.println("End of cost code, type change to " + currentType + " at index: " + counter);
                System.out.println("Resetting all the totals.");*/
                if (!(income.isInserted())) {
                    newVector.insertElementAt(income.getTotal(income), n);
                    income.setInserted(n);
                    pay.index++;
                    nonPay.index++;
                    recharge.index++;
                    noOfTotals++;
                }

                if (!(pay.isInserted())) {
                    newVector.insertElementAt(pay.getTotal(pay), income.getIndex()+1);
                    pay.setInserted(income.getIndex()+1);
                    nonPay.index++;
                    recharge.index++;
                    income.index++;
                    noOfTotals++;
                }

                if (!(nonPay.isInserted())) {
                    newVector.insertElementAt(nonPay.getTotal(nonPay), pay.getIndex()+1);
                    nonPay.setInserted(pay.getIndex()+1);
                    recharge.index++;
                    income.index++;
                    pay.index++;
                    noOfTotals++;
                }

                if (!(recharge.isInserted())) {
                    newVector.insertElementAt(recharge.getTotal(recharge), nonPay.getIndex()+1);
                    recharge.setInserted(nonPay.getIndex()+1);
                    nonPay.index++;
                    income.index++;
                    pay.index++;
                    noOfTotals++;
                }

                n = counter+noOfTotals;
                income.clear();
                pay.clear();
                nonPay.clear();
                recharge.clear();
                grandTotal.clear();
                /*System.out.println("Adding value to " + currentType + " instead");*/
                Total varTotal = totalTreeMap.get(currentType);
                varTotal.budgetAdd(nf.parse(aVector.get(6)).doubleValue());
                varTotal.actualAdd(nf.parse(aVector.get(7)).doubleValue());
                varTotal.varianceAdd(nf.parse(aVector.get(8)).doubleValue());
                varTotal.YTDBudgetAdd(nf.parse(aVector.get(9)).doubleValue());
                varTotal.YTDActualAdd(nf.parse(aVector.get(10)).doubleValue());
                varTotal.YTDVarianceAdd(nf.parse(aVector.get(11)).doubleValue());
                varTotal.WTEBudgetAdd(nf.parse(aVector.get(12)).doubleValue());
                varTotal.WTEContractedAdd(nf.parse(aVector.get(13)).doubleValue());
                varTotal.WTEWorkedAdd(nf.parse(aVector.get(14)).doubleValue());
                currentCode = iteratedCode;
                counter++;
            }
        }
        return newVector;
    }

    JTable createSpecificTable(Object costCode, Object period) throws ParseException {

        // Sort each vector to match cost code and period parameters
        sortedVector.clear();
        divisions.clear();
        divisions.add("ALL");
        names.clear();
        names.add("ALL");
        CDGs.clear();
        CDGs.add("ALL");
        for (Vector<String> databaseEntry : databaseEntries) {
            Object x = databaseEntry.get(1);
            Object y = databaseEntry.get(3);
            if (costCode == "ALL") {
                if (y.equals(period)) {
                    Vector<String> tableVector = new Vector<>(databaseEntry);
                    name = tableVector.get(16);
                    sortedVector.add(tableVector);
                    divisions.add(databaseEntry.get(18));
                    CDGs.add(databaseEntry.get(19));
                    names.add(databaseEntry.get(22));
                }
            }

            else {
                if (x.equals(costCode) && y.equals(period)) {
                    Vector<String> tableVector = new Vector<>(databaseEntry);
                    name = tableVector.get(16);
                    sortedVector.add(tableVector);
                    divisions.add(databaseEntry.get(18));
                    CDGs.add(databaseEntry.get(19));
                    names.add(databaseEntry.get(22));
                }
            }
        }
        try {
            Vector<Vector<String>> finalVector = addTotals(sortedVector);
            model = new DefaultTableModel(finalVector, hd);
        }

        catch (ArrayIndexOutOfBoundsException e) {
            String noEntries = "No entries available!";
            Vector<String> vvv = new Vector<>();
            vvv.add(noEntries);
            Vector<Vector<String>> vvvv = new Vector<>();
            vvvv.add(vvv);
            model = new DefaultTableModel(vvvv, hd);
        }
        return removeColumns(new JTable(model){
            //Implement table cell tool tips.
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                String rowIndex = String.valueOf(rowAtPoint(p));
                String colIndex = String.valueOf(columnAtPoint(p));
                try {
                    tip = UserInterface.dataWithDecimal.get(rowIndex+colIndex).get(0);
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }

                return tip;
            }
        });
    }

    void drillTable(JTable table, Object name, Object division, Object cdg) throws ParseException {
        Vector<Vector<String>> newVector = new Vector<>();

        if (Objects.isNull(name) && Objects.isNull(cdg) && Objects.isNull(division)) {
            Vector<Vector<String>> finalVector = addTotals(sortedVector);
            model = new DefaultTableModel(finalVector, hd);
            table.setModel(model);
            removeColumns(table);
        }

        else if (Objects.isNull(division) && Objects.isNull(cdg)) {
            for (Vector<String> vector : sortedVector) {
                Object nameObject = vector.get(22);
                if (nameObject == null) continue;
                if (nameObject.equals(name)) {
                    newVector.add(vector);
                }
            }

            try {
                newVector = addTotals(newVector);
            }

            catch (ArrayIndexOutOfBoundsException e) {
                String isEmpty = "No entries matched your search criteria!";
                Vector<String> nn = new Vector<>();
                nn.add(isEmpty);
                newVector.add(nn);

            }
            model = new DefaultTableModel(newVector, hd);
            table.setModel(model);
            removeColumns(table);
        }

        else if (Objects.isNull(division) && Objects.isNull(name)) {
            for (Vector<String> vector : sortedVector) {
                Object cdgObject = vector.get(19);
                if (cdgObject == null) continue;
                if (cdgObject.equals(cdg)) {
                    newVector.add(vector);
                }
            }

            try {
                newVector = addTotals(newVector);
            }

            catch (ArrayIndexOutOfBoundsException e) {
                String isEmpty = "No entries matched your search criteria!";
                Vector<String> nn = new Vector<>();
                nn.add(isEmpty);
                newVector.add(nn);

            }
            model = new DefaultTableModel(newVector, hd);
            table.setModel(model);
            removeColumns(table);
        }

        else if (Objects.isNull(name) && Objects.isNull(cdg)) {
            for (Vector<String> vector : sortedVector) {
                Object divisionObject = vector.get(18);

                if (divisionObject == null) continue;
                if (divisionObject.equals(division)) {
                    divisions.add(vector.get(18));
                    names.add(vector.get(22));
                    CDGs.add(vector.get(19));
                    newVector.add(vector);
                }
            }

            try {
                newVector = addTotals(newVector);
            }

            catch (ArrayIndexOutOfBoundsException e) {
                String isEmpty = "No entries matched your search criteria!";
                Vector<String> nn = new Vector<>();
                nn.add(isEmpty);
                newVector.add(nn);

            }
            model = new DefaultTableModel(newVector, hd);
            table.setModel(model);
            removeColumns(table);
        }

        else if (Objects.isNull(division)) {
            for (Vector<String> vector : sortedVector) {
                Object nameObject = vector.get(22);
                Object cdgObject = vector.get(19);

                if (nameObject == null || cdgObject == null) continue;
                if (nameObject.equals(name) && cdgObject.equals(cdg)) {
                    newVector.add(vector);
                }
            }

            try {
                newVector = addTotals(newVector);
            }

            catch (ArrayIndexOutOfBoundsException e) {
                String isEmpty = "No entries matched your search criteria!";
                Vector<String> nn = new Vector<>();
                nn.add(isEmpty);
                newVector.add(nn);

            }
            model = new DefaultTableModel(newVector, hd);
            table.setModel(model);
            removeColumns(table);
        }

        else if (Objects.isNull(name)) {
            for (Vector<String> vector : sortedVector) {
                Object cdgObject = vector.get(19);
                Object divisionObject = vector.get(18);

                if (divisionObject == null || cdgObject == null) continue;
                if (cdgObject.equals(cdg) && divisionObject.equals(division)) {
                    newVector.add(vector);
                }
            }

            try {
                newVector = addTotals(newVector);
            }

            catch (ArrayIndexOutOfBoundsException e) {
                String isEmpty = "No entries matched your search criteria!";
                Vector<String> nn = new Vector<>();
                nn.add(isEmpty);
                newVector.add(nn);

            }

            model = new DefaultTableModel(newVector, hd);
            table.setModel(model);
            removeColumns(table);
        }

        else if (Objects.isNull(cdg)) {
            for (Vector<String> vector : sortedVector) {
                Object nameObject = vector.get(22);
                Object divisionObject = vector.get(18);

                if (divisionObject == null || nameObject == null) continue;
                if (nameObject.equals(name) && divisionObject.equals(division)) {
                    newVector.add(vector);
                }
            }

            try {
                newVector = addTotals(newVector);
            }

            catch (ArrayIndexOutOfBoundsException e) {
                String isEmpty = "No entries matched your search criteria!";
                Vector<String> nn = new Vector<>();
                nn.add(isEmpty);
                newVector.add(nn);

            }
            model = new DefaultTableModel(newVector, hd);
            table.setModel(model);
            removeColumns(table);
        }

        else {
            for (Vector<String> vector : sortedVector) {
                Object nameObject = vector.get(22);
                Object cdgObject = vector.get(19);
                Object divisionObject = vector.get(18);

                if (divisionObject == null || nameObject == null || cdgObject == null) continue;
                if (nameObject.equals(name) && cdgObject.equals(cdg) && divisionObject.equals(division)) {
                    newVector.add(vector);
                }
            }

            try {
                newVector = addTotals(newVector);
            }

            catch (ArrayIndexOutOfBoundsException e) {
                String isEmpty = "No entries matched your search criteria!";
                Vector<String> nn = new Vector<>();
                nn.add(isEmpty);
                newVector.add(nn);

            }
            model = new DefaultTableModel(newVector, hd);
            table.setModel(model);
            removeColumns(table);
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(14).setMinWidth(200);
        table.getColumnModel().getColumn(13).setMinWidth(50);
        table.getColumnModel().getColumn(12).setMinWidth(150);
        table.getColumnModel().getColumn(11).setMinWidth(150);
        table.getColumnModel().getColumn(10).setMinWidth(30);
        table.getColumnModel().getColumn(9).setMinWidth(30);
        table.getColumnModel().getColumn(8).setMinWidth(30);
    }

    private JTable removeColumns(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        int count = columnModel.getColumnCount();
        for (int i = 0; i<count; i++) {
            columnModel.getColumn(i).setIdentifier(table.getColumnName(i));
        }
        table.removeColumn(table.getColumn("Unique Key"));
        table.removeColumn(table.getColumn("Period and Month"));
        table.removeColumn(table.getColumn("Month"));
        table.removeColumn(table.getColumn("Year"));
        table.removeColumn(table.getColumn("Department"));
        table.removeColumn(table.getColumn("Group"));
        table.removeColumn(table.getColumn("Division"));
        table.removeColumn(table.getColumn("CDG"));
        table.removeColumn(table.getColumn("Service"));
        table.removeColumn(table.getColumn("National Specialty"));
        table.removeColumn(table.getColumn("Name"));
        table.removeColumn(table.getColumn("Investigation Limit"));
        table.removeColumn(table.getColumn("WTE Paid"));

        return table;
    }

    boolean limitExceeded(int row, JTable table) throws ParseException {
        Object o = table.getModel().getValueAt(row, 23);
        if (o == null){
            return false;
        }

        else {
            int limit = nf.parse(String.valueOf(table.getModel().getValueAt(row, 23))).intValue();
            double variance = nf.parse(String.valueOf(table.getModel().getValueAt(row, 8))).doubleValue();
            return variance > limit || variance < -limit;
        }
    }

    boolean isTotal(int row, JTable table) {
        Object o = table.getModel().getValueAt(row, 2);
        return o == null;
    }

    boolean hasNote(int row, JTable table) {
        Object o = table.getModel().getValueAt(row, 27);
        return o != null;
    }
}
