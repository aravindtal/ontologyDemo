import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by aravindp on 10/2/16.
 */
public class ExcelUtils {
    public static String XSSF = "XSSF"; // for working with Excel 2003 and earlier.
    public static String HSSF = "HSSF"; // for working with Excel 2007 and later.
    private Workbook workbook;
    private FileInputStream inputStream = null;
    private static int sheetIndex = 0;
    private List<String> header = new ArrayList<String>();

    public ExcelUtils(FileInputStream inputStream){
        this.inputStream = inputStream;
    }

    public void setSheetIndex(int index){
        this.sheetIndex = index;
    }

    public  List<String> getHeader(){
        return header;
    }

    public Workbook getWorkBook(String type) throws IOException {

        if (inputStream == null) {
            throw new IOException("inputStream is empty");
        }

        if (XSSF.equalsIgnoreCase(type)) {
            workbook = new XSSFWorkbook(inputStream);
        } else if (HSSF.equalsIgnoreCase(type)) {
            workbook = new HSSFWorkbook(inputStream);
        } else {
            throw new IOException("Only HSSF and XSSF types are supported.");
        }
        return workbook;
    }

    public Map<String,List<String>> openWorkbook(String type) throws IOException{

        Map<String,List<String>> workbookData = new HashMap<String, List<String>>();
        Workbook workbook = getWorkBook(type);
        Sheet sheet = workbook.getSheetAt(sheetIndex);

        Iterator<Row> iterator = sheet.iterator();
        boolean headFlag = true;

        while (iterator.hasNext()) {
            Row nextRow = iterator.next();
            Iterator<Cell> cellIterator = nextRow.cellIterator();
            int headerIndex = header.size();

            while (cellIterator.hasNext()) {
                Cell nextCell = cellIterator.next();
                int columnIndex = nextCell.getColumnIndex();

                if (headFlag) {
                    header.add(nextCell.getStringCellValue());
                    workbookData.put(nextCell.getStringCellValue(), new LinkedList<String>());
                } else {
                    String key = header.get(header.size() - headerIndex);
                    List<String> temp = workbookData.get(key);
                    temp.add(nextCell.getStringCellValue());
                    workbookData.put(key,temp);
                }

                headerIndex--;
            }

            headFlag = false;
        }

        return workbookData;
    }
}
