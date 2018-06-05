import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class UserInterface extends JFrame {

    // default values for department view
    private String currentCostCode = "U020";
    private String period = "171801";


    private UserInterface() {

        JLabel month = new JLabel("Month");
        JButton previousMonth = new JButton("Previous");
        JButton nextMonth = new JButton("Next");
        final JPanel westPanel = new JPanel();
        westPanel.add(month);
        westPanel.add(previousMonth);
        westPanel.add(nextMonth);

        JLabel department = new JLabel("Department");
        JButton previousDepartment = new JButton("Previous");
        JButton nextDepartment = new JButton("Next");
        final JPanel eastPanel = new JPanel();
        eastPanel.add(department);
        eastPanel.add(previousDepartment);
        eastPanel.add(nextDepartment);

        final JRadioButton overview = new JRadioButton("Overview", true);
        final JRadioButton departmentView = new JRadioButton("Department View");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(overview);
        buttonGroup.add(departmentView);
        final JPanel radioButtons = new JPanel();
        radioButtons.add(overview);
        radioButtons.add(departmentView);

        setLayout(new BorderLayout());
        final JPanel buttonPanel = new JPanel();
        final BoxLayout boxLayoutButton = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);


        if (overview.isSelected()) {
            System.out.println("Overview view selected");
            getContentPane().removeAll();
            JTable table = null;
            try {
                table = ReadWriteExcelFile.createTable();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            JPanel tableHeader = new JPanel();
            if (table != null) {
                tableHeader.add(table.getTableHeader());
            }

            else {
                System.out.println("Can't get data!");
            }
            add(tableHeader, BorderLayout.CENTER);
            tableHeader.setAutoscrolls(true);
            scrollPane.setPreferredSize(new Dimension(1500, 950));
            tableHeader.add(scrollPane);
            buttonPanel.setLayout(boxLayoutButton);
            buttonPanel.add(westPanel);
            buttonPanel.add(radioButtons);
            buttonPanel.add(eastPanel);
            add(buttonPanel, BorderLayout.NORTH);
            setVisible(true);
            getDefaultCloseOperation();
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            validate();
        }
        overview.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Overview view selected");
                getContentPane().removeAll();
                JTable table = null;
                try {
                    table = ReadWriteExcelFile.createTable();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                JPanel tableHeader = new JPanel();
                if (table != null) {
                    tableHeader.add(table.getTableHeader());
                }

                else {
                    System.out.println("Can't get data!");
                }
                add(tableHeader, BorderLayout.CENTER);
                tableHeader.setAutoscrolls(true);
                scrollPane.setPreferredSize(new Dimension(1500, 950));
                tableHeader.add(scrollPane);
                buttonPanel.setLayout(boxLayoutButton);
                buttonPanel.add(westPanel);
                buttonPanel.add(radioButtons);
                buttonPanel.add(eastPanel);
                add(buttonPanel, BorderLayout.NORTH);
                setVisible(true);
                getDefaultCloseOperation();
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                validate();
            }
        });

        departmentView.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Department view selected");
                getContentPane().removeAll();
                JTable table = ReadWriteExcelFile.createSpecificTable(currentCostCode, period);
                JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                JComboBox costCodeList = new JComboBox(ReadWriteExcelFile.ccNames);
                costCodeList.setSelectedIndex(0);
                costCodeList.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox box = (JComboBox) e.getSource();
                        currentCostCode = (String) box.getSelectedItem();
                        getContentPane().repaint();
                    }
                });
                JPanel listView= new JPanel();
                listView.add(costCodeList);
                add(listView, BorderLayout.EAST);
                JPanel tableHeader = new JPanel();
                tableHeader.add(table.getTableHeader());
                add(tableHeader, BorderLayout.CENTER);
                tableHeader.setAutoscrolls(true);
                scrollPane.setPreferredSize(new Dimension(1500, 950));
                tableHeader.add(scrollPane);
                buttonPanel.setLayout(boxLayoutButton);
                buttonPanel.add(westPanel);
                buttonPanel.add(radioButtons);
                buttonPanel.add(eastPanel);
                add(buttonPanel, BorderLayout.NORTH);
                setVisible(true);
                getDefaultCloseOperation();
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                validate();
            }
        });

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new UserInterface();
            }
        });
    }

}


