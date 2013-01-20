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



package sonia.scm.archive.internal;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.io.Closeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.CatCommandBuilder;
import sonia.scm.repository.api.RepositoryService;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class RepositoryWalker
{

  /**
   * the logger for RepositoryWalker
   */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryWalker.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param service
   */
  public RepositoryWalker(RepositoryService service)
  {
    this(service, null, null);
  }

  /**
   * Constructs ...
   *
   *
   * @param service
   * @param revision
   */
  public RepositoryWalker(RepositoryService service, String revision)
  {
    this(service, revision, null);
  }

  /**
   * Constructs ...
   *
   *
   * @param service
   * @param revision
   * @param path
   */
  public RepositoryWalker(RepositoryService service, String revision,
    String path)
  {
    this.service = service;
    this.revision = revision;
    this.startPath = path;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param processor
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public void walk(FileObjectProcessor processor)
    throws IOException, RepositoryException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("start repository walk");

      Stopwatch sw = new Stopwatch().start();

      doWalk(processor);
      logger.debug("finish repository walk in {}", sw.stop());
    }
    else
    {
      doWalk(processor);
    }
  }

  /**
   * Method description
   *
   *
   * @param processor
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private void doWalk(FileObjectProcessor processor)
    throws IOException, RepositoryException
  {
    BrowseCommandBuilder browse = service.getBrowseCommand();

    CatCommandBuilder cat = service.getCatCommand();

    if (!Strings.isNullOrEmpty(revision))
    {
      browse.setRevision(revision);
      cat.setRevision(revision);
    }

    //J-
    browse
      .setRecursive(true)
      .setDisableCache(true)
      .setDisableLastCommit(true)
      .setDisablePreProcessors(true)
      .setDisableSubRepositoryDetection(true);
    //J+

    doWalk(processor, browse, cat, Strings.nullToEmpty(startPath));
  }

  /**
   * Method description
   *
   *
   * @param processor
   * @param browse
   * @param cat
   * @param path
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private void doWalk(FileObjectProcessor processor,
    BrowseCommandBuilder browse, CatCommandBuilder cat, String path)
    throws IOException, RepositoryException
  {
    logger.trace("start walk of directory {}", path);

    BrowserResult result = browse.setPath(path).getBrowserResult();

    for (FileObject file : result)
    {
      if (!file.isDirectory() &&!path.equals(file.getPath()))
      {
        process(processor, cat, file);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param processor
   * @param cat
   * @param file
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private void process(FileObjectProcessor processor, CatCommandBuilder cat,
    FileObject file)
    throws IOException, RepositoryException
  {
    logger.trace("process file {}", file.getPath());

    OutputStream output = null;

    try
    {
      output = processor.createOutputStream(file);

      if (output != null)
      {
        cat.retriveContent(output, file.getPath());
      }
    }
    finally
    {
      Closeables.closeQuietly(output);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String revision;

  /** Field description */
  private RepositoryService service;

  /** Field description */
  private String startPath;
}
