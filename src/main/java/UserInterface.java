import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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

    // TODO: DRILLING
    private ArrayList<Object> names;
    private ArrayList<Object> divisions;
    private ArrayList<Object> CDGs;
    private JComboBox nameList;
    private JComboBox cdgList;
    private JComboBox divisionList;

    private int ccCounter = 0;
    private int pCounter = 0;
    private Object currentCostCode;
    private Object period;
    private JPanel contentPanel;
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
            names = new ArrayList<Object>();
            names.add("No names available");
            divisions = new ArrayList<Object>();
            divisions.add("No divisions available");
            CDGs = new ArrayList<Object>();
            CDGs.add("No CDGs available");
        }

        else {
            names = new ArrayList<Object>(Arrays.asList(databaseConn.names.toArray()));
            names.add(0, "ALL");
            divisions = new ArrayList<Object>(Arrays.asList(databaseConn.divisions.toArray()));
            divisions.add(0, "ALL");
            CDGs = new ArrayList<Object>(Arrays.asList(databaseConn.CDGs.toArray()));
            CDGs.add(0, "ALL");
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

        Object currentName = null;
        Object currentDivision = null;
        Object currentCDG = null;

        nameList = new JComboBox(names.toArray());
        nameList.setSelectedIndex(0);

        divisionList = new JComboBox(divisions.toArray());
        divisionList.setSelectedIndex(0);

        cdgList = new JComboBox(CDGs.toArray());
        cdgList.setSelectedIndex(0);

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
        importSpreadsheet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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

                    catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    catch (ClassNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        radioButtons.add(importSpreadsheet, BorderLayout.SOUTH);
        add(radioButtons, BorderLayout.NORTH);
        north.add(labelPanel, BorderLayout.CENTER);
        departmentCard.add(scrollPane2, BorderLayout.CENTER);

        costCodeList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox combo = (JComboBox)e.getSource();
                int pk = 0;
                try {
                    for (Object x : ccNames) {
                        if (combo.getSelectedItem().equals(x)) {
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
                departmentTable.getModel().addTableModelListener(new TableModelListener() {
                    @Override
                    public void tableChanged(TableModelEvent e) {
                        if (e.getColumn() == 14) {
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
                            } catch (ClassNotFoundException e1) {
                                e1.printStackTrace();
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
            }
        });

        nameList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selection = nameList.getSelectedItem();
                try {
                    databaseConn.drillTable(departmentTable, selection, DatabaseConn.SortType.Name);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
        });

        divisionList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selection = divisionList.getSelectedItem();
                try {
                    databaseConn.drillTable(departmentTable, selection, DatabaseConn.SortType.Division);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
        });

        cdgList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selection = cdgList.getSelectedItem();
                try {
                    databaseConn.drillTable(departmentTable, selection, DatabaseConn.SortType.CDG);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
        });

        JLabel month = new JLabel("Month");
        JButton previousMonth = new JButton("Previous");
        JButton nextMonth = new JButton("Next");
        previousMonth.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

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
            }
        });

        nextMonth.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
            }
        });
        final JPanel eastPanel = new JPanel();
        eastPanel.add(month);
        eastPanel.add(previousMonth);
        eastPanel.add(nextMonth);

        JLabel department = new JLabel("Department");
        JButton previousDepartment = new JButton("Previous");
        previousDepartment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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

            }
        });
        JButton nextDepartment = new JButton("Next");
        nextDepartment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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

        overview.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(contentPanel, "1");
            }
        });

        departmentView.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(contentPanel, "2");
            }
        });

        departmentTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getColumn() == 14) {
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
                    } catch (ClassNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
        pack();
        getDefaultCloseOperation();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
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
        departmentTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getColumn() == 14) {
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
                    } catch (ClassNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    private String getPeriod (Object o) {
        String current = o.toString();
        String year = current.substring(0, 4);
        String month = current.substring(4, 6);
        String fullYear = "20" + year.substring(0, 2) + "-" + "20" + year.substring(2, 4);
        HashMap<String, String> monthsMap = new HashMap<String, String>();
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

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new UserInterface();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}


