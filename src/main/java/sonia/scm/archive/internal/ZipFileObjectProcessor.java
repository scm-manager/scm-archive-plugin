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

package sonia.scm.archive.internal;

import sonia.scm.repository.FileObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFileObjectProcessor implements FileObjectProcessor {

  private final ZipOutputStream outputStream;
  private final PathBuilder pathBuilder;

  public ZipFileObjectProcessor(ZipOutputStream outputStream, PathBuilder pathBuilder) {
    this.outputStream = outputStream;
    this.pathBuilder = pathBuilder;
  }

  @Override
  public OutputStream createOutputStream(FileObject file) throws IOException {
    ZipEntry entry = new ZipEntry(pathBuilder.build(file.getPath()));
    outputStream.putNextEntry(entry);
    return new ZipEntryOutputStream(outputStream);
  }

  private static class ZipEntryOutputStream extends OutputStream {

    private final ZipOutputStream delegate;

    private ZipEntryOutputStream(ZipOutputStream delegate) {
      this.delegate = delegate;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      delegate.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
      delegate.write(b);
    }

    @Override
    public void flush() throws IOException {
      delegate.flush();
    }

    @Override
    public void close() throws IOException {
      delegate.closeEntry();
    }

  }

}
