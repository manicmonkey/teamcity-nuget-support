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

package jetbrains.buildServer.nuget.server.exec;

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 13:10
 */
public class ListPackagesCommandImpl implements ListPackagesCommand {
  private NuGetExecutor myExec;

  public ListPackagesCommandImpl(NuGetExecutor exec) {
    myExec = exec;
  }

  @NotNull
  public Collection<PackageInfo> checkForChanges(
          @NotNull final File nugetPath,
          @Nullable final String source,
          @NotNull final String packageId,
          @Nullable final String versionSpec) {
    List<String> cmd = new ArrayList<String>();

    cmd.add("TeamCity.List");
    if (!StringUtil.isEmptyOrSpaces(source)) {
      cmd.add("-Source");
      cmd.add(source);
    }
    cmd.add("-Id");
    cmd.add(packageId);

    if (!StringUtil.isEmptyOrSpaces(versionSpec)) {
      cmd.add("-Version");
      cmd.add(versionSpec);
    }

    return myExec.executeNuGet(nugetPath, cmd, new ListPackagesCommandProcessor(source));
  }

}
