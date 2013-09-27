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
package com.jsql.view.dnd.list;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

public class ListTransfertHandler extends TransferHandler{
    private static final long serialVersionUID = -1029596387333896827L;

    private DnDList dnDlist;
    
    public ListTransfertHandler(DnDList list){
        dnDlist = list;
    }
    
    // Export
    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }

    List<ListItem> dragPaths = null;

    @Override
    protected Transferable createTransferable(JComponent c) {
        JList<ListItem> list = (JList<ListItem>)c;
        dragPaths = list.getSelectedValuesList();

        StringBuffer buff = new StringBuffer();
        for(ListItem t: dragPaths)
            buff.append(t+"\n");

        return new StringSelection(buff.toString());
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        if (action == TransferHandler.MOVE) {
            JList<ListItem> list = (JList<ListItem>)c;
            DefaultListModel<ListItem> model = (DefaultListModel<ListItem>)list.getModel();
            for(ListItem t: dragPaths)
                model.remove(model.indexOf(t));

            dragPaths = null;
        }
    }

    //Import
    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDataFlavorSupported(DataFlavor.stringFlavor) || support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        JList<ListItem> list = (JList<ListItem>)support.getComponent();
        DefaultListModel<ListItem> model = (DefaultListModel<ListItem>)list.getModel();
        if (support.isDrop()) { //This is a drop
            if (support.isDataFlavorSupported(DataFlavor.stringFlavor)){
                JList.DropLocation dl = (JList.DropLocation)support.getDropLocation();
                int childIndex = dl.getIndex();

                List<Integer> selectAfterDrop = new ArrayList<Integer>();

                if(dragPaths != null && dragPaths.size()>0){ // DnD from list
                    for(ListItem value: dragPaths)
                        if(!value.toString().equals("")){
                            ListItem newValue = new ListItem(value.toString().replace("\\", "/")); //! FUUuu
                            selectAfterDrop.add(childIndex);
                            model.add(childIndex++, newValue);
                        }
                }else{ // DnD from outside
                    try {
                        String importString = (String) (support.getTransferable().getTransferData(DataFlavor.stringFlavor));
                        for(String value: importString.split("\\n"))
                            if(!value.equals("")){
                                selectAfterDrop.add(childIndex);
                                model.add(childIndex++, new ListItem(value.replace("\\", "/")));
                            }
                    } catch (UnsupportedFlavorException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //array is the Integer array
                int[] selectedIndices = new int[selectAfterDrop.size()];
                int i=0;
                for (Integer integer: selectAfterDrop) {
                    selectedIndices[i] = integer.intValue();
                    i++;
                }
                list.setSelectedIndices(selectedIndices);
            }else if(support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                JList.DropLocation dl = (JList.DropLocation)support.getDropLocation();
                int childIndex = dl.getIndex();

                try {
                    dnDlist.dropPasteFile((List<File>)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor), childIndex);
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else { //This is a paste
            Transferable transferableFromClipboard = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (transferableFromClipboard != null)
                if(transferableFromClipboard.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    try {
                        String clipboardText = (String) transferableFromClipboard.getTransferData(DataFlavor.stringFlavor);

                        int y = 0;
                        if(list.getSelectedIndex() > 0)
                            y = list.getSelectedIndex();
                        list.clearSelection();

                        List<Integer> k = new ArrayList<Integer>();
                        for(String f: clipboardText.split("\\n"))
                            if(!f.equals("")){
                                ListItem c = new ListItem(f.replace("\\", "/"));
                                k.add(y);
                                model.add(y++, c);
                            }
                        int[] array2 = new int[k.size()];
                        int i=0;
                        for (Integer integer : k) {
                            array2[i] = integer.intValue();
                            i++;
                        }
                        list.setSelectedIndices(array2);
                        list.scrollRectToVisible(
                                list.getCellBounds(
                                        list.getMinSelectionIndex(),
                                        list.getMaxSelectionIndex()
                                        )
                                );
                    } catch (UnsupportedFlavorException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if(transferableFromClipboard.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        int y = 0;
                        if(list.getSelectedIndex() > 0)
                            y = list.getSelectedIndex();
                        list.clearSelection();

                        dnDlist.dropPasteFile((List<File>)transferableFromClipboard.getTransferData(DataFlavor.javaFileListFlavor), y);
                    } catch (UnsupportedFlavorException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

        }

        return true;
    }
}
