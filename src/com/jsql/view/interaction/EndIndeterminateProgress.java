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
package com.jsql.view.interaction;

import javax.swing.tree.DefaultTreeModel;

import com.jsql.model.bean.ElementDatabase;
import com.jsql.view.GUIMediator;
import com.jsql.view.tree.NodeModel;

/**
 * Stop refreshing the progress bar of an untracked
 * progression (like colum search).
 */
public class EndIndeterminateProgress implements IInteractionCommand {
    /**
     * The element in the database tree for which the progress ends.
     */
    private ElementDatabase dataElementDatabase;

    /**
     * @param interactionParams Element to update
     */
    public EndIndeterminateProgress(Object[] interactionParams) {
        dataElementDatabase = (ElementDatabase) interactionParams[0];
    }

    public void execute() {
        // Tree model, update the tree (refresh, add node, etc)
        DefaultTreeModel treeModel = (DefaultTreeModel) GUIMediator.databaseTree().getModel();

        // Get the node
        NodeModel progressingTreeNodeModel =
                (NodeModel) GUIMediator.gui().getTreeNodeModels().get(dataElementDatabase).getUserObject();
        // Mark the node model as 'no loading bar'
        progressingTreeNodeModel.hasIndeterminatedProgress = false;
        // Mark the node model as 'no stop/pause/resume button'
        progressingTreeNodeModel.isRunning = false;

        // Update the node
        treeModel.nodeChanged(GUIMediator.gui().getTreeNodeModels().get(dataElementDatabase));
    }
}
