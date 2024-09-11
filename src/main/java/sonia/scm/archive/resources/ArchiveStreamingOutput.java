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

package sonia.scm.archive.resources;

import sonia.scm.archive.ArchiveManager;
import sonia.scm.repository.Repository;

import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

public class ArchiveStreamingOutput implements StreamingOutput {

  private final ArchiveManager manager;
  private final Repository repository;
  private final String revision;
  private final String path;

  public ArchiveStreamingOutput(ArchiveManager manager, Repository repository, String revision, String path) {
    this.manager = manager;
    this.repository = repository;
    this.revision = revision;
    this.path = path;
  }

  @Override
  public void write(OutputStream output) throws IOException {
    manager.createArchive(output, repository, revision, path);
  }
}
