/*
 * semanticcms-news-taglib - SemanticCMS newsfeeds in a JSP environment.
 * Copyright (C) 2016, 2017, 2020, 2021, 2022, 2023, 2025, 2026  AO Industries, Inc.
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

import static com.aoapps.lang.Strings.nullIfEmpty;
import static com.aoapps.servlet.el.ElUtils.resolveValue;

import com.aoapps.html.servlet.DocumentEE;
import com.semanticcms.core.model.ElementContext;
import com.semanticcms.core.servlet.CaptureLevel;
import com.semanticcms.core.servlet.PageIndex;
import com.semanticcms.core.servlet.PageUtils;
import com.semanticcms.core.servlet.SemanticCMS;
import com.semanticcms.core.taglib.ElementTag;
import com.semanticcms.news.model.News;
import com.semanticcms.news.servlet.impl.NewsImpl;
import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.PageContext;
import java.io.IOException;
import java.io.Writer;

/**
 * A newsfeed item, used to annotate pages and elements of what
 * changes.  The news entries are not typically directly visible, but
 * instead are available via news views, RSS feeds, newsletters, Twitter
 * tweets, and such.
 */
public class NewsTag extends ElementTag<News> {

  private ValueExpression bookExpr;

  public void setBook(ValueExpression book) {
    this.bookExpr = book;
  }

  private ValueExpression pageExpr;

  public void setPage(ValueExpression page) {
    this.pageExpr = page;
  }

  private ValueExpression elementExpr;

  public void setElement(ValueExpression element) {
    this.elementExpr = element;
  }

  private ValueExpression viewExpr;

  public void setView(ValueExpression view) {
    this.viewExpr = view;
  }

  private ValueExpression titleExpr;

  public void setTitle(ValueExpression title) {
    this.titleExpr = title;
  }

  private ValueExpression descriptionExpr;

  public void setDescription(ValueExpression description) {
    this.descriptionExpr = description;
  }

  private ValueExpression pubDateExpr;

  public void setPubDate(ValueExpression pubDate) {
    this.pubDateExpr = pubDate;
  }

  private ValueExpression allowRobotsExpr;

  public void setAllowRobots(ValueExpression allowRobots) {
    this.allowRobotsExpr = allowRobots;
  }

  @Override
  protected News createElement() {
    return new News();
  }

  @Override
  protected void evaluateAttributes(News news, ELContext elContext) throws JspTagException {
    super.evaluateAttributes(news, elContext);
    news.setBook(resolveValue(bookExpr, String.class, elContext));
    news.setTargetPage(resolveValue(pageExpr, String.class, elContext));
    news.setElement(resolveValue(elementExpr, String.class, elContext));
    String viewStr = nullIfEmpty(resolveValue(viewExpr, String.class, elContext));
    if (viewStr == null) {
      viewStr = SemanticCMS.DEFAULT_VIEW_NAME;
    }
    news.setView(viewStr);
    news.setTitle(resolveValue(titleExpr, String.class, elContext));
    news.setDescription(resolveValue(descriptionExpr, String.class, elContext));
    news.setPubDate(PageUtils.toDateTime(resolveValue(pubDateExpr, Object.class, elContext)));
    String allowRobotsStr = resolveValue(allowRobotsExpr, String.class, elContext);
    Boolean allowRobots;
    // Not using Boolean.valueOf to be more specific in parsing, "blarg" is not same as "false".
    if (
        allowRobotsStr == null
            || (allowRobotsStr = allowRobotsStr.trim()).isEmpty()
            || "auto".equalsIgnoreCase(allowRobotsStr)
    ) {
      allowRobots = null;
    } else if ("true".equalsIgnoreCase(allowRobotsStr)) {
      allowRobots = true;
    } else if ("false".equalsIgnoreCase(allowRobotsStr)) {
      allowRobots = false;
    } else {
      // Matches ao-tld-parser:XmlHelper.java
      // Matches semanticcms-changelog-taglib:ReleaseTag.java
      // Matches semanticcms-core-taglib:PageTag.java
      // Matches semanticcms-news-taglib:NewsTag.java
      throw new IllegalArgumentException("Unexpected value for allowRobots, expect one of \"auto\", \"true\", or \"false\": " + allowRobotsStr);
    }
    news.setAllowRobots(allowRobots);
  }

  private ServletContext servletContext;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private PageIndex pageIndex;

  @Override
  protected void doBody(News news, CaptureLevel captureLevel) throws JspException, IOException {
    PageContext pageContext = (PageContext) getJspContext();
    servletContext = pageContext.getServletContext();
    request = (HttpServletRequest) pageContext.getRequest();
    response = (HttpServletResponse) pageContext.getResponse();
    pageIndex = PageIndex.getCurrentPageIndex(request);
    super.doBody(news, captureLevel);
    try {
      NewsImpl.doBodyImpl(servletContext, request, response, news);
    } catch (ServletException e) {
      throw new JspTagException(e);
    }
  }

  @Override
  public void writeTo(Writer out, ElementContext context) throws IOException, ServletException {
    NewsImpl.writeNewsImpl(
        request,
        new DocumentEE(servletContext, request, response, out,
            false, // Do not add extra newlines to JSP
            false  // Do not add extra indentation to JSP
        ),
        context,
        getElement(),
        pageIndex
    );
  }
}
