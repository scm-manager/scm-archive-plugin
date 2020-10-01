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


package sonia.scm.archive;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
      RepositoryWalker walker = new RepositoryWalker(service, revision, path);

      walker.walk(new ZipFileObjectProcessor(zos, repository.getName().concat("/")));
    }
  }
}
