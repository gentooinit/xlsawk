/**
 * 
 */
package org.init0;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * @author init0
 *
 */

public class Awk {
	public enum ExcelType { XLS, XLSX };
	
	public Awk(OutputStream _os, InputStream _is, ExcelType type)
			throws IOException
	{
		os = _os;
		is = _is;
		
		if (type == ExcelType.XLS) {
				wb = new HSSFWorkbook(is);
		} else {
				wb = new XSSFWorkbook(is);
		}
		
		NS = wb.getNumberOfSheets();
		
		sheet = new HashMap<String, Sheet>();
		for (int i = 0; i < NS; ++i) {
			sheet.put(wb.getSheetName(i), wb.getSheetAt(i));
		}
		
		/* Default sheet */
		AS = wb.getSheetAt(wb.getActiveSheetIndex());
		
		field = new ArrayList<String>();
	}
	
	public void begin() {}
	public void end() {}
	protected void each() {}
	
	public void loop()
	{
		int i, j;
		
		if (AS == null) {
			return;
		}
		
		for (i = 0; i <= AS.getLastRowNum(); ++i) {
			NR++;
			Row row = AS.getRow(i);
			
			field.clear();
			
			if (row != null) {			
				for (j = 0; j <= row.getLastCellNum(); ++j) {
					Cell cell = row.getCell(j);
					
					if (cell == null) {
						field.add("");
					} else {
						String literal;
						
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_FORMULA:
							literal = cell.getCellFormula();
							break;
						case Cell.CELL_TYPE_NUMERIC:
							literal = Double.toString(cell.getNumericCellValue());
							break;
						case Cell.CELL_TYPE_STRING:
							literal = cell.getStringCellValue();
							break;
						default:
							literal = cell.toString();
							break;
						}
						field.add(literal.replaceAll("\n", ""));
					}
				}
			}
			
			NF = field.size();
			
			each();
		}
	}
	
	public void print(String str)
	{
		System.out.print(str);
	}
	
	/* For Runnable Jar */
	public static void main(String[] args) {}

	protected int NS;                               /* Number of Sheet */
	protected int NR;                               /* Number of Record */
	protected int NF;                               /* Number of Field */
	protected Sheet AS = null;                      /* Active Sheet */
	protected ArrayList<String> field;
	protected Map<String, Sheet> sheet;
	
	private OutputStream os = null;
	private InputStream is = null;
	private Workbook wb = null;
}

