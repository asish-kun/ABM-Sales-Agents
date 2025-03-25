package util;

import java.util.List;
import java.io.FileReader;

import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.HeaderColumnNameMappingStrategy;
import view.LeadData;


/** 
 * This class is to read a CSV file with the data about the leads
 * 
 * @author mchica
 * @date 07/05/2024
 * @place Granada
 *
 */

public class CSVReader4Leads {		
	
	
	/**
	 * Load the CSV file for the information about the leads
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List<LeadData> parseCSVFile (String _fileForVessels) throws IOException {
		
		
		CSVReader reader = new CSVReader(new FileReader(_fileForVessels), ';');
		
		HeaderColumnNameMappingStrategy<LeadData> bean = new HeaderColumnNameMappingStrategy<LeadData>();
		bean.setType(LeadData.class);
		
		CsvToBean<LeadData> csvToBean = new CsvToBean<LeadData>();
		
		List<LeadData> data = csvToBean.parse(bean, reader);
		
		reader.close();		
		
		return data;
		
	}

	
}
