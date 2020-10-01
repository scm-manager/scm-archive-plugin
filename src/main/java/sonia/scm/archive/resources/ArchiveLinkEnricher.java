package sonia.scm.archive.resources;

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

import javax.inject.Inject;
import javax.inject.Provider;


@Extension
@Enrich(BrowserResult.class)
public class ArchiveLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public ArchiveLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStore) {
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    BrowserResult result = context.oneRequireByType(BrowserResult.class);
    FileObject file = result.getFile();
    if (file.isDirectory()) {
      NamespaceAndName namespaceAndName = context.oneRequireByType(NamespaceAndName.class);
      String link = createLink(result, file, namespaceAndName);
      appender.appendLink("archive", link);
    }
  }

  private String createLink(BrowserResult result, FileObject file, NamespaceAndName namespaceAndName) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), ArchiveResource.class);
    return linkBuilder.method("archive")
      .parameters(namespaceAndName.getNamespace(), namespaceAndName.getName(), result.getRevision(), file.getPath())
      .href();
  }
}
