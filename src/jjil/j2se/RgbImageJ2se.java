package jjil.j2se;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.Raster;
import java.awt.image.WritableRenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import jjil.core.Gray8Image;
import jjil.core.RgbImage;

/**
 * RgbImageJ2se is the interface between JJIL's RgbImage and java.awt.Image
 * objects. It provides an implementation of the Show interface for use
 * with the Debug object, which allows images to be displayed and sent
 * to files without being aware of the underlying architecture.<br>
 * It also provides methods for transforming java.awt.Image
 * objects to and from jjil.core.RgbImage objects.<br>
 * @author webb
 *
 */
public class RgbImageJ2se implements jjil.debug.Show {
	static private Graphics graphics = null;
	
	/**
	 * Creates a new instance of RgbImageJ2se. The graphics object
	 * will be used to display images in all subsequently created instances
	 * of RgbImageJ2se.
	 * @param graphics the graphics object to use for all image displays.
	 */
	public RgbImageJ2se(Graphics graphics) {
		RgbImageJ2se.graphics = graphics;
	}
	
	/**
	 * Creates an instance of RgbImageJ2se which will use the common graphics
	 * object for image display.
	 */
	public RgbImageJ2se() {
	}
	
	/**
	 * Sends an RgbImage to the current graphics object.
	 * @param rgb the image to display.
	 */
	public synchronized void toDisplay(RgbImage rgb) {
		if (RgbImageJ2se.graphics != null) {
			WritableRenderedImage im = RgbImageJ2se.toImage(rgb);
			RgbImageJ2se.graphics.drawImage((Image) im, 0, 0, null);
		}
	}
	
	/**
	 * Sends an RgbImage to a file.
	 * @param rgb the RgbImage to send to the file.
	 * @param szFile the path of the file to save the image in. The
	 * file suffix is used to determine the image type (JPEG or PNG).
	 * Storage is in JPEG by default.
	 */
	public void toFile(RgbImage rgb, String szFile) {
		WritableRenderedImage im = RgbImageJ2se.toImage(rgb);
		String szSuffix = "JPG";
		if (szFile.contains(".")) {
			int nSuffix = szFile.lastIndexOf('.');
			szSuffix = szFile.substring(nSuffix + 1);
		}
		File f = new File(szFile);
		try {
			ImageIO.write(im, szSuffix, f);
		} catch (IOException ex) {
			
		}
	}
	
	private static WritableRenderedImage toImage(RgbImage rgb) {
		WritableRenderedImage im = new BufferedImage(
				rgb.getWidth(), 
				rgb.getHeight(), 
				BufferedImage.TYPE_INT_RGB);
		DataBufferInt dbi = new DataBufferInt(
				rgb.getData(),
				rgb.getHeight() * rgb.getWidth());
		Raster r = Raster.createRaster(
				im.getSampleModel(),
				dbi, 
				null);
		im.setData(r);
		return im;
	}
	public static Image toImage(Gray8Image g8) {
		BufferedImage im = new BufferedImage(
				g8.getWidth(), 
				g8.getHeight(), 
				BufferedImage.TYPE_BYTE_GRAY);
		DataBufferByte dbi = new DataBufferByte(
				g8.getData(),
				g8.getHeight() * g8.getWidth());
		Raster r = Raster.createRaster(
				im.getSampleModel(),
				dbi, 
				null);
		im.setData(r);
		return (Image)im;
	}
	
	static public BufferedImage toBufferedImage(RgbImage im){
		BufferedImage bimage = new BufferedImage(im.getWidth(), im.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bimage.createGraphics();
		WritableRenderedImage wim = RgbImageJ2se.toImage(im);
		g.drawImage((Image) wim, 0, 0, null);
		return bimage;
	}
	
	static public RgbImage toRgbImage(Gray8Image im){
		int[] data = new int[im.getHeight()*im.getWidth()];
		byte[] bdata = im.getData();
		for(int i=0;i<data.length;i++)
			data[i] = bdata[i] - Byte.MIN_VALUE;;
		return new RgbImage(im.getWidth(),im.getHeight(),data);
	}
	
	static public BufferedImage toBufferedImage(Gray8Image im){
		return toBufferedImage(toRgbImage(im));
	}
	/**
	 * Transforms an input java.awt.Image to a jjil.core.RgbImage.
	 * The image is read from the ImageProducer until the entire image is
	 * received, and the result is stored into the returned RgbImage.
	 * @param im the input java.awt.Image object.
	 * @return a jjil.core.RgbImage with the same contents as the input im.
	 */
	static public RgbImage toRgbImage(Image im) {
		 class getImageData implements ImageConsumer {
			boolean bComplete = false;
			ImageProducer ip;
			RgbImage rgb;
			
			getImageData(ImageProducer ip) {
				this.ip = ip;
			}
			
			public boolean getComplete() {
				return this.bComplete;
			}
			
			synchronized public RgbImage getRgbImage() {
				return this.rgb;
			}
			public void setHints(int hintflags) {
				
			}
			public void setPixels(
					int x, 
					int y, 
					int w, 
					int h, 
					ColorModel model, 
					int[] pixels, 
					int off, 
					int scansize) {
				if (model instanceof DirectColorModel) {
					// see if we can just directly copy the data because its
					// already laid out the way we want
					DirectColorModel dcm = (DirectColorModel) model;
					if (dcm.getBlueMask()  == 0x000000ff &
						dcm.getGreenMask() == 0x0000ff00 &
						dcm.getRedMask()   == 0x00ff0000) {
						int[] nData = this.rgb.getData();
						for (int i=0; i<h; i++) {
							System.arraycopy(
									pixels, 
									i*scansize + off, 
									nData, 
									(i+y)*this.rgb.getWidth() + x, 
									w);
						}
						return;
					}	
				}
				// can't just copy. Use color conversion
				int[] nData = this.rgb.getData();
				for (int i = 0; i<h; i++) {
					for (int j=0; j<w; j++) {
						int nRgb = model.getRGB(pixels[j+off + i*scansize]);
						nData[(i+y) * this.rgb.getWidth() + (j+x)] = nRgb;
					}
				}
			}
			
			public void setPixels(
                                int x, 
                                int y, 
                                int w, 
                                int h, 
                                ColorModel model, 
                                byte[] pixels, 
                                int off, 
                                int scansize) {
				// can't just copy. Use color conversion
				int[] nData = this.rgb.getData();
				for (int i = 0; i<h; i++) {
					for (int j=0; j<w; j++) {
						int nRgb = model.getRGB(pixels[j+off + i*scansize]);
						nData[(i+y) * this.rgb.getWidth() + (j+x)] = nRgb;
					}
				}				
			}
			
			public void setColorModel(ColorModel model) {
				
			}
			@SuppressWarnings("unchecked")
			public void setProperties(Hashtable props) {
				
			}
			public  void setDimensions(int nWidth, int nHeight) {
				this.rgb = new RgbImage(nWidth, nHeight);
			}
			public void imageComplete(int status) {
				this.bComplete = true;
				this.ip.removeConsumer(this);
			}
		}	
		 
		ImageProducer ip = im.getSource();
		getImageData ic = new getImageData(ip);
		ip.startProduction(ic);
		while (!ic.getComplete()) {
			Thread.yield();
		}
		return ic.getRgbImage();
	}
}
