package org.phenoscape.swing;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventSelectionModel;

/**
 * This class listens to changes to an EventList and its associated EventSelectionModel. 
 * Upon deletion of an item from the list, it selects the list item now occupying the 
 * index of the deleted item, so that the list is not left with an empty selection.
 * 
 * TODO - Move code for selecting newly inserted items from other classes to here.
 * 
 * @author Jim Balhoff
 */
public class ListSelectionMaintainer<T> {
  
  final EventList<T> list;
  final EventSelectionModel<T> selectionModel;
  boolean deleteHappened;
  int deletedIndex = 0;
  
  public ListSelectionMaintainer(EventList<T> list, EventSelectionModel<T> selectionModel) {
    this.list = list;
    this.selectionModel = selectionModel;
    this.list.addListEventListener(new ListListener<T>());
    this.selectionModel.addListSelectionListener(new SelectionListener());
  }
  
  private class ListListener<E> implements ListEventListener<E> {

    public void listChanged(ListEvent<E> listChanges) {
      if (listChanges.hasNext()) {
        listChanges.nextBlock();
        if (listChanges.getType() == ListEvent.DELETE) {
          deletedIndex = listChanges.getBlockStartIndex();
          deleteHappened = true;
        }
      }
    }
    
  }
  
  private class SelectionListener implements ListSelectionListener {

    public void valueChanged(ListSelectionEvent e) {
      if (deleteHappened && selectionModel.isSelectionEmpty()) {
        final int selectionIndex = (deletedIndex > (list.size() - 1)) ? (list.size() - 1) : deletedIndex;
        selectionModel.setSelectionInterval(selectionIndex, selectionIndex);
      }
      deleteHappened = false;
    }

  }
  
  @SuppressWarnings("unused")
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
 
}
