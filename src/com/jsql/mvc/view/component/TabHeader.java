package com.jsql.mvc.view.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * Panel displayed as a header for tabs
 */
public class TabHeader extends JPanel implements MouseListener{
    private static final long serialVersionUID = 8127944685828300647L;
    
    private JTabbedPane valuesTabbedPane;
    
    public TabHeader(JTabbedPane newJTabbedPane){
        this(newJTabbedPane, new ImageIcon(TabHeader.class.getResource("/com/jsql/images/table.png")));
    }
        
    public TabHeader(JTabbedPane newJTabbedPane, ImageIcon imageIcon){
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        
        this.valuesTabbedPane = newJTabbedPane;
        this.setOpaque(false);
        
        this.addMouseListener(new MouseAdapter(){
            /**
             * Right click: remove tab
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof TabHeader) {
                    if(e.getButton() == MouseEvent.BUTTON2){
                        int closeTabNumber = valuesTabbedPane.indexOfTabComponent(TabHeader.this);
                        valuesTabbedPane.removeTabAt(closeTabNumber);
                    }
                }
            }
            /**
             * Left click: select tab
             */
            @Override
            public void mousePressed(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof TabHeader) {
                    if(e.getButton() == MouseEvent.BUTTON1){
                        int selectTabNumber = valuesTabbedPane.indexOfTabComponent(TabHeader.this);
                        valuesTabbedPane.setSelectedIndex(selectTabNumber);
                        valuesTabbedPane.requestFocusInWindow();
                    }
                }
            }
        });
        
        // Set the text of tab
        JLabel tabTitleLabel = new JLabel(){
            private static final long serialVersionUID = -3224791474462317469L;

            public String getText() {
                int i = valuesTabbedPane.indexOfTabComponent(TabHeader.this);
                if (i != -1) {
                    return valuesTabbedPane.getTitleAt(i);
                }
                return null;
            }
        };
        tabTitleLabel.setIcon(imageIcon);
        this.add(tabTitleLabel);
        
        // Icon for closing tab
        Icon closeIcon = new ImageIcon(this.getClass().getResource("/com/jsql/images/gtk_close_cross2.png"));
        Dimension closeButtonSize = new Dimension(closeIcon.getIconWidth(), closeIcon.getIconHeight());
        
        JButton tabCloseButton = new JButton(closeIcon);
        tabCloseButton.setPreferredSize(closeButtonSize);
        tabCloseButton.setContentAreaFilled(false);
        tabCloseButton.setFocusable(false);
        tabCloseButton.setBorderPainted(false);
        tabCloseButton.setRolloverEnabled(true); // turn on before rollovers work
        tabCloseButton.setRolloverIcon(new ImageIcon(this.getClass().getResource("/com/jsql/images/gtk_close_cross4.png")));
        tabCloseButton.setPressedIcon(new ImageIcon(this.getClass().getResource("/com/jsql/images/gtk_close_cross.png")));
        tabCloseButton.addMouseListener(this);
        
        this.add(tabCloseButton);
    }
    
    /**
     * Action for close button: remove tab
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e))return;
        int closeTabNumber = valuesTabbedPane.indexOfTabComponent(TabHeader.this);
        valuesTabbedPane.removeTabAt(closeTabNumber);
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
}