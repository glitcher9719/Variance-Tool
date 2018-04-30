import javafx.scene.control.ScrollPane;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class UserInterface extends JFrame {

    public UserInterface() throws IOException {

        JLabel month = new JLabel("Month");
        JButton previousMonth = new JButton("Previous");
        JButton nextMonth = new JButton("Next");
        JPanel westPanel = new JPanel();
        westPanel.add(month);
        westPanel.add(previousMonth);
        westPanel.add(nextMonth);

        JLabel department = new JLabel("Department");
        JButton previousDepartment = new JButton("Previous");
        JButton nextDepartment = new JButton("Next");
        JPanel eastPanel = new JPanel();
        eastPanel.add(department);
        eastPanel.add(previousDepartment);
        eastPanel.add(nextDepartment);
        setLayout(new BorderLayout());
        JTable table = ReadWriteExcelFile.readXLSXFile();
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel buttonPanel = new JPanel();
        BoxLayout boxLayoutButton = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
        buttonPanel.setLayout(boxLayoutButton);
        buttonPanel.add(westPanel);
        buttonPanel.add(eastPanel);
        JPanel tableHeader = new JPanel();
        tableHeader.add(table.getTableHeader());
        scrollPane.setPreferredSize(new Dimension(800,600));
        tableHeader.add(scrollPane);
        tableHeader.setAutoscrolls(true);

        add(buttonPanel, BorderLayout.NORTH);
        add(tableHeader, BorderLayout.CENTER);

        setVisible(true);
        getDefaultCloseOperation();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public static void main(String[] args) throws IOException {
        new UserInterface();

    }

}


