package com.hascode.plugin.components;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.google.zxing.WriterException;

public interface CodeCreator {

	public abstract BufferedImage createURLCode(final String url)
			throws WriterException, IOException;

}