package com.iris.dss.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.bouncycastle.util.encoders.Base64;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeUtil {

	final static String FILE_TYPE_PNG = "png";
	final static int QR_IMAGE_SIZE = 100;
	final static int QR_CONTENT_SIZE = 100;
	//final static double SCALE = 0.9;
	
	public static byte[] constructQRCode(String qrContent) throws WriterException, IOException {
		// Create the ByteMatrix for the QR-Code that encodes the given String
		Hashtable hintMap = new Hashtable();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		//hintMap.put(EncodeHintType.MARGIN, 2);

		final QRCodeWriter qrCodeWriter = new QRCodeWriter();

		//		int qrCodeSize = 200;
		BitMatrix byteMatrix = qrCodeWriter.encode(
				qrContent, BarcodeFormat.QR_CODE, QR_CONTENT_SIZE, QR_CONTENT_SIZE, hintMap);		

		//BitMatrix byteMatrix = qrCodeWriter.encodeBinary(
		//		readFiletoBuffer(qrfile), BarcodeFormat.QR_CODE, qrSize, qrSize, hintMap);

		// Make the BufferedImage that are to hold the QRCode
		BufferedImage image = new BufferedImage(QR_IMAGE_SIZE, QR_IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D graphics = image.createGraphics();


		//		BufferedImage in = ImageIO.read( new File("C:/Users/trlok/Desktop/seristamford_images/QRCodeBackground.v2.png"));
		//		graphics.drawImage(in, 0, 0, null);
		
		// Get the current transform
		//AffineTransform saveAT = graphics.getTransform();
		// Perform transformation
		//graphics.transform(AffineTransform.getScaleInstance(SCALE, SCALE));
		
		//		graphics.transform( AffineTransform.getTranslateInstance(24.0, 24.0) );		
		//		graphics.setColor(new Color(0,0,0,0));		
		//		graphics.setBackground(new Color(0,0,0,0));	

		//		// draw QRcode background
		graphics.fillRect(0, 0, QR_IMAGE_SIZE, QR_IMAGE_SIZE);

		// draw QRcode content
		graphics.setColor(Color.BLACK);

		for (int i = 0; i < QR_CONTENT_SIZE; i++) {
			for (int j = 0; j < QR_CONTENT_SIZE; j++) {				
				if (byteMatrix.get(i, j)) {					
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}		

		// Restore original transform
		//graphics.setTransform(saveAT);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, FILE_TYPE_PNG, baos); //.write(image, fileType, outFile);

		baos.flush();
		return baos.toByteArray();

	}
	
	public static void main(String... args) throws WriterException, IOException {
		String strQRContent = "12345678";
		
		byte[] strQRBuffers = QRCodeUtil.constructQRCode(UUID.randomUUID().toString());
		System.err.println(Base64.toBase64String(strQRBuffers));
	}

}
