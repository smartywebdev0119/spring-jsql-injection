/*******************************************************************************
 * Copyhacked (H) 2012-2014.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.view.manager;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import com.jsql.exception.PreparationException;
import com.jsql.exception.StoppableException;
import com.jsql.i18n.I18n;
import com.jsql.model.InjectionModel;
import com.jsql.view.ToolsGUI;
import com.jsql.view.list.dnd.DnDList;
import com.jsql.view.scrollpane.JScrollPanePixelBorder;
import com.jsql.view.textcomponent.JPopupTextField;

/**
 * Manager for uploading PHP webshell to the host and send system commands.
 */
@SuppressWarnings("serial")
public class ManagerWebshell extends ManagerAbstractList {
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getLogger(ManagerWebshell.class);

    /**
     * Build the manager panel.
     */
    public ManagerWebshell() {
        this.setLayout(new BorderLayout());

        this.setDefaultText(I18n.SHELL_RUN_BUTTON);
        
        List<String> pathsList = new ArrayList<String>();
        try {
            InputStream in = this.getClass().getResourceAsStream("/com/jsql/list/shell.txt");
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                pathsList.add(line);
            }
            reader.close();
        } catch (IOException e) {
            LOGGER.error(e, e);
        }

        this.listPaths = new DnDList(pathsList);
        this.add(new JScrollPanePixelBorder(1, 1, 0, 0, this.listPaths), BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));

        final JTextField shellURL = new JPopupTextField(I18n.SHELL_URL_LABEL).getProxy();
        
        String urlTooltip = I18n.SHELL_URL_TOOLTIP;
        
        shellURL.setToolTipText(urlTooltip);
        shellURL.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 1, 0, 0, ToolsGUI.COMPONENT_BORDER),
                        BorderFactory.createMatteBorder(1, 1, 0, 1, ToolsGUI.DEFAULT_BACKGROUND)),
                        ToolsGUI.BLU_ROUND_BORDER));

        JPanel lastLine = new JPanel();
        lastLine.setLayout(new BoxLayout(lastLine, BoxLayout.X_AXIS));
        lastLine.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, ToolsGUI.COMPONENT_BORDER),
                BorderFactory.createEmptyBorder(1, 0, 1, 1)));
        
        this.run = new JButton(I18n.SHELL_RUN_BUTTON, new ImageIcon(getClass().getResource("/com/jsql/view/images/shellSearch.png")));
        this.run.setToolTipText(I18n.SHELL_RUN_BUTTON_TOOLTIP);
        this.run.setEnabled(false);

        this.run.setBorder(ToolsGUI.BLU_ROUND_BORDER);

        this.run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (ManagerWebshell.this.listPaths.getSelectedValuesList().isEmpty()) {
                    LOGGER.warn("Select at least one directory");
                    return;
                }

                if (!"".equals(shellURL.getText())) {
                    try {
                        new URL(shellURL.getText());
                    } catch (MalformedURLException e) {
                        LOGGER.warn("URL is malformed: no protocol", e);
                        return;
                    }
                }

                for (final Object path: ManagerWebshell.this.listPaths.getSelectedValuesList()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                InjectionModel.RAO.getShell(path.toString(), shellURL.getText());
                            } catch (PreparationException e) {
                                LOGGER.warn("Problem writing into " + path, e);
                            } catch (StoppableException e) {
                                LOGGER.warn("Problem writing into " + path, e);
                            }
                        }
                    }, "getShell").start();
                }

            }
        });

        this.privilege = new JLabel(I18n.PRIVILEGE_LABEL, ToolsGUI.SQUARE_GREY, SwingConstants.LEFT);
        this.privilege.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, ToolsGUI.DEFAULT_BACKGROUND));
        this.privilege.setToolTipText(I18n.PRIVILEGE_TOOLTIP);

        lastLine.add(this.privilege);
        lastLine.add(Box.createHorizontalStrut(5));
        lastLine.add(Box.createHorizontalGlue());
        lastLine.add(this.run);

        southPanel.add(shellURL);
        southPanel.add(lastLine);
        this.add(southPanel, BorderLayout.SOUTH);
    }
}
