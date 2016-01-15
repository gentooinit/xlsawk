/**
 * 
 */
package org.init0;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.*;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 * @author init0
 *
 */

public class Awk {
	public Awk(String fileName) throws Exception {
		this.fileName = fileName;

		try {
			wb = WorkbookFactory.create(
					new FileInputStream(this.fileName)
			);
		} catch (EncryptedDocumentException e) {
			throw new Exception("Encrypted document detected.");
		} catch (InvalidFormatException e) {
			throw new Exception("Invalid document format.");
		} catch (IOException e) {
			throw e;
		}

		init();
	}

	public Awk(OutputStream os, InputStream is) throws Exception {
		this.os = os;
		this.is = is;

		try {
			wb = WorkbookFactory.create(this.is);
		} catch (EncryptedDocumentException e) {
			throw new Exception("Encrypted document detected.");
		} catch (InvalidFormatException e) {
			throw new Exception("Invalid document format.");
		} catch (IOException e) {
			throw e;
		}

		init();
	}

	private void init() {
		sheets = new HashMap<String, Sheet>();
		NS = wb.getNumberOfSheets();
		for (int i = 0; i < NS; ++i) {
			sheets.put(wb.getSheetName(i), wb.getSheetAt(i));
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

		exitFlag = false;
	}

	protected void print(String str) {
		System.out.print(str);
	}

	protected void println(String str) {
		print(str + ORS);
	}

	protected void setStdsheet(String name) {
		String safeName = WorkbookUtil.createSafeSheetName(name);
		try {
			if (sheets.containsKey(safeName)) {
				System.err.println("Warning: " +
						"Workbook already contains " +
						"a sheet with this name.");

				OS = sheets.get(safeName);
				// Clear the output sheet
				List<Row> toRemove = new ArrayList<Row>();
				Iterator<Row> it = OS.rowIterator();
				while (it.hasNext()) {
					Row row = it.next();
					toRemove.add(row);
				}

				for (Row row: toRemove)
					OS.removeRow(row);
			} else {
				OS = wb.createSheet(safeName);
			}
		} catch (IllegalArgumentException e) {
			System.err.println("Error: " +
					"Sheet name is empty or invalid.");
		}

		osCursorX = 0;
		osCursorY = 0;
	}

	private String[] cut(String str, String delimter) {
		int pos = str.indexOf(delimter);
		int end = pos - 1;
		String[] ret = new String[3];
		String piece;

		ret[2] = "False";

		if (pos != -1) {
			ret[2] = "True";

			if (end < 1)
				end = 1; 

			piece = str.substring(0, pos);
			if (pos < str.length() - 1)
				str = str.substring(pos + 1);
			else
				str = "";
		} else {
			piece = str;
			str = "";
		}

		ret[0] = piece;
		ret[1] = str;

		return ret;
	}

	protected void printSheet(String str) {
		while (!"".equals(str)) {
			String[] lineList;
			String oneLine;

			lineList = cut(str, "\n");
			oneLine = lineList[0];
			str = lineList[1];

			Row row = OS.getRow(osCursorX);
			if (row == null)
				row = OS.createRow(osCursorX);

			while (!"".equals(oneLine)) {
				String[] cellList;
				String oneCell;

				cellList = cut(oneLine, "\t");
				oneCell = cellList[0];
				oneLine = cellList[1];

				Cell cell = row.getCell(osCursorY);
				if (cell == null)
					cell = row.createCell(osCursorY);

				cell.setCellValue(getContent(cell) + oneCell);

				// Go to next Cell
				if ("True".equals(cellList[2]))
					++osCursorY;
			}

			// Simulate CR and NL
			if ("True".equals(lineList[2])) {
				++osCursorX;
				osCursorY = 0;
			}
		}
	}

	protected void printRow() {
		if (OS == null) {
			System.err.println("Error: " + 
					"Call setStdsheet() first.");
			return;
		}

		if (NR == 0) {
			System.err.println("Error: " + 
					"Trying to printRow at 0 Row.");
			return;
		}

		Row row = OS.createRow(NR);
		for (int i = 1; i <= NF; ++i) {
			Cell cell = row.createCell(i - 1);
			cell.setCellValue(getField(i));
		}
	}

	protected String getField(int index) {
		if (index <= NF) {
			return field.get(index);
		} else {
			return "";
		}
	}

	protected void setField(int index, String str) {
		if (index <= NF) {
			field.set(index, str);
		} else {
			for (int i = NF + 1; i < index; ++i)
				field.add("");

			field.add(str);
			NF = field.size() - 1;
		}
	}

	protected boolean regexMatch(String str, String regex, boolean icase) {
		Pattern p;

		if (icase)
			p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		else
			p = Pattern.compile(regex);

		Matcher m = p.matcher(str);

		return m.find();
	}

	protected boolean regexMatch(String str, String regex) {
		return regexMatch(str, regex, false);
	}

	protected String gensub(String regex, String replacement,
			String hits, String str) {
		Pattern p;

		p = Pattern.compile(regex);

		Matcher m = p.matcher(str);

		switch (hits.charAt(0)) {
		case 'g':
		case 'G':
			return m.replaceAll(replacement);
		case '1':
			return m.replaceFirst(replacement);
		default:
			// TODO: the nth hits replacement
			return "";
		}
	}

	protected void flush() {
		try {
			if (os == null)
				os = new FileOutputStream(fileName);
			
			if (os != null)
				wb.write(os);

			if (is == null)
				os.close();
		} catch (Exception e) {
			System.err.println("Write to OutputStream failed.");
		}
	}

	public void begin() {
	}

	public void end() {
	}

	protected void exit() {
		exitFlag = true;
	}

	protected void each() {
	}

	public void loop() {
		int i, j;

		if (AS == null || AS.getPhysicalNumberOfRows() == 0) {
			return;
		}

		for (i = 0; i <= AS.getLastRowNum() && !exitFlag; ++i) {
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

	protected int NS;                          /* Number of Sheet */
	protected int NR;                          /* Number of Record */
	protected int NF;                          /* Number of Field */
	protected Sheet AS = null;                 /* Active Sheet */
	protected Sheet OS = null;                 /* Output Sheet */
	protected List<String> field;
	protected Map<String, Sheet> sheets;
	protected String FS;
	protected String RS;
	protected String ORS;
	protected String OFS;
	

	private boolean exitFlag;
	private int osCursorX;
	private int osCursorY;
	private String fileName;
	private OutputStream os = null;
	private InputStream is = null;
	private Workbook wb = null;
}
