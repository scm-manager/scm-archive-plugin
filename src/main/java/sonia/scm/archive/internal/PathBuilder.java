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
