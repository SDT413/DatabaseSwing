import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SearchPanel extends JPanel {
    private ArrayList<JTextField> textFields;
    private ArrayList<JLabel> columnsNames;
    private int columns_quantity;


    public SearchPanel(JLabel[] columnsNames,  int columns_quantity) {
        super();
        this.columns_quantity = columns_quantity;
        this.columnsNames = new ArrayList<>();
        textFields = new ArrayList<>();
        setLayout(new GridLayout(columns_quantity, 2));
        for (int i = 0; i < columns_quantity; i++) {
            textFields.add(new JTextField());
            this.columnsNames.add(columnsNames[i]);
            add(this.columnsNames.get(i),":");
            add(textFields.get(i));
        }
    }

    private void add(JLabel c,String s) {
        JLabel v = new JLabel(c.getText() + s);
        super.add(v);
    }

    public ArrayList<JTextField> getTextFields() {
        return textFields;
    }
    public int getColumnsCount() {
        return columns_quantity;
    }
    public void clear() {
        for (int i = 0; i < columns_quantity; i++) {
            textFields.get(i).setText("");
        }
    }
    public String[] getData() {
        String[] data = new String[columns_quantity];
        for (int i = 0; i < columns_quantity; i++) {
            if (textFields.get(i).getText() != null) {
                data[i] = textFields.get(i).getText();
            } else {
                data[i] = "";
            }
        }
        return data;
    }
public String getValueAt(int column) {
        return textFields.get(column).getText();
    }
    public void setValueAt(int column, String value) {
        textFields.get(column).setText(value);
    }
    public String[] getColumnsNames() {
        String[] columnsNames = new String[columns_quantity];
        for (int i = 0; i < columns_quantity; i++) {
            columnsNames[i] = this.columnsNames.get(i).getText();
        }
        return columnsNames;
    }
    public void addColumn(String columnName) {
        JTextField textField = new JTextField();
        JLabel label = new JLabel(columnName);
        columnsNames.add(label);
        textFields.add(textField);
        columns_quantity++;
        add(label);
        add(textField);
    }

}
