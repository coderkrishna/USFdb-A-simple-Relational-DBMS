package query;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

import common.DavisBaseConstants;
import io.IOManager;
import io.model.Page;
import model.table.Column;

public class CreateTable {
	public static void createTable(String tableName,List<Column> cs){
		File file = new File( DavisBaseConstants.DEFAULT_DATA_DIRNAME + "/" + tableName +DavisBaseConstants.FILE_EXTENSION);
	    try {
			if (file.createNewFile()) {
				RandomAccessFile tableFile = new RandomAccessFile(file, "rw");
			    tableFile.setLength(DavisBaseConstants.pageSize);											
			    tableFile.seek(0);				
			    tableFile.writeByte(0x0D);	
			    tableFile.close();
			}
			RandomAccessFile SystemTableTablesFile = new RandomAccessFile( DavisBaseConstants.DEFAULT_DATA_DIRNAME + "/" + DavisBaseConstants.SYSTEM_TABLES_TABLENAME+DavisBaseConstants.FILE_EXTENSION, "rw");
			int numOfPages = IOManager.numberOfPages(SystemTableTablesFile);
			int page=1;
			for(int p = 1; p <= numOfPages; ++p){
				if (Page.getRightMostPage(SystemTableTablesFile, p) == 0) {
					page = p;
					break;
				}
			}
			
			int[] recordsrowIdsList = Page.getRecordsrowIds(SystemTableTablesFile, page);
			int lastRecordRowId = recordsrowIdsList[0];
			for(int i = 0; i < recordsrowIdsList.length; ++i)
				if (recordsrowIdsList[i] > lastRecordRowId) {
					lastRecordRowId = recordsrowIdsList[i];
				}
			SystemTableTablesFile.close();
			String[] newRecord = {Integer.toString(lastRecordRowId+1), tableName}; // insert new id and tablename into davisbase_tables
			insertInto(DavisBaseConstants.SYSTEM_TABLES_TABLENAME, newRecord);
			

			RandomAccessFile SystemTableColumnsFile = new RandomAccessFile( DavisBaseConstants.DEFAULT_DATA_DIRNAME + "/" + DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME+DavisBaseConstants.FILE_EXTENSION, "rw");
			
			numOfPages = IOManager.numberOfPages(SystemTableColumnsFile);
			page=1;
			for(int p = 1; p <= numOfPages; ++p){
				if (Page.getRightMostPage(SystemTableColumnsFile, p) == 0)
					page = p;
			}
			
			recordsrowIdsList = Page.getRecordsrowIds(SystemTableColumnsFile, page);
			lastRecordRowId = recordsrowIdsList[0];
			for(int i = 0; i < recordsrowIdsList.length; ++i)
				if(recordsrowIdsList[i]>lastRecordRowId)
					lastRecordRowId = recordsrowIdsList[i];
			SystemTableColumnsFile.close();

			for(int i = 0; i < cs.size(); ++i){
				++lastRecordRowId;
				String col_name = cs.get(i).name;
				String dataType = cs.get(i).type.toUpperCase();
				String position = Integer.toString(i+1);
				String nullable;
				nullable = !cs.get(i).isNull ? "NO" : "YES";
				String[] value = {Integer.toString(lastRecordRowId),tableName,col_name,dataType,position,nullable};
				insertInto(DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME,value);
			}
				
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	}
	public static void insertInto(String table, String[] record){
		try{
			RandomAccessFile filePointer = new RandomAccessFile(DavisBaseConstants.DEFAULT_DATA_DIRNAME+"/"+table+DavisBaseConstants.FILE_EXTENSION, "rw");
			insertInto(filePointer, table, record);
			filePointer.close();

		}catch(Exception e){
			System.out.println(e);
		}
	}
	public static void insertInto(RandomAccessFile filePointer, String table, String[] values){
		String[] dataType = IOManager.getDataTypeList(table);
		String[] nullableColumnsList = IOManager.getNullable(table);

		for(int i = 0; i < nullableColumnsList.length; ++i)
			if(values[i].equals("null") && nullableColumnsList[i].equals("NO")){
				System.out.println("Violating NULL-value constraint ");
				System.out.println();
				return;
			}

		int key = new Integer(values[0]);
		int page = IOManager.searchKeyPage(filePointer, key);
		if(page != 0 && Page.keyExists(filePointer, page, key)) {
			System.out.println("Violating Unique constraint ");
			return;
		}
		if(page == 0)
			page = 1;


		byte[] stc = new byte[dataType.length-1];
		short payLoadSize = (short) IOManager.calPayloadSize(table, values, stc);
		int cellSize = payLoadSize + 6;
		int offset = Page.checkLeafFreeSpace(filePointer, page, cellSize);


		if(offset != -1)
			Page.insertLeafCellAt(filePointer, page, offset, payLoadSize, key, stc, values);
		else{
			Page.splitLeaf(filePointer, page);
			insertInto(filePointer, table, values);
		}
	}
}
