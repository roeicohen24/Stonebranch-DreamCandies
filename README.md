# DreamCandies File Tool


## Problem Summary

The requested feature is a file tool to be used in the test automation process of DreamCandies' billing system migration. Stonebranch will have access to three full database extraction files from DreamCandies. The functionality of the tool is, given a pre-selected sample of 1000 customers, to provide three smaller files containing only data corresponding to those pre-selected customers. These smaller files will be used in the test automation process.

File Specifications:
- Customer Sample: CUSTOMER_CODE (CHAR)

- Customer: CUSTOMER_CODE (CHAR), FIRSTNAME (CHAR), LASTNAME (CHAR)

- Invoice: CUSTOMER_CODE (CHAR), INVOICE_CODE (CHAR), AMOUNT (FLOAT), DATE (DATE)

- Invoice Item: INVOICE_CODE (CHAR), ITEM_CODE (CHAR), AMOUNT (FLOAT), QUANTITY (INTEGER)


## Assumptions
- All files are not necessarily sorted by any field.

- The pair double quotes surrounding each field are made up of a left and right quote. In the examples of the specifications document, every double quote after the first in each row was a right quote. I assumed this was an error based on the earlier statement that "all fields are double quoted regardless of data type (e.g. &#10077;Oliver&#10078;)". See further explanation in Implementation section.

- Every input file will be correctly formatted. 


## Algorithm Design

The algorithm I designed outlines as follows:
1. Read Customer Sample file. Store each CUSTOMER_CODE in a set (sampleCustomers).
2. Read Customer file and create the smaller output file. For each line in the Customer file, if the CUSTOMER_CODE is contained in the sampleCustomers set, write that line to the output file. 
3. Read Invoice file and create the smaller output file. For each line in the Invoice file, if the CUSTOMER_CODE is contained in the sampleCustomers set, write that line to the output file. Additionally, add the INVOICE_CODE of that line to a set (sampleInvocices). 
4. Read Invoice Items file and create the smaller output file. For each line in the Invoie Item file, if the INVOICE_CODE is contained in the sampleInvoices set, write that line to the output file.


## Runtime and Space Analysis

- Number of Sample Customers: 1000

- Mean Number of Invoices Per Customer: ~2 (based on given estimate of customer and invoice files being 500k and 1 million respectively — in any case will be some constant)

- Size of Full Customer File: *n*

- Size of Full Invoice File: 2*n* (based on given estimate of customer and invoice files being 500k and 1 million respectively — in any case will be in the order of *n*).

- Size of Full Invoice File: 10*n* (based on given estimate of customer and invoice item files being 500k and 5 million respectively — in any case will be in the order of *n*).

**Runtime Complexity**: O(*n*)

The runtime complexity is determined by the size of the files that we're iterating on. Each file is iterated through a single time. Using a hashset allows for constant time lookup. So, all operations are done in constant time. Reading through each file is O(1000 + *n* + 2*n* + 10*n*), or O(*n*).

**Space Complexity**: O(*1*)

The sampleCustomers set will be of size 1000, per the specifications. The number of invoices per customer is ~2 (or some constant *c*), given the file size estimates. So, the sampleInvoices set will be of size 2000 (or *1000c*). In any case, the space complexity will be independent of the size of the full files, so the space complexity is O(1000 + 1000*c*), or O(1).

*Note*: In the case where each file is sorted by the appropriate field, an implementation using binary search could be implented resulting in a runtime in the order of O(log*n*). However, since we assumed the data was not sorted, the theoretical lower bound of the algorithm is O(*n*), since every row must be read in order to ensure no entries of interest are overlooked.


# Implementation Decisions

- I chose to use hashsets to store the sample customers and invoices to allow for constant time lookup.

- The sets store strings containing the surrounding left and right quotes in addition to the actual customer/invoice code, in order to avoid the extra substring operation. For example, the set contains the String ""CUST0000010231"" rather than "CUST0000010231". Looking back to the quote format assumption made above, the current implemention would lead to a bug (e.g. &#10077;IN0000001&#10078; would not match with &#10078;IN0000001&#10078;) if the assumption was incorrect. However, this could easily be solved by storing and comparing the substrings excluding the first and last characters in each field.

- I chose to overload both the constructor and extractTestFiles method, allowing for arguments of either the file names as String types or access to the files themselves through Reader/Writer types. The former gives the reader/writer creation responsibility to the class, while the latter gives the user more flexibility by allowing any memory representation of the data, and not binding it to the file system. This also allows for easier testing since an entire file system doesn't have to be mocked.

