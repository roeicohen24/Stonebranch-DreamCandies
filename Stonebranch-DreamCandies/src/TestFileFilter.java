import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Roei Cohen
 * 
 * The TestFileFilter class prepares testable files from full extraction data, as per the specifications given in feature CR1888_DRC.
 * Given a pre-selected set of customers as a test sample, a TestFileFilter object can extract all relevant entries from three full 
 * files, creating three smaller files to be ingested by the test automation process.
 */
public class TestFileFilter {
	
	private Set<String> sampleCustomers = new HashSet<String>(); //set of sample customers
	private Set<String> sampleInvoices = new HashSet<String>(); //set of invoices attached to sample customers
	private int sampleSize; //number of sample customers
	private Reader customer; //reader for full customer extraction file
	private Reader invoice; //reader for full invoice extraction file
	private Reader invoiceItem; //reader for full invoiceItem extraction file
	
	
	/**
	 * Constructor for TestFileFilter with reader parameters
	 * 
	 * @param Reader sampleCustomers: reader for file containing sample set of customers
	 * @param Reader customer: reader for full extraction file containing all customer data
	 * @param Reader invoice: reader for full extraction file containing all invoice data
	 * @param Reader invoiceItem: reader for full extraction file containing all invoice item data
	 */
	public TestFileFilter(Reader sampleCustomers, Reader customer, Reader invoice, Reader invoiceItem) {
		parseSampleCustomers(sampleCustomers);
		this.customer = customer;
		this.invoice = invoice;
		this.invoiceItem = invoiceItem;
	}
	
	
	/**
	 * Constructor for TestFileFilter with String file name parameters
	 * 
	 * @param String sampleCustomers: name of file containing sample set of customers
	 * @param String customer: name of full extraction file containing all customer data
	 * @param String invoice: name of full extraction file containing all invoice data
	 * @param String invoiceItem: name of full extraction file containing all invoice item data
	 * @throws IOException: throws exception if readers can't be instantiated
	 */
	public TestFileFilter(String sampleCustomers, String customer, String invoice, String invoiceItem) throws IOException {
		this(Files.newBufferedReader(Paths.get(sampleCustomers)),
				Files.newBufferedReader(Paths.get(customer)),
				Files.newBufferedReader(Paths.get(invoice)),
				Files.newBufferedReader(Paths.get(invoiceItem)));
	}

	
	/**
	 * Reads through the customer sample file and adds each customer code to the set
	 * 
	 * @param Reader r: reader for file containing the sample set of customers
	 */
	private void parseSampleCustomers(Reader r) {
		try {
			BufferedReader reader = new BufferedReader(r);
			reader.readLine(); //read file header
			
			//read every customer id and add to set of sampleCustomers
			String line;
			while ((line = reader.readLine()) != null) {
				sampleCustomers.add(line);
				sampleSize++;
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Reads through the full customer extraction file and produces a smaller file containing only pre-selected customers
	 * 
	 * @param Reader r: reader for full extraction file of customer data
	 * @param Writer w: writer for smaller file of sample customer data
	 */
	private void extractFromCustomer(Reader r, Writer w) {
		try {
			BufferedReader reader = new BufferedReader(r);
			BufferedWriter writer = new BufferedWriter(w);
			
			writer.write(reader.readLine() + "\n"); //read file header and write it to new file
			
			int count = 0; //number of customers found
			String line;
			while ((line = reader.readLine()) != null) {
				if (sampleCustomers.contains(line.split(",")[0])) { //get the customer code and check if it's in the sample set
					writer.write(line + "\n");
					count++;
				}
				if (count == sampleSize) {break;} //all sample customers found
			}
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Reads through the full invoice extraction file and produces a smaller file containing only invoices attached
	 * to pre-selected customers. Populates sampleInvoices set with invoice codes attached to preselected customers.
	 * 
	 * @param Reader r: reader for full extraction file of invoice data
	 * @param Writer w: writer for smaller file of sample invoice data
	 */
	private void extractFromInvoice(Reader r, Writer w) {
		try {
			BufferedReader reader = new BufferedReader(r);
			BufferedWriter writer = new BufferedWriter(w);
			
			writer.write(reader.readLine() + "\n"); //read file header and write it to new file
			
			String line;
			while ((line = reader.readLine()) != null) {
				String[] splitLine = line.split(",");
				if (sampleCustomers.contains(splitLine[0])) { //check if customer code is in set
					writer.write(line + "\n");
					sampleInvoices.add(splitLine[1]); //add invoice to sample invoice set if correlated to sample customer
				}
			}
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Reads through the full invoice item extraction file and produces a smaller file containing only invoices items attached
	 * to pre-selected customers.
	 * 
	 * @param Reader r: name of full extraction file of invoice item data
	 * @param Writer w: writer for smaller file of sample invoice item data
	 */
	private void extractFromInvoiceItem(Reader r, Writer w) {
		try {
			BufferedReader reader = new BufferedReader(r);
			BufferedWriter writer = new BufferedWriter(w);
			
			writer.write(reader.readLine() + "\n"); //read file header and write it to new file
			
			String line;
			while ((line = reader.readLine()) != null) {
				String[] splitLine = line.split(",");
				if (sampleInvoices.contains(splitLine[0])) { //check if invoice number is in set
					writer.write(line + "\n");
				}
			}
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Executes the entire extraction process in the necessary order, with Writer parameters
	 * 
	 * @param Writer customerOut: writer for smaller file to be produced with pre-selected customer data
	 * @param Writer invoiceOut: writer for smaller file to be produced with invoice data attached to pre-selected customers
	 * @param Writer invoiceItemOut: writer for smaller file to be produced with invoice item data attached to pre-selected customers
	 */
	public void extractTestFiles(Writer customerOut, Writer invoiceOut, Writer invoiceItemOut) {
		extractFromCustomer(customer, customerOut);
		extractFromInvoice(invoice, invoiceOut);
		extractFromInvoiceItem(invoiceItem, invoiceItemOut);
	}
	
	
	/**
	 * Executes the entire extraction process in the necessary order, with String file name parameters
	 * 
	 * @param String customerOut: name of smaller file to be produced with pre-selected customer data
	 * @param String invoiceOut: name of smaller file to be produced with invoice data attached to pre-selected customers
	 * @param String invoiceItemOut: name of smaller file to be produced with invoice item data attached to pre-selected customers
	 */
	public void extractTestFiles(String customerOut, String invoiceOut, String invoiceItemOut) {
		try {
			extractTestFiles(Files.newBufferedWriter(Paths.get(customerOut)),
					Files.newBufferedWriter(Paths.get(invoiceOut)),
					Files.newBufferedWriter(Paths.get(invoiceItemOut)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @return Set<String>: set of sample customer codes
	 */
	public Set<String> getSampleCustomers(){
		return sampleCustomers;
	}
	
	
	/**
	 * @return Set<String>: set of sample invoice codes
	 */
	public Set<String> getSampleInvoices(){
		return sampleInvoices;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 4) {throw new IllegalArgumentException();}
		try {
			TestFileFilter test = new TestFileFilter(args[0],args[1],args[2],args[3]);
			test.extractTestFiles("customer_test.csv","invoice_test.csv","invoice_item_test.csv");	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
