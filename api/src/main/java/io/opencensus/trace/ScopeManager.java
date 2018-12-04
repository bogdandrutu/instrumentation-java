package io.opencensus.trace;

import io.opencensus.common.Scope;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.BlankTagContext;
import java.util.concurrent.Callable;

public interface ScopeManager {
  Span activeSpan();
  TagContext activeTagContext();

  Scope withSpan(Span span);

  Scope withSpan(Span span, boolean finishSpanOnClose);

  Runnable withSpan(Span span, boolean finishSpanOnClose, Runnable runnable);

  <C> Callable<C> withSpan(Span span, boolean finishSpanOnClose, Callable<C> callable);

  Scope withTagContext(TagContext context);

  static final class NoopScopeManager implements ScopeManager {
      public static final ScopeManager INSTANCE = new NoopScopeManager();

      private NoopScopeManager() {
      }

      @Override
      public Span activeSpan() {
          return BlankSpan.INSTANCE;
      }

      @Override
      public TagContext activeTagContext() {
          return BlankTagContext.INSTANCE;
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

      @Override
      public Scope withTagContext(TagContext context) {
          return BlankScope.INSTANCE;
      }
  }
}
