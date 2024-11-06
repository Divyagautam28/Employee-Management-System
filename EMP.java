import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Employee Class
class Employee {
    String id; 
    String name;
    String department;
    double salary;
    String DOJ; 

    public Employee(String id, String name, String department, double salary, String DOJ) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.salary = salary;
        this.DOJ = DOJ;
    }

    public int calculateExperience() {
        String[] parts = DOJ.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        LocalDate joiningDate = LocalDate.of(year, month, 1);
        LocalDate currentDate = LocalDate.now();

        return (currentDate.getYear() - joiningDate.getYear()) * 12 + currentDate.getMonthValue() - joiningDate.getMonthValue();
    }
}

// EmployeeLinkedList Class (modified for SQLite)
class EmployeeLinkedList {
    private Connection connection;

    public EmployeeLinkedList() {
        connect();
        createTable();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:employees.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS employees (" +
                     "Employee_ID TEXT PRIMARY KEY," +
                     "Name TEXT NOT NULL," +
                     "Department TEXT NOT NULL," +
                     "Salary REAL NOT NULL," +
                     "Date_of_Joining TEXT NOT NULL)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertEmployee(String id, String name, String department, double salary, String DOJ) {
        String sql = "INSERT INTO employees (Employee_ID, Name, Department, Salary, Date_of_Joining) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, department);
            pstmt.setDouble(4, salary);
            pstmt.setString(5, DOJ);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Employee findEmployee(String id) {
        String sql = "SELECT * FROM employees WHERE Employee_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Employee(
                    rs.getString("Employee_ID"),
                    rs.getString("Name"),
                    rs.getString("Department"),
                    rs.getDouble("Salary"),
                    rs.getString("Date_of_Joining")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteEmployee(String id, String name) {
        String sql = "DELETE FROM employees WHERE Employee_ID = ? AND Name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            int deletedRows = pstmt.executeUpdate();
            return deletedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employeeList = new ArrayList<>();
        String sql = "SELECT * FROM employees";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                employeeList.add(new Employee(
                    rs.getString("Employee_ID"),
                    rs.getString("Name"),
                    rs.getString("Department"),
                    rs.getDouble("Salary"),
                    rs.getString("Date_of_Joining")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeeList;
    }
}

// EmployeeManagementSystem Class
public class EMP extends Frame implements ActionListener {
    private TextField idField, nameField, salaryField;
    private Choice departmentChoice, monthChoice, yearChoice;
    private EmployeeLinkedList employeeList;

    public EMP() {
        employeeList = new EmployeeLinkedList();
        createMainMenu();
    }

    private void createMainMenu() {
        setTitle("Employee Management System");
        setSize(300, 400);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Create a panel for the title
        Panel titlePanel = new Panel();
        Label titleLabel = new Label("EMPLOYEE MANAGEMENT SYSTEM", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.BLACK);
        titlePanel.add(titleLabel);
        
        // Add title panel to the top of the frame
        add(titlePanel, BorderLayout.NORTH);

        // Create a panel for buttons
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new GridLayout(6, 1, 10, 10));

        Button insertButton = new Button("Insert Employee");
        Button updateButton = new Button("Update Employee");
        Button displayButton = new Button("Display Employees");
        Button deleteButton = new Button("Delete Employee");
        Button searchButton = new Button("Search Employee by ID");
        Button exitButton = new Button("Exit");

        // Set button font
        Font buttonFont = new Font("Comic Sans MS", Font.PLAIN, 14);
        insertButton.setFont(buttonFont);
        updateButton.setFont(buttonFont);
        displayButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);
        searchButton.setFont(buttonFont);
        exitButton.setFont(buttonFont);

        insertButton.addActionListener(this);
        updateButton.addActionListener(this);
        displayButton.addActionListener(this);
        deleteButton.addActionListener(this);
        searchButton.addActionListener(this);
        exitButton.addActionListener(this);

        buttonPanel.add(insertButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(displayButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(exitButton);

        // Add button panel to the center of the frame
        add(buttonPanel, BorderLayout.CENTER);

        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    private void createDetailPage(String action) {
        Frame detailFrame = new Frame(action + " Employee");
        detailFrame.setSize(400, 300);
        detailFrame.setLayout(new GridLayout(8, 2, 10, 10));

        Label idLabel = new Label("ID:");
        idField = new TextField(20);
        Label nameLabel = new Label("Employee Name:");
        nameField = new TextField(20);
        Label departmentLabel = new Label("Department:");
        departmentChoice = new Choice();
        departmentChoice.add("Select Department");
        departmentChoice.add("Manager");
        departmentChoice.add("Developer");
        departmentChoice.add("Designer");
        departmentChoice.add("HR");

        Label salaryLabel = new Label("Salary:");
        salaryField = new TextField(20);
        Label joiningDateLabel = new Label("Joining Date:");
        monthChoice = new Choice();
        monthChoice.add("Select Month");
        monthChoice.add("January");
        monthChoice.add("February");
        monthChoice.add("March");
        monthChoice.add("April");
        monthChoice.add("May");
        monthChoice.add("June");
        monthChoice.add("July");
        monthChoice.add("August");
        monthChoice.add("September");
        monthChoice.add("October");
        monthChoice.add("November");
        monthChoice.add("December");

        yearChoice = new Choice();
        yearChoice.add("Select Year");
        for (int i = 1960; i <= Year.now().getValue(); i++) {
            yearChoice.add(String.valueOf(i));
        }

        // Adding labels for month and year choices
        Label monthGuide = new Label("Month of Joining:");
        Label yearGuide = new Label("Year of Joining:");

        Button submitButton = new Button(action);
        Button backButton = new Button("Back");

        if (action.equals("Delete")) {
            // In delete, we only need ID and name
            detailFrame.remove(departmentLabel);
            detailFrame.remove(departmentChoice);
            detailFrame.remove(salaryLabel);
            detailFrame.remove(salaryField);
            detailFrame.remove(joiningDateLabel);
            detailFrame.remove(monthChoice);
            detailFrame.remove(yearChoice);

            submitButton.addActionListener(e -> {
                String id = idField.getText();
                String name = nameField.getText();
                if (employeeList.deleteEmployee(id, name)) {
                    JOptionPane.showMessageDialog(detailFrame, "Employee deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(detailFrame, "Employee not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                detailFrame.dispose();
            });
        } else {
            submitButton.addActionListener(e -> {
                if (action.equals("Insert")) {
                    insertEmployee(detailFrame);
                } else {
                    updateEmployee(detailFrame);
                }
            });
        }

        detailFrame.add(idLabel);
        detailFrame.add(idField);
        detailFrame.add(nameLabel);
        detailFrame.add(nameField);
        if (!action.equals("Delete")) {
            detailFrame.add(departmentLabel);
            detailFrame.add(departmentChoice);
            detailFrame.add(salaryLabel);
            detailFrame.add(salaryField);
            detailFrame.add(monthGuide);
            detailFrame.add(monthChoice);
            detailFrame.add(yearGuide);
            detailFrame.add(yearChoice);
        }
        detailFrame.add(submitButton);
        detailFrame.add(backButton);

        backButton.addActionListener(e -> detailFrame.dispose());

        detailFrame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "Insert Employee":
                createDetailPage("Insert");
                break;
            case "Update Employee":
                createDetailPage("Update");
                break;
            case "Display Employees":
                displayEmployees();
                break;
            case "Delete Employee":
                createDetailPage("Delete");
                break;
            case "Search Employee by ID":
                String id = JOptionPane.showInputDialog(this, "Enter Employee ID:");
                if (id != null && !id.trim().isEmpty()) {
                    Employee emp = employeeList.findEmployee(id);
                    if (emp != null) {
                        JOptionPane.showMessageDialog(this, "Employee found:\n" +
                            "ID: " + emp.id + "\nName: " + emp.name + 
                            "\nDepartment: " + emp.department + 
                            "\nSalary: " + emp.salary + 
                            "\nJoining Date: " + emp.DOJ, "Employee Details", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Employee not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
            case "Exit":
                System.exit(0);
                break;
        }
    }

    private void insertEmployee(Frame detailFrame) {
        String id = idField.getText();
        String name = nameField.getText();
        String department = departmentChoice.getSelectedItem();
        String salaryText = salaryField.getText();
        String month = String.valueOf(monthChoice.getSelectedIndex());
        String year = yearChoice.getSelectedItem();

        boolean valid = true; // Track if the inputs are valid

        if (department.equals("Select Department")) {
            JOptionPane.showMessageDialog(detailFrame, "Please select a department.", "Error", JOptionPane.ERROR_MESSAGE);
            valid = false;
        }
        if (salaryText.isEmpty()) {
            JOptionPane.showMessageDialog(detailFrame, "Please enter a salary.", "Error", JOptionPane.ERROR_MESSAGE);
            valid = false;
        }
        if (month.equals("0")) {
            JOptionPane.showMessageDialog(detailFrame, "Please select a month.", "Error", JOptionPane.ERROR_MESSAGE);
            valid = false;
        }
        if (year.equals("Select Year")) {
            JOptionPane.showMessageDialog(detailFrame, "Please select a year.", "Error", JOptionPane.ERROR_MESSAGE);
            valid = false;
        }

        if (valid) {
            try {
                double salary = Double.parseDouble(salaryText);
                String DOJ = year + "-" + String.format("%02d", Integer.parseInt(month));
                employeeList.insertEmployee(id, name, department, salary, DOJ);
                JOptionPane.showMessageDialog(detailFrame, "Employee inserted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields(); // Clear fields only after success
                detailFrame.dispose(); // Close the detail frame
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(detailFrame, "Invalid salary format. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateEmployee(Frame detailFrame) {
        String id = idField.getText();
        String name = nameField.getText();
        String department = departmentChoice.getSelectedItem();
        String salaryText = salaryField.getText();
    
        boolean valid = true; // Track if the inputs are valid
    
        if (department.equals("Select Department")) {
            JOptionPane.showMessageDialog(detailFrame, "Please select a department.", "Error", JOptionPane.ERROR_MESSAGE);
            valid = false;
        }
        if (salaryText.isEmpty()) {
            JOptionPane.showMessageDialog(detailFrame, "Please enter a salary.", "Error", JOptionPane.ERROR_MESSAGE);
            valid = false;
        }
    
        if (valid) {
            try {
                double salary = Double.parseDouble(salaryText);
                Employee emp = employeeList.findEmployee(id); // Find employee by ID
    
                if (emp != null) {
                    if (emp.name.equals(name)) { // Check if the name matches the employee's name
                        // Proceed with the update
                        emp.name = name;
                        emp.department = department;
                        emp.salary = salary;
                        String DOJ = emp.DOJ; // Keep existing DOJ
    
                        // Remove old entry and insert updated entry
                        employeeList.deleteEmployee(id, emp.name); 
                        employeeList.insertEmployee(id, name, department, salary, DOJ); 
    
                        JOptionPane.showMessageDialog(detailFrame, "Employee updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearFields(); // Clear fields only after success
                        detailFrame.dispose(); // Close the detail frame
                    } else {
                        // Show error if the name doesn't match the existing one
                        JOptionPane.showMessageDialog(detailFrame, "Employee name doesn't match the provided ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Show error if employee with the given ID doesn't exist
                    JOptionPane.showMessageDialog(detailFrame, "Employee with the given ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(detailFrame, "Invalid salary format. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    private void displayEmployees() {
        List<Employee> employees = employeeList.getAllEmployees();
        
        // Sort employees by experience
        employees.sort(Comparator.comparingInt(Employee::calculateExperience).reversed());

        // Create column names
        String[] columnNames = {"No", "ID", "Name", "Department", "Salary"};

        // Prepare data for the table
        Object[][] data = new Object[employees.size()][columnNames.length];
        for (int i = 0; i < employees.size(); i++) {
            Employee emp = employees.get(i);
            data[i][0] = i + 1; // Serial Number
            data[i][1] = emp.id;
            data[i][2] = emp.name;
            data[i][3] = emp.department;
            data[i][4] = emp.salary;
        }

        // Create a JTable
        JTable table = new JTable(data, columnNames);
        table.setFillsViewportHeight(true);

        // Create a JScrollPane for the table
        JScrollPane scrollPane = new JScrollPane(table);

        // Create a frame for displaying the table
        JFrame frame = new JFrame("Employee List");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        salaryField.setText("");
        monthChoice.select(0);
        yearChoice.select(0);
        departmentChoice.select(0); // Reset department choice
    }

    public static void main(String[] args) {
        new EMP();
    }
}