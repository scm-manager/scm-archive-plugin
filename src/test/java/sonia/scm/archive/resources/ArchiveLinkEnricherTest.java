/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.archive.resources;

import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

import java.net.URI;

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
