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

package sonia.scm.archive.internal;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.CatCommandBuilder;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.io.OutputStream;

public class RepositoryWalker {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryWalker.class);

  private final RepositoryService service;
  private final String revision;
  private final String startPath;

  public RepositoryWalker(RepositoryService service, String revision, String path) {
    this.service = service;
    this.revision = revision;
    this.startPath = path;
  }

  public void walk(FileObjectProcessor processor) throws IOException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("start repository walk");
      Stopwatch sw = Stopwatch.createStarted();
      doWalk(processor);
      LOG.debug("finish repository walk in {}", sw.stop());
    } else {
      doWalk(processor);
    }
  }

  private void doWalk(FileObjectProcessor processor) throws IOException {
    BrowseCommandBuilder browse = service.getBrowseCommand();
    CatCommandBuilder cat = service.getCatCommand();

    if (!Strings.isNullOrEmpty(revision)) {
      browse.setRevision(revision);
      cat.setRevision(revision);
    }

    browse
      .setRecursive(true)
      .setDisableCache(true)
      .setDisableLastCommit(true)
      .setDisablePreProcessors(true)
      .setDisableSubRepositoryDetection(true);

    doWalk(processor, browse, cat, Strings.nullToEmpty(startPath));
  }

  private void doWalk(FileObjectProcessor processor, BrowseCommandBuilder browse, CatCommandBuilder cat, String path) throws IOException {
    LOG.trace("start walk of directory {}", path);

    BrowserResult result = browse.setPath(path).getBrowserResult();

    // TODO check if this is correct
    // for (FileObject file : result)
    for (FileObject file : result.getFile().getChildren()) {
      if (!file.isDirectory() && !path.equals(file.getPath())) {
        process(processor, cat, file);
      }
    }
  }

  private void process(FileObjectProcessor processor, CatCommandBuilder cat, FileObject file) throws IOException {
    LOG.trace("process file {}", file.getPath());
    try (OutputStream output = processor.createOutputStream(file)) {
      cat.retriveContent(output, file.getPath());
    }
  }

}
