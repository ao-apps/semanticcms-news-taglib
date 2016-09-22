/*
 * semanticcms-news-taglib - SemanticCMS newsfeeds in a JSP environment.
 * Copyright (C) 2016  AO Industries, Inc.
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
 * along with semanticcms-news-taglib.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.semanticcms.news.taglib;

import com.aoindustries.encoding.Coercion;
import com.aoindustries.io.TempFileList;
import com.aoindustries.io.buffer.AutoTempFileWriter;
import com.aoindustries.io.buffer.BufferResult;
import com.aoindustries.io.buffer.BufferWriter;
import com.aoindustries.io.buffer.SegmentedWriter;
import com.aoindustries.servlet.filter.TempFileContext;
import com.semanticcms.core.model.ElementContext;
import com.semanticcms.core.servlet.CaptureLevel;
import com.semanticcms.core.servlet.SemanticCMS;
import com.semanticcms.core.taglib.ElementTag;
import com.semanticcms.news.model.News;
import com.semanticcms.news.servlet.impl.NewsImpl;
import java.io.IOException;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import org.joda.time.DateTime;
import org.joda.time.ReadableDateTime;

public class NewsTag extends ElementTag<News> {

	public NewsTag() {
		super(new News());
		element.setView(SemanticCMS.DEFAULT_VIEW_NAME);
	}

	public void setBook(String book) {
		element.setBook(book);
	}

	public void setPage(String page) {
		element.setTargetPage(page);
	}

	public void setElement(String element) {
		this.element.setElement(element);
	}

	public void setView(String view) {
		element.setView(view==null || view.isEmpty() ? SemanticCMS.DEFAULT_VIEW_NAME : view);
	}

	public void setTitle(String title) {
		element.setTitle(title);
	}

	public void setDescription(String description) {
		element.setDescription(description);
	}

	public void setPubDate(Object pubDate) throws JspTagException {
		try {
			element.setPubDate(
				(pubDate instanceof ReadableDateTime) ? (ReadableDateTime)pubDate
				: Coercion.isEmpty(pubDate) ? null
				: new DateTime(pubDate));
		} catch(IOException e) {
			throw new JspTagException(e);
		}
	}

	private BufferResult writeMe;
	@Override
	protected void doBody(CaptureLevel captureLevel) throws JspException, IOException {
		try {
			super.doBody(captureLevel);
			final PageContext pageContext = (PageContext)getJspContext();
			final HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			BufferWriter capturedOut;
			if(captureLevel == CaptureLevel.BODY) {
				// Enable temp files if temp file context active
				capturedOut = TempFileContext.wrapTempFileList(
					new SegmentedWriter(),
					request,
					// Java 1.8: AutoTempFileWriter::new
					new TempFileContext.Wrapper<BufferWriter>() {
						@Override
						public BufferWriter call(BufferWriter original, TempFileList tempFileList) {
							return new AutoTempFileWriter(original, tempFileList);
						}
					}
				);
			} else {
				capturedOut = null;
			}
			try {
				NewsImpl.writeNewsImpl(
					pageContext.getServletContext(),
					request,
					(HttpServletResponse)pageContext.getResponse(),
					capturedOut,
					element
				);
			} finally {
				if(capturedOut != null) capturedOut.close();
			}
			writeMe = capturedOut==null ? null : capturedOut.getResult();
		} catch(ServletException e) {
			throw new JspTagException(e);
		}
	}

	@Override
	public void writeTo(Writer out, ElementContext context) throws IOException {
		if(writeMe != null) writeMe.writeTo(out);
	}
}
