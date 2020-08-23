package test;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import main.java.PdfDiff;


public class PdfImageDiffTest {
	
	/**
	 * Test a file against itself
	 * @throws IOException
	 */
	@Test
	public void selfTest() throws IOException {
	
	//Timenotes: File fetch takes 4ms
	File file1 = new File(System.getProperty("java.io.tmpdir"), "expected_sample7.pdf");
	IOUtils.getGDriveDocument("1bXdBSoz9taBj2bg03i2sUr5Ri4tlZNVu", file1);
	
		Assertions.assertTrue(PdfDiff.imageDiff(file1, file1));
		
	
	}

	//only text on both docs, same format but differing text on both
	/**
	 * Test two documents with plain text, in the same format, but with differing text
	 * @throws IOException
	 */
	@Test
	public void onlyText() throws IOException {
	  
		File file1 = new File("/Users/vidush/git/Docs/RandomText1page.pdf");
		File file2 = new File("/Users/vidush/git/Docs/Random#2Text1page.pdf");
		
		Assertions.assertFalse(PdfDiff.imageDiff(file1, file2));
	}
	
	/**
	 * Test two documents with no content i.e blank pages, but different margins
	 * @throws IOException
	 */
	@Test
	public void blankMargins() throws IOException {
		
		File file1 = new File("/Users/vidush/git/Docs/Blank1page.pdf");
		File file2 = new File("/Users/vidush/git/Docs/Blank1pageNarrowMargins.pdf");

		Assertions.assertTrue(PdfDiff.imageDiff(file1, file2));

		
	}
	
	/**
	 * Tests two documents which differ in the spelling of the word 'becoming' 
	 * 	'becaming' vs 'becoming'
	 * @throws IOException
	 */
	@Test
	public void wordDiff() throws IOException {
		
		File file1 = new File("/Users/vidush/git/Docs/WordTest1.pdf");
		File file2 = new File("/Users/vidush/git/Docs/WordTest2.pdf");

		Assertions.assertFalse(PdfDiff.imageDiff(file1, file2));
		
	}
	

	/**
	 * Tests two files with the same image, but the image is grayscaled in file1
	 * @throws IOException
	 */
	@Test
	public void colorDiff() throws IOException {
		
		File file1 = new File("/Users/vidush/git/Docs/ImageTest1.pdf");
		File file2 = new File("/Users/vidush/git/Docs/ImageTest2.pdf");
		
		Assertions.assertFalse(PdfDiff.imageDiff(file1, file2));
		
	}


	/**
	 * Tests two files with the same image, but the dimensions differ slightly
	 * @throws IOException
	 */
	@Test
	public void imageSizeDiff() throws IOException {
		
		File file1 = new File("/Users/vidush/git/Docs/ImageTest2.pdf");
		File file2 = new File("/Users/vidush/git/Docs/ImagePropTest.pdf");
		
		Assertions.assertFalse(PdfDiff.imageDiff(file1, file2));
	}
	
	
	/**
	 * Tests two files where the font of only one character differs
	 * 	doc1: 'a' in 'becaming' uses Arial
	 * 	doc2: 'a' in 'becaming' uses American Typewriter
	 * @throws IOException
	 */
	@Test
	public void fontDiff() throws IOException {
		
		File file1 = new File("/Users/vidush/git/Docs/WordTest2.pdf");
		File file2 = new File("/Users/vidush/git/Docs/FontTest.pdf");
		
		Assertions.assertFalse(PdfDiff.imageDiff(file1, file2));
		
	}
	
}