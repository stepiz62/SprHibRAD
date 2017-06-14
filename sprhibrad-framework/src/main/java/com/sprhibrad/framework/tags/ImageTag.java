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

import com.sprhibrad.framework.common.ShrImage;
import com.sprhibrad.framework.common.Utils;

/**
 * renders the place-holder for accessing an image with its preview.
 */
public class ImageTag extends ShrBinaryTag {
	String previewPath;
	String width;
	String height;

	@Override
	String getArguments() {
		return super.getArguments() + previewPath;
	}

	@Override
	protected String getObjectPath() {
		return previewPath;
	}

	@Override
	String rendering() {
		String format = new ShrImage(getBytes(), previewPath, true).getFormatName();
		return Utils.cellWrap(imageTag(getEntityName(), null, getEntityObj().getId(), previewPath, path, format, width,
				height, true));
	}

	public String getPreviewPath() {
		return previewPath;
	}

	public void setPreviewPath(String previewPath) {
		this.previewPath = previewPath;
	}

	@Override
	protected void checkFurtherInheritance(TermTag editableBox) {
		String tagPath = getPreviewPath();
		if ((tagPath == null || tagPath.isEmpty()) && editableBox instanceof DataItem)
			setPreviewPath(((BinaryItem) editableBox).getPreviewPath());
		super.checkFurtherInheritance(editableBox);
	}

	void reset() {
		super.reset();
		previewPath = null;
		width = null;
		height = null;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	@Override
	String getVerbose() {
		return "image";
	}

}
