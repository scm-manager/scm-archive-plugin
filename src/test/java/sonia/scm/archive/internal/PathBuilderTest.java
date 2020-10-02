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

import org.junit.jupiter.api.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.assertj.core.api.Assertions.assertThat;

class PathBuilderTest {

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @Test
  void shouldPrefixWithRepositoryName() {
    shouldPrefixWithRepositoryName("/");
    shouldPrefixWithRepositoryName("");
    shouldPrefixWithRepositoryName(null);
  }

  private void shouldPrefixWithRepositoryName(String startPath) {
    PathBuilder pathBuilder = PathBuilder.create(repository, startPath);
    assertThat(pathBuilder.build("pom.xml")).isEqualTo("HeartOfGold/pom.xml");
    assertThat(pathBuilder.build("/pom.xml")).isEqualTo("HeartOfGold/pom.xml");
    assertThat(pathBuilder.build("src/index.tsx")).isEqualTo("HeartOfGold/src/index.tsx");
    assertThat(pathBuilder.build("/src/index.tsx")).isEqualTo("HeartOfGold/src/index.tsx");
  }

  @Test
  void shouldRemovePrefix() {
    PathBuilder pathBuilder = PathBuilder.create(repository, "src/main/java");
    assertThat(pathBuilder.build("src/main/java/App.java")).isEqualTo("java/App.java");
  }

  @Test
  void shouldNotChangePath() {
    shouldNotChangePath("src");
    shouldNotChangePath("src/");
  }

  private void shouldNotChangePath(String startPath) {
    PathBuilder pathBuilder = PathBuilder.create(repository, startPath);
    assertThat(pathBuilder.build("src/index.tsx")).isEqualTo("src/index.tsx");
    assertThat(pathBuilder.build("src/main/java/App.java")).isEqualTo("src/main/java/App.java");
  }
}
