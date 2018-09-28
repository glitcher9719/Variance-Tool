import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
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
    final String DB_URL = "jdbc:mysql://10.43.136.208:3306/experimental-db?useSSL=false";
    private Vector<Vector<String>> databaseEntries = new Vector<>();
    private Vector<Vector<String>> previousDatabaseEntries = new Vector<>();
    private Vector<Vector<String>> sortedVector = new Vector<>();
    private Vector<String> hd = new Vector<>();

    LinkedHashSet<Object> ccNames = new LinkedHashSet<>();
    LinkedHashSet<Object> periodNames = new LinkedHashSet<>();
    LinkedHashSet<Object> names = new LinkedHashSet<>();
    LinkedHashSet<Object> divisions = new LinkedHashSet<>();
    LinkedHashSet<Object> CDGs = new LinkedHashSet<>();

    LinkedHashSet<Object> sortedCostCentreNames = new LinkedHashSet<>();
    LinkedHashSet<Object> sortedNames = new LinkedHashSet<>();
    LinkedHashSet<Object> sortedDivisions = new LinkedHashSet<>();
    LinkedHashSet<Object> sortedCDGs = new LinkedHashSet<>();

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
            previousDatabaseEntries.clear();
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, "root", "some pass");
            stmt = conn.createStatement();
            String dataSQL;
            String previousDataSQL;
            dataSQL = "SELECT * FROM data;";
            previousDataSQL = "SELECT * FROM previous;";
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
                Vector<String> element = new Vector<>();
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
            rs.close();
            ResultSet rs2 = stmt.executeQuery(previousDataSQL);
            while (rs2.next()) {
                Vector<String> element = new Vector<>();
                String uniqueKey = rs2.getString("Unique Key");
                String costCentre = rs2.getString("Cost Centre");
                String expenseHead = rs2.getString("Expense Header");
                int periodAndMonth = rs2.getInt("Period and Month");
                int month = rs2.getInt("Month");
                int year = rs2.getInt("Year");
                double budget = rs2.getDouble("Budget");
                double actuals = rs2.getDouble("Actuals");
                double variance = rs2.getDouble("Variance");
                double budgetYTD = rs2.getDouble("Budget YTD");
                double actualsYTD = rs2.getDouble("Actual YTD");
                double varianceYTD = rs2.getDouble("VarianceYTD");
                double WTEBud = rs2.getDouble("WTE Bud");
                double WTECon = rs2.getDouble("WTE Con");
                double WTEWork = rs2.getDouble("WTE Work");
                double WTEPaid = rs2.getDouble("WTE Paid");
                String department = rs2.getString("Department");
                String group = rs2.getString("Group");
                String division = rs2.getString("Division");
                String CDG = rs2.getString("CDG");
                String service = rs2.getString("Service");
                String nationalSpecialty = rs2.getString("National Specialty");
                String name = rs2.getString("Name");
                String investigationLimit = rs2.getString("Investigation Limit");
                String expenseDescription = rs2.getString("Expense Description");
                String expenseGrouping = rs2.getString("Expense Grouping");
                String expenseType = rs2.getString("Expense Type");
                String note = rs2.getString("Note");
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
                previousDatabaseEntries.add(element);
            }

            //STEP 6: Clean-up environment
            rs2.close();
            stmt.close();
            conn.close();
        }

        catch (SQLException se) {
            JOptionPane.showMessageDialog(null, se);
        }

        return new JTable(databaseEntries, hd);
    }

    void importSpreadsheet(String path) throws IOException {
        long start = System.currentTimeMillis();
        int x = 0;
        int y = 0;
        Vector<Vector<String>> tableData = new Vector<>();
        ArrayList<String> tableDataPeriod = new ArrayList<>();
        TreeMap<String, Vector<Double>> head = new TreeMap<>();
        InputStream ExcelFileToRead = new FileInputStream(path);
        XSSFWorkbook wb = new XSSFWorkbook(ExcelFileToRead);
        XSSFSheet sheet = wb.getSheetAt(0);
        int numberOfRows = sheet.getPhysicalNumberOfRows();
        UserInterface.progressBar.setMaximum(numberOfRows *2);
        UserInterface.progressBar.setString("Processing spreadsheet data...");
        XSSFRow row;
        XSSFCell cell;
        Iterator rows = sheet.rowIterator();
        rows.next();
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            Iterator cells = row.cellIterator();
            Vector<String> currentRow = new Vector<>();

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
                varianceYTD += (budgetYTD - actualYTD);

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
                double varianceYTD = (budgetYTD - actualYTD);
                Vector<Double> newYTD = new Vector<>();
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
            UserInterface.progressBar.setValue(rowsCompleted);
        }

        long process = System.currentTimeMillis();
        System.out.println("Processing time: " + (process - start));
        Connection conn = null;
        Statement stmt;

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, "root", "some pass");

            stmt = conn.createStatement();
            String checkSQL;
            checkSQL = "SELECT DISTINCT `Period and Month` FROM data;";
            ResultSet rs = stmt.executeQuery(checkSQL);
            while (rs.next()) {
                tableDataPeriod.add(rs.getString("Period and Month"));
            }
            String insertSQL = "INSERT INTO data VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            String firstDeleteSQL = "DELETE FROM previous WHERE `Period and Month` = " + "?" + ";";
            String secondDeleteSQL = "DELETE FROM data WHERE `Period and Month` = " + "?" + ";";
            String updateSQL = "INSERT INTO previous " +
                    "SELECT * " +
                    "FROM data " +
                    "WHERE `Period and Month` = " + "?" + ";";

            final int BATCH_SIZE = 1000;
            int currentBatch = 0;
            PreparedStatement firstDeletePreparedStatement = conn.prepareStatement(firstDeleteSQL);
            PreparedStatement secondDeletePreparedStatement = conn.prepareStatement(secondDeleteSQL);
            PreparedStatement updatePreparedStatement = conn.prepareStatement(updateSQL);
            PreparedStatement insertPreparedStatement = conn.prepareStatement(insertSQL);
            ArrayList<String> iteratedPeriods = new ArrayList<>();
            conn.setAutoCommit(false);

            for (Vector<String> aTableData : tableData) {
                if (currentBatch >= BATCH_SIZE) {
                    insertPreparedStatement.executeBatch();
                    conn.commit();
                    currentBatch = 0;
                }

                if (tableDataPeriod.contains(aTableData.get(2))) {
                    if (!(iteratedPeriods.contains(aTableData.get(2)))) {
                        iteratedPeriods.add(aTableData.get(2));
                        firstDeletePreparedStatement.setInt(1, Integer.parseInt(aTableData.get(2)));
                        firstDeletePreparedStatement.executeUpdate();
                        updatePreparedStatement.setInt(1, Integer.parseInt(aTableData.get(2)));
                        updatePreparedStatement.executeUpdate();
                        secondDeletePreparedStatement.setInt(1, Integer.parseInt(aTableData.get(2)));
                        secondDeletePreparedStatement.executeUpdate();
                        conn.commit();
                    }
                    insertPreparedStatement.setString(1, aTableData.get(15));
                    insertPreparedStatement.setString(2, aTableData.get(0));
                    insertPreparedStatement.setString(3, aTableData.get(1));
                    insertPreparedStatement.setInt(4, (int) Math.round(Double.parseDouble(aTableData.get(2))));
                    insertPreparedStatement.setInt(5, (int) Math.round(Double.parseDouble(aTableData.get(3))));
                    insertPreparedStatement.setInt(6, (int) Math.round(Double.parseDouble(aTableData.get(4))));
                    insertPreparedStatement.setDouble(7, Double.parseDouble(aTableData.get(5)));
                    insertPreparedStatement.setDouble(8, Double.parseDouble(aTableData.get(6)));
                    insertPreparedStatement.setDouble(9, Double.parseDouble(aTableData.get(7)));
                    insertPreparedStatement.setDouble(10, Double.parseDouble(aTableData.get(8)));
                    insertPreparedStatement.setDouble(11, Double.parseDouble(aTableData.get(9)));
                    insertPreparedStatement.setDouble(12, Double.parseDouble(aTableData.get(10)));
                    insertPreparedStatement.setDouble(13, Double.parseDouble(aTableData.get(11)));
                    insertPreparedStatement.setDouble(14, Double.parseDouble(aTableData.get(12)));
                    insertPreparedStatement.setDouble(15, Double.parseDouble(aTableData.get(13)));
                    insertPreparedStatement.setDouble(16, Double.parseDouble(aTableData.get(14)));
                    insertPreparedStatement.setString(17, aTableData.get(16));
                    insertPreparedStatement.setString(18, aTableData.get(17));
                    insertPreparedStatement.setString(19, aTableData.get(18));
                    insertPreparedStatement.setString(20, aTableData.get(19));
                    insertPreparedStatement.setString(21, aTableData.get(20));
                    insertPreparedStatement.setString(22, aTableData.get(21));
                    insertPreparedStatement.setString(23, aTableData.get(22));
                    insertPreparedStatement.setString(24, aTableData.get(23));
                    insertPreparedStatement.setString(25, aTableData.get(24));
                    insertPreparedStatement.setString(26, aTableData.get(25));
                    insertPreparedStatement.setString(27, aTableData.get(26));
                    insertPreparedStatement.setString(28, null);
                    insertPreparedStatement.addBatch();
                    rowsCompleted++;
                    UserInterface.progressBar.setValue(rowsCompleted);
                    currentBatch++;
                }

                else {
                    insertPreparedStatement.setString(1, aTableData.get(15));
                    insertPreparedStatement.setString(2, aTableData.get(0));
                    insertPreparedStatement.setString(3, aTableData.get(1));
                    insertPreparedStatement.setInt(4, (int) Math.round(Double.parseDouble(aTableData.get(2))));
                    insertPreparedStatement.setInt(5, (int) Math.round(Double.parseDouble(aTableData.get(3))));
                    insertPreparedStatement.setInt(6, (int) Math.round(Double.parseDouble(aTableData.get(4))));
                    insertPreparedStatement.setDouble(7, Double.parseDouble(aTableData.get(5)));
                    insertPreparedStatement.setDouble(8, Double.parseDouble(aTableData.get(6)));
                    insertPreparedStatement.setDouble(9, Double.parseDouble(aTableData.get(7)));
                    insertPreparedStatement.setDouble(10, Double.parseDouble(aTableData.get(8)));
                    insertPreparedStatement.setDouble(11, Double.parseDouble(aTableData.get(9)));
                    insertPreparedStatement.setDouble(12, Double.parseDouble(aTableData.get(10)));
                    insertPreparedStatement.setDouble(13, Double.parseDouble(aTableData.get(11)));
                    insertPreparedStatement.setDouble(14, Double.parseDouble(aTableData.get(12)));
                    insertPreparedStatement.setDouble(15, Double.parseDouble(aTableData.get(13)));
                    insertPreparedStatement.setDouble(16, Double.parseDouble(aTableData.get(14)));
                    insertPreparedStatement.setString(17, aTableData.get(16));
                    insertPreparedStatement.setString(18, aTableData.get(17));
                    insertPreparedStatement.setString(19, aTableData.get(18));
                    insertPreparedStatement.setString(20, aTableData.get(19));
                    insertPreparedStatement.setString(21, aTableData.get(20));
                    insertPreparedStatement.setString(22, aTableData.get(21));
                    insertPreparedStatement.setString(23, aTableData.get(22));
                    insertPreparedStatement.setString(24, aTableData.get(23));
                    insertPreparedStatement.setString(25, aTableData.get(24));
                    insertPreparedStatement.setString(26, aTableData.get(25));
                    insertPreparedStatement.setString(27, aTableData.get(26));
                    insertPreparedStatement.setString(28, null);
                    insertPreparedStatement.addBatch();
                    rowsCompleted++;
                    UserInterface.progressBar.setValue(rowsCompleted);
                    currentBatch++;
                }
            }

            long connectionTime = System.currentTimeMillis();
            System.out.println("Preparing time: " + (connectionTime-process));

            insertPreparedStatement.executeBatch();
            conn.commit();

            //STEP 6: Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            //Handle errors for Class.forName
            JOptionPane.showMessageDialog(null, e);
        } finally {
            //finally block used to close resources


            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                JOptionPane.showMessageDialog(null, se);
            }
        }
        long finish = System.currentTimeMillis();
        System.out.println("Database fetching: " + (finish - process));
        System.out.println("Total time: " + (finish - start));

    }

    // Total class for PAY, NON PAY, INCOME, RECHARGE and GRAND TOTAL
    private class Total {

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

        private Vector<String> getSummaryTotal(Total object) {
            Vector<String> vector = new Vector<>();
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

    @SuppressWarnings("Duplicates")
    JTable createSummaryTable(Object period, Object CDG) throws ParseException {
        Vector<String> headers = new Vector<>();
        headers.add("CDG");
        headers.add("Cost Code");
        headers.add("Description");
        headers.add("Budget");
        headers.add("Actuals");
        headers.add("Variance");
        headers.add("Budget YTD");
        headers.add("Actuals YTD");
        headers.add("Variance YTD");
        headers.add("WTE Bud");
        headers.add("WTE Con");
        headers.add("WTE Work");
        Vector<Vector<String>> grandTotalVectors = new Vector<>();
        String currentCode = databaseEntries.get(0).get(1);
        String currentPeriod = databaseEntries.get(0).get(3);
        Total grandTotal = new Total("GRAND TOTAL");
        for (Vector<String> aVector : databaseEntries) {
            if (period == null && CDG == null) {
                if (aVector.get(1).equals(currentCode) && aVector.get(3).equals(currentPeriod)) {
                    grandTotal.budgetAdd(nf.parse(aVector.get(6)).doubleValue());
                    grandTotal.actualAdd(nf.parse(aVector.get(7)).doubleValue());
                    grandTotal.varianceAdd(nf.parse(aVector.get(8)).doubleValue());
                    grandTotal.YTDBudgetAdd(nf.parse(aVector.get(9)).doubleValue());
                    grandTotal.YTDActualAdd(nf.parse(aVector.get(10)).doubleValue());
                    grandTotal.YTDVarianceAdd(nf.parse(aVector.get(11)).doubleValue());
                    grandTotal.WTEBudgetAdd(nf.parse(aVector.get(12)).doubleValue());
                    grandTotal.WTEContractedAdd(nf.parse(aVector.get(13)).doubleValue());
                    grandTotal.WTEWorkedAdd(nf.parse(aVector.get(14)).doubleValue());
                }

                else {
                    Vector<String> costCodeVector = grandTotal.getSummaryTotal(grandTotal);
                    costCodeVector.set(1, currentCode);
                    costCodeVector.set(0, aVector.get(19));
                    costCodeVector.set(2, aVector.get(16));
                    grandTotalVectors.add(costCodeVector);
                    grandTotal = new Total("GRAND TOTAL");
                    currentCode = aVector.get(1);
                    currentPeriod = aVector.get(3);
                    grandTotal.budgetAdd(nf.parse(aVector.get(6)).doubleValue());
                    grandTotal.actualAdd(nf.parse(aVector.get(7)).doubleValue());
                    grandTotal.varianceAdd(nf.parse(aVector.get(8)).doubleValue());
                    grandTotal.YTDBudgetAdd(nf.parse(aVector.get(9)).doubleValue());
                    grandTotal.YTDActualAdd(nf.parse(aVector.get(10)).doubleValue());
                    grandTotal.YTDVarianceAdd(nf.parse(aVector.get(11)).doubleValue());
                    grandTotal.WTEBudgetAdd(nf.parse(aVector.get(12)).doubleValue());
                    grandTotal.WTEContractedAdd(nf.parse(aVector.get(13)).doubleValue());
                    grandTotal.WTEWorkedAdd(nf.parse(aVector.get(14)).doubleValue());
                }
            }

            else if (period != null && CDG == null) {
                if (aVector.get(3).equals(period.toString())) {
                    if (aVector.get(1).equals(currentCode)) {
                        grandTotal.budgetAdd(nf.parse(aVector.get(6)).doubleValue());
                        grandTotal.actualAdd(nf.parse(aVector.get(7)).doubleValue());
                        grandTotal.varianceAdd(nf.parse(aVector.get(8)).doubleValue());
                        grandTotal.YTDBudgetAdd(nf.parse(aVector.get(9)).doubleValue());
                        grandTotal.YTDActualAdd(nf.parse(aVector.get(10)).doubleValue());
                        grandTotal.YTDVarianceAdd(nf.parse(aVector.get(11)).doubleValue());
                        grandTotal.WTEBudgetAdd(nf.parse(aVector.get(12)).doubleValue());
                        grandTotal.WTEContractedAdd(nf.parse(aVector.get(13)).doubleValue());
                        grandTotal.WTEWorkedAdd(nf.parse(aVector.get(14)).doubleValue());
                    }

                    else {
                        Vector<String> costCodeVector = grandTotal.getSummaryTotal(grandTotal);
                        costCodeVector.set(1, currentCode);
                        costCodeVector.set(0, aVector.get(19));
                        costCodeVector.set(2, aVector.get(16));
                        grandTotalVectors.add(costCodeVector);
                        grandTotal = new Total("GRAND TOTAL");
                        currentCode = aVector.get(1);
                        grandTotal.budgetAdd(nf.parse(aVector.get(6)).doubleValue());
                        grandTotal.actualAdd(nf.parse(aVector.get(7)).doubleValue());
                        grandTotal.varianceAdd(nf.parse(aVector.get(8)).doubleValue());
                        grandTotal.YTDBudgetAdd(nf.parse(aVector.get(9)).doubleValue());
                        grandTotal.YTDActualAdd(nf.parse(aVector.get(10)).doubleValue());
                        grandTotal.YTDVarianceAdd(nf.parse(aVector.get(11)).doubleValue());
                        grandTotal.WTEBudgetAdd(nf.parse(aVector.get(12)).doubleValue());
                        grandTotal.WTEContractedAdd(nf.parse(aVector.get(13)).doubleValue());
                        grandTotal.WTEWorkedAdd(nf.parse(aVector.get(14)).doubleValue());
                    }
                }
            }

            else if (period == null) {
                if (aVector.get(19).equals(CDG.toString())) {
                    if (aVector.get(1).equals(currentCode)) {
                        grandTotal.budgetAdd(nf.parse(aVector.get(6)).doubleValue());
                        grandTotal.actualAdd(nf.parse(aVector.get(7)).doubleValue());
                        grandTotal.varianceAdd(nf.parse(aVector.get(8)).doubleValue());
                        grandTotal.YTDBudgetAdd(nf.parse(aVector.get(9)).doubleValue());
                        grandTotal.YTDActualAdd(nf.parse(aVector.get(10)).doubleValue());
                        grandTotal.YTDVarianceAdd(nf.parse(aVector.get(11)).doubleValue());
                        grandTotal.WTEBudgetAdd(nf.parse(aVector.get(12)).doubleValue());
                        grandTotal.WTEContractedAdd(nf.parse(aVector.get(13)).doubleValue());
                        grandTotal.WTEWorkedAdd(nf.parse(aVector.get(14)).doubleValue());
                    }

                    else {
                        Vector<String> costCodeVector = grandTotal.getSummaryTotal(grandTotal);
                        costCodeVector.set(1, currentCode);
                        costCodeVector.set(0, aVector.get(19));
                        costCodeVector.set(2, aVector.get(16));
                        grandTotalVectors.add(costCodeVector);
                        grandTotal = new Total("GRAND TOTAL");
                        currentCode = aVector.get(1);
                        grandTotal.budgetAdd(nf.parse(aVector.get(6)).doubleValue());
                        grandTotal.actualAdd(nf.parse(aVector.get(7)).doubleValue());
                        grandTotal.varianceAdd(nf.parse(aVector.get(8)).doubleValue());
                        grandTotal.YTDBudgetAdd(nf.parse(aVector.get(9)).doubleValue());
                        grandTotal.YTDActualAdd(nf.parse(aVector.get(10)).doubleValue());
                        grandTotal.YTDVarianceAdd(nf.parse(aVector.get(11)).doubleValue());
                        grandTotal.WTEBudgetAdd(nf.parse(aVector.get(12)).doubleValue());
                        grandTotal.WTEContractedAdd(nf.parse(aVector.get(13)).doubleValue());
                        grandTotal.WTEWorkedAdd(nf.parse(aVector.get(14)).doubleValue());
                    }
                }
            }

            else {
                if (aVector.get(3).equals(period.toString()) && aVector.get(19).equals(CDG.toString())) {
                    if (aVector.get(1).equals(currentCode)) {
                        grandTotal.budgetAdd(nf.parse(aVector.get(6)).doubleValue());
                        grandTotal.actualAdd(nf.parse(aVector.get(7)).doubleValue());
                        grandTotal.varianceAdd(nf.parse(aVector.get(8)).doubleValue());
                        grandTotal.YTDBudgetAdd(nf.parse(aVector.get(9)).doubleValue());
                        grandTotal.YTDActualAdd(nf.parse(aVector.get(10)).doubleValue());
                        grandTotal.YTDVarianceAdd(nf.parse(aVector.get(11)).doubleValue());
                        grandTotal.WTEBudgetAdd(nf.parse(aVector.get(12)).doubleValue());
                        grandTotal.WTEContractedAdd(nf.parse(aVector.get(13)).doubleValue());
                        grandTotal.WTEWorkedAdd(nf.parse(aVector.get(14)).doubleValue());
                    }

                    else {
                        Vector<String> costCodeVector = grandTotal.getSummaryTotal(grandTotal);
                        costCodeVector.set(1, currentCode);
                        costCodeVector.set(0, aVector.get(19));
                        costCodeVector.set(2, aVector.get(16));
                        grandTotalVectors.add(costCodeVector);
                        grandTotal = new Total("GRAND TOTAL");
                        currentCode = aVector.get(1);
                        grandTotal.budgetAdd(nf.parse(aVector.get(6)).doubleValue());
                        grandTotal.actualAdd(nf.parse(aVector.get(7)).doubleValue());
                        grandTotal.varianceAdd(nf.parse(aVector.get(8)).doubleValue());
                        grandTotal.YTDBudgetAdd(nf.parse(aVector.get(9)).doubleValue());
                        grandTotal.YTDActualAdd(nf.parse(aVector.get(10)).doubleValue());
                        grandTotal.YTDVarianceAdd(nf.parse(aVector.get(11)).doubleValue());
                        grandTotal.WTEBudgetAdd(nf.parse(aVector.get(12)).doubleValue());
                        grandTotal.WTEContractedAdd(nf.parse(aVector.get(13)).doubleValue());
                        grandTotal.WTEWorkedAdd(nf.parse(aVector.get(14)).doubleValue());
                    }
                }
            }
        }

        return new JTable(grandTotalVectors, headers);
    }

    /***
     *
     * @param vector - the initial vector with the data collected from excel spreadsheets
     * @return newVector - returns a new vector with totals calculated and added to the collection
     * (uncomment the prints for better understanding of the method)
     * @throws ParseException - specific format
     */
    @SuppressWarnings("Duplicates")
    private Vector<Vector<String>> addTotals(Vector<Vector<String>> vector) throws ParseException {

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

    // @param currentPrevious -> 0 for current database entries, 1 for previous database entries
    JTable createSpecificTable(Object costCode, Object period, int currentPrevious) throws ParseException {
        Vector<Vector<String>> databaseEntries;

        if (currentPrevious == 0) {
            databaseEntries = previousDatabaseEntries;
        }

        else {
            databaseEntries = this.databaseEntries;
        }

        // Sort each vector to match cost code and period parameters
        sortedVector.clear();
        divisions.clear();
        divisions.add("Division");
        names.clear();
        names.add("Name");
        CDGs.clear();
        CDGs.add("CDG");
        for (Vector<String> databaseEntry : databaseEntries) {
            Object x = databaseEntry.get(1);
            Object y = databaseEntry.get(3);
            if (costCode == "ALL") {
                if (y.equals(period)) {
                    Vector<String> tableVector = new Vector<>(databaseEntry);
                    name = "Unavailable, select cost code";
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
        boolean returnNewVector = true;
        if (Objects.isNull(name) && Objects.isNull(cdg) && Objects.isNull(division)) {
            Vector<Vector<String>> finalVector = addTotals(sortedVector);
            model = new DefaultTableModel(finalVector, hd);
            table.setModel(model);
            removeColumns(table);
            returnNewVector = false;
        }

        else if (Objects.isNull(division) && Objects.isNull(cdg)) {
            sortedCostCentreNames.clear();
            sortedCostCentreNames.add("ALL");
            sortedDivisions.clear();
            sortedDivisions.add("Division");
            sortedCDGs.clear();
            sortedCDGs.add("CDG");
            for (Vector<String> vector : sortedVector) {
                Object nameObject = vector.get(22);
                if (nameObject == null) continue;
                if (nameObject.equals(name)) {
                    newVector.add(vector);
                    sortedCostCentreNames.add(vector.get(1));
                    sortedDivisions.add(vector.get(18));
                    sortedCDGs.add(vector.get(19));
                }
            }
        }

        else if (Objects.isNull(division) && Objects.isNull(name)) {
            sortedCostCentreNames.clear();
            sortedCostCentreNames.add("ALL");
            sortedDivisions.clear();
            sortedDivisions.add("Division");
            sortedNames.clear();
            sortedNames.add("Name");
            for (Vector<String> vector : sortedVector) {
                Object cdgObject = vector.get(19);
                if (cdgObject == null) continue;
                if (cdgObject.equals(cdg)) {
                    newVector.add(vector);
                    sortedCostCentreNames.add(vector.get(1));
                    sortedDivisions.add(vector.get(18));
                    sortedNames.add(vector.get(22));
                }
            }
        }

        else if (Objects.isNull(name) && Objects.isNull(cdg)) {
            sortedCostCentreNames.clear();
            sortedCostCentreNames.add("ALL");
            sortedCDGs.clear();
            sortedCDGs.add("CDG");
            sortedNames.clear();
            sortedNames.add("Name");
            for (Vector<String> vector : sortedVector) {
                Object divisionObject = vector.get(18);

                if (divisionObject == null) continue;
                if (divisionObject.equals(division)) {
                    newVector.add(vector);
                    sortedCostCentreNames.add(vector.get(1));
                    sortedCDGs.add(vector.get(19));
                    sortedNames.add(vector.get(22));
                }
            }
        }

        else if (Objects.isNull(division)) {
            sortedCostCentreNames.clear();
            sortedCostCentreNames.add("ALL");
            sortedDivisions.clear();
            sortedDivisions.add("Division");
            for (Vector<String> vector : sortedVector) {
                Object nameObject = vector.get(22);
                Object cdgObject = vector.get(19);

                if (nameObject == null || cdgObject == null) continue;
                if (nameObject.equals(name) && cdgObject.equals(cdg)) {
                    newVector.add(vector);
                    sortedCostCentreNames.add(vector.get(1));
                    sortedDivisions.add(vector.get(18));
                }
            }
        }

        else if (Objects.isNull(name)) {
            sortedCostCentreNames.clear();
            sortedCostCentreNames.add("ALL");
            sortedNames.clear();
            sortedNames.add("Name");
            for (Vector<String> vector : sortedVector) {
                Object cdgObject = vector.get(19);
                Object divisionObject = vector.get(18);

                if (divisionObject == null || cdgObject == null) continue;
                if (cdgObject.equals(cdg) && divisionObject.equals(division)) {
                    newVector.add(vector);
                    sortedCostCentreNames.add(vector.get(1));
                    sortedNames.add(vector.get(22));
                }
            }
        }

        else if (Objects.isNull(cdg)) {
            sortedCostCentreNames.clear();
            sortedCostCentreNames.add("ALL");
            sortedCDGs.clear();
            sortedCDGs.add("CDG");
            for (Vector<String> vector : sortedVector) {
                Object nameObject = vector.get(22);
                Object divisionObject = vector.get(18);

                if (divisionObject == null || nameObject == null) continue;
                if (nameObject.equals(name) && divisionObject.equals(division)) {
                    newVector.add(vector);
                    sortedCostCentreNames.add(vector.get(1));
                    sortedCDGs.add(vector.get(19));
                }
            }

        }

        else {
            sortedCostCentreNames.clear();
            sortedCostCentreNames.add("ALL");
            for (Vector<String> vector : sortedVector) {
                Object nameObject = vector.get(22);
                Object cdgObject = vector.get(19);
                Object divisionObject = vector.get(18);

                if (divisionObject == null || nameObject == null || cdgObject == null) continue;
                if (nameObject.equals(name) && cdgObject.equals(cdg) && divisionObject.equals(division)) {
                    newVector.add(vector);
                    sortedCostCentreNames.add(vector.get(1));
                }
            }
        }

        if (returnNewVector) {
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
