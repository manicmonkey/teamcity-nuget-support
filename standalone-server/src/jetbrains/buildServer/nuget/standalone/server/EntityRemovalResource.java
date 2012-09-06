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

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * @author jbaxter - 03/09/12
 */
@Path("{packageName}/{packageVersion}")
public class EntityRemovalResource {

  @DELETE
  public Response deletePackage(@PathParam("packageName") String packageName,
                                @PathParam("packageVersion") String packageVersion) {

    System.out.println("packageName = " + packageName);
    System.out.println("packageVersion = " + packageVersion);

    final File nuspecFile = new PackageFileLocationUtil().getPackage(packageName + "." + packageVersion);
    nuspecFile.delete();

    return Response.status(Response.Status.OK).build();
  }
}
