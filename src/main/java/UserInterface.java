import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class UserInterface extends JFrame {

    // default values for department view
    private JTable departmentTable;
    private JTable summaryCostCodeTable;
    private JScrollPane scrollPane2;
    private JScrollPane scrollPane3;
    private CardLayout cardLayout = new CardLayout();
    private JLabel ccLabel;
    private JLabel periodLabel;
    private JLabel descriptionLabel;
    private JTable table;
    private JPanel departmentCard;
    private JPanel summaryCard;

    private Object currentPeriod = null;
    private Object currentCDG = null;

    private JComboBox<Object> costCodeList;
    private Object[] ccNames;
    private Object[] periodNames;

    private JComboBox<Object> nameList;
    private JComboBox<Object> cdgList;
    private JComboBox<Object> divisionList;

    private int ccCounter = 0;
    private int pCounter = 0;
    private Object currentCostCode;
    private Object period;
    private JPanel contentPanel;

    private Object currentSelectedName;
    private Object currentSelectedDivision;
    private Object currentSelectedCDG;

    private DatabaseConn databaseConn;
    static ListMultimap<String, String> dataWithDecimal = ArrayListMultimap.create();
    static JProgressBar progressBar;

    private UserInterface() throws ClassNotFoundException, ParseException {
        databaseConn = new DatabaseConn();
        table = databaseConn.generateDataFromDB();
        summaryCostCodeTable = databaseConn.createSummaryTable(currentPeriod, currentCDG);
        summaryCostCodeTable.setDefaultRenderer(Object.class, new SummaryTableCellRenderer());
        ccNames = databaseConn.ccNames.toArray();
        periodNames = databaseConn.periodNames.toArray();
        try {
            currentCostCode = ccNames[ccCounter];
            period = periodNames[pCounter];
        }

        catch (ArrayIndexOutOfBoundsException | NullPointerException e){
            ccNames = new Object[]{"No cost codes available"};
            periodNames = new Object[]{"No periods available"};
        }

        contentPanel = new JPanel(cardLayout);
        JPanel overviewCard = new JPanel(new BorderLayout());
        departmentCard = new JPanel(new BorderLayout());
        summaryCard = new JPanel(new BorderLayout());
        JPanel north = new JPanel(new BorderLayout());

         /*
            -------- Buttons for switching between cards ---------
         */
        final JRadioButton overview = new JRadioButton("Overview", true);
        final JRadioButton departmentView = new JRadioButton("Department View");
        final JRadioButton summaryView = new JRadioButton("Summary View");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(overview);
        buttonGroup.add(departmentView);
        buttonGroup.add(summaryView);
        final JPanel radioButtons = new JPanel();
        radioButtons.add(overview);
        radioButtons.add(departmentView);
        radioButtons.add(summaryView);

        /*
            -------- Overview ---------
         */

        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        if (table != null) {
            add(table.getTableHeader());
        }

        else {
            System.out.println("Can't get data!");
        }

        scrollPane.setPreferredSize(new Dimension(1900, 950));
        overviewCard.add(scrollPane, BorderLayout.CENTER);

        /*
           -------- Department view ---------
        */

        departmentTable = databaseConn.createSpecificTable(currentCostCode, period, 1);
        dataWithDecimal.clear();
        departmentTable.setDefaultRenderer(Object.class, new BoardTableCellRenderer());
        departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        departmentTable.getColumnModel().getColumn(14).setMinWidth(200);
        departmentTable.getColumnModel().getColumn(13).setMinWidth(50);
        departmentTable.getColumnModel().getColumn(12).setMinWidth(150);
        departmentTable.getColumnModel().getColumn(11).setMinWidth(150);
        departmentTable.getColumnModel().getColumn(10).setMinWidth(30);
        departmentTable.getColumnModel().getColumn(9).setMinWidth(30);
        departmentTable.getColumnModel().getColumn(8).setMinWidth(30);
        scrollPane2 = new JScrollPane(departmentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        costCodeList = new JComboBox<>(ccNames);
        costCodeList.setSelectedIndex(0);

        nameList = new JComboBox<>(databaseConn.names.toArray());
        nameList.setSelectedIndex(0);

        divisionList = new JComboBox<>(databaseConn.divisions.toArray());
        divisionList.setSelectedIndex(0);

        cdgList = new JComboBox<>(databaseConn.CDGs.toArray());
        cdgList.setSelectedIndex(0);

        currentSelectedName = null;
        currentSelectedDivision = null;
        currentSelectedCDG = null;

        JLabel costCode  = new JLabel("Cost code: ");
        JLabel filters = new JLabel("Filter by: ");
        JPanel listView= new JPanel();
        listView.add(costCode);
        listView.add(costCodeList);
        listView.add(filters);
        listView.add(nameList);
        listView.add(divisionList);
        listView.add(cdgList);

        add(departmentTable.getTableHeader());
        scrollPane2.setPreferredSize(new Dimension(1900, 950));
        departmentCard.add(scrollPane2, BorderLayout.CENTER);
        JPanel label = new JPanel();
        label.setLayout(new BoxLayout(label, BoxLayout.Y_AXIS));
        ccLabel = new JLabel("Cost code: " + currentCostCode.toString());
        descriptionLabel = new JLabel("Description: " + databaseConn.name);
        periodLabel = new JLabel("Month: " + getPeriod(period));
        label.add(ccLabel);
        label.add(descriptionLabel);
        label.add(periodLabel);
        JPanel labelPanel = new JPanel();
        labelPanel.add(label);
        JButton importSpreadsheet = new JButton("Import Spreadsheet");

        radioButtons.add(importSpreadsheet, BorderLayout.SOUTH);
        add(radioButtons, BorderLayout.NORTH);
        north.add(labelPanel, BorderLayout.CENTER);
        departmentCard.add(scrollPane2, BorderLayout.CENTER);

        JLabel month = new JLabel("Month");
        JButton previousMonth = new JButton("Previous");
        JButton nextMonth = new JButton("Next");

        final boolean[] current = {true};
        JButton checkPrevious = new JButton("Current data");

        final JPanel eastPanel = new JPanel();
        eastPanel.add(checkPrevious);
        eastPanel.add(month);
        eastPanel.add(previousMonth);
        eastPanel.add(nextMonth);

        JLabel department = new JLabel("Department");
        JButton previousDepartment = new JButton("Previous");
        JButton nextDepartment = new JButton("Next");

        JButton clear = new JButton("Clear");

        final JPanel westPanel = new JPanel();
        westPanel.add(department);
        westPanel.add(previousDepartment);
        westPanel.add(nextDepartment);
        north.add(eastPanel, BorderLayout.EAST);
        JPanel westP = new JPanel();
        westP.add(westPanel);
        westP.add(listView);
        westP.add(clear);
        north.add(westP, BorderLayout.WEST);
        departmentCard.add(north, BorderLayout.NORTH);

        /*
        Summary layout
         */

        scrollPane3 = new JScrollPane(summaryCostCodeTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        summaryCard.add(scrollPane3);
        JComboBox<Object> periods = new JComboBox<>();
        periods.addItem("Period");
        for (Object x : periodNames) {
            periods.addItem(x);
        }
        JComboBox<Object> summaryCDG = new JComboBox<>(databaseConn.CDGs.toArray());
        JPanel buttonTable = new JPanel();
        buttonTable.add(periods);
        buttonTable.add(summaryCDG);
        summaryCard.add(buttonTable, BorderLayout.NORTH);

        /*
        Card Layout
         */

        contentPanel.add(overviewCard, "1");
        contentPanel.add(departmentCard, "2");
        contentPanel.add(summaryCard, "3");

        contentPanel.setLayout(cardLayout);
        add(contentPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
        pack();
        getDefaultCloseOperation();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("VAT: Variance Analysis Tool");

        // LISTENERS
        importSpreadsheet.addActionListener(e -> {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();
                JFrame loadingFrame = new JFrame("Loading");
                loadingFrame.setVisible(true);
                loadingFrame.setSize(400, 100);
                progressBar = new JProgressBar();
                loadingFrame.add(progressBar);
                loadingFrame.setLocationRelativeTo(null);
                SwingWorker x = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        databaseConn.importSpreadsheet(filePath);
                        return null;
                    }

                    @Override
                    public void done() {
                        dispose();
                        loadingFrame.dispose();
                        try {
                            new UserInterface();
                        } catch (ClassNotFoundException | ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                };
                x.execute();
            }
        });

        costCodeList.addActionListener(e -> {
            ccCounter = costCodeList.getSelectedIndex();
            currentCostCode = costCodeList.getSelectedItem();
            departmentCard.remove(scrollPane2);
            try {
                departmentTable = databaseConn.createSpecificTable(currentCostCode, period, 1);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            dataWithDecimal.clear();
            departmentTable.setDefaultRenderer(Object.class, new BoardTableCellRenderer());
            if (Objects.isNull(currentSelectedCDG)) cdgList.setModel(new DefaultComboBoxModel<>(databaseConn.sortedCDGs.toArray()));
            if (Objects.isNull(currentSelectedName)) nameList.setModel(new DefaultComboBoxModel<>(databaseConn.sortedNames.toArray()));
            if (Objects.isNull(currentSelectedDivision)) divisionList.setModel(new DefaultComboBoxModel<>(databaseConn.sortedDivisions.toArray()));
            if (Objects.isNull(currentSelectedDivision) && Objects.isNull(currentSelectedCDG) && Objects.isNull(currentSelectedName)) {
                nameList.setModel(new DefaultComboBoxModel<>(databaseConn.names.toArray()));
                divisionList.setModel(new DefaultComboBoxModel<>(databaseConn.divisions.toArray()));
                cdgList.setModel(new DefaultComboBoxModel<>(databaseConn.CDGs.toArray()));
            }
            departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            departmentTable.getColumnModel().getColumn(14).setMinWidth(200);
            departmentTable.getColumnModel().getColumn(13).setMinWidth(50);
            departmentTable.getColumnModel().getColumn(12).setMinWidth(150);
            departmentTable.getColumnModel().getColumn(11).setMinWidth(150);
            departmentTable.getColumnModel().getColumn(10).setMinWidth(30);
            departmentTable.getColumnModel().getColumn(9).setMinWidth(30);
            departmentTable.getColumnModel().getColumn(8).setMinWidth(30);
            scrollPane2 = new JScrollPane(departmentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane2.setPreferredSize(new Dimension(1900, 950));
            departmentCard.add(scrollPane2, BorderLayout.CENTER);
            ccLabel.setText("Cost code: " + currentCostCode.toString());
            descriptionLabel.setText("Description: " + databaseConn.name);
            departmentTable.getModel().addTableModelListener(e12 -> {
                if (e12.getColumn() == 27) {
                    int row = e12.getFirstRow();
                    int column = e12.getColumn();
                    TableModel model = (TableModel) e12.getSource();
                    Object value = model.getValueAt(row, column);
                    String uniqueKey = period.toString() + departmentTable.getValueAt(row, 0).toString() + departmentTable.getValueAt(row, 1).toString();
                    final Connection[] conn = new Connection[1];
                    final Statement[] stmt = new Statement[1];
                    SwingWorker x = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() {
                            try {
                                Class.forName(databaseConn.JDBC_DRIVER);
                                conn[0] = DriverManager.getConnection(databaseConn.DB_URL, "dan", "ParolaMea123");
                                stmt[0] = conn[0].createStatement();
                                String noteSQL;
                                noteSQL =   "UPDATE data " +
                                        "SET `Note`= +'" + value +
                                        "'WHERE `Unique Key`='" +  uniqueKey + "';";
                                stmt[0].executeUpdate(noteSQL);
                                stmt[0].close();
                                conn[0].close();
                                table = databaseConn.generateDataFromDB();
                            } catch (ClassNotFoundException | SQLException e1) {
                                e1.printStackTrace();
                            }
                            return null;
                        }

                    };
                    x.execute();
                }
            });
        });

        nameList.addActionListener(e -> {
            if (Objects.requireNonNull(nameList.getSelectedItem()).toString().equals("Name")) {
                currentSelectedName = null;
            }
            else {
                currentSelectedName = nameList.getSelectedItem();
            }
            try {
                databaseConn.drillTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);
                if (Objects.nonNull(currentCostCode)) {
                    ccNames = databaseConn.sortedCostCentreNames.toArray();
                    costCodeList.setModel(new DefaultComboBoxModel<>(ccNames));
                }

                else {
                    ccNames = databaseConn.ccNames.toArray();
                    costCodeList.setModel(new DefaultComboBoxModel<>(ccNames));
                }

                if (Objects.isNull(currentSelectedCDG)) cdgList.setModel(new DefaultComboBoxModel<>(databaseConn.sortedCDGs.toArray()));
                if (Objects.isNull(currentSelectedDivision)) divisionList.setModel(new DefaultComboBoxModel<>(databaseConn.sortedDivisions.toArray()));
                if (Objects.isNull(currentSelectedDivision) && Objects.isNull(currentSelectedCDG) && Objects.isNull(currentSelectedName)) {
                    costCodeList.setModel(new DefaultComboBoxModel<>(databaseConn.ccNames.toArray()));
                    nameList.setModel(new DefaultComboBoxModel<>(databaseConn.names.toArray()));
                    divisionList.setModel(new DefaultComboBoxModel<>(databaseConn.divisions.toArray()));
                    cdgList.setModel(new DefaultComboBoxModel<>(databaseConn.CDGs.toArray()));
                }
            }

            catch (ParseException e1) {
                e1.printStackTrace();
            }

        });

        divisionList.addActionListener(e -> {
            if (Objects.requireNonNull(divisionList.getSelectedItem()).toString().equals("Division")) {
                currentSelectedDivision = null;
            }
            else {
                currentSelectedDivision = divisionList.getSelectedItem();
            }
            try {
                databaseConn.drillTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);
                if (Objects.nonNull(currentCostCode)) {
                    ccNames = databaseConn.sortedCostCentreNames.toArray();
                    costCodeList.setModel(new DefaultComboBoxModel<>(ccNames));
                }

                else {
                    ccNames = databaseConn.ccNames.toArray();
                    costCodeList.setModel(new DefaultComboBoxModel<>(ccNames));
                }

                if (Objects.isNull(currentSelectedCDG)) cdgList.setModel(new DefaultComboBoxModel<>(databaseConn.sortedCDGs.toArray()));
                if (Objects.isNull(currentSelectedName)) nameList.setModel(new DefaultComboBoxModel<>(databaseConn.sortedNames.toArray()));
                if (Objects.isNull(currentSelectedDivision) && Objects.isNull(currentSelectedCDG) && Objects.isNull(currentSelectedName)) {
                    costCodeList.setModel(new DefaultComboBoxModel<>(databaseConn.ccNames.toArray()));
                    nameList.setModel(new DefaultComboBoxModel<>(databaseConn.names.toArray()));
                    divisionList.setModel(new DefaultComboBoxModel<>(databaseConn.divisions.toArray()));
                    cdgList.setModel(new DefaultComboBoxModel<>(databaseConn.CDGs.toArray()));
                }
            }

            catch (ParseException e1) {
                e1.printStackTrace();
            }
        });

        cdgList.addActionListener(e -> {
            if (Objects.requireNonNull(cdgList.getSelectedItem()).toString().equals("CDG")) {
                currentSelectedCDG = null;
            }

            else {
                currentSelectedCDG = cdgList.getSelectedItem();
            }

            try {
                databaseConn.drillTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);
                if (Objects.nonNull(currentCostCode)) {
                    ccNames = databaseConn.sortedCostCentreNames.toArray();
                    costCodeList.setModel(new DefaultComboBoxModel<>(ccNames));
                }

                else {
                    ccNames = databaseConn.ccNames.toArray();
                    costCodeList.setModel(new DefaultComboBoxModel<>(ccNames));
                }

                if (Objects.isNull(currentSelectedDivision)) divisionList.setModel(new DefaultComboBoxModel<>(databaseConn.sortedDivisions.toArray()));
                if (Objects.isNull(currentSelectedName)) nameList.setModel(new DefaultComboBoxModel<>(databaseConn.sortedNames.toArray()));
                if (Objects.isNull(currentSelectedDivision) && Objects.isNull(currentSelectedCDG) && Objects.isNull(currentSelectedName)) {
                    costCodeList.setModel(new DefaultComboBoxModel<>(databaseConn.ccNames.toArray()));
                    nameList.setModel(new DefaultComboBoxModel<>(databaseConn.names.toArray()));
                    divisionList.setModel(new DefaultComboBoxModel<>(databaseConn.divisions.toArray()));
                    cdgList.setModel(new DefaultComboBoxModel<>(databaseConn.CDGs.toArray()));
                }
        }

            catch (ParseException e1) {
                e1.printStackTrace();
            }
        });

        previousMonth.addActionListener(e -> {

            try {
                pCounter--;
                tableRenew();
            }

            catch (ArrayIndexOutOfBoundsException error) {
                pCounter = databaseConn.periodNames.size()-1;
                tableRenew();
            }
        });

        periods.addActionListener(e -> {
            if (periods.getSelectedItem() == "Period") currentPeriod = null;
            else {
                currentPeriod = periods.getSelectedItem();
            }
            summaryCard.remove(scrollPane3);
            try {
                summaryCostCodeTable = databaseConn.createSummaryTable(currentPeriod, currentCDG);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            scrollPane3 = new JScrollPane(summaryCostCodeTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            summaryCard.add(scrollPane3);
            summaryCostCodeTable.setDefaultRenderer(Object.class, new SummaryTableCellRenderer());
            tableRenew();
        });

        summaryCDG.addActionListener(e -> {
            if (summaryCDG.getSelectedItem() == "CDG") currentPeriod = null;
            else {
                currentCDG = summaryCDG.getSelectedItem();
            }
            summaryCard.remove(scrollPane3);
            try {
                summaryCostCodeTable = databaseConn.createSummaryTable(currentPeriod, currentCDG);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            scrollPane3 = new JScrollPane(summaryCostCodeTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            summaryCard.add(scrollPane3);
            summaryCostCodeTable.setDefaultRenderer(Object.class, new SummaryTableCellRenderer());
            tableRenew();
        });

        nextMonth.addActionListener(e -> {
            try {
                pCounter++;
                tableRenew();
            }

            catch (ArrayIndexOutOfBoundsException error) {
                pCounter = 0;
                tableRenew();
            }
        });

        checkPrevious.addActionListener(e -> {
            try {
                costCodeList.setSelectedIndex(ccCounter);
            }

            catch (IllegalArgumentException exc) {
                ccCounter = 0;
                costCodeList.setSelectedIndex(ccCounter);

            }

            currentCostCode = ccNames[ccCounter];
            period = periodNames[pCounter];
            ccLabel.setText("Cost code: " + currentCostCode.toString());
            descriptionLabel.setText("Description: " + databaseConn.name);
            periodLabel.setText("Month: " + getPeriod(period));
            departmentCard.remove(scrollPane2);
            if (current[0]) {
                try {
                    departmentTable = databaseConn.createSpecificTable(currentCostCode, period, 0);
                    current[0] = false;
                    checkPrevious.setText("Previous data");
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }

            else {
                try {
                    departmentTable = databaseConn.createSpecificTable(currentCostCode, period, 1);
                    current[0] = true;
                    checkPrevious.setText("Current data");
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
            if (Objects.nonNull(currentSelectedDivision) || Objects.nonNull(currentSelectedName) || Objects.nonNull(currentSelectedCDG)) {
                try {
                    databaseConn.drillTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }

            }
            dataWithDecimal.clear();
            departmentTable.setDefaultRenderer(Object.class, new BoardTableCellRenderer());
            departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            departmentTable.getColumnModel().getColumn(14).setMinWidth(200);
            departmentTable.getColumnModel().getColumn(13).setMinWidth(50);
            departmentTable.getColumnModel().getColumn(12).setMinWidth(150);
            departmentTable.getColumnModel().getColumn(11).setMinWidth(150);
            departmentTable.getColumnModel().getColumn(10).setMinWidth(30);
            departmentTable.getColumnModel().getColumn(9).setMinWidth(30);
            departmentTable.getColumnModel().getColumn(8).setMinWidth(30);
            scrollPane2 = new JScrollPane(departmentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane2.setPreferredSize(new Dimension(1900, 950));
            departmentCard.add(scrollPane2, BorderLayout.CENTER);
            departmentTable.getModel().addTableModelListener(d -> {
                if (d.getColumn() == 27) {
                    int row = d.getFirstRow();
                    int column = d.getColumn();
                    TableModel model = (TableModel) d.getSource();
                    Object value = model.getValueAt(row, column);
                    String uniqueKey = period.toString() + departmentTable.getValueAt(row, 0).toString() + departmentTable.getValueAt(row, 1).toString();
                    final Connection[] conn = new Connection[1];
                    final Statement[] stmt = new Statement[1];
                    SwingWorker x = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() {
                            try {
                                Class.forName(databaseConn.JDBC_DRIVER);
                                conn[0] = DriverManager.getConnection(databaseConn.DB_URL, "dan", "ParolaMea123");
                                stmt[0] = conn[0].createStatement();
                                String noteSQL;
                                noteSQL =   "UPDATE data " +
                                        "SET `Note`= +'" + value +
                                        "'WHERE `Unique Key`='" +  uniqueKey + "';";
                                stmt[0].executeUpdate(noteSQL);
                                stmt[0].close();
                                conn[0].close();
                                table = databaseConn.generateDataFromDB();
                            } catch (ClassNotFoundException | SQLException e1) {
                                e1.printStackTrace();
                            }
                            return null;
                        }

                    };
                    x.execute();
                }
            });
        });

        previousDepartment.addActionListener(e -> {
            if (ccCounter == 0) {
                ccCounter = costCodeList.getItemCount()-1;
                tableRenew();
            }

            else {
                ccCounter--;
                tableRenew();
            }
        });

        nextDepartment.addActionListener(e -> {
            try {
                ccCounter++;
                tableRenew();
            }

            catch (ArrayIndexOutOfBoundsException | IllegalArgumentException error) {
                ccCounter = 0;
                tableRenew();
            }
        });

        clear.addActionListener(e -> {
            if (!(currentCostCode.equals("ALL")) || Objects.nonNull(currentSelectedName) || Objects.nonNull(currentSelectedDivision) || Objects.nonNull(currentSelectedCDG)) {
                ccCounter = 0;
                divisionList.setSelectedIndex(0);
                nameList.setSelectedIndex(0);
                cdgList.setSelectedIndex(0);
                tableRenew();
            }
        });

        overview.addActionListener(e -> cardLayout.show(contentPanel, "1"));

        departmentView.addActionListener(e -> cardLayout.show(contentPanel, "2"));

        summaryView.addActionListener(e -> cardLayout.show(contentPanel, "3"));

        departmentTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 27) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                TableModel model = (TableModel) e.getSource();
                Object value = model.getValueAt(row, column);
                String uniqueKey = period.toString() + departmentTable.getValueAt(row, 0).toString() + departmentTable.getValueAt(row, 1).toString();
                final Connection[] conn = new Connection[1];
                final Statement[] stmt = new Statement[1];
                SwingWorker x = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        try {
                            Class.forName(databaseConn.JDBC_DRIVER);
                            conn[0] = DriverManager.getConnection(databaseConn.DB_URL, "dan", "ParolaMea123");
                            stmt[0] = conn[0].createStatement();
                            String noteSQL;
                            noteSQL =   "UPDATE data " +
                                    "SET `Note`= +'" + value +
                                    "'WHERE `Unique Key`='" +  uniqueKey + "';";
                            stmt[0].executeUpdate(noteSQL);
                            stmt[0].close();
                            conn[0].close();
                            table = databaseConn.generateDataFromDB();
                        } catch (ClassNotFoundException | SQLException e1) {
                            e1.printStackTrace();
                        }
                        return null;
                    }

                };
                x.execute();
            }
        });
    }

    private void tableRenew() {
        costCodeList.setSelectedIndex(ccCounter);
        currentCostCode = costCodeList.getSelectedItem();
        period = periodNames[pCounter];
        ccLabel.setText("Cost code: " + currentCostCode);
        descriptionLabel.setText("Description: " + databaseConn.name);
        periodLabel.setText("Month: " + getPeriod(period));
        departmentCard.remove(scrollPane2);
        try {
            departmentTable = databaseConn.createSpecificTable(currentCostCode, period, 1);
            if (Objects.nonNull(currentSelectedDivision) || Objects.nonNull(currentSelectedName) || Objects.nonNull(currentSelectedCDG)) {
                databaseConn.drillTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);

            }
        }

        catch (ParseException e) {
            e.printStackTrace();
        }
        dataWithDecimal.clear();
        departmentTable.setDefaultRenderer(Object.class, new BoardTableCellRenderer());
        departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        departmentTable.getColumnModel().getColumn(14).setMinWidth(200);
        departmentTable.getColumnModel().getColumn(13).setMinWidth(50);
        departmentTable.getColumnModel().getColumn(12).setMinWidth(150);
        departmentTable.getColumnModel().getColumn(11).setMinWidth(150);
        departmentTable.getColumnModel().getColumn(10).setMinWidth(30);
        departmentTable.getColumnModel().getColumn(9).setMinWidth(30);
        departmentTable.getColumnModel().getColumn(8).setMinWidth(30);
        scrollPane2 = new JScrollPane(departmentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane2.setPreferredSize(new Dimension(1900, 950));
        departmentCard.add(scrollPane2, BorderLayout.CENTER);
        departmentTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 27) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                TableModel model = (TableModel) e.getSource();
                Object value = model.getValueAt(row, column);
                String uniqueKey = period.toString() + departmentTable.getValueAt(row, 0).toString() + departmentTable.getValueAt(row, 1).toString();
                final Connection[] conn = new Connection[1];
                final Statement[] stmt = new Statement[1];
                SwingWorker x = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        try {
                            Class.forName(databaseConn.JDBC_DRIVER);
                            conn[0] = DriverManager.getConnection(databaseConn.DB_URL, "dan", "ParolaMea123");
                            stmt[0] = conn[0].createStatement();
                            String noteSQL;
                            noteSQL =   "UPDATE data " +
                                    "SET `Note`= +'" + value +
                                    "'WHERE `Unique Key`='" +  uniqueKey + "';";
                            stmt[0].executeUpdate(noteSQL);
                            stmt[0].close();
                            conn[0].close();
                            table = databaseConn.generateDataFromDB();
                        } catch (ClassNotFoundException | SQLException e1) {
                            e1.printStackTrace();
                        }
                        return null;
                    }

                };
                x.execute();
            }
        });
    }

    private String getPeriod (Object o) {
        if (Objects.isNull(o)) {
            return "No periods available";
        }

        else {
            String current = o.toString();
            String year = current.substring(0, 4);
            String month = current.substring(4, 6);
            String fullYear = "20" + year.substring(0, 2) + "-" + "20" + year.substring(2, 4);
            HashMap<String, String> monthsMap = new HashMap<>();
            monthsMap.put("01", "April");
            monthsMap.put("02", "May");
            monthsMap.put("03", "June");
            monthsMap.put("04", "July");
            monthsMap.put("05", "August");
            monthsMap.put("06", "September");
            monthsMap.put("07", "October");
            monthsMap.put("08", "November");
            monthsMap.put("09", "December");
            monthsMap.put("10", "January");
            monthsMap.put("11", "February");
            monthsMap.put("12", "March");
            return monthsMap.get(month) + " " + fullYear;
        }
    }


    public class BoardTableCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            DecimalFormat nf = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormatSymbols symbols = nf.getDecimalFormatSymbols();
            Object s = table.getModel().getValueAt(row, col);
            symbols.setCurrencySymbol(""); // Don't use null.
            nf.setDecimalFormatSymbols(symbols);
            nf.setMaximumFractionDigits(0);
            Color lightGray = new Color(217, 229, 247);
            Color lightRed = new Color(254, 209, 209);
            Color lightGreen = new Color(226, 249, 225);
            String x = String.valueOf(table.convertRowIndexToView(row));
            String y = String.valueOf(table.convertColumnIndexToView(col));
            if (Objects.nonNull(s)) {
                if (dataWithDecimal.containsKey(x + y)) {
                    dataWithDecimal.put(x + y, s.toString());
                } else {
                    String coordinateCode = x + y;
                    dataWithDecimal.put(coordinateCode, s.toString());
                }
                setToolTipText(dataWithDecimal.get(x + y).get(0));
            }
            boolean isLimitExceeded;
            try {

                if (col>=6 && col <=11 && Objects.nonNull(s)) {
                    int roundedValue = Math.toIntExact(Math.round(databaseConn.nf.parse(s.toString()).doubleValue()));
                    table.getModel().setValueAt(nf.format(roundedValue), row, col);
                }
                isLimitExceeded = databaseConn.limitExceeded(row, departmentTable);
                boolean isTotal = databaseConn.isTotal(row, departmentTable);
                boolean hasNote = databaseConn.hasNote(row, departmentTable);
                if (isTotal) {
                    c.setBackground(lightGray);
                    c.setForeground(Color.BLACK);
                }

                else if (hasNote && isLimitExceeded) {
                    c.setBackground(lightGreen);
                    c.setForeground(Color.BLACK);
                }

                else if (isLimitExceeded) {
                    c.setBackground(lightRed);
                    c.setForeground(Color.BLACK);
                }

                else {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }
            }

            catch (ParseException e) {
                e.printStackTrace();
            }

            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }

            return c;
        }
    }

    public class SummaryTableCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            DecimalFormat nf = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormatSymbols symbols = nf.getDecimalFormatSymbols();
            Object s = table.getModel().getValueAt(row, col);
            symbols.setCurrencySymbol(""); // Don't use null.
            nf.setDecimalFormatSymbols(symbols);
            nf.setMaximumFractionDigits(0);

            try {
                int roundedValue = Math.toIntExact(Math.round(databaseConn.nf.parse(s.toString()).doubleValue()));
                if ((col == 5 || col == 8) && roundedValue<0) c.setForeground(Color.RED);

                else {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }

            }

            catch (ParseException e) {
                c.setBackground(table.getBackground());
                c.setForeground(table.getForeground());
            }

            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }

            return c;
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(() -> {
            try {
                new UserInterface();
            } catch (ClassNotFoundException | ParseException e) {
                e.printStackTrace();
            }
        });
    }

}