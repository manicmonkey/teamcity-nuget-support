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

package jetbrains.buildServer.nuget.agent.dependencies.impl;

import jetbrains.buildServer.nuget.agent.runner.install.impl.NuGetPackagesCollectorEx;
import jetbrains.buildServer.nuget.common.PackageDependencies;
import jetbrains.buildServer.nuget.common.PackageInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.TreeSet;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 22:52
 */
public class NuGetPackagesCollectorImpl implements NuGetPackagesCollectorEx {
  private final TreeSet<PackageInfo> myPackages = new TreeSet<PackageInfo>();


  public void addPackage(@NotNull String packageId, @NotNull String version, @Nullable String allowedVersions) {
    myPackages.add(new PackageInfo(packageId, version));
  }

  @NotNull
  public PackageDependencies getPackages() {
    return new PackageDependencies(
            Collections.<String>emptyList(),
            myPackages);
  }

  public void removeAllPackages() {
    myPackages.clear();
  }
}
