import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class UserInterface extends JFrame {

    // default values for department view
    private JTable departmentTable;
    private JScrollPane scrollPane2;
    private CardLayout cardLayout = new CardLayout();
    private JLabel ccLabel;
    private JLabel periodLabel;
    private JTable table;
    private JPanel departmentCard;
    private JComboBox costCodeList;

    {
        try {
            table = DataImport.createTable();
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Object[] ccNames = DataImport.ccNames.toArray();
    private Object[] periodNames = DataImport.periodNames.toArray();
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
        ccLabel.setText(currentCostCode.toString());
        periodLabel.setText(period.toString());
        departmentCard.remove(scrollPane2);
        departmentTable = DataImport.createSpecificTable(currentCostCode, period);
        departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollPane2 = new JScrollPane(departmentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane2.setPreferredSize(new Dimension(1900, 950));
        departmentCard.add(scrollPane2, BorderLayout.CENTER);
        ccLabel.setText(currentCostCode.toString());
    }

    private UserInterface() {

        contentPanel = new JPanel(cardLayout);
        JPanel overviewCard = new JPanel(new BorderLayout());
        departmentCard = new JPanel(new BorderLayout());
        JPanel north = new JPanel();

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
        JPanel centerNorth = new JPanel();
        centerNorth.add(radioButtons);

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

        departmentTable = DataImport.createSpecificTable(currentCostCode, period);
        departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollPane2 = new JScrollPane(departmentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        costCodeList = new JComboBox(DataImport.ccNames.toArray());
        costCodeList.setSelectedIndex(0);

        JPanel listView= new JPanel();
        listView.add(costCodeList);
        north.add(listView);

        add(departmentTable.getTableHeader());
        scrollPane2.setPreferredSize(new Dimension(1900, 950));
        departmentCard.add(scrollPane2, BorderLayout.CENTER);
        JPanel label = new JPanel();
        ccLabel = new JLabel(currentCostCode.toString());
        periodLabel = new JLabel(period.toString());
        label.add(ccLabel);
        label.add(periodLabel);
        add(radioButtons, BorderLayout.NORTH);
        north.add(label);
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
                departmentTable = DataImport.createSpecificTable(currentCostCode, period);
                departmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                scrollPane2 = new JScrollPane(departmentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollPane2.setPreferredSize(new Dimension(1900, 950));
                departmentCard.add(scrollPane2, BorderLayout.CENTER);
                ccLabel.setText(currentCostCode.toString());
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
        final JPanel westPanel = new JPanel();
        westPanel.add(month);
        westPanel.add(previousMonth);
        westPanel.add(nextMonth);

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
        final JPanel eastPanel = new JPanel();
        eastPanel.add(department);
        eastPanel.add(previousDepartment);
        eastPanel.add(nextDepartment);
        north.add(eastPanel);
        north.add(westPanel);
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


