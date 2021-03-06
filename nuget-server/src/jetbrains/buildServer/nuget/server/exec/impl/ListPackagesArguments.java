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

package jetbrains.buildServer.nuget.server.exec.impl;

import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 18:22
 */
public class ListPackagesArguments {

  public void encodeParameters(@NotNull final File file, @NotNull Collection<SourcePackageReference> refs) throws IOException {
    final Element root = new Element("nuget-packages");
    final Element packages = new Element("packages");
    for (SourcePackageReference ref : refs) {
      final Element pkg = new Element("package");
      pkg.setAttribute("id", ref.getPackageId());
      final String source = ref.getSource();
      if (source != null) {
        pkg.setAttribute("source", source);
      }
      final String spec = ref.getVersionSpec();
      if (spec != null) {
        pkg.setAttribute("versions", spec);
      }
      packages.addContent((Content)pkg);
    }
    root.addContent((Content)packages);

    final OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
    try {
      XmlUtil.saveDocument(new Document(root), os);
    } finally {
      FileUtil.close(os);
    }
  }

  private static String trim(String s) {
    if (s == null || jetbrains.buildServer.util.StringUtil.isEmptyOrSpaces(s)) return null;
    return s;
  }

  public Map<SourcePackageReference, Collection<SourcePackageInfo>> decodeParameters(@NotNull final File file) throws IOException {
    final Element root;
    try {
      root = FileUtil.parseDocument(file);
    } catch (final JDOMException e) {
      throw new IOException("Failed to parse " + file + ". " + e.getMessage()) {{initCause(e);}};
    }

    if (!root.getName().equals("nuget-packages")) throw new IOException("Invalid xml");
    final Element packages = root.getChild("packages");
    if (packages == null) throw new IOException("Invalid xml");

    final Map<SourcePackageReference, Collection<SourcePackageInfo>> result = new HashMap<SourcePackageReference, Collection<SourcePackageInfo>>();

    for (Object pElement : packages.getChildren("package")) {
      final Element pkg = (Element) pElement;

      final String id = trim(pkg.getAttributeValue("id"));
      final String spec = trim(pkg.getAttributeValue("versions"));
      final String source = trim(pkg.getAttributeValue("source"));

      if (id == null) continue;
      final SourcePackageReference ref = new SourcePackageReference(source, id, spec);

      final Collection<SourcePackageInfo> versions = new ArrayList<SourcePackageInfo>();
      final Element pkgChild = pkg.getChild("package-entries");
      for (Object pEntry : pkgChild.getChildren("package-entry")) {
        final Element entry = (Element) pEntry;

        final String version = entry.getAttributeValue("version");
        if (version == null || jetbrains.buildServer.util.StringUtil.isEmptyOrSpaces(version)) continue;

        versions.add(ref.toInfo(version));
      }

      result.put(ref, versions);
    }

    return result;
  }
}
