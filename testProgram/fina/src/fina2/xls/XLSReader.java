package fina2.xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import fina2.returns.cbn.AuthSignatoryType;
import fina2.returns.cbn.BodyType;
import fina2.returns.cbn.CallReport;
import fina2.returns.cbn.ContactDetailsType;
import fina2.returns.cbn.FooterType;
import fina2.returns.cbn.HeaderType;
import fina2.returns.cbn.ItemsInfoType;


@SuppressWarnings("unused")
public class XLSReader {
	private File matrix;
	private File xls;
	private InputStream matrixIn;
	private InputStream xlsIn;
	private POIFSFileSystem matrixPoi;
	private POIFSFileSystem xlsPoi;
	private HSSFWorkbook matrixWb;
	private HSSFWorkbook xlsWb;

	private Logger log = Logger.getLogger(XLSReader.class);
    
	private int rowNum=6;
	
	/**
	 * Initializes matrix workbook and xls workbook
	 * 
	 * @param matrix
	 * @param xls
	 */
	public XLSReader(File matrix, File xls) {
		this.matrix = matrix;
		this.xls = xls;
		try {
			matrixIn = new FileInputStream(matrix);
			xlsIn = new FileInputStream(xls);
			matrixPoi = new POIFSFileSystem(matrixIn);
			xlsPoi = new POIFSFileSystem(xlsIn);
			matrixWb = new HSSFWorkbook(matrixPoi);
			xlsWb = new HSSFWorkbook(xlsPoi);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private int[] getXYReferenceFromMatrix(String sheetName, int rowIndex, int colIndex) {
		int rowCol[] = new int[2];
		Object value = null;

		Properties p = new Properties();
		HSSFSheet sheet = matrixWb.getSheet(sheetName);
		HSSFRow row = sheet.getRow(rowIndex);
		if (row != null) {
			HSSFCell cell = row.getCell(colIndex);
			if (cell != null) {
				if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK)
					value = cell.getCellComment();
				else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN)
					value = cell.getBooleanCellValue();
				else if (cell.getCellType() == HSSFCell.CELL_TYPE_ERROR)
					value = cell.getErrorCellValue();
				else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA)
					value = cell.getCellFormula();
				else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)
					value = cell.getNumericCellValue();
				else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING)
					value = cell.getRichStringCellValue();
			}
		}

		if (value != null) {
			try {
				p.load(this.getClass().getResourceAsStream("matrixsymbols.properties"));
				rowCol[0] = Integer.parseInt(value.toString().trim().substring(1)) - 1;
				rowCol[1] = Integer.parseInt(p.get(value.toString().trim().substring(0, 1)).toString());
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				ex.printStackTrace();
			}
		}
		return rowCol;
	}

	private Object getValueByRowCol(HSSFWorkbook wb,String sheetName, int rowIndex, int colIndex) {
		Object value = null;
		HSSFSheet sheet = wb.getSheet(sheetName);
		HSSFRow row = sheet.getRow(rowIndex);
		if (row != null) {
			HSSFCell cell = row.getCell(colIndex);
			if (cell != null) {
				if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK)
					value = cell.getCellComment();
				else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN)
					value = cell.getBooleanCellValue();
				else if (cell.getCellType() == HSSFCell.CELL_TYPE_ERROR)
					value = cell.getErrorCellValue();
				else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA)
					value = cell.getCellFormula();
				else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)
					value = cell.getNumericCellValue();
				else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING)
					value = cell.getRichStringCellValue();
			}
		}
		return value;
	}
    
	
	/**
	 * 
	 * @param sheetName
	 * @return the HeaderType object which contains the header information of
	 *         generated xml.
	 */
	public HeaderType getHeaderType(String sheetName) {
		log.info("Generating xml header...");
		HeaderType ht = new HeaderType();

		ht.setCALLREPORT_ID(getValueByRowCol(matrixWb,sheetName,  0, 1).toString());
		ht.setCALLREPORT_DESC(getValueByRowCol(matrixWb,sheetName,  1, 1).toString());
		ht.setINST_CODE(getValueByRowCol(matrixWb,sheetName,  2, 1).toString());
		ht.setINST_NAME(getValueByRowCol(matrixWb,sheetName,  3, 1).toString());
		ht.setAS_AT(getValueByRowCol(matrixWb,sheetName,  4, 1).toString());
        log.info("XML header generated....");
		return ht;
	}

	/**
	 * 
	 * @param sheetName
	 * @return the List of ItemsInfoType,each ItemsInfoType object containts
	 *         code,description and ammount
	 */
	public List<ItemsInfoType> getAllItems(String sheetName) {
		log.info("Generating xml items...");
		
		ArrayList<ItemsInfoType> data = new ArrayList<ItemsInfoType>();
		// ////////////////////////////////////////////
        
        
        for(int i=6;;i++,++rowNum)	
         {
		 		ItemsInfoType iit = new ItemsInfoType();
				Object code = getValueByRowCol(matrixWb,sheetName,  i, 0);
				if (code != null)
					iit.setITEM_CODE((int) Float.parseFloat(code.toString()));

				Object desc = getValueByRowCol(matrixWb,sheetName, i,1);
				if (desc != null)
					iit.setITEM_DESC(desc.toString());
				else
					iit.setITEM_DESC("SOME DESCRIPTION");

				Object ammount = getValueByRowCol(xlsWb,sheetName, getXYReferenceFromMatrix(sheetName, i, 2)[0], getXYReferenceFromMatrix(sheetName, i, 2)[1]);
				if (ammount != null) {
					try {
						iit.setAMMOUNT(Float.parseFloat(ammount.toString()));
					} catch (NumberFormatException ex) {
						iit.setAMMOUNT(0.0f);
					}
				} else
					iit.setAMMOUNT(0.0f);
				
                if(code==null && desc==null && getValueByRowCol(matrixWb,sheetName,i,2)==null)
                	break;
                	data.add(iit);	
			}
        log.info("XML items generated...");
		return data;
	}

	/**
	 * 
	 * @param sheetName
	 * @return the FooterType which contains the Footer information of generated
	 *         xml.
	 */
	public FooterType getFooterType(String sheetName) {
		log.info("Generating xml footer...");
		FooterType ft = new FooterType();

		HSSFSheet sheet = matrixWb.getSheet(sheetName);
		int lastRow = sheet.getLastRowNum();

		AuthSignatoryType ast = new AuthSignatoryType();
		ast.setNAME(getValueByRowCol(matrixWb,sheetName,  rowNum+1, 1).toString());
		ast.setPOSITION(getValueByRowCol(matrixWb,sheetName,  rowNum+2, 1).toString());
		ast.setDATE(getValueByRowCol(matrixWb,sheetName,  rowNum+3, 1).toString());

		ContactDetailsType cdt = new ContactDetailsType();
		cdt.setNAME(getValueByRowCol(matrixWb,sheetName,  rowNum+4, 1).toString());
		cdt.setTEL_NO(getValueByRowCol(matrixWb,sheetName,  rowNum+5, 1).toString());

		ft.setDESC(getValueByRowCol(matrixWb,sheetName,  rowNum+6, 1).toString());

		ft.setAUTH_SIGNATORY(ast);
		ft.setCONTACT_DETAILS(cdt);
		
        rowNum=0;
        
        log.info("XML footer generated...");
        
		return ft;

	}

	/**
	 * 
	 * @return the array of matrix sheets names
	 */
	public String[] getAllSheets() {
		String sheets[] = new String[matrixWb.getNumberOfSheets()];
		for (int i = 0; i < sheets.length; i++)
			sheets[i] = matrixWb.getSheetName(i);
		return sheets;
	}

	public static void main(String[] args) {
		String sheets[];
		XLSReader xlsReader = new XLSReader(new File("D:\\FinA\\Inputs\\CBN\\Matrix.xls"), new File("D:\\FinA\\Inputs\\CBN\\MFB12345m012009.xls"));
		sheets = xlsReader.getAllSheets();
		for (int i = 0; i < sheets.length; i++) {
			try {

				JAXBContext jc = JAXBContext.newInstance("fina2.returns.cbn");
				Marshaller mar = jc.createMarshaller();
				CallReport cr = new CallReport();

				HeaderType ht = cr.getHEADER();
				HeaderType tempHt = xlsReader.getHeaderType(sheets[i]);

				ht.setAS_AT(tempHt.getAS_AT());
				ht.setCALLREPORT_DESC(tempHt.getCALLREPORT_DESC());
				ht.setCALLREPORT_ID(tempHt.getCALLREPORT_ID());
				ht.setINST_CODE(tempHt.getINST_CODE());
				ht.setINST_NAME(tempHt.getINST_NAME());

				BodyType bt = cr.getBODY();
				bt.setITEMS_INFO(xlsReader.getAllItems(sheets[i]));

				FooterType ft = cr.getFOOTER();
				FooterType tempFt = xlsReader.getFooterType(sheets[i]);

				ft.setAUTH_SIGNATORY(tempFt.getAUTH_SIGNATORY());
				ft.setCONTACT_DETAILS(tempFt.getCONTACT_DETAILS());
				ft.setDESC(tempFt.getDESC());

				mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				mar.marshal(cr, new File("C:\\fina-server\\MFB" + sheets[i] + ".xml"));

			} catch (Exception ex) {
				System.out.println(sheets[i]);
				ex.printStackTrace();
			}
		}
	}
}