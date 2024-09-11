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
