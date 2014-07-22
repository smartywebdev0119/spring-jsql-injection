/*******************************************************************************
 * Copyhacked (H) 2012-2013.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.view.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.jsql.model.bean.ElementDatabase;
import com.jsql.model.bean.Table;
import com.jsql.view.GUIMediator;
import com.jsql.view.GUITools;

/**
 * Model adding functional layer to the node, add information to tree node in term of injection process.
 * Used by renderer and editor.
 * @param <T> The database element for this node.
 */
public class NodeModelTable extends NodeModel{
	
	public NodeModelTable(ElementDatabase newObject) {
		super(newObject);
	}

	@Override
	Icon getIcon(boolean leaf) {
		if(leaf)
			return new ImageIcon(getClass().getResource("/com/jsql/view/images/tableGo.png"));
		else
			return GUITools.TABLE;
	}

	@Override
	void displayProgress(NodePanel panel, DefaultMutableTreeNode currentNode) {
		if(this.getParent().toString().equals("information_schema")){
			panel.showLoader();

			if(this.interruptable.isPaused()){
				ImageIcon animatedGIFPaused = new IconOverlap(GUITools.PATH_PROGRESSBAR, GUITools.PATH_PAUSE);
				animatedGIFPaused.setImageObserver(new AnimatedObserver(GUIMediator.databaseTree(), currentNode));
				panel.setLoaderIcon( animatedGIFPaused );
			}
		}else
			super.displayProgress(panel, currentNode);
	}

	@Override
	void runAction() {
		Table selectedTable = (Table) this.dataObject;
		if(!this.hasBeenSearched && !this.isRunning){
			this.interruptable = GUIMediator.controller().selectTable(selectedTable);
			this.isRunning = true;
		}		
	}

	@Override
	void displayMenu(JPopupMenu tablePopupMenu, TreePath path) {
		final DefaultMutableTreeNode currentTableNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		final NodeModel currentTableModel = (NodeModel) currentTableNode.getUserObject();

		JMenuItem mnCheckAll = new JMenuItem("Check All",'C');
		JMenuItem mnUncheckAll = new JMenuItem("Uncheck All",'U');

		mnCheckAll.setIcon(GUITools.EMPTY);
		mnUncheckAll.setIcon(GUITools.EMPTY);

		if(!this.hasBeenSearched){
			mnCheckAll.setEnabled(false);
			mnUncheckAll.setEnabled(false);

			tablePopupMenu.add(mnCheckAll);
			tablePopupMenu.add(mnUncheckAll);
			tablePopupMenu.add(new JSeparator());
		}

		mnCheckAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				DefaultTreeModel treeModel = (DefaultTreeModel) GUIMediator.databaseTree().getModel();

				int tableChildCount = treeModel.getChildCount(currentTableNode);
				for(int i=0; i < tableChildCount ;i++) {
					DefaultMutableTreeNode currentChild = (DefaultMutableTreeNode) treeModel.getChild(currentTableNode, i);
					if( currentChild.getUserObject() instanceof NodeModel ){
						NodeModel columnTreeNodeModel = (NodeModel) currentChild.getUserObject();
						columnTreeNodeModel.isChecked = true;
						currentTableModel.hasChildChecked = true;
					}
				}

				treeModel.nodeChanged(currentTableNode);
			}
		});

		mnUncheckAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				DefaultTreeModel treeModel = (DefaultTreeModel) GUIMediator.databaseTree().getModel();

				int tableChildCount = treeModel.getChildCount(currentTableNode);
				for(int i=0; i < tableChildCount ;i++) {
					DefaultMutableTreeNode currentChild = (DefaultMutableTreeNode) treeModel.getChild(currentTableNode, i);
					if( currentChild.getUserObject() instanceof NodeModel ){
						NodeModel columnTreeNodeModel = (NodeModel) currentChild.getUserObject();
						columnTreeNodeModel.isChecked = false;
						currentTableModel.hasChildChecked = false;
					}
				}

				treeModel.nodeChanged(currentTableNode);
			}
		});

		mnCheckAll.setIcon(GUITools.EMPTY);
		mnUncheckAll.setIcon(GUITools.EMPTY);

		tablePopupMenu.add(mnCheckAll);
		tablePopupMenu.add(mnUncheckAll);
		tablePopupMenu.add(new JSeparator());
	}
}
