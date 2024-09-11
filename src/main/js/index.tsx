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

import React from "react";
import { binder } from "@scm-manager/ui-extensions";
import { File, Link } from "@scm-manager/ui-types";

import styled from "styled-components";
import { FC } from "react";
import { useTranslation } from "react-i18next";

const Button = styled.a`
  width: 50px;
  &:hover {
    color: #33b2e8;
  }
`;

type Props = {
  sources: File;
};

const ArchiveDownload: FC<Props> = ({ sources }) => {
  const [t] = useTranslation("plugins");
  const link = sources._links.archive as Link;
  return (
    <Button className="button" href={link.href} title={t("scm-archive-plugin.button.title")}>
      <i className="fas fa-file-archive" />
    </Button>
  );
};

binder.bind("repos.sources.actionbar", ArchiveDownload, props => !!props.sources?._links.archive);
