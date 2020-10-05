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

  private final BrowseCommandBuilder browse;
  private final CatCommandBuilder cat;

  public RepositoryWalker(RepositoryService service, String revision) {
    browse = createBrowseCommand(service, revision);
    cat = createCatCommand(service, revision);
  }

  private BrowseCommandBuilder createBrowseCommand(RepositoryService service, String revision) {
    BrowseCommandBuilder command = service.getBrowseCommand();
    if (!Strings.isNullOrEmpty(revision)) {
      command.setRevision(revision);
    }
    return command
      .setRecursive(true)
      .setDisableCache(true)
      .setDisableLastCommit(true)
      .setDisablePreProcessors(true)
      .setDisableSubRepositoryDetection(true)
      .setLimit(100000);
  }

  private CatCommandBuilder createCatCommand(RepositoryService service, String revision) {
    CatCommandBuilder command = service.getCatCommand();
    if (!Strings.isNullOrEmpty(revision)) {
      command.setRevision(revision);
    }
    return command;
  }

  public void walk(FileObjectProcessor processor, String startPath) throws IOException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("start repository walk");
      Stopwatch sw = Stopwatch.createStarted();
      doWalk(processor, startPath);
      LOG.debug("finish repository walk in {}", sw.stop());
    } else {
      doWalk(processor, startPath);
    }
  }

  private void doWalk(FileObjectProcessor processor, String path) throws IOException {
    LOG.trace("start walk of directory {}", path);

    BrowserResult result = browse.setPath(path).getBrowserResult();
    process(processor, result.getFile());

  }

  private void process(FileObjectProcessor processor, FileObject file) throws IOException {
    if (file.isDirectory()) {
      processDirectory(processor, file);
    } else {
      processFile(processor, file);
    }
  }

  private void processDirectory(FileObjectProcessor processor, FileObject directory) throws IOException {
    LOG.trace("process directory {}", directory.getPath());
    for (FileObject file : directory.getChildren()) {
        process(processor, file);
    }
  }

  private void processFile(FileObjectProcessor processor, FileObject file) throws IOException {
    LOG.trace("process file {}", file.getPath());
    try (OutputStream output = processor.createOutputStream(file)) {
      cat.retriveContent(output, file.getPath());
    }
  }

}
