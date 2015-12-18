/**
 * 
 */
package org.init0;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 * @author init0
 *
 */

public class Awk {
	public Awk(OutputStream _os, InputStream _is) throws Exception {
		os = _os;
		is = _is;

		try {
			wb = WorkbookFactory.create(is);
		} catch (EncryptedDocumentException e) {
			throw new Exception("Encrypted document detected.");
		} catch (InvalidFormatException e) {
			throw new Exception("Invalid document format.");
		} catch (IOException e) {
			throw e;
		}

		sheet = new HashMap<String, Sheet>();
		NS = wb.getNumberOfSheets();
		for (int i = 0; i < NS; ++i) {
			sheet.put(wb.getSheetName(i), wb.getSheetAt(i));
		}

		/* Default sheet */
		AS = wb.getSheetAt(wb.getActiveSheetIndex());

		field = new ArrayList<String>();

		FS = "\t";
		RS = "\n";
		OFS = "\t";
		ORS = "\n";

		NR = 0;
		NF = 0;
	}

	public void print(String str) {
		System.out.print(str);
	}

	public void println(String str) {
		print(str + ORS);
	}

	public void begin() {
	}

	public void end() {
	}

	protected void each() {
	}

	public void loop() {
		int i, j;

		if (AS == null || AS.getPhysicalNumberOfRows() == 0) {
			return;
		}

		for (i = 0; i <= AS.getLastRowNum(); ++i) {
			NR++;
			Row row = AS.getRow(i);

			field.clear();

			/* Set $0 as entire row */
			field.add("");

			if (row != null) {
				cellLoop: for (j = 0; j < row.getLastCellNum(); ++j) {
					Cell cell = row.getCell(j);

					CellRangeAddress range;
					for (int k = 0; k < AS.getNumMergedRegions(); ++k) {
						range = AS.getMergedRegion(k);

						if (range.isInRange(i, j) == true) {
							field.add(getMergeContent(range));
							continue cellLoop;
						}
					}

					field.add(getContent(cell));
				}
			}

			NF = field.size() - 1;

			for (j = 1; j <= NF - 1; ++j) {
				field.set(0, field.get(0) + field.get(j) + FS);
			}

			if (j == NF) {
				field.set(0, field.get(0) + field.get(j));
			}

			each();
		}
	}

	private String getContent(Cell cell) {
		String literal = "";

		if (cell != null) {
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_FORMULA:
				literal = cell.getCellFormula();
				break;
			case Cell.CELL_TYPE_NUMERIC:
				literal = new DataFormatter()
					.formatCellValue(cell);
				break;
			case Cell.CELL_TYPE_STRING:
				literal = cell.getStringCellValue();
				break;
			default:
				literal = cell.toString();
				break;
			}
			literal = literal.trim().replaceAll("\n", "");
		}

		return literal;
	}

	private String getMergeContent(CellRangeAddress range) {
		String literal = "";

		int rowInx = range.getFirstRow();
		int colInx = range.getFirstColumn();

		Row row = AS.getRow(rowInx);
		if (row != null) {
			Cell cell = row.getCell(colInx);
			literal = getContent(cell);
		}

		return literal;
	}

	protected int NS;                                  /* Number of Sheet */
	protected int NR;                                  /* Number of Record */
	protected int NF;                                  /* Number of Field */
	protected Sheet AS = null;                         /* Active Sheet */
	protected ArrayList<String> field;
	protected Map<String, Sheet> sheet;
	protected String FS;
	protected String RS;
	protected String ORS;
	protected String OFS;


	private OutputStream os = null;
	private InputStream is = null;
	private Workbook wb = null;
}
