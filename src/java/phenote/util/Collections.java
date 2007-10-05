package phenote.util;

import java.util.Collection;
import java.util.Iterator;

public class Collections {

  private Collections(){}

  public static String join(Collection<?> collection, String separator) {
    final Iterator<?> iterator = collection.iterator();
    final StringBuffer buffer = new StringBuffer();
    while (iterator.hasNext()) {
      buffer.append(iterator.next());
      if (iterator.hasNext()) buffer.append(separator);
    }
    return buffer.toString();
  }

}
