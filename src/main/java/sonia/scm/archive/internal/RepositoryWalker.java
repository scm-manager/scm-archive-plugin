/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
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
