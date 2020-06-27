package query;

import java.io.RandomAccessFile;

import common.DavisBaseConstants;
import io.IOManager;
import io.model.Page;

public class InsertInto {
	public static void insertInto(String table, String[] values){
		try{
			RandomAccessFile file = new RandomAccessFile(DavisBaseConstants.DEFAULT_DATA_DIRNAME+"/"+table+DavisBaseConstants.FILE_EXTENSION, "rw");
			insertInto(file, table, values);
			file.close();

		}catch(Exception e){
			System.out.println(e);
		}
	}
	public static void insertInto(RandomAccessFile f, String table, String[] values){
		String[] datatypeList = IOManager.getDataTypeList(table);
		String[] nullableColumnsList = IOManager.getNullable(table);

		for(int i = 0; i < nullableColumnsList.length; ++i)
			if(values[i].equals("null") && nullableColumnsList[i].equals("NO")){
				System.out.println("NULL-value constraint violation");
				System.out.println();
				return;
			}

		int key = new Integer(values[0]);
		int page = IOManager.searchKeyPage(f, key);
		if(page != 0 && Page.keyExists(f, page, key)) {
			System.out.println("Uniqueness constraint violation");
			return;
		}
		if(page == 0)
			page = 1;


		byte[] serialTypeCode = new byte[datatypeList.length-1];
		short plSize = (short) IOManager.calPayloadSize(table, values, serialTypeCode);
		int cellSize = plSize + 6;
		int offset = Page.checkLeafFreeSpace(f, page, cellSize);


		if(offset != -1)
			Page.insertLeafCellAt(f, page, offset, plSize, key, serialTypeCode, values);
		else{
			Page.splitLeaf(f, page);
			insertInto(f, table, values);
		}
	}
}
