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

package jetbrains.buildServer.nuget.agent.install.impl;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.install.NuGetInstallPackageActionFactory;
import jetbrains.buildServer.nuget.agent.install.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 17:49
 */
public class NuGetInstallPackageActionFactoryImpl implements NuGetInstallPackageActionFactory {
  private final CommandlineBuildProcessFactory myFactory;

  public NuGetInstallPackageActionFactoryImpl(CommandlineBuildProcessFactory factory) {
    myFactory = factory;
  }

  public BuildProcess createBuildProcess(@NotNull final BuildRunnerContext context,
                                         @NotNull final PackagesInstallParameters params,
                                         @NotNull final File packagesConfig,
                                         @NotNull final File targetFolder) throws RunBuildException {
    final List<String> argz = new ArrayList<String>();
    argz.add("install");
    argz.add(FileUtil.getCanonicalFile(packagesConfig).getPath()); //path to package
    argz.add("-OutputDirectory");
    argz.add(FileUtil.getCanonicalFile(targetFolder).getPath());

    for (String source : params.getNuGetPackageSources()) {
      argz.add("-Source");
      argz.add(source);
    }

    return myFactory.executeCommandLine(
            context,
            params.getNuGetExeFile(),
            argz,
            packagesConfig.getParentFile()
    );
  }
}