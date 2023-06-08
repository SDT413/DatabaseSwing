import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
    private SearchPanel searchPanel;
    private ResaultPanel resaultPanel;
    public MainPanel(SearchPanel searchPanel, ResaultPanel resaultPanel) {
        super();
        setLayout(new BorderLayout());
        this.searchPanel = searchPanel;
        this.resaultPanel = resaultPanel;
        add(searchPanel, BorderLayout.NORTH);
        add(resaultPanel, BorderLayout.CENTER);
    }
public SearchPanel getSearchPanel() {
       return searchPanel;
    }
    public ResaultPanel getResaultPanel() {
        return resaultPanel;
}


}
