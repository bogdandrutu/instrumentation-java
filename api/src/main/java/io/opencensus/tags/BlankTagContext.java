package io.opencensus.tags;

import java.util.Collections;
import java.util.Iterator;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class BlankTagContext extends TagContext {
  public static final BlankTagContext INSTANCE = new BlankTagContext();

  private BlankTagContext() {
  }

  @Override
  protected Iterator<Tag> getIterator() {
      return Collections.<Tag>emptyList().iterator();
  }
}
