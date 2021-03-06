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
<jsp:useBean id="available"
             type="java.util.Collection<jetbrains.buildServer.nuget.server.toolRegistry.NuGetTool>"
             scope="request"/>
<div id="nugetInstallFormResreshInner">
  <c:choose>
    <c:when test="${fn:length(available) eq 0}">
      No other NuGet command line versions available
    </c:when>
    <c:otherwise>
      <table class="runnerFormTable">
        <tr>
          <td colspan="2">
            Select NuGet command line version to download:
          </td>
        </tr>
        <tr>
          <th><label for="toolId">Version:<l:star/></label></th>
          <td>
            <input type="hidden" name="whatToDo" value="install" />
            <forms:select name="toolId" style="width:15em;">
              <forms:option value="">-- Please choose version --</forms:option>
              <c:forEach var="t" items="${available}">
                <forms:option value="${t.id}"><c:out value="${t.version}"/></forms:option>
              </c:forEach>
            </forms:select>
            <span class="error" id="error_toolId"></span>
          </td>
        </tr>
      </table>
    </c:otherwise>
  </c:choose>
  <script type="text/javascript">
    BS.NuGet.Tools.InstallPopup.enableSubmit();
  </script>
</div>

