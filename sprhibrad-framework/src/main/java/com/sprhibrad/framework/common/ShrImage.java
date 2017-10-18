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

package com.sprhibrad.framework.common;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.logging.log4j.LogManager;

public class ShrImage {

	BufferedImage image;
	String formatName;

	public BufferedImage getImage() {
		return image;
	}
	public String getFormatName() {
		return formatName;
	}

	public ShrImage(byte[] bytes, String imageField, boolean onlyFormatName) {
		image = null;
		formatName = null;
		try {
			if (bytes != null) {
				ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
				Iterator iter = ImageIO.getImageReaders(stream);
				ImageReader reader = null;
				if (iter.hasNext())
					reader = (ImageReader) iter.next();
				if (reader == null)
					LogManager.getLogger(getClass()).error("Not a valid image (path=" + imageField + "!");
				else {
					formatName = reader.getFormatName();
					if ( ! onlyFormatName)
						image = ImageIO.read(stream);
				}
				stream.close();
			}
		} catch (IOException e) {
		}
	}
}
