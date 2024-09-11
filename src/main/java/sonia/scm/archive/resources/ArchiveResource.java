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

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

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
