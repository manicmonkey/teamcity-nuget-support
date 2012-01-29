/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.standalone.server;

import jetbrains.buildServer.nuget.server.feed.server.index.impl.NuGetPackageBuilder;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackagesIndexBase;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.transform.IsLatestCalculator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 29.01.12 23:42
 */
public class NuGetApplicationPackagesIndex extends PackagesIndexBase<Entry> {
  @NotNull
  @Override
  protected Iterator<Entry> getEntries() {
    return Collections.<Entry>emptyList().iterator();
  }

  @Override
  protected NuGetPackageBuilder builderFromEntry(@NotNull Entry entry) {
    return new NuGetPackageBuilder(entry.getKey(), entry.getId(), entry.getMap());
  }

  @Override
  protected String getDownloadUrl(@NotNull NuGetPackageBuilder builder) {
    return null;
  }

  @NotNull
  @Override
  protected IsLatestCalculator createIsLatestTransformation() {
    return new IsLatestCalculator() {
      public Boolean isLatest(@NotNull NuGetPackageBuilder builder) {
        return null;
      }
    };
  }
}