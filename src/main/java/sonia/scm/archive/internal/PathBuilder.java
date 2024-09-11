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

import com.google.common.base.Strings;
import sonia.scm.repository.Repository;

public interface PathBuilder {

  String build(String filePath);

  static PathBuilder create(Repository repository, String startPath) {
    if (Strings.isNullOrEmpty(startPath) || "/".equals(startPath)) {
      return new AddPrefixPathBuilder(repository.getName());
    }

    int index = startPath.lastIndexOf('/');
    if (index > 0 && isNotLastChar(startPath, index)) {
      return new RemovePrefixPathBuilder(startPath.substring(0, index + 1));
    }
    return new NoopPathBuilder();
  }

  static boolean isNotLastChar(String value, int index) {
    return index < value.length() - 1;
  }

  class NoopPathBuilder implements PathBuilder {

    @Override
    public String build(String filePath) {
      return filePath;
    }
  }

  class AddPrefixPathBuilder implements PathBuilder {

    private final String prefix;

    public AddPrefixPathBuilder(String prefix) {
      this.prefix = ensureEndsWithSlash(prefix);
    }

    private String ensureEndsWithSlash(String prefix) {
      if (!prefix.endsWith("/")) {
        return prefix + "/";
      }
      return prefix;
    }

    @Override
    public String build(String filePath) {
      if (filePath.startsWith("/")) {
        return prefix + filePath.substring(1);
      }
      return prefix + filePath;
    }
  }

  class RemovePrefixPathBuilder implements PathBuilder {

    private final String prefix;

    public RemovePrefixPathBuilder(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public String build(String filePath) {
      if (filePath.startsWith(prefix)) {
        return filePath.substring(prefix.length());
      }
      return filePath;
    }
  }

}
