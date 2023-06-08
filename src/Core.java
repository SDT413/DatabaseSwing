import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

class Core extends JFrame {
    SQL_Connection sql;
    JTabbedPane tabs;
    JMenuBar menuBar;
    JMenu TableMenu;
    ArrayList<MainPanel> ForcePanels;
    public Core() throws SQLException {
        super("JTable Example");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        sql = new SQL_Connection();
        ForcePanels = new ArrayList<>();
        menuBar = new JMenuBar();
        TableMenu = new JMenu("Редагування таблиць");
        tabs = new JTabbedPane();
        start();
        AddTable();
        DeleteTable();
        InsertRow();
        InsertColumn();
        menuBar.add(TableMenu);
        setJMenuBar(menuBar);

    }

    public String doQueryID() {
        return "id INTEGER PRIMARY KEY AUTOINCREMENT, ";
    }
    public String doQueryInfo(){ return "Инфо TEXT";}

    public void setTableColumns(DefaultTableModel model, ResultSet tableData, int columnCount) throws SQLException {
        for (int i = 2; i <= columnCount; i++) {
            model.addColumn(tableData.getMetaData().getColumnName(i));
        }
    }
    public void setTableRows(CustomTableModel model, ResultSet tableData, int columnCount) throws SQLException {
        while (tableData.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 2; i <= columnCount; i++) {
                row[i - 2] = tableData.getString(i);
            }
            model.addRow(row, tableData.getInt(1));
        }
    }
    public void ResetTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
    }
    public void ReloadMainPanel(MainPanel mainPanel) throws SQLException {
        String tableName = mainPanel.getResaultPanel().getTableName();
        ResultSet tableData = sql.CreateResultSet(sql.getUseableQuery(tableName).replace("SELECT ", "SELECT id, "));
        int columnCount = tableData.getMetaData().getColumnCount();

        CustomTableModel model = new CustomTableModel();
        JTable table = new JTable(model);


        table.setRowHeight(24);

        model.setColumnCount(0);
        model.setRowCount(0);


        setTableColumns(model, tableData, columnCount);

        SearchPanel searchPanel =  InitSearchPanel(InitLabels(columnCount, tableData), columnCount);
        ResaultPanel resultPanel =  InitResaultPanel(table, tableName);
        mainPanel = new MainPanel(searchPanel, resultPanel);
        ForcePanels.remove(tabs.getSelectedIndex());
        ForcePanels.add(tabs.getSelectedIndex(), mainPanel);
        tabs.setComponentAt(tabs.getSelectedIndex(), mainPanel);
        tableData.close();
        addTableMouseListener(table);
        AddChangeListener(mainPanel);
    }
  public void addTableMouseListener(JTable table){
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //if (e.getClickCount() == 2) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int IndexofPanel = tabs.getSelectedIndex();
                    CustomTableModel cs =(CustomTableModel) ForcePanels.get(IndexofPanel).getResaultPanel().getTable().getModel();
                    int row = table.getSelectedRow();
                    int rowID = cs.getRowID(row);
                    System.out.println(rowID);
                    try {
                         new QuickSearch(Core.this, ForcePanels.get(IndexofPanel), row, rowID);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
  }

public void AddChangeListener(MainPanel panel) {
    SearchPanel searchPanel = panel.getSearchPanel();
    ResaultPanel resaultPanel = panel.getResaultPanel();
       ArrayList<JTextField> textFields = searchPanel.getTextFields();
       for (int i = 0; i < textFields.size(); i++) {
          textFields.get(i).addKeyListener(new KeyAdapter() {
              @Override
              public void keyReleased(KeyEvent e) {
                  try {
                      ResetTable(resaultPanel.getTable());
                      ResultSet rs = sql.Search(resaultPanel.getTableName(), searchPanel.getColumnsNames(), searchPanel.getData());
                      setTableRows((CustomTableModel) resaultPanel.getTable().getModel(), rs, rs.getMetaData().getColumnCount());
                  } catch (SQLException ex) {
                      throw new RuntimeException(ex);
                  }
              }
          });
       }
}
    public SearchPanel InitSearchPanel(JLabel[]  labels, int column_count) throws SQLException {
        SearchPanel searchPanel = new SearchPanel(labels, column_count-1);
        tabs.add(searchPanel, BorderLayout.NORTH);
        return searchPanel;
    }
    public ResaultPanel InitResaultPanel(JTable table, String tableName) throws SQLException {
        ResaultPanel resultPanel  = new ResaultPanel(table, tableName);
        tabs.add(resultPanel, BorderLayout.CENTER);
        return resultPanel;
    }

    public void InitTable(String tableName) throws SQLException {
        ResultSet tableData = sql.CreateResultSet(sql.getUseableQuery(tableName).replace("SELECT ", "SELECT id, "));
        int columnCount = tableData.getMetaData().getColumnCount();

        CustomTableModel model = new CustomTableModel();
        JTable table = new JTable(model);


        table.setRowHeight(24);

        model.setColumnCount(0);
        model.setRowCount(0);


        setTableColumns(model, tableData, columnCount);

        SearchPanel searchPanel =  InitSearchPanel(InitLabels(columnCount, tableData), columnCount);
        ResaultPanel resultPanel =  InitResaultPanel(table, tableName);
        MainPanel mainPanel = new MainPanel(searchPanel, resultPanel);

        ForcePanels.add(mainPanel);
        tabs.add(mainPanel, tableName);

        tableData.close();
        addTableMouseListener(table);
        AddChangeListener(mainPanel);
    }
   public JLabel[] InitLabels(int column_count, ResultSet tableData) throws SQLException {
        JLabel[] labels = new JLabel[column_count-1];
        for (int i = 2; i <= column_count; i++) {
            labels[i-2] = new JLabel();
            labels[i-2].setText(tableData.getMetaData().getColumnName(i));
        }
        return labels;
    }
    public void start() throws SQLException {
        while (sql.getTablesSet().next()) {
            if (sql.getTablesSet().getString(1).equals("sqlite_sequence") || sql.getTablesSet().getString(1).equals("RelationsTable")) {
                continue;
            }
            String tableName = sql.getTablesSet().getString(1);
            InitTable(tableName);
        }
        add(tabs);
    }

    public void AddTable() {
        JMenuItem menuItem = new JMenuItem("Нова таблиця");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tableName = JOptionPane.showInputDialog("Введіть назву таблиці:", "");
                String columns = JOptionPane.showInputDialog("Введіть назву стовпців:", "");
                if ((tableName == null || columns == null) || (tableName.equals("") || columns.equals(""))) {
                    tableName = "";
                    columns = "";
                    return;
                }
                while (columns.contains(", ")) {
                    columns = columns.replace(", ", " varchar(255),");
                }
                columns += " varchar(255), ";
                System.out.println("CREATE TABLE " + tableName + " (" + doQueryID() + columns+ doQueryInfo() + ")");
                try {
                    sql.SDUpdate("CREATE TABLE " + tableName + " (" + doQueryID() + columns+ doQueryInfo() + ")");
                    InitTable(tableName);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

            }
        });
        TableMenu.add(menuItem);
    }

    public void DeleteTable() {
        JMenuItem deleteTableItem = new JMenuItem("Видалити таблицю");
        deleteTableItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tableName = JOptionPane.showInputDialog("Введіть назву таблиці:", "");
                if (tableName == null || tableName.equals("")) {
                    tableName = "";
                    return;
                }
                try {
                    sql.SDUpdate("DROP TABLE " + tableName);
                    ForcePanels.remove(tabs.indexOfTab(tableName));
                    tabs.remove(tabs.indexOfTab(tableName));
                    JOptionPane.showMessageDialog(Core.this, "Таблиця '" + tableName + "' Видалена!");
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        TableMenu.add(deleteTableItem);
    }

    public void InsertRow() {
        JMenuItem saveItem = new JMenuItem("Добавити рядок");
        saveItem.setMnemonic(KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SearchPanel searchPanel = ForcePanels.get(tabs.getSelectedIndex()).getSearchPanel();
                ResaultPanel resaultPanel = ForcePanels.get(tabs.getSelectedIndex()).getResaultPanel();
                String tableName = resaultPanel.getTableName();
                JTable table = resaultPanel.getTable();
                StringBuilder columnNames = new StringBuilder();
                int columnCount = resaultPanel.getColumnCount(table);
                int count = 0;
                for (int i = 0; i < columnCount; i++) {
                    if (i != 0) {
                        columnNames.append(", ");
                    }
                        columnNames.append(table.getColumnName(i));
                }
                StringBuilder values = new StringBuilder();
                for (int i = 0; i < columnCount; i++) {
                    if (i != 0) {
                        values.append(", ");
                    }
                        values.append("'[\"").append(searchPanel.getValueAt(i)).append("\"]'");
                }
                for (String value :values.toString().split(",")) {
                    if (!value.equals(" '[\"\"]'") && !value.equals(" '[]'") && !value.equals(" ''") && !value.equals("'[\"\"]'")) {
                        count++;
                        System.out.println("passed value:"+value);
                    }
                }
                if (columnCount == 0) {
                    return;
                } else if (count == 0) {
                    return;
                }
                System.out.println(count);
                try {
                    System.out.println("INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + values + ")");
                    sql.SDUpdate("INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + values + ")");
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        TableMenu.add(saveItem);
    }
    public void InsertColumn() {
        JMenuItem saveColumn = new JMenuItem("Добавити стовпець");
        saveColumn.setMnemonic(KeyEvent.VK_S);
        saveColumn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK));
        saveColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tableName = tabs.getTitleAt(tabs.getSelectedIndex());
                String columnName = JOptionPane.showInputDialog("Введіть назву стовпця:", "");
                if ((columnName == null) || (columnName.equals(""))) {
                    columnName = "";
                    return;
                }
                try {
                    if (columnName.equals("Инфо") || columnName.equals("id") || columnName.equals("Медіа") || columnName.equals("Документи"))
                    {
                        JOptionPane.showMessageDialog(Core.this, "Неможливо додати стовпець з такою назвою!");
                    }
                    else
                    {
                        System.out.println("ALTER TABLE  " + tableName + " ADD COLUMN " + columnName + " text");
                    sql.SDUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " varchar(255)");
                    ReloadMainPanel(ForcePanels.get(tabs.getSelectedIndex()));
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        TableMenu.add(saveColumn);
    }
}