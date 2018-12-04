package io.opencensus.trace;

import javax.annotation.concurrent.Immutable;
import io.opencensus.common.Scope;

@Immutable
public final class BlankScope implements Scope {
  public static final BlankScope INSTANCE = new BlankScope();

  private BlankScope() {
  }

  @Override
  public void close() {
  }
}
