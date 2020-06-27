package query;

import java.io.RandomAccessFile;

import common.DavisBaseConstants;
import io.BufferManager;
import io.IOManager;

public class Select {
	public static void select(String table, String[] cols, String[] cmp){
		try{
			
			RandomAccessFile file = new RandomAccessFile(DavisBaseConstants.DEFAULT_DATA_DIRNAME+"/"+table+DavisBaseConstants.FILE_EXTENSION, "rw");
			String[] columnName = IOManager.getColNameList(table);
			String[] type = IOManager.getDataTypeList(table);
			
			BufferManager buffer = new BufferManager();
			
			IOManager.filterRecords(file, cmp, columnName, type, buffer);
			buffer.displayContent(cols);
			file.close();
		}catch(Exception e){
			System.out.println(e);
		}
	}
}
