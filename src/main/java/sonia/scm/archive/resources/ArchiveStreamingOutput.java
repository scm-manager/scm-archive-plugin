/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
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
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.archive.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Closeables;

import sonia.scm.archive.ArchiveManager;
import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author Sebastian Sdorra
 */
public class ArchiveStreamingOutput implements StreamingOutput
{

  /**
   * Constructs ...
   *
   *
   * @param manager
   * @param repository
   * @param revision
   * @param path
   */
  public ArchiveStreamingOutput(ArchiveManager manager, Repository repository,
    String revision, String path)
  {
    this.manager = manager;
    this.repository = repository;
    this.revision = revision;
    this.path = path;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param output
   *
   * @throws IOException
   * @throws WebApplicationException
   */
  @Override
  public void write(OutputStream output)
    throws IOException, WebApplicationException
  {
    try
    {
      manager.createArchive(output, repository, revision, path);
    }
    catch (Exception ex)
    {
      throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    }
    finally
    {
      Closeables.closeQuietly(output);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ArchiveManager manager;

  /** Field description */
  private String path;

  /** Field description */
  private Repository repository;

  /** Field description */
  private String revision;
}
