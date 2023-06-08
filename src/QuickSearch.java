import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;


public class QuickSearch extends JFrame {

    private final JTextField[] textFields;
    private ArrayList<JButton> deleteButtons;
    private ArrayList<JButton> addButtons;
    private ArrayList<JComboBox> Variants;
    private final Core core;
    private final String tableName;
    String[] columnsNames;
    int columns_quantity;
    int selectedRow;
    int rowID;
    JPanel ColumnsPanel;
    JPanel TextAreaPanel;
    JPanel ButtonsPanel;
    JPanel AdditionalButtonsPanel;
    ArrayList<JPanel> MainPanels;
    JButton Save;
    ArrayList<MainPanel> RelationShipPanels;
    JTextArea textArea;
    JTabbedPane InfoTabbedPane;
    JTabbedPane RelationShipTabbedPane;

    MainPanel recievedPanel;
    private JTabbedPane tabbedPane1;
    private JTabbedPane tabbedPane2;

    public QuickSearch(Core core, MainPanel panel, int row, int rowID) throws SQLException {
         this.core = core;
         tableName = panel.getResaultPanel().getTableName();
         selectedRow = row;
         this.rowID = rowID;
         recievedPanel = panel;
         addButtons = new ArrayList<>();
         deleteButtons = new ArrayList<>();
         Variants = new ArrayList<>();
         MainPanels = new ArrayList<>();
         RelationShipPanels = new ArrayList<>();
         columnsNames = recievedPanel.getResaultPanel().getColumnsNames();
         columns_quantity = recievedPanel.getSearchPanel().getColumnsCount();
         textFields = new JTextField[columns_quantity];
         InfoTabbedPane = new JTabbedPane();
         RelationShipTabbedPane = new JTabbedPane();
         start();
         pack();
         setLocationRelativeTo(null);
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         setVisible(true);
    }
    public void start() throws SQLException {
        JPanel MainPanel = new JPanel();
        MainPanels.add(MainPanel);
        MainPanel.setLayout(new BorderLayout());
        SetColumnsPanel();
        MainPanel.add(InfoTabbedPane, BorderLayout.CENTER);
        SetRelationShip();
        MainPanel.add(RelationShipTabbedPane, BorderLayout.SOUTH);
        System.out.println(RelationShipPanels.size());
        add(MainPanel, BorderLayout.CENTER);
    }
 public void SetColumnsPanel() throws SQLException {
        ColumnsPanel = new JPanel();
        ColumnsPanel.setLayout(new GridLayout(columns_quantity, 5));
        for (int i = 0; i < columns_quantity; i++) {
                ColumnsPanel.add(new JLabel(columnsNames[i]+":"));
                textFields[i] = new JTextField();
                ColumnsPanel.add(textFields[i]);
                addButtons.add(new JButton("+"));
                ColumnsPanel.add(addButtons.get(i));
                deleteButtons.add(new JButton("-"));
                ColumnsPanel.add(deleteButtons.get(i));
                Variants.add(new JComboBox());
                ColumnsPanel.add(Variants.get(i));
        }
     InfoTabbedPane.addTab("Основне", ColumnsPanel);
     SetDropdownContent();
     SetActionsforVariants();
     SetPlusButtons();
     SetMinusButtons();
 }
public void SetDropdownContent() throws SQLException {
    System.out.println(Variants.size());
    for (int i = 0; i < Variants.size(); i++) {
       String[] data = core.sql.SeparateJSONColumnToArray(tableName, columnsNames[i], rowID);
        for (String variant : data) {
            Variants.get(i).addItem(variant);
            textFields[i].setText(variant);
        }
    }
    }
 public void SetActionsforVariants() {
        for (int i = 0; i < Variants.size(); i++) {
                int finalI = i;
                Variants.get(i).addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (Variants.get(finalI).getSelectedItem() != null) {
                            textFields[finalI].setText(Variants.get(finalI).getSelectedItem().toString());
                        }
                        else {
                            textFields[finalI].setText("");
                        }
                    }
                });
        }
    }
    public void SetPlusButtons() {
        for (int i = 0; i < addButtons.size(); i++) {
            int finalI = i;
            addButtons.get(i).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        core.sql.AddToJSON(tableName, columnsNames[finalI], rowID, textFields[finalI].getText());
                        Variants.get(finalI).addItem(textFields[finalI].getText());
                        Variants.get(finalI).setSelectedItem(textFields[finalI].getText());
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
    }
    public void SetMinusButtons() {
        for (int i = 0; i < deleteButtons.size(); i++) {
            int finalI = i;
            deleteButtons.get(i).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        core.sql.RemoveFromJSON(tableName, columnsNames[finalI], rowID,Variants.get(finalI).getSelectedIndex());
                        Variants.get(finalI).removeItem(textFields[finalI].getText());
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                }
            });
        }
    }
  public void SetRelationShip() throws SQLException {
        String[] tables = core.sql.getTableNames();
      for (int i = 0; i < tables.length; i++) {
          if (tables[i].equals("sqlite_sequence") || tables[i].equals("RelationsTable")) {
              continue;
          }
          System.out.println(tables[i]);
          String tableName = tables[i];
          InitTable(tableName);
      }
      }

    public void InitTable(String tableName) throws SQLException {
        ResultSet tableData = core.sql.CreateResultSet(core.sql.getUseableQuery(tableName));
        int columnCount = tableData.getMetaData().getColumnCount();

        CustomTableModel model = new CustomTableModel();
        JTable table = new JTable(model);


        table.setRowHeight(24);

        model.setColumnCount(0);
        model.setRowCount(0);


        setTableColumns(model, tableData, columnCount);

        SearchPanel searchPanel =  InitSearchPanel(new JLabel("Зв'язок"));
        ResaultPanel resultPanel =  InitResaultPanel(table, tableName);
        MainPanel mainPanel = new MainPanel(searchPanel, resultPanel);

        RelationShipPanels.add(mainPanel);
        RelationShipTabbedPane.add(mainPanel, tableName);
        FillTable(model);
        tableData.close();

    }
    public void FillTable(CustomTableModel customTableModel) throws SQLException {
        ResultSet Relations = core.sql.CreateResultSet("Select Rid, RTableName FROM RelationsTable WHERE id = " +rowID);
        ArrayList<String> Rid = new ArrayList<>();
        ArrayList<String> RTableName = new ArrayList<>();
        while (Relations.next()){
            Rid.add(Relations.getString(1));
            RTableName.add(Relations.getString(2));
        }
        ResultSet rs = core.sql.CreateResultSet("Select json_extract() FROM "+RTableName.get(0)+" WHERE id = "+ Rid.get(0));
        core.setTableRows(customTableModel, rs, rs.getMetaData().getColumnCount());
    }
    public void setTableColumns(DefaultTableModel model, ResultSet tableData, int columnCount) throws SQLException {
        for (int i = 1; i <= columnCount; i++) {
            model.addColumn(tableData.getMetaData().getColumnName(i));
        }
    }
    public SearchPanel InitSearchPanel(JLabel labels) throws SQLException {
        SearchPanel searchPanel = new SearchPanel(new JLabel[]{labels}, 1);
        RelationShipTabbedPane.add(searchPanel, BorderLayout.NORTH);
        return searchPanel;
    }
    public ResaultPanel InitResaultPanel(JTable table, String tableName) throws SQLException {
        ResaultPanel resultPanel  = new ResaultPanel(table, tableName);
        RelationShipTabbedPane.add(resultPanel, BorderLayout.CENTER);
        return resultPanel;
    }

  public void setAdditionalSave() {
      Save.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              try {
                  String query = "UPDATE "+tableName+" SET Инфо ='"+textArea.getText()+"' WHERE id = "+rowID+";";
                  core.sql.SDUpdate(query);
                  core.ReloadMainPanel(recievedPanel);
              } catch (SQLException ex) {
                  ex.printStackTrace();
              }
          }
      });
  }
}
/*
  labelPanel.setLayout(new GridLayoutManager(columns_quantity, 2, new Insets(0, 0, 0, 0), -1, -1));
      for (int i = 0; i < columns_quantity; i++) {
          if (!Objects.equals(columnsNames[i], "Инфо")){
              labelPanel.add(new JLabel(columnsNames[i]));
              textFields[i] = new JTextField();
              textFields[i].setPreferredSize(new Dimension(150, 30));
              textFields[i].setText((String) recievedPanel.getResaultPanel().getTable().getValueAt(selectedRow, i));
              labelPanel.add(textFields[i]);
          }
          else {
              setAdditionalTab(i);
          }
      }
     setSave();
 */
