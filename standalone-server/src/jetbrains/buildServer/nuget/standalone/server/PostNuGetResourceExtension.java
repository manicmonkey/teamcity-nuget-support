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

import com.sun.jersey.multipart.FormDataParam;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackageFile;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackageLoadException;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.odata4j.core.ODataConstants;
import org.odata4j.producer.resources.ServiceDocumentResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author jbaxter - 16/08/12
 */
@Path("")
public class PostNuGetResourceExtension extends ServiceDocumentResource {

  private static final Logger log = Logger.getLogger(PostNuGetResourceExtension.class.getName());

  @PUT
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces({ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8, ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8, ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8})
  public Response postEntity(@FormDataParam("package") InputStream payload) throws Exception {

    final File tempFile = saveToTempFile(payload);
    String packageId = getPackageId(tempFile);
    moveToRepository(tempFile, packageId);

    return Response.status(Response.Status.CREATED).build();
  }

  private File saveToTempFile(InputStream payload) throws IOException {

    final File tempFile = File.createTempFile("upload", ".nupkg");
    FileUtils.copyInputStreamToFile(payload, tempFile);
    return tempFile;
  }

  private String getPackageId(final File tempFile) throws PackageLoadException {
    final Map<String, String> properties = new LocalNuGetPackageItemsFactory().loadPackage(new PackageFile() {
      public long getSize() {
        return tempFile.length();
      }

      @NotNull
      public Date getLastUpdated() {
        return new Date(tempFile.lastModified());
      }

      @NotNull
      public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(new FileInputStream(tempFile));
      }
    });

    return new Entry(0, properties).getKey();
  }

  private void moveToRepository(File tempFile, String packageId) throws IOException {
    final File nuspecFile = new PackageFileLocationUtil().getPackage(packageId);
    log.info("Storing nuspec package [" + packageId + "] at location [" + nuspecFile + "]");
    FileUtils.moveFile(tempFile, nuspecFile);
  }
}