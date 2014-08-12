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
package com.jsql.view.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.jsql.view.ToolsGUI;
import com.jsql.view.ui.RoundBorder;

/**
 * Model for default item used on an empty tree.
 */
public class NodeModelEmpty extends AbstractNodeModel {
    /**
     * Flat node for empty tree.
     * @param textNode
     */
    public NodeModelEmpty(String textNode) {
        super(textNode);
    }

    @Override
    public Component getComponent(JTree tree, Object nodeRenderer,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) nodeRenderer;
        JPanel emptyPanel = new JPanel(new BorderLayout());
        JLabel text = new JLabel(currentNode.getUserObject().toString());
        emptyPanel.add(text);
        text.setBorder(new RoundBorder(4, 1, false));
        if (currentNode != null) {
            if (selected) {
                emptyPanel.setBackground(ToolsGUI.SELECTION_BACKGROUND);
                text.setBorder(new RoundBorder(4, 1, true));
            } else {
                emptyPanel.setBackground(Color.WHITE);
            }
            text.setBorder(new RoundBorder(4, 1, hasFocus));
        }
        return emptyPanel;
    }

    @Override Icon getLeafIcon(boolean leaf) {
        // No icon for default node
        return null;
    }
    @Override void runAction() {
        // Not used
    }
    @Override void displayMenu(JPopupMenu tablePopupMenu, TreePath path) {
        // Not used
    }
    @Override public void showPopup(final DefaultMutableTreeNode currentTableNode, TreePath path, int i, int j) {
        // Not used
    }
    @Override boolean verifyShowPopup() {
        // Not used
        return false;
    }
}
