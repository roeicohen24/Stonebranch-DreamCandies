import static org.junit.Assert.*;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import org.junit.Test;


/**
 * @author Roei Cohen
 * 
 * Unit tests for TestFileFilter Class: Constructor and extractTestFiles
 */
public class TestFileFilterTest {
	
	/**
	 * Test method for {@link TestFileFilter#TestFileFilter(java.io.Reader, java.io.Reader, java.io.Reader, java.io.Reader)}.
	 * 
	 * Tests TestFileFilterTest Constructor in the case where the file of sample customers is completely empty (no header).
	 * Expected behavior is that the sampleCustomers set is empty.
	 */
	@Test
	public void constructor_EmptySampleCustomerFile_SampleSetIsEmpty() {
		Reader sampleCustomers = new StringReader("");
		Reader customer = new StringReader("");
		Reader invoice = new StringReader("");
		Reader invoiceItems = new StringReader("");
		
		TestFileFilter test = new TestFileFilter(sampleCustomers,customer,invoice,invoiceItems);
		assertTrue(test.getSampleCustomers().isEmpty());
	}
	
	
	/**
	 * Test method for {@link TestFileFilter#TestFileFilter(java.io.Reader, java.io.Reader, java.io.Reader, java.io.Reader)}.
	 * 
	 * Tests TestFileFilterTest Constructor in the case where the file of sample customers contains no customer codes.
	 * Expected behavior is that the sampleCustomers set is empty.
	 */
	@Test
	public void constructor_NoCustomerCodes_SampleSetIsEmpty() {
		Reader sampleCustomers = new StringReader("\"CUSTOMER_CODE\"\n");
		Reader customer = new StringReader("");
		Reader invoice = new StringReader("");
		Reader invoiceItems = new StringReader("");
		
		TestFileFilter test = new TestFileFilter(sampleCustomers,customer,invoice,invoiceItems);
		assertTrue(test.getSampleCustomers().isEmpty());
	}
	
	
	/**
	 * Test method for {@link TestFileFilter#TestFileFilter(java.io.Reader, java.io.Reader, java.io.Reader, java.io.Reader)}.
	 * 
	 * Tests TestFileFilterTest Constructor in the typical case where the file of sample customers contains some customer codes.
	 * Expected behavior is that the sampleCustomers set contains each of these codes.
	 */
	@Test
	public void constructor_TwoSampleCustomers_SampleSetContainsTwoCustomers() {
		Reader sampleCustomers = new StringReader("\"CUSTOMER_CODE\"\n" + 
				"\"CUST0000010231\"\n" + 
				"\"CUST0000010235\"\n");
		Reader customer = new StringReader("");
		Reader invoice = new StringReader("");
		Reader invoiceItems = new StringReader("");
		Set<String> expected = new HashSet<String>();
		expected.add("\"CUST0000010231\"");
		expected.add("\"CUST0000010235\"");
		
		TestFileFilter test = new TestFileFilter(sampleCustomers,customer,invoice,invoiceItems);
		assertEquals(test.getSampleCustomers(),expected);
	}

	
	/**
	 * Test method for {@link TestFileFilter#extractTestFiles(java.io.Writer, java.io.Writer, java.io.Writer)}.
	 * 
	 * Tests extractTestFiles in the case where the full data files are completely empty (no headers).
	 * Expected behavior is that all output files are completely empty (no headers).
	 */
	@Test
	public void extractTestFiles_DataFilesAreEmpty_AllFilesAreEmptyWithoutHeaders() {
		Reader sampleCustomers = new StringReader("\"CUSTOMER_CODE\"\n" + 
				"\"CUST0000010240\"\n");
		Reader customer = new StringReader("");
		Reader invoice = new StringReader("");
		Reader invoiceItems = new StringReader("");
		Writer customerOut = new StringWriter();
		Writer invoiceOut = new StringWriter();
		Writer invoiceItemOut = new StringWriter();

		TestFileFilter test = new TestFileFilter(sampleCustomers,customer,invoice,invoiceItems);
		test.extractTestFiles(customerOut,invoiceOut,invoiceItemOut);
		assertEquals(customerOut.toString(),"");
		assertEquals(invoiceOut.toString(),"");
		assertEquals(invoiceItemOut.toString(),"");
	}
	
	
	/**
	 * Test method for {@link TestFileFilter#extractTestFiles(java.io.Writer, java.io.Writer, java.io.Writer)}.
	 * 
	 * Tests extractTestFiles in the case where the sample customer codes are not contained in the full customer file.
	 * Expected behavior is that all output files contain only headers and are otherwise empty.
	 */
	@Test
	public void extractTestFiles_OneCustomerWithNoMatchingData_AllFilesAreEmpty() {
		Reader sampleCustomers = new StringReader("\"CUSTOMER_CODE\"\n" + 
				"\"CUST0000010240\"\n");
		Reader customer = new StringReader("\"CUSTOMER_CODE\",\"FIRSTNAME\",\"LASTNAME\"\n" + 
				"\"CUST0000010231\",\"Maria\",\"Alba\"\n" + 
				"\"CUST0000010233\",\"Jamie\",\"Hayes\"\n");
		Reader invoice = new StringReader("\"CUSTOMER_CODE\",\"INVOICE_CODE\",\"AMOUNT\",\"DATE\"\n" +  
				"\"CUST0000010231\",\"IN0000011\",\"0.0\",\"01-Jan-2000\"\n" + 
				"\"CUST0000010233\",\"IN0000010\",\"0.0\",\"01-Jan-2000\"\n");
		Reader invoiceItems = new StringReader("\"INVOICE_CODE\",\"ITEM_CODE\",\"AMOUNT\",\"QUANTITY\"\n" + 
				"\"IN0000011\",\"AAA\",\"0.0\",\"0\"\n" + 
				"\"IN0000010\",\"AAA\",\"0.0\",\"0\"\n" + 
				"\"IN0000007\",\"AAA\",\"0.0\",\"0\"\n");
		Writer customerOut = new StringWriter();
		Writer invoiceOut = new StringWriter();
		Writer invoiceItemOut = new StringWriter();

		TestFileFilter test = new TestFileFilter(sampleCustomers,customer,invoice,invoiceItems);
		test.extractTestFiles(customerOut,invoiceOut,invoiceItemOut);
		assertEquals(customerOut.toString(),"\"CUSTOMER_CODE\",\"FIRSTNAME\",\"LASTNAME\"\n");
		assertEquals(invoiceOut.toString(),"\"CUSTOMER_CODE\",\"INVOICE_CODE\",\"AMOUNT\",\"DATE\"\n");
		assertEquals(invoiceItemOut.toString(),"\"INVOICE_CODE\",\"ITEM_CODE\",\"AMOUNT\",\"QUANTITY\"\n");
	}
	
	
	/**
	 * Test method for {@link TestFileFilter#extractTestFiles(java.io.Writer, java.io.Writer, java.io.Writer)}.
	 * 
	 * Tests extractTestFiles in the case where the sample customer codes is contained in the full customer file, but
	 * has no corresponding invoices.
	 * Expected behavior is that the smaller customer file contains the customer information, and that the other 
	 * two files are empty.
	 */
	@Test
	public void extractTestFiles_OneCustomerWithNoInvoice_LastTwoExtractedFilesAreEmpty() {
		Reader sampleCustomers = new StringReader("\"CUSTOMER_CODE\"\n" + 
				"\"CUST0000010235\"\n");
		Reader customer = new StringReader("\"CUSTOMER_CODE\",\"FIRSTNAME\",\"LASTNAME\"\n" + 
				"\"CUST0000010231\",\"Maria\",\"Alba\"\n" + 
				"\"CUST0000010233\",\"Jamie\",\"Hayes\"\n" + 
				"\"CUST0000010236\",\"Stephanie\",\"James\"\n" + 
				"\"CUST0000010235\",\"George\",\"Lucas\"\n");
		Reader invoice = new StringReader("\"CUSTOMER_CODE\",\"INVOICE_CODE\",\"AMOUNT\",\"DATE\"\n" +  
				"\"CUST0000010236\",\"IN0000011\",\"0.0\",\"01-Jan-2000\"\n" + 
				"\"CUST0000010238\",\"IN0000010\",\"0.0\",\"01-Jan-2000\"\n" + 
				"\"CUST0000010239\",\"IN0000013\",\"0.0\",\"01-Jan-2000\"\n");
		Reader invoiceItems = new StringReader("\"INVOICE_CODE\",\"ITEM_CODE\",\"AMOUNT\",\"QUANTITY\"\n" + 
				"\"IN0000005\",\"AAA\",\"0.0\",\"0\"\n" + 
				"\"IN0000008\",\"AAA\",\"0.0\",\"0\"\n" + 
				"\"IN0000007\",\"AAA\",\"0.0\",\"0\"\n");
		Writer customerOut = new StringWriter();
		Writer invoiceOut = new StringWriter();
		Writer invoiceItemOut = new StringWriter();

		TestFileFilter test = new TestFileFilter(sampleCustomers,customer,invoice,invoiceItems);
		test.extractTestFiles(customerOut,invoiceOut,invoiceItemOut);
		assertEquals(customerOut.toString(),"\"CUSTOMER_CODE\",\"FIRSTNAME\",\"LASTNAME\"\n" +   
				"\"CUST0000010235\",\"George\",\"Lucas\"\n");
		assertEquals(invoiceOut.toString(),"\"CUSTOMER_CODE\",\"INVOICE_CODE\",\"AMOUNT\",\"DATE\"\n");
		assertEquals(invoiceItemOut.toString(),"\"INVOICE_CODE\",\"ITEM_CODE\",\"AMOUNT\",\"QUANTITY\"\n");
	}
	
	
	/**
	 * Test method for {@link TestFileFilter#extractTestFiles(java.io.Writer, java.io.Writer, java.io.Writer)}.
	 * 
	 * Tests extractTestFiles in the case of a single customer code that has corresponding data in all three files.
	 * Expected behavior is that each output file contains the data corresponding to the customer code.
	 */
	@Test
	public void extractTestFiles_HappyPathWithOneCustomer_ExtractedFilesContainRelevantData() {
		Reader sampleCustomers = new StringReader("\"CUSTOMER_CODE\"\n" + 
				"\"CUST0000010231\"\n");
		Reader customer = new StringReader("\"CUSTOMER_CODE\",\"FIRSTNAME\",\"LASTNAME\"\n" + 
				"\"CUST0000010231\",\"Maria\",\"Alba\"\n" + 
				"\"CUST0000010233\",\"Jamie\",\"Hayes\"\n");
		Reader invoice = new StringReader("\"CUSTOMER_CODE\",\"INVOICE_CODE\",\"AMOUNT\",\"DATE\"\n" + 
				"\"CUST0000010231\",\"IN0000001\",\"105.50\",\"01-Jan-2016\"\n" + 
				"\"CUST0000010239\",\"IN0000013\",\"0.0\",\"01-Jan-2000\"\n");
		Reader invoiceItems = new StringReader("\"INVOICE_CODE\",\"ITEM_CODE\",\"AMOUNT\",\"QUANTITY\"\n" + 
				"\"IN0000001\",\"MEIJI\",\"75.60\",\"100\"\n" + 
				"\"IN0000005\",\"AAA\",\"0.0\",\"0\"\n" + 
				"\"IN0000001\",\"POCKY\",\"10.40\",\"250\"\n");
		Writer customerOut = new StringWriter();
		Writer invoiceOut = new StringWriter();
		Writer invoiceItemOut = new StringWriter();

		TestFileFilter test = new TestFileFilter(sampleCustomers,customer,invoice,invoiceItems);
		test.extractTestFiles(customerOut,invoiceOut,invoiceItemOut);
		assertEquals(customerOut.toString(),"\"CUSTOMER_CODE\",\"FIRSTNAME\",\"LASTNAME\"\n" + 
				"\"CUST0000010231\",\"Maria\",\"Alba\"\n");
		assertEquals(invoiceOut.toString(),"\"CUSTOMER_CODE\",\"INVOICE_CODE\",\"AMOUNT\",\"DATE\"\n" + 
				"\"CUST0000010231\",\"IN0000001\",\"105.50\",\"01-Jan-2016\"\n");
		assertEquals(invoiceItemOut.toString(),"\"INVOICE_CODE\",\"ITEM_CODE\",\"AMOUNT\",\"QUANTITY\"\n" + 
				"\"IN0000001\",\"MEIJI\",\"75.60\",\"100\"\n" + 
				"\"IN0000001\",\"POCKY\",\"10.40\",\"250\"\n");
	}

	
	/**
	 * Test method for {@link TestFileFilter#extractTestFiles(java.io.Writer, java.io.Writer, java.io.Writer)}.
	 * 
	 * Tests extractTestFiles in the case of multiple customer codes that have corresponding data in all three files.
	 * Expected behavior is that each output file contains the data corresponding to the customer codes.
	 */
	@Test
	public void extractTestFiles_HappyPathWithMultipleCustomers_ExtractedFilesContainRelevantData() {
		Reader sampleCustomers = new StringReader("\"CUSTOMER_CODE\"\n" + 
				"\"CUST0000010231\"\n" + 
				"\"CUST0000010235\"\n");
		Reader customer = new StringReader("\"CUSTOMER_CODE\",\"FIRSTNAME\",\"LASTNAME\"\n" + 
				"\"CUST0000010231\",\"Maria\",\"Alba\"\n" + 
				"\"CUST0000010233\",\"Jamie\",\"Hayes\"\n" + 
				"\"CUST0000010236\",\"Stephanie\",\"James\"\n" + 
				"\"CUST0000010235\",\"George\",\"Lucas\"\n");
		Reader invoice = new StringReader("\"CUSTOMER_CODE\",\"INVOICE_CODE\",\"AMOUNT\",\"DATE\"\n" + 
				"\"CUST0000010231\",\"IN0000001\",\"105.50\",\"01-Jan-2016\"\n" + 
				"\"CUST0000010236\",\"IN0000011\",\"0.0\",\"01-Jan-2000\"\n" + 
				"\"CUST0000010235\",\"IN0000002\",\"186.53\",\"01-Jan-2016\"\n" + 
				"\"CUST0000010231\",\"IN0000003\",\"114.14\",\"01-Feb-2016\"\n" + 
				"\"CUST0000010238\",\"IN0000010\",\"0.0\",\"01-Jan-2000\"\n" + 
				"\"CUST0000010239\",\"IN0000013\",\"0.0\",\"01-Jan-2000\"\n");
		Reader invoiceItems = new StringReader("\"INVOICE_CODE\",\"ITEM_CODE\",\"AMOUNT\",\"QUANTITY\"\n" + 
				"\"IN0000001\",\"MEIJI\",\"75.60\",\"100\"\n" + 
				"\"IN0000005\",\"AAA\",\"0.0\",\"0\"\n" + 
				"\"IN0000001\",\"POCKY\",\"10.40\",\"250\"\n" + 
				"\"IN0000009\",\"AAA\",\"0.0\",\"0\"\n" + 
				"\"IN0000001\",\"PUCCHO\",\"19.50\",\"40\"\n" + 
				"\"IN0000002\",\"MEIJI\",\"113.40\",\"150\"\n" + 
				"\"IN0000002\",\"PUCCHO\",\"73.13\",\"150\"\n" + 
				"\"IN0000008\",\"AAA\",\"0.0\",\"0\"\n" + 
				"\"IN0000007\",\"AAA\",\"0.0\",\"0\"\n" + 
				"\"IN0000003\",\"POCKY\",\"16.64\",\"400\"\n" + 
				"\"IN0000003\",\"PUCCHO\",\"97.50\",\"200\"\n");
		Writer customerOut = new StringWriter();
		Writer invoiceOut = new StringWriter();
		Writer invoiceItemOut = new StringWriter();

		TestFileFilter test = new TestFileFilter(sampleCustomers,customer,invoice,invoiceItems);
		test.extractTestFiles(customerOut,invoiceOut,invoiceItemOut);
		assertEquals(customerOut.toString(),"\"CUSTOMER_CODE\",\"FIRSTNAME\",\"LASTNAME\"\n" + 
				"\"CUST0000010231\",\"Maria\",\"Alba\"\n" +  
				"\"CUST0000010235\",\"George\",\"Lucas\"\n");
		assertEquals(invoiceOut.toString(),"\"CUSTOMER_CODE\",\"INVOICE_CODE\",\"AMOUNT\",\"DATE\"\n" + 
				"\"CUST0000010231\",\"IN0000001\",\"105.50\",\"01-Jan-2016\"\n" + 
				"\"CUST0000010235\",\"IN0000002\",\"186.53\",\"01-Jan-2016\"\n" + 
				"\"CUST0000010231\",\"IN0000003\",\"114.14\",\"01-Feb-2016\"\n");
		assertEquals(invoiceItemOut.toString(),"\"INVOICE_CODE\",\"ITEM_CODE\",\"AMOUNT\",\"QUANTITY\"\n" + 
				"\"IN0000001\",\"MEIJI\",\"75.60\",\"100\"\n" + 
				"\"IN0000001\",\"POCKY\",\"10.40\",\"250\"\n" + 
				"\"IN0000001\",\"PUCCHO\",\"19.50\",\"40\"\n" + 
				"\"IN0000002\",\"MEIJI\",\"113.40\",\"150\"\n" + 
				"\"IN0000002\",\"PUCCHO\",\"73.13\",\"150\"\n" + 
				"\"IN0000003\",\"POCKY\",\"16.64\",\"400\"\n" + 
				"\"IN0000003\",\"PUCCHO\",\"97.50\",\"200\"\n");
	}
	
}
