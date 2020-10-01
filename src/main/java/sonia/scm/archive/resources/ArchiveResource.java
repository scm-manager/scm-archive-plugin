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
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.archive.ArchiveManager;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

@OpenAPIDefinition(tags = {
  @Tag(name = "Archive", description = "Archive related endpoints")
})
@Path(ArchiveResource.PATH)
public class ArchiveResource {

  static final String PATH = "v2/archive";
  private static final String MEDIA_TYPE = "application/zip";


  private final ArchiveManager archiveManager;
  private final RepositoryManager repositoryManager;

  @Inject
  public ArchiveResource(RepositoryManager repositoryManager, ArchiveManager archiveManager) {
    this.archiveManager = archiveManager;
    this.repositoryManager = repositoryManager;
  }

  @GET
  @Path("{namespace}/{name}/{revision}")
  @Produces(MEDIA_TYPE)
  @Operation(
    summary = "Get root archive",
    description = "Returns all files of the given revision as zip.",
    tags = "Archive",
    operationId = "get_root_archive"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MEDIA_TYPE
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "404",
    description = "repository not found",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response rootArchive(
    @PathParam("namespace") String namespace, @PathParam("name") String name,
    @PathParam("revision") String revision) {
    return archive(namespace, name, revision, Util.EMPTY_STRING);
  }

  @GET
  @Path("{namespace}/{name}/{revision}/{path}")
  @Produces(MEDIA_TYPE)
  @Operation(
    summary = "Get archive",
    description = "Returns all files of the given directory as zip.",
    tags = "Archive",
    operationId = "get_archive"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MEDIA_TYPE
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "404",
    description = "repository not found",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response archive(
    @PathParam("namespace") String namespace, @PathParam("name") String name,
    @PathParam("revision") String revision,
    @PathParam("path") String path) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    Repository repository = repositoryManager.get(namespaceAndName);

    if (repository == null) {
      throw notFound(entity(Repository.class, namespaceAndName.toString()));
    }

    return Response.ok(new ArchiveStreamingOutput(archiveManager, repository, revision, path))
      .header("Content-Disposition", createContentDisposition(repository, revision))
      .build();
  }

  private String createContentDisposition(Repository repository, String revision) {
    String name = repository.getName();
    if (!Strings.isNullOrEmpty(revision)) {
      name += "." + revision;
    }
    name += ".zip";
    return HttpUtil.createContentDispositionAttachmentHeader(name);
  }

}
