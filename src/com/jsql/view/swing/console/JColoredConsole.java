package com.jsql.view.swing.console;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;

import org.apache.log4j.Logger;

import com.jsql.view.swing.MediatorGUI;

/**
 * A JTextPane which displays colored strings.
 */
@SuppressWarnings("serial")
public class JColoredConsole extends JTextPane {
    /**
     * Text name of tab.
     */
    private String tabName;

    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getLogger(JColoredConsole.class);

    /**
     * Create a JTextPane which displays colored strings.
     * @param newTabName Text name of tab
     */
    public JColoredConsole(final String newTabName) {
        super();
        this.tabName = newTabName;
        
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent arg0) {
                JColoredConsole.this.getCaret().setVisible(true);
                JColoredConsole.this.getCaret().setSelectionVisible(true);
            }
        });
        // this.setAutoscrolls(true);    // does not work

        this.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    /**
     * Add a string to the end of JTextPane.
     * @param message Text to add
     * @param attribut Font
     */
    public void append(String message, SimpleAttributeSet attribut) {
        try {
            this.getDocument().insertString(
                this.getDocument().getLength(),
                (this.getDocument().getLength() == 0 ? "" : "\n") + message,
                attribut
            );

            int tabIndex = MediatorGUI.bottom().indexOfTab(tabName);
            Component tabHeader
                    = MediatorGUI.bottom().getTabComponentAt(tabIndex);
            if (MediatorGUI.bottom().getSelectedIndex() != tabIndex) {
                tabHeader.setFont(tabHeader.getFont().deriveFont(Font.BOLD));
            }
        } catch (BadLocationException e) {
            LOGGER.fatal(message);
        }
    }
}
