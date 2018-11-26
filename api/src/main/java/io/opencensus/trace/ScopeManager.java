package io.opencensus.trace;

import io.opencensus.common.Scope;
import java.util.concurrent.Callable;

public interface ScopeManager {
  Span activeSpan();

  Scope withSpan(Span span);

  Scope withSpan(Span span, boolean finishSpanOnClose);

  Runnable withSpan(Span span, boolean finishSpanOnClose, Runnable runnable);

  <C> Callable<C> withSpan(Span span, boolean finishSpanOnClose, Callable<C> callable);

  static final class NoopScopeManager implements ScopeManager {
      public static final ScopeManager INSTANCE = new NoopScopeManager();

      private NoopScopeManager() {
      }

      @Override
      public Span activeSpan() {
          return BlankSpan.INSTANCE;
      }

      @Override
      public Scope withSpan(Span span) {
          return BlankScope.INSTANCE;
      }

      @Override
      public Scope withSpan(Span span, boolean finishSpanOnClose) {
          return BlankScope.INSTANCE;
      }

      @Override
      public Runnable withSpan(Span span, boolean finishSpanOnClose, Runnable runnable) {
          return runnable;
      }

      @Override
      public <C> Callable<C> withSpan(Span span, boolean finishSpanOnClose, Callable<C> callable) {
          return callable;
      }
  }
}
