package sonia.scm.archive.resources;

import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArchiveLinkEnricherTest {

  @Mock
  private HalEnricherContext context;

  @Mock
  private HalAppender appender;

  private ArchiveLinkEnricher enricher;

  @BeforeEach
  void setUpObjectUnderTest() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));
    enricher = new ArchiveLinkEnricher(Providers.of(pathInfoStore));
  }

  @Test
  void shouldNotEnrichFiles() {
    FileObject file = new FileObject();
    file.setDirectory(false);
    BrowserResult result = new BrowserResult("42", file);
    when(context.oneRequireByType(BrowserResult.class)).thenReturn(result);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(any(), any());
  }

  @Test
  void shouldEnrichDirectory() {
    FileObject file = new FileObject();
    file.setPath("src/main");
    file.setDirectory(true);
    BrowserResult result = new BrowserResult("42", file);
    when(context.oneRequireByType(BrowserResult.class)).thenReturn(result);
    when(context.oneRequireByType(NamespaceAndName.class)).thenReturn(new NamespaceAndName("hitchhiker", "hog"));

    enricher.enrich(context, appender);

    verify(appender).appendLink("archive", "/v2/archive/hitchhiker/hog/42/src%2Fmain");
  }

}
