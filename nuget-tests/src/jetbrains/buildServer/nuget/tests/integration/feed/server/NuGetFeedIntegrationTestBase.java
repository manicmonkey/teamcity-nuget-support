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

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import jetbrains.buildServer.nuget.server.feed.impl.FeedGetMethodFactory;
import jetbrains.buildServer.nuget.server.feed.impl.FeedHttpClientHolder;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.ArtifactPackageFile;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackageLoadException;
import jetbrains.buildServer.nuget.tests.integration.IntegrationTestBase;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.*;
import java.util.*;

import static jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex.TEAMCITY_ARTIFACT_RELPATH;
import static jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex.TEAMCITY_BUILD_TYPE_ID;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 05.01.12 0:57
 */
public abstract class NuGetFeedIntegrationTestBase extends IntegrationTestBase {
  protected Collection<InputStream> myStreams;
  protected FeedHttpClientHolder myHttpClient;
  protected FeedGetMethodFactory myHttpMethods;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myStreams = new ArrayList<InputStream>();
    myHttpClient = new FeedHttpClientHolder();
    myHttpMethods = new FeedGetMethodFactory();

  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    for (InputStream stream : myStreams) {
      FileUtil.close(stream);
    }
  }

  protected abstract String getNuGetServerUrl();

  @NotNull
  protected HttpGet createGetQuery(@NotNull String req, @NotNull final NameValuePair... reqs) {
    return myHttpMethods.createGet(getNuGetServerUrl() + req, reqs);
  }

  protected interface ExecuteAction<T> {
    T processResult(@NotNull HttpResponse response) throws IOException;
  }

  protected <T> T execute(@NotNull final HttpGet get, @NotNull final ExecuteAction<T> action) {
    try {
      final HttpResponse execute = myHttpClient.execute(get);
      return action.processResult(execute);
    } catch (IOException e) {
      throw new RuntimeException("Failed to connect to " + get.getRequestLine() + ". " + e.getClass() + " " + e.getMessage(), e);
    } finally {
      get.abort();
    }
  }


  @NotNull
  protected Map<String, String> indexPackage(@NotNull final File packageFile,
                                             final boolean isLatest,
                                             final long buildId) throws IOException {
    final SFinishedBuild build = m.mock(SFinishedBuild.class, "build-" + packageFile.getPath());
    final BuildArtifact artifact = m.mock(BuildArtifact.class, "artifact-" + packageFile.getPath());

    m.checking(new Expectations() {{
      allowing(build).getBuildId(); will(returnValue(buildId));
      allowing(build).getBuildTypeId();  will(returnValue("bt"));
      allowing(build).getBuildTypeName(); will(returnValue("buidldzzz"));
      allowing(build).getFinishDate(); will(returnValue(new Date(1319214849319L)));

      allowing(artifact).getInputStream();
      will(new CustomAction("open file") {
        public Object invoke(Invocation invocation) throws Throwable {
          final FileInputStream stream = new FileInputStream(packageFile);
          myStreams.add(stream);
          return stream;
        }
      });

      allowing(artifact).getTimestamp(); will(returnValue(packageFile.lastModified()));
      allowing(artifact).getSize(); will(returnValue(packageFile.length()));
      allowing(artifact).getRelativePath(); will(returnValue(packageFile.getPath()));
      allowing(artifact).getName(); will(returnValue(packageFile.getName()));
    }});

    try {
      final LocalNuGetPackageItemsFactory factory = new LocalNuGetPackageItemsFactory();
      final Map<String, String> map = new HashMap<String, String>(factory.loadPackage(new ArtifactPackageFile(artifact, new Date())));
      map.put(TEAMCITY_ARTIFACT_RELPATH, "some/package/download/" + packageFile.getName());
      map.put(TEAMCITY_BUILD_TYPE_ID, "bt_" + packageFile.getName());
      map.put("TeamCityDownloadUrl", "some-download-url/" + packageFile.getName());
      map.put("IsLatestVersion", String.valueOf(isLatest));
      return map;
    } catch (PackageLoadException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  protected String openRequest(@NotNull final String req,
                               @NotNull final NameValuePair... reqs) {
    final HttpGet get = createGetQuery(req, reqs);
    return execute(get, new ExecuteAction<String>() {
      public String processResult(@NotNull HttpResponse response) throws IOException {
        final HttpEntity entity = response.getEntity();
        System.out.println("Request: " + get.getRequestLine());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        entity.writeTo(bos);
        String enc = null;
        final Header encE = entity.getContentEncoding();
        if (encE != null) {
           enc = encE.getValue();
        }
        if (StringUtil.isEmptyOrSpaces(enc)) {
          enc = "utf-8";
        }
        
        return bos.toString(enc);
      }
    });
  }

  @NotNull
  protected Runnable assert200(@NotNull final String req,
                               @NotNull final NameValuePair... reqs) {
    return new Runnable() {
      public void run() {
        final HttpGet get = createGetQuery(req, reqs);
        execute(get, new ExecuteAction<Object>() {
          public Object processResult(@NotNull HttpResponse response) throws IOException {
            final HttpEntity entity = response.getEntity();
            System.out.println("Request: " + get.getRequestLine());
            entity.writeTo(System.out);
            System.out.println();
            System.out.println();

            Assert.assertTrue(response.getStatusLine().getStatusCode() == SC_OK);
            return null;
          }
        });
      }
    };
  }

}
