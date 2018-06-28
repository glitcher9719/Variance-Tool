import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class UserInterface extends JFrame {

    // default values for department view
    private JTable departmentTable;
    private JScrollPane scrollPane2;
    private CardLayout cardLayout = new CardLayout();
    private JLabel ccLabel;
    private JLabel periodLabel;
    private JLabel descriptionLabel;
    private JTable table;
    private JPanel departmentCard;
    private JComboBox costCodeList;

    {
        try {
            table = DatabaseConn.generateDataFromDB();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    }

    private Object[] ccNames = DatabaseConn.ccNames.toArray();
    private Object[] periodNames = DatabaseConn.periodNames.toArray();
    private int ccCounter = 0;
    private int pCounter = 0;
    private Object currentCostCode = ccNames[ccCounter];
    private Object period = periodNames[pCounter];

    private JPanel contentPanel;

    private void tableRenew() {

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
        descriptionLabel.setText("Description: " + DatabaseConn.name);
        periodLabel.setText("Month: " + period.toString());
        departmentCard.remove(scrollPane2);
        departmentTable = DatabaseConn.createSpecificTable(currentCostCode, period);
        resizeColumnWidth(departmentTable);
        departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        scrollPane2 = new JScrollPane(departmentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane2.setPreferredSize(new Dimension(1900, 950));
        departmentCard.add(scrollPane2, BorderLayout.CENTER);
    }

    private void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 15; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width +1 , width);
            }
            if(width > 300)
                width=300;
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    private UserInterface() {

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

        departmentTable = DatabaseConn.createSpecificTable(currentCostCode, period);
        resizeColumnWidth(departmentTable);
        departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        scrollPane2 = new JScrollPane(departmentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        costCodeList = new JComboBox(DatabaseConn.ccNames.toArray());
        costCodeList.setSelectedIndex(0);

        JPanel listView= new JPanel();
        listView.add(costCodeList);

        add(departmentTable.getTableHeader());
        scrollPane2.setPreferredSize(new Dimension(1900, 950));
        departmentCard.add(scrollPane2, BorderLayout.CENTER);
        JPanel label = new JPanel();
        label.setLayout(new BoxLayout(label, BoxLayout.Y_AXIS));
        ccLabel = new JLabel("Cost code: " + currentCostCode.toString());
        descriptionLabel = new JLabel("Description: " + DatabaseConn.name);
        periodLabel = new JLabel("Month: " + period.toString());
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
                        DatabaseConn.importSpreadsheet(filePath);
                    }

                    catch (IOException e1) {
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
                departmentTable = DatabaseConn.createSpecificTable(currentCostCode, period);
                resizeColumnWidth(departmentTable);
                departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                scrollPane2 = new JScrollPane(departmentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollPane2.setPreferredSize(new Dimension(1900, 950));
                departmentCard.add(scrollPane2, BorderLayout.CENTER);
                ccLabel.setText("Cost code: " + currentCostCode.toString());
                descriptionLabel.setText("Description: " + DatabaseConn.name);
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
                    tableRenew();
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
                    tableRenew();
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
                        tableRenew();
                    }

            }
        });
        JButton nextDepartment = new JButton("Next");
        nextDepartment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    ccCounter++;
                    tableRenew();
                }

                catch (ArrayIndexOutOfBoundsException error) {
                    ccCounter = 0;
                    tableRenew();
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

        setLocationRelativeTo(null);
        setVisible(true);
        pack();
        getDefaultCloseOperation();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new UserInterface();
            }
        });
    }

}


