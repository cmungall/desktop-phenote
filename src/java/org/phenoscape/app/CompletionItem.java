package org.phenoscape.app;

/**
 * A CompletionItem represents a matched item for a text autocompletion interface.
 * Often autocompletion will match other aspects of target items besides their title.
 * This interface allows separation of the match context from the specific matched value item.
 * @author Jim Balhoff
 * @param <T> The value class represented by this completion item.
 */
public interface CompletionItem<T> {
  
  /**
   * Returns text to be displayed in an autocompletion interface component.
   */
  public String getDescription();
  
  /**
   * Returns the value object matched by this autocompletion.
   */
  public T getValue();
  
}
