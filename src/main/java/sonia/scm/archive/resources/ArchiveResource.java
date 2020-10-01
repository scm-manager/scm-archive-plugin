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

import com.google.common.base.Strings;

import sonia.scm.archive.ArchiveManager;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

// TODO fix path
@Path("plugins/archive")
public class ArchiveResource {

  private final ArchiveManager archiveManager;
  private final RepositoryManager repositoryManager;

  @Inject
  public ArchiveResource(RepositoryManager repositoryManager, ArchiveManager archiveManager) {
    this.archiveManager = archiveManager;
    this.repositoryManager = repositoryManager;
  }

  // TODO namespace/name instead of id
  @GET
  @Path("{repositoryId}.zip")
  @Produces("application/zip")
  public Response getArchive(@PathParam("repositoryId") String repositoryId, @QueryParam("revision") String revision, @QueryParam("path") String path) {
    Repository repository = repositoryManager.get(repositoryId);

    if (repository == null) {
      // TODO exception with context
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    StreamingOutput content = new ArchiveStreamingOutput(archiveManager, repository, revision, path);
    ResponseBuilder builder = Response.ok(content);
    builder.header("Content-Disposition", createContentDisposition(repository, revision));
    return builder.build();
  }

  private String createContentDisposition(Repository repository, String revision) {
    // TODO check httputil
    StringBuilder cd = new StringBuilder("attachment; filename=\"");

    cd.append(repository.getName());

    if (!Strings.isNullOrEmpty(revision)) {
      cd.append(".").append(revision);
    }

    return cd.append(".zip\"").toString();
  }

}
