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

import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 25.02.12 14:30
 */
public class DefaultSettings implements ServerSettings {
  @NotNull
  public File getPackagesFolder() {
    String nugetStore = System.getenv("NUGET_STORE");
    if (nugetStore == null)
      throw new RuntimeException("Could not look up env variable 'NUGET_STORE'");
    return FileUtil.getCanonicalFile(new File(nugetStore));
  }

  public long getPackagesRefreshInterval() {
    return 3000;
  }

  @NotNull
  public String getServerUrl() {
    return "http://localhost:8888/nuget/feed";
  }
}
