/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.server.trigger.impl;

import jetbrains.buildServer.util.ExceptionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 18:21
 */
public class PackageChangesCheckerThreadTask {
  private final PackageCheckQueue myHolder;
  private final ScheduledExecutorService myExecutor;
  private final Collection<PackageChecker> myCheckers;

  public PackageChangesCheckerThreadTask(@NotNull final PackageCheckQueue holder,
                                         @NotNull final ScheduledExecutorService executor,
                                         @NotNull final Collection<PackageChecker> checkers) {
    myHolder = holder;
    myExecutor = executor;
    myCheckers = checkers;
  }

  public void checkForUpdates() {
    if (myExecutor.isShutdown()) return;

    for (final PackageChecker checker : myCheckers) {
      final List<CheckablePackage> items = getMatchedItems(checker, myHolder.getItemsToCheckNow());
      if (myExecutor.isShutdown()) return;
      if (items.size() > 0) {
        checker.update(myExecutor, items);
      }
    }

    myHolder.cleaupObsolete();
    postCheckTask();
  }

  public void postCheckTask() {
    if (myExecutor.isShutdown()) return;
    myExecutor.schedule(asRunnable(), myHolder.getSleepTime(), TimeUnit.MILLISECONDS);
  }

  @NotNull
  private Runnable asRunnable() {
    return ExceptionUtil.catchAll(
            "NuGet Trigger update scheduler",
            new Runnable() {
              public void run() {
                PackageChangesCheckerThreadTask.this.checkForUpdates();
              }
            });
  }

  @NotNull
  private List<CheckablePackage> getMatchedItems(@NotNull final PackageChecker checker,
                                                 @NotNull final Collection<PackageCheckEntry> toCheck) {
    final List<CheckablePackage> items = new ArrayList<CheckablePackage>();
    for (PackageCheckEntry entry : toCheck) {
      if (checker.accept(entry.getRequest())) {
        //mark as pending
        entry.setExecuting();
        items.add(entry);
      }
    }
    return items;
  }
}
