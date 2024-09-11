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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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
