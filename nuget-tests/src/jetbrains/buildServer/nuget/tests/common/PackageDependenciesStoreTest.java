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

package jetbrains.buildServer.nuget.tests.common;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.PackageDependencies;
import jetbrains.buildServer.nuget.common.PackageDependenciesStore;
import jetbrains.buildServer.nuget.common.PackageInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 19.07.11 13:24
 */
public class PackageDependenciesStoreTest extends BaseTestCase {
  private PackageDependenciesStore store;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    store = new PackageDependenciesStore();
  }

  @Test
  public void test_load() throws IOException {
    File temp = createTempFile("<nuget-dependencies>\n" +
            "  <packages>\n" +
            "    <package id=\"id1\" version=\"v1\" />\n" +
            "    <package id=\"id2\" version=\"v2\" />\n" +
            "  </packages>\n" +
            "  <sources>\n" +
            "    <source>source1</source>\n" +
            "    <source>source2</source>\n" +
            "  </sources>\n" +
            "</nuget-dependencies>");

    PackageDependencies deps = new PackageDependencies(Arrays.asList("source1", "source2"),
            Arrays.asList(new PackageInfo("id1", "v1"), new PackageInfo("id2", "v2")));

    PackageDependencies load = store.load(temp);
    assertEquals(deps, load);
  }


  @Test
  public void test_saveLoad() throws IOException {
    final File tmp = createTempFile();

    PackageDependencies deps = new PackageDependencies(Arrays.asList("source1", "source2"),
            Arrays.asList(new PackageInfo("id1", "v1"), new PackageInfo("id2", "v2")));
    store.save(deps, tmp);

    dumpFile(tmp);

    PackageDependencies load = store.load(tmp);

    assertEquals(deps, load);
  }

  private void assertEquals(PackageDependencies deps, PackageDependencies load) {
    Assert.assertEquals(new TreeSet<String>(load.getSources()), new TreeSet<String>(deps.getSources()));
    Assert.assertEquals(new TreeSet<PackageInfo>(load.getPackages()), new TreeSet<PackageInfo>(deps.getPackages()));
  }
}
