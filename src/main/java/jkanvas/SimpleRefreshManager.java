package jkanvas;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple implementation of a {@link RefreshManager}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class SimpleRefreshManager implements RefreshManager {

  /** The list containing all {@link Refreshable Refreshables}. */
  private final List<Refreshable> refreshables = new ArrayList<>();
  /** The number of active bulk operations. */
  private int bulkOps = 0;

  @Override
  public void addRefreshable(final Refreshable r) {
    if(refreshables.contains(r)) return;
    refreshables.add(r);
  }

  @Override
  public void removeRefreshable(final Refreshable r) {
    refreshables.remove(r);
  }

  @Override
  public Refreshable[] getRefreshables() {
    return refreshables.toArray(new Refreshable[refreshables.size()]);
  }

  @Override
  public void refreshAll() {
    if(bulkOps > 0) return;
    for(final Refreshable r : refreshables) {
      r.refresh();
    }
  }

  @Override
  public void startBulkOperation() {
    ++bulkOps;
  }

  @Override
  public void endBulkOperation() {
    --bulkOps;
    refreshAll();
  }

  /** Clears all {@link Refreshable Refreshables}. */
  protected void clearRefreshables() {
    refreshables.clear();
  }

}
