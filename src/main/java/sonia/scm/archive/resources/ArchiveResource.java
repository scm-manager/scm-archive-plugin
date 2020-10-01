/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
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
