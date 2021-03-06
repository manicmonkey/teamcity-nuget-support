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

package jetbrains.buildServer.nuget.server.feed.server.index.impl;

import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.01.12 14:32
 */
public class ArtifactPackageFile implements PackageFile {
  private final BuildArtifact myArtifact;
  private final Date myLastUpdated;

  public ArtifactPackageFile(@NotNull final BuildArtifact artifact,
                             @NotNull final Date lastUpdated) {
    myArtifact = artifact;
    myLastUpdated = lastUpdated;
  }

  public long getSize() {
    return myArtifact.getSize();
  }

  @NotNull
  public Date getLastUpdated() {
    return myLastUpdated;
  }

  @NotNull
  public InputStream getInputStream() throws IOException {
    return myArtifact.getInputStream();
  }
}
