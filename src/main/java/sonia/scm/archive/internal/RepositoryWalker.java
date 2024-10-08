/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
    LOG.debug("start repository walk");
    Stopwatch sw = Stopwatch.createStarted();
    doWalk(processor, startPath);
    LOG.debug("finish repository walk in {}", sw.stop());
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
