import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.sql.SQLException;

public class ResaultPanel extends JPanel {
    private JTable table;
    private String tableName;

    public ResaultPanel(JTable ResaultTable, String tableName) {
        super();
        this.table = ResaultTable;
        this.tableName = tableName;
        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
    public String getTableName() {
        return tableName;
    }
    public JTable getTable() {
        return table;
    }
    public int getColumnCount(JTable table) {
        return table.getColumnCount();
    }
    public String[] getRowData(JTable table, int row) throws SQLException {
        String[] data = new String[table.getColumnCount()];
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getValueAt(row, i) != null) {
                data[i] = table.getValueAt(row, i).toString();
            } else {
                data[i] = "";
            }

        }
        return data;
    }
public String[] getColumnsNames() {
        String[] columnsNames = new String[table.getColumnCount()];
        for (int i = 0; i < table.getColumnCount(); i++) {
            columnsNames[i] = table.getColumnName(i);
        }
        return columnsNames;
    }
public int getRowID(int row){
            CustomTableModel model = (CustomTableModel) table.getModel();
            return  model.getRowID(row);
}
}
