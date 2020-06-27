package query;

import java.io.RandomAccessFile;

import common.DavisBaseConstants;
import io.IOManager;
import io.BufferManager;

public class ShowTables {
	public static void showTables() {
		try{
			String[] cols = {"table_name"};
			String[] cmp = new String[0];
			RandomAccessFile file = new RandomAccessFile(DavisBaseConstants.DEFAULT_DATA_DIRNAME + "/" + DavisBaseConstants.SYSTEM_TABLES_TABLENAME+DavisBaseConstants.FILE_EXTENSION, "rw");
			String[] columnName = IOManager.getColNameList(DavisBaseConstants.SYSTEM_TABLES_TABLENAME);
			String[] type = IOManager.getDataTypeList(DavisBaseConstants.SYSTEM_TABLES_TABLENAME);
			BufferManager buffer = new BufferManager();
			IOManager.filterRecords(file, cmp, columnName, type, buffer);
			buffer.displayContent(cols);
			file.close();
		}catch(Exception e){
			System.out.println(e);
		}
	}
}
