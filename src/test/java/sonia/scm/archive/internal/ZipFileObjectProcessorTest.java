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

import org.apache.commons.compress.archivers.zip.UnicodePathExtraField;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.FileObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ZipFileObjectProcessorTest {

  @Test
  void shouldWriteUnicodeFileNameWithCompatibilityMetadata() throws IOException {
    ZipArchiveEntry entry = createAndReadEntry("blöd.txt");

    assertThat(entry.getName()).isEqualTo("blöd.txt");
    assertThat(entry.getGeneralPurposeBit().usesUTF8ForNames()).isTrue();
    assertThat(entry.getExtraField(UnicodePathExtraField.UPATH_ID)).isInstanceOf(UnicodePathExtraField.class);
  }

  @Test
  void shouldKeepAsciiFileNameAndContent() throws IOException {
    byte[] archive = createArchive("readme.txt");

    try (ZipArchiveInputStream input = new ZipArchiveInputStream(new ByteArrayInputStream(archive))) {
      ZipArchiveEntry entry = input.getNextZipEntry();
      assertThat(entry.getName()).isEqualTo("readme.txt");
      assertThat(input.readAllBytes()).isEqualTo("content".getBytes(StandardCharsets.UTF_8));
    }
  }

  private ZipArchiveEntry createAndReadEntry(String fileName) throws IOException {
    byte[] archive = createArchive(fileName);
    try (ZipArchiveInputStream input = new ZipArchiveInputStream(new ByteArrayInputStream(archive))) {
      return input.getNextZipEntry();
    }
  }

  private byte[] createArchive(String fileName) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try (ZipArchiveOutputStream output = new ZipArchiveOutputStream(buffer)) {
      ZipFileObjectProcessor processor = new ZipFileObjectProcessor(output, filePath -> filePath);
      FileObject file = new FileObject();
      file.setPath(fileName);
      try (OutputStream entry = processor.createOutputStream(file)) {
        entry.write("content".getBytes(StandardCharsets.UTF_8));
      }
    }
    return buffer.toByteArray();
  }
}
