package com.hascode.plugin.components;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URLDecoder;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class CodeCreatorImpl implements CodeCreator {
	private static final int SIZE = 230;
	private final Logger log = LoggerFactory.getLogger(CodeCreatorImpl.class);

	public CodeCreatorImpl() {

	}

	@Override
	public BufferedImage createURLCode(final String url)
			throws WriterException, IOException {
		String decodedWebsite = url;
		if (!StringUtils.isEmpty(url)) {
			decodedWebsite = URLDecoder.decode(url, "UTF-8");
		} else {
			log.warn("given url was empty");
		}
		return createImage(decodedWebsite);
	}

	private BufferedImage createImage(final String cardString)
			throws WriterException {
		BitMatrix mtx = null;
		QRCodeWriter writer = new QRCodeWriter();
		mtx = writer.encode(cardString, BarcodeFormat.QR_CODE, SIZE, SIZE);

		if (mtx != null) {
			BufferedImage image = MatrixToImageWriter.toBufferedImage(mtx);
			return image;
		}

		return null;
	}

}