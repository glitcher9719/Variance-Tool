import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.TableFilterHeader;

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

    private Object currentSummaryPeriod = null;
    private Object currentSummaryCDG = null;
    private Object currentSummaryDivision = null;

    private JComboBox<Object> costCodeComboBox;
    private JComboBox<Object> namesComboBox;
    private JComboBox<Object> cdgComboBox;
    private JComboBox<Object> divisionsComboBox;
    private boolean isTableFiltered = false;
    private boolean nameFilter = false;
    private boolean cdgFilter = false;
    private boolean divisionFilter = false;

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
        table = databaseConn.generateOverviewTableFromDB();
        TableFilterHeader filterHeader = new TableFilterHeader(table, AutoChoices.ENABLED);
        summaryCostCodeTable = databaseConn.createSummaryTable(currentSummaryPeriod, currentSummaryCDG, currentSummaryDivision);
        summaryCostCodeTable.setDefaultRenderer(Object.class, new SummaryTableCellRenderer());
        costCodeComboBox = new JComboBox<>(databaseConn.ccNames.toArray());
        costCodeComboBox.setSelectedIndex(0);
        currentCostCode = costCodeComboBox.getSelectedItem();
        pCounter = databaseConn.periodNames.toArray().length-1;
        period = databaseConn.periodNames.toArray()[pCounter];
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

        departmentTable = databaseConn.generateTable(currentCostCode, period, 1);
        dataWithDecimal.clear();
        departmentTable.setDefaultRenderer(Object.class, new BoardTableCellRenderer());
        departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        departmentTable.getColumnModel().getColumn(13).setMinWidth(80);
        departmentTable.getColumnModel().getColumn(12).setMinWidth(200);
        departmentTable.getColumnModel().getColumn(11).setMinWidth(150);
        departmentTable.getColumnModel().getColumn(10).setMinWidth(30);
        departmentTable.getColumnModel().getColumn(9).setMinWidth(30);
        departmentTable.getColumnModel().getColumn(8).setMinWidth(30);
        scrollPane2 = new JScrollPane(departmentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        namesComboBox = new JComboBox<>(databaseConn.names.toArray());
        namesComboBox.setSelectedIndex(0);

        divisionsComboBox = new JComboBox<>(databaseConn.divisions.toArray());
        divisionsComboBox.setSelectedIndex(0);

        cdgComboBox = new JComboBox<>(databaseConn.CDGs.toArray());
        cdgComboBox.setSelectedIndex(0);

        currentSelectedName = namesComboBox.getSelectedItem();
        currentSelectedDivision = divisionsComboBox.getSelectedItem();
        currentSelectedCDG = cdgComboBox.getSelectedItem();

        JLabel costCode  = new JLabel("Cost code: ");
        JLabel filters = new JLabel("Filter by: ");
        JPanel listView= new JPanel();
        listView.add(costCode);
        listView.add(costCodeComboBox);
        listView.add(filters);
        listView.add(namesComboBox);
        listView.add(divisionsComboBox);
        listView.add(cdgComboBox);

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
        JButton importButton = new JButton("Import Spreadsheet");

        radioButtons.add(importButton, BorderLayout.SOUTH);
        add(radioButtons, BorderLayout.NORTH);
        north.add(labelPanel, BorderLayout.CENTER);
        departmentCard.add(scrollPane2, BorderLayout.CENTER);

        JLabel month = new JLabel("Month");
        JButton previousMonthButton = new JButton("Previous");
        JButton nextMonthButton = new JButton("Next");

        final boolean[] current = {true};
        JButton dataButton = new JButton("Current data");

        final JPanel eastPanel = new JPanel();
        eastPanel.add(dataButton);
        eastPanel.add(month);
        eastPanel.add(previousMonthButton);
        eastPanel.add(nextMonthButton);

        JLabel department = new JLabel("Department");
        JButton previousDepartmentButton = new JButton("Previous");
        JButton nextDepartmentButton = new JButton("Next");

        JButton clearButton = new JButton("Clear");

        final JPanel westPanel = new JPanel();
        westPanel.add(department);
        westPanel.add(previousDepartmentButton);
        westPanel.add(nextDepartmentButton);
        north.add(eastPanel, BorderLayout.EAST);
        JPanel westP = new JPanel();
        westP.add(westPanel);
        westP.add(listView);
        westP.add(clearButton);
        north.add(westP, BorderLayout.WEST);
        departmentCard.add(north, BorderLayout.NORTH);

        /*
        Summary layout
         */

        scrollPane3 = new JScrollPane(summaryCostCodeTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        summaryCard.add(scrollPane3);
        JComboBox<Object> summaryPeriods = new JComboBox<>();
        summaryPeriods.addItem("Period");
        for (Object x : databaseConn.periodNames) {
            summaryPeriods.addItem(x);
        }
        JComboBox<Object> summaryCDG = new JComboBox<>(databaseConn.CDGs.toArray());
        JComboBox<Object> summaryDivision = new JComboBox<>(databaseConn.divisions.toArray());
        JPanel buttonTable = new JPanel();
        buttonTable.add(summaryPeriods);
        buttonTable.add(summaryCDG);
        buttonTable.add(summaryDivision);
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
        importButton.addActionListener(e -> {
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

        costCodeComboBox.addActionListener(e -> {
            ccCounter = costCodeComboBox.getSelectedIndex();
            currentCostCode = costCodeComboBox.getSelectedItem();
            departmentCard.remove(scrollPane2);
            try {
                if (isTableFiltered) {
                    departmentTable = databaseConn.generateTable(currentCostCode, period, 1);
                    databaseConn.filterTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);
                    summaryView.setEnabled(false);
                }

                else {
                    departmentTable = databaseConn.generateTable(currentCostCode, period, 1);
                    summaryView.setEnabled(true);
                }
                departmentTable = databaseConn.generateTable(currentCostCode, period, 1);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            dataWithDecimal.clear();
            departmentTable.setDefaultRenderer(Object.class, new BoardTableCellRenderer());
            departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            departmentTable.getColumnModel().getColumn(13).setMinWidth(80);
            departmentTable.getColumnModel().getColumn(12).setMinWidth(200);
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
                                table = databaseConn.generateOverviewTableFromDB();
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

        namesComboBox.addActionListener(e -> {
            currentSelectedName = namesComboBox.getSelectedItem();
            try {
                databaseConn.filterTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);

                if (currentSelectedName.equals("Name")) {
                    nameFilter = false;
                    summaryView.setEnabled(true);
                    if (currentSelectedDivision.equals("Division") && currentSelectedCDG.equals("CDG")) isTableFiltered = false;
                }

                else if (!isTableFiltered) {
                    isTableFiltered = true;
                    nameFilter = true;
                    summaryView.setEnabled(false);
                    costCodeComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedCostCentreNames.toArray()));
                    divisionsComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedDivisions.toArray()));
                    cdgComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedCDGs.toArray()));
                }

                else {
                    nameFilter = true;
                    summaryView.setEnabled(false);
                    costCodeComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedCostCentreNames.toArray()));
                    if (!divisionFilter) divisionsComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedDivisions.toArray()));
                    if (!cdgFilter) cdgComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedCDGs.toArray()));
                }
            }

            catch (ParseException e1) {
                e1.printStackTrace();
            }

        });

        divisionsComboBox.addActionListener(e -> {
            currentSelectedDivision = divisionsComboBox.getSelectedItem();
            try {
                databaseConn.filterTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);

                if (currentSelectedDivision.equals("Division")) {
                    divisionFilter = false;
                    summaryView.setEnabled(true);
                    if (currentSelectedName.equals("Name") && currentSelectedCDG.equals("CDG")) isTableFiltered = false;
                }

                else if (!isTableFiltered) {
                    isTableFiltered = true;
                    divisionFilter = true;
                    summaryView.setEnabled(false);
                    costCodeComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedCostCentreNames.toArray()));
                    cdgComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedCDGs.toArray()));
                    namesComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedNames.toArray()));
                }

                else {
                    divisionFilter = true;
                    summaryView.setEnabled(false);
                    costCodeComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedCostCentreNames.toArray()));
                    if (!nameFilter) namesComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedNames.toArray()));
                    if (!cdgFilter) cdgComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedCDGs.toArray()));
                }
            }

            catch (ParseException e1) {
                e1.printStackTrace();
            }
        });

        cdgComboBox.addActionListener(e -> {
            currentSelectedCDG = cdgComboBox.getSelectedItem();
            try {
                databaseConn.filterTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);

                if (currentSelectedCDG.equals("CDG")) {
                    cdgFilter = false;
                    summaryView.setEnabled(true);
                    if (currentSelectedName.equals("Name") && currentSelectedDivision.equals("Division")) isTableFiltered = false;
                }

                else if (!isTableFiltered) {
                    isTableFiltered = true;
                    cdgFilter = true;
                    summaryView.setEnabled(false);
                    costCodeComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedCostCentreNames.toArray()));
                    divisionsComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedDivisions.toArray()));
                    namesComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedNames.toArray()));
                }

                else {
                    cdgFilter = true;
                    summaryView.setEnabled(false);
                    costCodeComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedCostCentreNames.toArray()));
                    if (!nameFilter) namesComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedNames.toArray()));
                    if (!divisionFilter) divisionsComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.sortedDivisions.toArray()));
                }

            }

            catch (ParseException e1) {
                e1.printStackTrace();
            }
        });

        previousMonthButton.addActionListener(e -> {

            try {
                pCounter--;
                tableRenew();
            }

            catch (ArrayIndexOutOfBoundsException error) {
                pCounter = databaseConn.periodNames.size()-1;
                tableRenew();
            }
        });

        summaryPeriods.addActionListener(e -> {
            if (summaryPeriods.getSelectedItem() == "Period") {
                currentSummaryPeriod = null;
            }
            else {
                currentSummaryPeriod = summaryPeriods.getSelectedItem();
            }
            summaryCard.remove(scrollPane3);
            try {
                summaryCostCodeTable = databaseConn.createSummaryTable(currentSummaryPeriod, currentSummaryCDG, currentSummaryDivision);
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
            if (summaryCDG.getSelectedItem() == "CDG") {
                currentSummaryCDG = null;
            }

            else {
                currentSummaryCDG = summaryCDG.getSelectedItem();
            }
            summaryCard.remove(scrollPane3);
            try {
                summaryCostCodeTable = databaseConn.createSummaryTable(currentSummaryPeriod, currentSummaryCDG, currentSummaryDivision);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            scrollPane3 = new JScrollPane(summaryCostCodeTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            summaryCard.add(scrollPane3);
            summaryCostCodeTable.setDefaultRenderer(Object.class, new SummaryTableCellRenderer());
            tableRenew();
        });

        summaryDivision.addActionListener(e -> {
            if (summaryDivision.getSelectedItem() == "Division") {
                currentSummaryDivision = null;
            }

            else {
                currentSummaryDivision = summaryDivision.getSelectedItem();
            }
            summaryCard.remove(scrollPane3);
            try {
                summaryCostCodeTable = databaseConn.createSummaryTable(currentSummaryPeriod, currentSummaryCDG, currentSummaryDivision);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            scrollPane3 = new JScrollPane(summaryCostCodeTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            summaryCard.add(scrollPane3);
            summaryCostCodeTable.setDefaultRenderer(Object.class, new SummaryTableCellRenderer());
            tableRenew();
        });

        nextMonthButton.addActionListener(e -> {
            try {
                pCounter++;
                tableRenew();
            }

            catch (ArrayIndexOutOfBoundsException error) {
                pCounter = 0;
                tableRenew();
            }
        });

        dataButton.addActionListener(e -> {
            try {
                costCodeComboBox.setSelectedIndex(ccCounter);
            }

            catch (IllegalArgumentException exc) {
                ccCounter = 0;
                costCodeComboBox.setSelectedIndex(ccCounter);

            }

            currentCostCode = costCodeComboBox.getSelectedItem();
            period = databaseConn.periodNames.toArray()[pCounter];
            ccLabel.setText("Cost code: " + currentCostCode.toString());
            descriptionLabel.setText("Description: " + databaseConn.name);
            periodLabel.setText("Month: " + getPeriod(period));
            departmentCard.remove(scrollPane2);
            if (current[0]) {
                try {
                    departmentTable = databaseConn.generateTable(currentCostCode, period, 0);
                    if (isTableFiltered) {
                        databaseConn.filterTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);
                    }
                    current[0] = false;
                    dataButton.setText("Previous data");
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }

            else {
                try {
                    departmentTable = databaseConn.generateTable(currentCostCode, period, 1);
                    if (isTableFiltered) {
                        databaseConn.filterTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);
                    }
                    current[0] = true;
                    dataButton.setText("Current data");
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }

            dataWithDecimal.clear();
            departmentTable.setDefaultRenderer(Object.class, new BoardTableCellRenderer());
            departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            departmentTable.getColumnModel().getColumn(13).setMinWidth(80);
            departmentTable.getColumnModel().getColumn(12).setMinWidth(200);
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
                                conn[0] = DriverManager.getConnection(databaseConn.DB_URL, databaseConn.USER_NAME, databaseConn.PASSWORD);
                                stmt[0] = conn[0].createStatement();
                                String noteSQL;
                                noteSQL =   "UPDATE data " +
                                        "SET `Note`= +'" + value +
                                        "'WHERE `Unique Key`='" +  uniqueKey + "';";
                                stmt[0].executeUpdate(noteSQL);
                                stmt[0].close();
                                conn[0].close();
                                table = databaseConn.generateOverviewTableFromDB();
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

        previousDepartmentButton.addActionListener(e -> {
            if (ccCounter == 0) {
                ccCounter = costCodeComboBox.getItemCount()-1;
                tableRenew();
            }

            else {
                ccCounter--;
                tableRenew();
            }
        });

        nextDepartmentButton.addActionListener(e -> {
            try {
                ccCounter++;
                tableRenew();
            }

            catch (ArrayIndexOutOfBoundsException | IllegalArgumentException error) {
                ccCounter = 0;
                tableRenew();
            }
        });

        clearButton.addActionListener(e -> {
            boolean isAlreadyCleared = currentCostCode.toString().equals("ALL") && currentSelectedDivision.toString().equals("Division") && currentSelectedName.toString().equals("Name") && currentSelectedCDG.toString().equals("CDG");
            if (!isAlreadyCleared) {
                ccCounter = 0;
                tableRenew();
                isTableFiltered = false;
                nameFilter = false;
                divisionFilter = false;
                cdgFilter = false;
                currentSelectedName = namesComboBox.getSelectedItem();
                currentSelectedDivision = divisionsComboBox.getSelectedItem();
                currentSelectedCDG = cdgComboBox.getSelectedItem();
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
                            conn[0] = DriverManager.getConnection(databaseConn.DB_URL, databaseConn.USER_NAME, databaseConn.PASSWORD);
                            stmt[0] = conn[0].createStatement();
                            String noteSQL;
                            noteSQL =   "UPDATE data " +
                                    "SET `Note`= +'" + value +
                                    "'WHERE `Unique Key`='" +  uniqueKey + "';";
                            stmt[0].executeUpdate(noteSQL);
                            stmt[0].close();
                            conn[0].close();
                            table = databaseConn.generateOverviewTableFromDB();
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
        if (!isTableFiltered) {
            costCodeComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.ccNames.toArray()));
            costCodeComboBox.setSelectedIndex(ccCounter);
            currentCostCode = costCodeComboBox.getSelectedItem();
            period = databaseConn.periodNames.toArray()[pCounter];
            ccLabel.setText("Cost code: " + currentCostCode);
            descriptionLabel.setText("Description: " + databaseConn.name);
            periodLabel.setText("Month: " + getPeriod(period));
            departmentCard.remove(scrollPane2);
            try {
                departmentTable = databaseConn.generateTable(currentCostCode, period, 1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            divisionsComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.divisions.toArray()));
            namesComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.names.toArray()));
            cdgComboBox.setModel(new DefaultComboBoxModel<>(databaseConn.CDGs.toArray()));
        }

        else {
            costCodeComboBox.setSelectedIndex(ccCounter);
            currentCostCode = costCodeComboBox.getSelectedItem();
            period = databaseConn.periodNames.toArray()[pCounter];
            ccLabel.setText("Cost code: " + currentCostCode);
            descriptionLabel.setText("Description: " + databaseConn.name);
            periodLabel.setText("Month: " + getPeriod(period));
            departmentCard.remove(scrollPane2);
            try {
                departmentTable = databaseConn.generateTable(currentCostCode, period, 1);
                databaseConn.filterTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        dataWithDecimal.clear();
        departmentTable.setDefaultRenderer(Object.class, new BoardTableCellRenderer());
        departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        departmentTable.getColumnModel().getColumn(13).setMinWidth(80);
        departmentTable.getColumnModel().getColumn(12).setMinWidth(200);
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
                            conn[0] = DriverManager.getConnection(databaseConn.DB_URL, databaseConn.USER_NAME, databaseConn.PASSWORD);
                            stmt[0] = conn[0].createStatement();
                            String noteSQL;
                            noteSQL =   "UPDATE data " +
                                    "SET `Note`= +'" + value +
                                    "'WHERE `Unique Key`='" +  uniqueKey + "';";
                            stmt[0].executeUpdate(noteSQL);
                            stmt[0].close();
                            conn[0].close();
                            table = databaseConn.generateOverviewTableFromDB();
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

            try {
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

            catch (StringIndexOutOfBoundsException e) {
                return "No periods available";
            }
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
                if (col >= 2 && col<= 10) {
                    this.setHorizontalAlignment(SwingConstants.RIGHT);
                }

                else {
                    this.setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (col == 4 || col == 7) {
                    c.setFont(this.getFont().deriveFont(Font.BOLD));
                }


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
                    c.setFont(this.getFont().deriveFont(Font.BOLD));
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
                if (col >= 3 && col<= 11) {
                    this.setHorizontalAlignment(SwingConstants.RIGHT);
                }

                else {
                    this.setHorizontalAlignment(SwingConstants.LEFT);
                }

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