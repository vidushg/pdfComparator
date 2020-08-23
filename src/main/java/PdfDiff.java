package main.java;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import com.aspose.pdf.Document;
import com.aspose.pdf.Page;
import com.aspose.pdf.devices.PngDevice;
import com.aspose.pdf.devices.Resolution;
import com.becomingmachinic.common.Strings;
import com.becomingmachinic.common.document.MRectangle;

/**
 * TODO: classlevel description
 * @author vidush
 *
 */

public class PdfDiff {
	protected static Pattern WHITESPACE_PATTERN = Pattern.compile("[ \r\n\t]+");
	
	protected final boolean removeWhitespace;
	protected final List<MRectangle> regions;
	
	public PdfDiff() {
		this(true, null);
	}
	/**
	 * Constructor setting defaults for creating PdfDiff objects
	 * @param removeWhitespace Boolean to establish whether or not text from file should have whitespace removed
	 * @param regions  List of regions on the document, needed if comparing only specific regions of documents
	 */
	public PdfDiff(boolean removeWhitespace, List<MRectangle> regions) {
		this.removeWhitespace = removeWhitespace;
		this.regions = regions;
	}
	
	/**
	 * compares two pdfs by converting them to long strings of text, and then checking equality, can also check corresponding regions 
	 * @param document1 File to compare
	 * @param document2 File to compare
	 * @return boolean, true if files are the same, false if they aren't
	 * @throws PdfDocumentException
	 */
	
	public boolean diff(File document1, File document2) throws PdfDocumentException {
		if (document1 != null && document1.isFile() && document2 != null && document2.isFile()) {
			try (PdfDocument pdfDocument1 = new PdfDocument(document1)) {
				try (PdfDocument pdfDocument2 = new PdfDocument(document2)) {
					if (pdfDocument1.getPageCount() == pdfDocument2.getPageCount()) {
						for (int i = 1; i <= pdfDocument1.getPageCount(); i++) {
							if (regions != null && !regions.isEmpty()) {
								for (MRectangle rectangle : this.regions) {
									String text1 = extractText(pdfDocument1, i, rectangle);
									String text2 = extractText(pdfDocument2, i, rectangle);
									if (!text1.equals(text2)) {
										return false;
									}
								}
							} else {
								String text1 = extractText(pdfDocument1, i, null);
								String text2 = extractText(pdfDocument2, i, null);
								if (!text1.equals(text2)) {
									return false;
								}
							}
						}
						// All pages match
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected String extractText(PdfDocument document, int pageNumber, MRectangle rectangle) {
		String text = Strings.nullToEmpty(rectangle != null ? document.extractText(pageNumber, rectangle) : document.extractText(pageNumber));
		if (this.removeWhitespace) {
			return WHITESPACE_PATTERN.matcher(text).replaceAll("");
		}
		return text;
	}
	
	/**
	 * Compares two pdfs by converting every page to a low-res image, and then comparing pixels, trying to fail ASAP
	 * @param file1 File to compare
	 * @param file2 File to compare
	 * @return a boolean , true if files are the same, else false
	 */
	
	public static boolean imageDiff (File file1, File file2) {
		if (file1 != null && file1.isFile() && file2 != null && file2.isFile()) {
			
			Document doc1 = new Document(file1.getAbsolutePath());
			Document doc2 = new Document(file2.getAbsolutePath());
			
			
			if(doc1.getPages().size()!= doc2.getPages().size())
				return false;
			
			int length = doc1.getPages().size();
			int totalPageDiff=0;
			int totalPageDiffThreshold=0;
			int pixelThreshold=0;
			int singlePageThreshold=0;
			int pixelDiff;
			int singlePageDiff;
			
			PngDevice device1 = new PngDevice(new Resolution(25));
			PngDevice device2 = new PngDevice(new Resolution(25));
			
			
			//iterating through pages
			for (int page = 1; page <= length; page++) {
				
				Page pg1 = doc1.getPages().get_Item(page);
				Page pg2 = doc2.getPages().get_Item(page);   
				
				//Time notes: Image processing takes ~7500ms;; processToBufferedImage takes a significant portion of total time
				
				BufferedImage bfImage1 = device1.processToBufferedImage(pg1);
				BufferedImage bfImage2 = device2.processToBufferedImage(pg2);
				
				int width1 = bfImage1.getWidth();
				int height1 = bfImage1.getHeight();
				int width2 = bfImage2.getWidth();
				int height2 = bfImage2.getHeight();
				
				if(width1 != width2 || height1 != height2) {
					return false;
				}
				
				//now examining every pixel
				singlePageDiff = 0;
				for(int i = 0; i < width1; i++)
					for(int j = 0; j < height1; j++) {
						pixelDiff = rgbPixelDiff(bfImage1.getRGB(i, j), bfImage2.getRGB(i, j));
						if (pixelDiff > pixelThreshold)
						{
							System.out.println("pixdiff: "+pixelDiff+"  at "+i+"x"+j);
							return false;
						}
						else singlePageDiff += pixelDiff;
					}
				
				if (singlePageDiff > singlePageThreshold)
				{
					System.out.println("singdiff: "+singlePageDiff);
					
					return false;
				}
				else totalPageDiff += singlePageDiff;
				
			}
			System.out.println("totaldiff: "+totalPageDiff);
			
			return (totalPageDiff>totalPageDiffThreshold?false:true);
		}
		return false;
	}
	
	
	
	/**
	 * helper method for imageDiff
	 * @param rgbA RGB value returned from a pixel of a BufferedImage	
	 * @param rgbB RGB value returned from a pixel of a BufferedImage
	 * @return integer rgb value representing the difference in the two input values
	 */
	private static int rgbPixelDiff(int rgbA, int rgbB) {
		int difference=0;
		
		int redA = (rgbA >> 16) & 0xff; 
		int greenA = (rgbA >> 8) & 0xff; 
		int blueA = (rgbA) & 0xff; 
		int redB = (rgbB >> 16) & 0xff; 
		int greenB = (rgbB >> 8) & 0xff; 
		int blueB = (rgbB) & 0xff; 
		difference += Math.abs(redA - redB); 
		difference += Math.abs(greenA - greenB); 
		difference += Math.abs(blueA - blueB);
		
		
		return difference;
	}
	
}