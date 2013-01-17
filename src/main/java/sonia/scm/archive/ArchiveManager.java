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



package sonia.scm.archive;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.archive.internal.RepositoryWalker;
import sonia.scm.archive.internal.ZipFileObjectProcessor;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

import java.util.zip.ZipOutputStream;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class ArchiveManager
{

  /**
   * the logger for ArchiveManager
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ArchiveManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param serviceFactory
   */
  @Inject
  public ArchiveManager(RepositoryServiceFactory serviceFactory)
  {
    this.serviceFactory = serviceFactory;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param stream
   * @param repository
   * @param revision
   * @param path
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public void createArchive(OutputStream stream, Repository repository,
    String revision, String path)
    throws IOException, RepositoryException
  {
    //J-
    logger.debug("create archive for repository: {}, revision: {}, path: {}",
      new Object[] { repository.getName(), revision, path });
    //J+

    RepositoryService service = null;
    ZipOutputStream zos = new ZipOutputStream(stream);

    try
    {
      service = serviceFactory.create(repository);

      RepositoryWalker walker = new RepositoryWalker(service, revision, path);

      walker.walk(new ZipFileObjectProcessor(zos));
    }
    finally
    {
      Closeables.closeQuietly(zos);
      Closeables.closeQuietly(service);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private RepositoryServiceFactory serviceFactory;
}
