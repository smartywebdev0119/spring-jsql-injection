package com.jsql.view.swing.tab;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JTabbedPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mousewheel allows to navigate to next/previous tab.
 */
public class TabbedPaneMouseWheelListener implements MouseWheelListener {
    
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = LogManager.getRootLogger();
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        
        JTabbedPane tabPane = (JTabbedPane) e.getSource();

        int dir = -e.getWheelRotation();
        int selIndex = tabPane.getSelectedIndex();
        int maxIndex = tabPane.getTabCount() - 1;
        
        if ((selIndex == 0 && dir < 0) || (selIndex == maxIndex && dir > 0)) {
            
            selIndex = maxIndex - selIndex;
            
        } else {
            
            selIndex += dir;
        }
        
        if (0 <= selIndex && selIndex < tabPane.getTabCount()) {
            
            // Fix #54575: NullPointerException on setSelectedIndex()
            // Fix #90835: IllegalArgumentException on setSelectedIndex()
            try {
                tabPane.setSelectedIndex(selIndex);
                
            } catch (IllegalArgumentException | NullPointerException err) {
                
                LOGGER.error(err, err);
            }
        }
    }
}