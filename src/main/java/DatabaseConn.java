import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.oxbow.swingbits.table.filter.TableRowFilterSupport;

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
    final String DB_URL = "jdbc:mysql://localhost:3306/experimental-db?useSSL=false";
    private Vector<Vector<String>> databaseEntries = new Vector<>();
    Vector<Vector<String>> sortedVector = new Vector<>();
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
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }

        return TableRowFilterSupport
                .forTable(new JTable(databaseEntries, hd))
                .searchable(true)
                .actions(false)
                .apply();
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
            } else {
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

    private void addTotals(Vector<Vector<String>> vector) throws ParseException {

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

            private Vector<String> getTotal(Total object) {
                Vector<String> vector = new Vector<String>();
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
        }

        Total pay = new Total("PAY");
        Total nonPay = new Total("NON PAY");
        Total income = new Total("INCOME");
        Total grandTotal = new Total("GRAND TOTAL");

        int payCounter = 0;
        int nonPayCounter = 0;
        int incomeCounter = 0;

        for (Vector<String> aSortedVector : vector) {
            Total varTotal;

            switch (aSortedVector.get(26)) {
                case "Pay":
                    varTotal = pay;
                    payCounter++;
                    break;
                case "Non Pay":

                    nonPayCounter++;
                    varTotal = nonPay;

                    break;
                default:
                    incomeCounter++;
                    varTotal = income;
                    break;
            }

            varTotal.budgetAdd(nf.parse(aSortedVector.get(6)).doubleValue());
            varTotal.actualAdd(nf.parse(aSortedVector.get(7)).doubleValue());
            varTotal.varianceAdd(nf.parse(aSortedVector.get(8)).doubleValue());
            varTotal.YTDBudgetAdd(nf.parse(aSortedVector.get(9)).doubleValue());
            varTotal.YTDActualAdd(nf.parse(aSortedVector.get(10)).doubleValue());
            varTotal.YTDVarianceAdd(nf.parse(aSortedVector.get(11)).doubleValue());
            varTotal.WTEBudgetAdd(nf.parse(aSortedVector.get(12)).doubleValue());
            varTotal.WTEContractedAdd(nf.parse(aSortedVector.get(13)).doubleValue());
            varTotal.WTEWorkedAdd(nf.parse(aSortedVector.get(14)).doubleValue());
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
            vector.add(incomeCounter, incomeVector);
            payCounter += incomeCounter;
            payCounter++;
        } else {
            vector.add(incomeVector);
        }

        if (!(payCounter <= incomeCounter + 1)) {
            vector.add(payCounter, payVector);
            nonPayCounter += payCounter;
            nonPayCounter++;
        } else {
            vector.add(payVector);
        }

        if (nonPayCounter != 0) {
            vector.add(nonPayCounter, nonPayVector);
        } else {
            vector.add(nonPayVector);

        }

        vector.add(grandTotalVector);
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
            if (x.equals(costCode) && y.equals(period)) {
                Vector<String> tableVector = new Vector<>(databaseEntry);
                name = tableVector.get(16);
                sortedVector.add(tableVector);
                divisions.add(databaseEntry.get(18));
                CDGs.add(databaseEntry.get(19));
                names.add(databaseEntry.get(22));
            }
        }
        addTotals(sortedVector);
        model = new DefaultTableModel(sortedVector, hd);
        return removeColumns(new JTable(model){

            //Implement table cell tool tips.
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    tip = databaseEntries.get(rowIndex).get(colIndex);
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
            model = new DefaultTableModel(sortedVector, hd);
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

            addTotals(newVector);
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

            addTotals(newVector);
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

            addTotals(newVector);
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

            addTotals(newVector);
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

            addTotals(newVector);
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

            addTotals(newVector);
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

            addTotals(newVector);
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

    boolean limitExceeded(int row) throws ParseException {
        Object o = sortedVector.get(row).get(23);
        if (o == null){
            return false;
        }

        else {
            int limit = nf.parse(sortedVector.get(row).get(23)).intValue();
            double variance = nf.parse(sortedVector.get(row).get(8)).doubleValue();
            return variance > limit || variance < -limit;
        }
    }

    boolean isTotal(int row) {
        Object o = sortedVector.get(row).get(2);
        return o == null;
    }

    boolean hasNote(int row) {
        Object o = sortedVector.get(row).get(27);
        return o != null;
    }
}
