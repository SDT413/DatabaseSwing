import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

class CustomTableModel extends DefaultTableModel {
    ArrayList<Object> rowIDs = new ArrayList<>();

    public void addRow(Object[] rowData, Object rowID) {
        super.addRow(rowData);
        rowIDs.add(rowID);
    }
    public int getRowID(int row) {
       return (int) rowIDs.get(row);
    }
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

}