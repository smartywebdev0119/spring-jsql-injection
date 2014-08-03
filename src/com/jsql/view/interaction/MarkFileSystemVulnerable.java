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

import com.jsql.view.GUIMediator;
import com.jsql.view.GUITools;

/**
 * Mark the injection as using a user profile vulnerable to file I/O
 */
public class MarkFileSystemVulnerable implements IInteractionCommand{
    /**
     * @param nullParam
     */
    public MarkFileSystemVulnerable(Object[] nullParam){
    }

    /* (non-Javadoc)
     * @see com.jsql.mvc.view.message.ActionOnView#execute()
     */
    public void execute(){
        GUIMediator.left().fileManager.changeIcon(GUITools.TICK);
        GUIMediator.left().shellManager.changeIcon(GUITools.TICK);
        GUIMediator.left().sqlShellManager.changeIcon(GUITools.TICK);
        GUIMediator.left().uploadManager.changeIcon(GUITools.TICK);
    }
}
