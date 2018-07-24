import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Objects;

public class UserInterface extends JFrame {

    // default values for department view
    private JTable departmentTable;
    private JScrollPane scrollPane;
    private JScrollPane scrollPane2;
    private CardLayout cardLayout = new CardLayout();
    private JLabel ccLabel;
    private JLabel periodLabel;
    private JLabel descriptionLabel;
    private JTable table;
    private JPanel departmentCard;

    private JComboBox costCodeList;
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

    // TODO: PROGRESS BAR
    static JProgressBar pb;

    private UserInterface() throws ClassNotFoundException, ParseException {

        databaseConn = new DatabaseConn();
        table = databaseConn.generateDataFromDB();
        ccNames = databaseConn.ccNames.toArray();
        periodNames = databaseConn.periodNames.toArray();

        if (ccNames.length < 1) {
            ccNames = new Object[]{"No cost codes available"};
            periodNames = new Object[]{"No periods available"};
        }

        currentCostCode = ccNames[ccCounter];
        period = periodNames[pCounter];
        contentPanel = new JPanel(cardLayout);
        JPanel overviewCard = new JPanel(new BorderLayout());
        departmentCard = new JPanel(new BorderLayout());
        JPanel north = new JPanel(new BorderLayout());

         /*
            -------- Buttons for switching between cards ---------
         */
        final JRadioButton overview = new JRadioButton("Overview", true);
        final JRadioButton departmentView = new JRadioButton("Department View");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(overview);
        buttonGroup.add(departmentView);
        final JPanel radioButtons = new JPanel();
        radioButtons.add(overview);
        radioButtons.add(departmentView);

        /*
            -------- Overview ---------
         */

        scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
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

        departmentTable = databaseConn.createSpecificTable(currentCostCode, period);
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

        costCodeList = new JComboBox(ccNames);
        costCodeList.setSelectedIndex(0);

        nameList = new JComboBox(databaseConn.names.toArray());
        nameList.setSelectedIndex(0);

        divisionList = new JComboBox(databaseConn.divisions.toArray());
        divisionList.setSelectedIndex(0);

        cdgList = new JComboBox(databaseConn.CDGs.toArray());
        cdgList.setSelectedIndex(0);

        currentSelectedName = null;
        currentSelectedDivision = null;
        currentSelectedCDG = null;

        JPanel listView= new JPanel();
        listView.add(costCodeList);
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
        importSpreadsheet.addActionListener(e -> {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();

                try {
                    databaseConn.importSpreadsheet(filePath);
                    dispose();
                    new UserInterface();
                }

                catch (IOException | ClassNotFoundException | ParseException e1) {
                    e1.printStackTrace();
                }
            }
        });
        radioButtons.add(importSpreadsheet, BorderLayout.SOUTH);
        add(radioButtons, BorderLayout.NORTH);
        north.add(labelPanel, BorderLayout.CENTER);
        departmentCard.add(scrollPane2, BorderLayout.CENTER);

        costCodeList.addActionListener(e -> {
            JComboBox combo = (JComboBox)e.getSource();
            int pk = 0;
            try {
                for (Object x : ccNames) {
                    if (Objects.equals(combo.getSelectedItem(), x)) {
                        break;
                    } else {
                        pk++;
                    }
                }
            }

            catch (NullPointerException nul) {
                pk = 0;
            }
            ccCounter = pk;
            currentCostCode = ccNames[ccCounter];
            departmentCard.remove(scrollPane2);
            try {
                departmentTable = databaseConn.createSpecificTable(currentCostCode, period);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }

            departmentTable.setDefaultRenderer(Object.class, new BoardTableCellRenderer());
            nameList.setModel(new DefaultComboBoxModel<>(databaseConn.names.toArray()));
            divisionList.setModel(new DefaultComboBoxModel<>(databaseConn.divisions.toArray()));
            cdgList.setModel(new DefaultComboBoxModel<>(databaseConn.CDGs.toArray()));

            currentSelectedName = null;
            currentSelectedCDG = null;
            currentSelectedDivision = null;

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
                    String uniqueKey = period.toString() + currentCostCode.toString() + departmentTable.getValueAt(row, 1).toString();
                    Connection conn;
                    Statement stmt;
                    try {
                        Class.forName(databaseConn.JDBC_DRIVER);
                        conn = DriverManager.getConnection(databaseConn.DB_URL, "dan", "ParolaMea123");
                        stmt = conn.createStatement();
                        String noteSQL;
                        noteSQL =   "UPDATE data " +
                                "SET `Note`= +'" + value +
                                "'WHERE `Unique Key`='" +  uniqueKey + "';";
                        stmt.executeUpdate(noteSQL);
                        stmt.close();
                        conn.close();
                        table = databaseConn.generateDataFromDB();
                    } catch (ClassNotFoundException | SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        });

        nameList.addActionListener(e -> {
            if (Objects.requireNonNull(nameList.getSelectedItem()).toString().equals("ALL")) {
                currentSelectedName = null;
            }
            else {
                currentSelectedName = nameList.getSelectedItem();
            }
            try {
                databaseConn.drillTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }

        });

        divisionList.addActionListener(e -> {
            if (Objects.requireNonNull(divisionList.getSelectedItem()).toString().equals("ALL")) {
                currentSelectedDivision = null;
            }
            else {
                currentSelectedDivision = divisionList.getSelectedItem();
            }
            try {
                databaseConn.drillTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        });

        cdgList.addActionListener(e -> {
            if (Objects.requireNonNull(cdgList.getSelectedItem()).toString().equals("ALL")) {
                currentSelectedCDG = null;
            }
            else {
                currentSelectedCDG = cdgList.getSelectedItem();
            }
            try {
                databaseConn.drillTable(departmentTable, currentSelectedName, currentSelectedDivision, currentSelectedCDG);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        });

        JLabel month = new JLabel("Month");
        JButton previousMonth = new JButton("Previous");
        JButton nextMonth = new JButton("Next");
        previousMonth.addActionListener(e -> {

            try {
                pCounter--;
                tableRenew();
            }

            catch (ArrayIndexOutOfBoundsException error) {
                pCounter = 0;
                try {
                    tableRenew();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        });

        nextMonth.addActionListener(e -> {
            try {
                pCounter++;
                tableRenew();
            }

            catch (ArrayIndexOutOfBoundsException error) {
                pCounter = 0;
                try {
                    tableRenew();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        });
        final JPanel eastPanel = new JPanel();
        eastPanel.add(month);
        eastPanel.add(previousMonth);
        eastPanel.add(nextMonth);

        JLabel department = new JLabel("Department");
        JButton previousDepartment = new JButton("Previous");
        previousDepartment.addActionListener(e -> {
            try {
                ccCounter--;
                tableRenew();
            }


            catch(ArrayIndexOutOfBoundsException error){
                    ccCounter = 0;
                try {
                    tableRenew();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            } catch (ParseException e1) {
                e1.printStackTrace();
            }

        });

        JButton nextDepartment = new JButton("Next");
        nextDepartment.addActionListener(e -> {
            try {
                ccCounter++;
                try {
                    tableRenew();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }

            catch (ArrayIndexOutOfBoundsException error) {
                ccCounter = 0;
                try {
                    tableRenew();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
        });

        final JPanel westPanel = new JPanel();
        westPanel.add(department);
        westPanel.add(previousDepartment);
        westPanel.add(nextDepartment);
        north.add(eastPanel, BorderLayout.EAST);
        JPanel westP = new JPanel();
        westP.add(westPanel);
        westP.add(listView);
        north.add(westP, BorderLayout.WEST);
        departmentCard.add(north, BorderLayout.NORTH);

        /*
        Card Layout
         */

        contentPanel.add(overviewCard, "1");
        contentPanel.add(departmentCard, "2");

        contentPanel.setLayout(cardLayout);
        add(contentPanel, BorderLayout.CENTER);

        overview.addActionListener(e -> cardLayout.show(contentPanel, "1"));

        departmentView.addActionListener(e -> cardLayout.show(contentPanel, "2"));

        departmentTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 27) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                TableModel model = (TableModel) e.getSource();
                Object value = model.getValueAt(row, column);
                String uniqueKey = period.toString() + currentCostCode.toString() + departmentTable.getValueAt(row, 1).toString();
                Connection conn;
                Statement stmt;
                try {
                    Class.forName(databaseConn.JDBC_DRIVER);
                    conn = DriverManager.getConnection(databaseConn.DB_URL, "dan", "ParolaMea123");
                    stmt = conn.createStatement();
                    String noteSQL;
                    noteSQL =   "UPDATE data " +
                            "SET `Note`= +'" + value +
                            "'WHERE `Unique Key`='" +  uniqueKey + "';";
                    stmt.executeUpdate(noteSQL);
                    scrollPane.remove(table);
                    table = databaseConn.generateDataFromDB();
                    scrollPane.add(table);
                    stmt.close();
                    conn.close();
                    table = databaseConn.generateDataFromDB();
                } catch (ClassNotFoundException | SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
        pack();
        getDefaultCloseOperation();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("VVCP: Variance Visualisation and Calculation Program");
    }

    private void tableRenew() throws ParseException {

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
        departmentTable = databaseConn.createSpecificTable(currentCostCode, period);
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
                String uniqueKey = period.toString() + currentCostCode.toString() + departmentTable.getValueAt(row, 1).toString();
                Connection conn;
                Statement stmt;
                try {
                    Class.forName(databaseConn.JDBC_DRIVER);
                    conn = DriverManager.getConnection(databaseConn.DB_URL, "dan", "ParolaMea123");
                    stmt = conn.createStatement();
                    String noteSQL;
                    noteSQL =   "UPDATE data " +
                            "SET `Note`= +'" + value +
                            "'WHERE `Unique Key`='" +  uniqueKey + "';";
                    stmt.executeUpdate(noteSQL);
                    stmt.close();
                    conn.close();
                    table = databaseConn.generateDataFromDB();
                } catch (ClassNotFoundException | SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private String getPeriod (Object o) {
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


    public class BoardTableCellRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            boolean isLimitExceeded;
            Color lightGray = new Color(237, 237, 237);
            Color lightRed = new Color(254, 209, 209);
            Color lightGreen = new Color(226, 249, 225);
            try {
                isLimitExceeded = databaseConn.limitExceeded(row);
                boolean isTotal = databaseConn.isTotal(row);
                boolean hasNote = databaseConn.hasNote(row);
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