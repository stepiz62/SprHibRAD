/*
	Copyright (c) 2017, Stefano Pizzocaro. All rights reserved. Use is subject to license terms.

	This file is part of SprHibRAD 1.0.

	SprHibRAD 1.0 is free software: you can redistribute it and/or modify
	it under the terms of the GNU Lesser General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	SprHibRAD 1.0 is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public License
	along with SprHibRAD 1.0.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.sprhibrad.framework.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * It contains all the criteria (filters) that affects the query in a search page.
 * Furthermore it renders, aside, the "add new" command for adding a new element of 
 * the current entity and, when the page is listing the result, the "new search" button to recall the search panel.
 */
public class SearchBox extends ListManagerAccessor  {

	@Override
	public int doStartTag() throws JspException {
		String title = msgMgr().msgOrKey("label.serchCriteria");
		try {
			JspWriter out = out(); 
			out.println("<table class='searchbox'>\r\n" + 
					"			<tr>\r\n" + 
					"				<td>\r\n" + 
					"					<table class='searchbox2'>\r\n" + 
					"						<tr>\r\n" + 
					"							<td><table class='searchbox3'>\r\n" + 
					"									<tr>\r\n" + 
					"										<td>" + title + "</td>\r\n" + 
					"									</tr>\r\n" + 
					"								</table></td>\r\n" + 
					"						</tr>\r\n" + 
					"						<tr>\r\n" + 
					"							<td><table class='searchbox4'>");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws JspException {
		boolean listing = request().getAttribute("listmode") == "list";
		try {
			out().println("							</table></td>\r\n" + 
					"						</tr>\r\n" + 
					"					</table>\r\n" + 
					"				</td>\r\n" + 
					"				<td><table>\r\n");

			if (listing)
				outOneCellRow(styleWrap(href("add", msgMgr().msgOrKey("command.new"), getEntityName(), null), "linkWrap"));
			if (getIteration() > -1)
				outOneCellRow(styleWrap(href((String) request().getAttribute("listmode"), msgMgr().msgOrKey("command.newSearch"), getEntityName(), "-1"), "linkWrap"));

			out().println("				</table></td>\r\n" + 
					"			</tr>\r\n" + 
					"		</table>");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.doEndTag();
	}
	
	private void outOneCellRow(String content) throws IOException {
		out().println("<tr><td>" + content + "</td></tr>"); 
	}
}
