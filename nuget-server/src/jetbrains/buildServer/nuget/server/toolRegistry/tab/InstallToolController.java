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

package jetbrains.buildServer.nuget.server.toolRegistry.tab;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.controllers.*;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetTool;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetToolManager;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolException;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolsPolicy;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 12:12
 */
public class InstallToolController extends BaseFormXmlController {
  private static final Logger LOG = Logger.getInstance(InstallToolController.class.getName());

  private final String myPath;
  private final NuGetToolManager myToolsManager;
  private final PluginDescriptor myDescriptor;

  public InstallToolController(@NotNull final AuthorizationInterceptor auth,
                               @NotNull final PermissionChecker checker,
                               @NotNull final WebControllerManager web,
                               @NotNull final NuGetToolManager toolsManager,
                               @NotNull final PluginDescriptor descriptor) {
    myToolsManager = toolsManager;
    myDescriptor = descriptor;
    myPath = descriptor.getPluginResourcesPath("tool/nuget-server-tab-install-tool.html");
    auth.addPathBasedPermissionsChecker(myPath, new RequestPermissionsChecker() {
      public void checkPermissions(@NotNull AuthorityHolder authorityHolder,
                                   @NotNull HttpServletRequest request) throws AccessDeniedException {
        checker.assertAccess(authorityHolder);
      }
    });
    web.registerController(myPath, this);
  }

  @NotNull
  public String getPath() {
    return myPath;
  }

  @Override
  protected ModelAndView doGet(HttpServletRequest request, HttpServletResponse response) {
    final Collection<NuGetTool> availableTools = new ArrayList<NuGetTool>();

    final ToolsPolicy pol =
            StringUtil.isEmptyOrSpaces(request.getParameter("fresh"))
                    ? ToolsPolicy.ReturnCached
                    : ToolsPolicy.FetchNew;

    try {
      availableTools.addAll(myToolsManager.getAvailableTools(pol));
    } catch (Exception e) {
      ModelAndView mv = new ModelAndView(myDescriptor.getPluginResourcesPath("tool/installTool-error.jsp"));
      mv.getModel().put("errorText", e.getMessage());
      LOG.warn("Failed to fetch NuGet.Commandline package versions. " + e.getMessage(), e);
      return mv;
    }

    ModelAndView mv = new ModelAndView(myDescriptor.getPluginResourcesPath("tool/installTool-show.jsp"));
    mv.getModelMap().put("available", availableTools);
    mv.getModelMap().put("propertiesBean", new BasePropertiesBean(new HashMap<String, String>()));

    return mv;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response, Element xmlResponse) {
    String toolId = request.getParameter("toolId");
    if (StringUtil.isEmptyOrSpaces(toolId)) {
      ActionErrors ae = new ActionErrors();
      ae.addError("toolId", "Select NuGet.Commandline package version to install");
      ae.serialize(xmlResponse);
      return;
    }

    final String whatToDo = request.getParameter("whatToDo");
    try {
      if ("install".equals(whatToDo)) {
        myToolsManager.installTool(toolId);
        return;
      }

      if ("remove".equals(whatToDo)) {
        myToolsManager.removeTool(toolId);
      }
    } catch (ToolException e) {
      ActionErrors ae = new ActionErrors();
      ae.addError("toolId", "Failed to install package: " + e.getMessage());
      ae.serialize(xmlResponse);
    }
  }
}
