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
