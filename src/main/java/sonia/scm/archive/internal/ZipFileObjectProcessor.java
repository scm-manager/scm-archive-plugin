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

import com.google.common.base.Strings;

import sonia.scm.repository.FileObject;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class ZipFileObjectProcessor implements FileObjectProcessor
{

  /**
   * Constructs ...
   *
   *
   * @param outputStream
   * @param namePrefix
   */
  public ZipFileObjectProcessor(ZipOutputStream outputStream, String namePrefix)
  {
    this.outputStream = outputStream;
    this.namePrefix = Strings.nullToEmpty(namePrefix);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param file
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public OutputStream createOutputStream(FileObject file) throws IOException
  {
    ZipEntry entry = new ZipEntry(namePrefix.concat(file.getPath()));

    outputStream.putNextEntry(entry);

    return new ZipEntryOutputStream(outputStream);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/01/17
   * @author         Enter your name here...
   */
  private static class ZipEntryOutputStream extends OutputStream
  {

    /**
     * Constructs ...
     *
     *
     * @param delegate
     */
    public ZipEntryOutputStream(ZipOutputStream delegate)
    {
      this.delegate = delegate;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException
    {
      delegate.closeEntry();
    }

    /**
     * Method description
     *
     *
     * @throws IOException
     */
    @Override
    public void flush() throws IOException
    {
      delegate.flush();
    }

    /**
     * Method description
     *
     *
     * @param b
     *
     * @throws IOException
     */
    @Override
    public void write(int b) throws IOException
    {
      delegate.write(b);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private ZipOutputStream delegate;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String namePrefix;

  /** Field description */
  private ZipOutputStream outputStream;
}
