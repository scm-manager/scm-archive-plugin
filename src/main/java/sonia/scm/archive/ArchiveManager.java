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

package sonia.scm.archive;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.archive.internal.FileObjectProcessor;
import sonia.scm.archive.internal.PathBuilder;
import sonia.scm.archive.internal.RepositoryWalker;
import sonia.scm.archive.internal.ZipFileObjectProcessor;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

@Singleton
public class ArchiveManager {

  private static final Logger LOG = LoggerFactory.getLogger(ArchiveManager.class);

  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public ArchiveManager(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  public void createArchive(OutputStream stream, Repository repository, String revision, String path) throws IOException {
    LOG.debug("create archive for repository: {}, revision: {}, path: {}", repository.getName(), revision, path);

    try (RepositoryService service = serviceFactory.create(repository); ZipOutputStream zos = new ZipOutputStream(stream)) {
      RepositoryWalker walker = new RepositoryWalker(service, revision);

      PathBuilder pathBuilder = PathBuilder.create(repository, path);
      FileObjectProcessor processor = new ZipFileObjectProcessor(zos, pathBuilder);
      walker.walk(processor, path);
    }
  }
}
