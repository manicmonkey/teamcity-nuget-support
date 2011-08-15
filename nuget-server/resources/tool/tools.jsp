<%--
  ~ Copyright 2000-2011 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<%@ include file="/include-internal.jsp" %>
<jsp:useBean id="tools" type="java.util.Collection<jetbrains.buildServer.nuget.server.toolRegistry.tab.LocalTool>" scope="request"/>
<jsp:useBean id="installerUrl" type="java.lang.String" scope="request"/>
<jsp:useBean id="updateUrl" type="java.lang.String" scope="request"/>

<c:set var="actualUpdateUrl"><c:url value="${updateUrl}"/></c:set>
<bs:refreshable containerId="nugetPackagesList" pageUrl="${actualUpdateUrl}">
<c:set var="installedPluginsCount" value="${fn:length(tools)}"/>
<p>
  TeamCity NuGet plugin requires to configure NuGet.Exe Command Line clients.
  There are
  <strong><c:out value="${installedPluginsCount}"/></strong>
  plugin<bs:s val="${installedPluginsCount}"/> installed.
</p>

<h2 class="noBorder">Installed NuGet Versions</h2>
<c:choose>
  <c:when test="${installedPluginsCount eq 0}">
    <div>There are no installed NuGet.exe</div>
  </c:when>
  <c:otherwise>
      <table class="dark borderBottom" cellpadding="0" cellspacing="0" style="width: 30em;">
        <thead>
        <tr>
          <th class="header" style="width: 66%">Version</th>
          <th class="header"></th>
        </tr>
        </thead>
        <tbody>
          <c:forEach var="tool" items="${tools}">
            <tr>
              <td><c:out value="${tool.version}"/></td>
              <td>
                <c:choose>
                  <c:when test="${tool.state.installed}">
                    <a href="#" onclick="BS.NuGet.Tools.removeTool('<bs:forJs>${tool.id}</bs:forJs>');">Remove</a>
                  </c:when>
                  <c:when test="${tool.state.installing}">
                    <bs:commentIcon text="Messages"/>
                    Installing...
                  </c:when>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
        </tbody>
    </table>
  </c:otherwise>
</c:choose>
</bs:refreshable>

<div class="addNew">
  <a href="#" onclick="return BS.NuGet.Tools.InstallPopup.show();">
    Install
    <c:if test="${installedPluginsCount gt 0}">addintional versions of</c:if>
    NuGet.exe Command Line
    <forms:saving id="nugetInstallLinkSaving"/>
  </a>
</div>

<c:set var="actualInstallerUrl"><c:url value="${installerUrl}"/></c:set>
<bs:modalDialog
        formId="nugetInstallForm"
        title="Install NuGet.exe Command Line"
        action="${actualInstallerUrl}"
        closeCommand="BS.NuGet.Tools.InstallPopup.close();"
        saveCommand="BS.NuGet.Tools.InstallPopup.save();">
  <div id="nugetInstallFormLoading">
    <forms:saving style="float: left; display:block;"/>
    Discovering available NuGet.exe Command Line versions
  </div>

  <bs:refreshable containerId="nugetInstallFormResresh" pageUrl="${actualInstallerUrl}">

  </bs:refreshable>

  <div class="popupSaveButtonsBlock">
    <a href="javascript://" onclick="BS.NuGet.Tools.InstallPopup.close();" class="cancel">Cancel</a>
    <input class="submitButton" type="submit" value="Install" id="installNuGetApplyButton" />
    <a href="javascript://" onclick="BS.NuGet.Tools.InstallPopup.refreshForm(true);" class="cancel">Refresh</a>
    <forms:saving id="installNuGetApplyProgress"/>
    <br clear="all"/>
  </div>
</bs:modalDialog>

<script type="text/javascript">
  BS.NuGet.Tools.installUrl = "<bs:forJs>${actualInstallerUrl}</bs:forJs>";
  BS.NuGet.Tools.InstallPopup.disableSubmit();
</script>