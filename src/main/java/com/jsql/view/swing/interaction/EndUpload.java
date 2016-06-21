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
package com.jsql.view.swing.interaction;

import com.jsql.view.swing.MediatorGui;

/**
 * End the refreshing of File search button.
 */
public class EndUpload implements InteractionCommand {
    /**
     * @param interactionParams
     */
    public EndUpload(Object[] interactionParams) {
     // Do nothing
    }

    @Override
    public void execute() {
        MediatorGui.tabManagers().uploadManager.restoreButtonText();
        MediatorGui.tabManagers().uploadManager.setButtonEnable(true);
        MediatorGui.tabManagers().uploadManager.hideLoader();
    }
}
