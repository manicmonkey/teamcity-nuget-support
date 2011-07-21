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

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.publish.MatchFilesBuildProcess;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import jetbrains.buildServer.util.FileUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 20:38
 */
public class MatchFilesBuildProcessTest extends BuildProcessTestCase {
  private Mockery m;

  private MatchFilesBuildProcess match;
  private AgentRunningBuild build;
  private BuildRunnerContext ctx;
  private NuGetPublishParameters params;
  private MatchFilesBuildProcess.Callback cb;
  private File root;
  private List<String> files;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m = new Mockery();
    files = new ArrayList<String>();
    build = m.mock(AgentRunningBuild.class);
    ctx = m.mock(BuildRunnerContext.class);
    params = m.mock(NuGetPublishParameters.class);
    cb = m.mock(MatchFilesBuildProcess.Callback.class);

    match = new MatchFilesBuildProcess(ctx, params, cb);
    root = createTempDir();

    m.checking(new Expectations(){{
      allowing(params).getFiles(); will(returnValue(files));
      allowing(ctx).getBuild(); will(returnValue(build));
      allowing(build).getCheckoutDirectory(); will(returnValue(root));
    }});
  }

  @Test
  public void test_match_relative_file() {
    final File dest = new File(root, "aaa.txt");
    FileUtil.writeFile(dest, "some content");

    files.add("aaa.txt");
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);
  }

  @Test
  public void test_match_relative_file_wildcard() {
    final File dest = new File(root, "q/e/r/t/aaa.txt");
    FileUtil.createParentDirs(dest);
    FileUtil.writeFile(dest, "some content");

    files.add("**/*.txt");
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);
  }


  @Test
  public void test_match_fullPath_file() {
    final File dest = new File(root, "aaa.txt");
    FileUtil.writeFile(dest, "some content");

    files.add(FileUtil.getCanonicalFile(dest).getPath());
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);
  }

  @Test
  public void test_match_fullPath_file2() {
    final File dest = new File(root, "a/b/c/aaa.txt");
    FileUtil.createParentDirs(dest);
    FileUtil.writeFile(dest, "some content");

    files.add(FileUtil.getCanonicalFile(dest).getPath());
    assertRunSuccessfully(match, BuildFinishedStatus.FINISHED_SUCCESS);
  }

  @Test
  public void test_match_no_files() {
    final File dest = new File(root, "aaa.txt");
    FileUtil.writeFile(dest, "some content");

    assertRunException(match, "fick");
  }
}
