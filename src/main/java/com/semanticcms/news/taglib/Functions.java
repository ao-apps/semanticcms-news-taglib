/*
 * semanticcms-news-taglib - SemanticCMS newsfeeds in a JSP environment.
 * Copyright (C) 2016, 2021, 2022, 2025, 2026  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of semanticcms-news-taglib.
 *
 * semanticcms-news-taglib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * semanticcms-news-taglib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with semanticcms-news-taglib.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.semanticcms.news.taglib;

import static com.aoapps.servlet.filter.FunctionContext.getRequest;
import static com.aoapps.servlet.filter.FunctionContext.getResponse;
import static com.aoapps.servlet.filter.FunctionContext.getServletContext;

import com.semanticcms.core.model.Page;
import com.semanticcms.news.model.News;
import com.semanticcms.news.servlet.NewsUtils;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * Tag library function implementations.
 */
public final class Functions {

  /** Make no instances. */
  private Functions() {
    throw new AssertionError();
  }

  /**
   * Gets all the new items in the given page and below, ordered by <code>pubDate desc, page</code>.
   */
  public static List<News> findAllNews(Page page) throws ServletException, IOException {
    return NewsUtils.findAllNews(
        getServletContext(),
        getRequest(),
        getResponse(),
        page
    );
  }
}
