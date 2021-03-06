// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.idea.maven.dom.converters;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.hash.HashSet;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.indices.MavenProjectIndicesManager;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProject;

import java.util.Collection;
import java.util.Set;

public class MavenArtifactCoordinatesGroupIdConverter extends MavenArtifactCoordinatesConverter implements MavenSmartConverter<String> {
  @Override
  protected boolean doIsValid(MavenId id, MavenProjectIndicesManager manager, ConvertContext context) {
    if (StringUtil.isEmpty(id.getGroupId())) return false;

    if (manager.hasGroupId(id.getGroupId())) return true;

        // Check if artifact was found on importing.
    MavenProject mavenProject = findMavenProject(context);
    if (mavenProject != null) {
      for (MavenArtifact artifact : mavenProject.findDependencies(id.getGroupId(), id.getArtifactId())) {
        if (artifact.isResolved()) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  protected Set<String> doGetVariants(MavenId id, MavenProjectIndicesManager manager) {
    return manager.getGroupIds();
  }

  @Nullable
  @Override
  public LookupElement createLookupElement(String s) {
    LookupElementBuilder res = LookupElementBuilder.create(s);
    res = res.withInsertHandler(MavenGroupIdInsertHandler.INSTANCE);
    return res;
  }

  @Override
  public Collection<String> getSmartVariants(ConvertContext convertContext) {
    Set<String> groupIds = new HashSet<>();
    String artifactId = MavenArtifactCoordinatesHelper.getId(convertContext).getArtifactId();
    if (!StringUtil.isEmptyOrSpaces(artifactId)) {
      MavenProjectIndicesManager manager = MavenProjectIndicesManager.getInstance(convertContext.getFile().getProject());
      for (String groupId : manager.getGroupIds()) {
        if (manager.getArtifactIds(groupId).contains(artifactId)) {
          groupIds.add(groupId);
        }
      }
    }
    return groupIds;
  }

  private static class MavenGroupIdInsertHandler implements InsertHandler<LookupElement> {

    public static final InsertHandler<LookupElement> INSTANCE = new MavenGroupIdInsertHandler();

    @Override
    public void handleInsert(@NotNull final InsertionContext context, @NotNull LookupElement item) {
      if (TemplateManager.getInstance(context.getProject()).getActiveTemplate(context.getEditor()) != null) {
        return; // Don't brake the template.
      }

      context.commitDocument();

      PsiFile contextFile = context.getFile();
      if(!(contextFile instanceof XmlFile)) return;

      XmlFile xmlFile = (XmlFile)contextFile;

      PsiElement element = xmlFile.findElementAt(context.getStartOffset());
      XmlTag tag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
      if (tag == null) return;

      XmlTag dependencyTag = tag.getParentTag();

      DomElement domElement = DomManager.getDomManager(context.getProject()).getDomElement(dependencyTag);
      if (!(domElement instanceof MavenDomDependency)) return;

      MavenDomDependency dependency = (MavenDomDependency)domElement;

      String artifactId = dependency.getArtifactId().getStringValue();
      if (StringUtil.isEmpty(artifactId)) return;

      MavenDependencyCompletionUtil.addTypeAndClassifierAndVersion(context, dependency, item.getLookupString(), artifactId);
    }
  }

}
