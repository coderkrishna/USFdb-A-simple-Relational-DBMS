package io;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import common.DavisBaseConstants;
import io.model.Page;

public class IOManager {
	public static String[] getColNameList(String table){
		String[] $ = new String[0];
		try{
			RandomAccessFile file = new RandomAccessFile( DavisBaseConstants.DEFAULT_DATA_DIRNAME + "/" + DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME+DavisBaseConstants.FILE_EXTENSION, "rw");
			BufferManager buffer = new BufferManager();
			String[] columnName = {"rowid", "table_name", "column_name", "data_type", "ordinal_position", "is_nullable"};
			String[] cmp = {"table_name","=",table};
			filterRecords(file, cmp, columnName, buffer);
			HashMap<Integer, String[]> buffercontent = buffer.contentMap;
			ArrayList<String> array = new ArrayList<String>();
			for(String[] contentValue : buffercontent.values())
				array.add(contentValue[2]);
			int size=array.size();
			$ = array.toArray(new String[size]);
			file.close();
			return $;
		}catch(Exception e){
			System.out.println(e);
		}
		return $;
	}
	
	public static String[] getDataTypeList(String table){
		String[] $ = new String[0];
		try{
			RandomAccessFile file = new RandomAccessFile( DavisBaseConstants.DEFAULT_DATA_DIRNAME + "/" + DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME+DavisBaseConstants.FILE_EXTENSION, "rw");
			BufferManager buffer = new BufferManager();
			String[] columnNamesList = {"rowid", "table_name", "column_name", "data_type", "ordinal_position", "is_nullable"};
			String[] cmp = {"table_name","=",table};
			filterRecords(file, cmp, columnNamesList, buffer);
			HashMap<Integer, String[]> buffercontent = buffer.contentMap;
			ArrayList<String> array = new ArrayList<String>();
			for(String[] contentValue : buffercontent.values())
				array.add(contentValue[3]);
			int size=array.size();
			$ = array.toArray(new String[size]);
			file.close();
			return $;
		}catch(Exception e){
			System.out.println(e);
		}
		return $;
	}
	
	public static void filterRecords(RandomAccessFile f, String[] cmp, String[] columnName, BufferManager m){
		try{
			
			int numOfPages = numberOfPages(f);
			for(int page = 1; page <= numOfPages; ++page){
				
				f.seek((page-1)*DavisBaseConstants.pageSize);
				if (f.readByte() == 0x0D) {
					byte numOfCells = Page.getNumofCells(f, page);
					for (int i = 0; i < numOfCells; ++i) {
						long loc = Page.getLocOfCell(f, page, i);
						String[] vals = retrieveValuesList(f, loc);
						int rowid = Integer.parseInt(vals[0]);
						if (compCheck(vals, rowid, cmp, columnName))
							m.add_vals(rowid, vals);
					}
				}
			}

			m.columnName = columnName;
			m.displayformat = new int[columnName.length];

		}catch(Exception e){
			System.out.println("Error filtering the records");
			e.printStackTrace();
		}

	}
	
	public static void filterRecords(RandomAccessFile f, String[] cmp, String[] columnName, String[] type, BufferManager m){
		try{
			
			int numOfPages = numberOfPages(f);
			
			for(int page = 1; page <= numOfPages; ++page){
				
				f.seek((page-1)*DavisBaseConstants.pageSize);
				if (f.readByte() == 0x0D) {
						byte numOfCells = Page.getNumofCells(f, page);
						for (int i = 0; i < numOfCells; ++i) {
							long loc = Page.getLocOfCell(f, page, i);
							String[] values = retrieveValuesList(f, loc);
							int rowid = Integer.parseInt(values[0]);
							for (int j = 0; j < type.length; ++j)
								if (type[j].equals("DATE") || type[j].equals("DATETIME"))
									values[j] = "'" + values[j] + "'";
							boolean check = compCheck(values, rowid, cmp, columnName);
							for (int j = 0; j < type.length; ++j)
								if (type[j].equals("DATE") || type[j].equals("DATETIME"))
									values[j] = values[j].substring(1, values[j].length() - 1);
							if (check)
								m.add_vals(rowid, values);
						}
					}
			}

			m.columnName = columnName;
			m.displayformat = new int[columnName.length];

		}catch(Exception e){
			System.out.println("Error at filter");
			e.printStackTrace();
		}

	}
	
	public static String[] retrieveValuesList(RandomAccessFile filePointer, long loc){
		
	String[] $ = null;
	try{
		
		SimpleDateFormat dateFormat = new SimpleDateFormat (DavisBaseConstants.DATEPATTERN);

		filePointer.seek(loc+2);
		int key = filePointer.readInt();
		int num_cols = filePointer.readByte();
		
		byte[] serialTypeCode = new byte[num_cols];
		filePointer.read(serialTypeCode);
		
		$ = new String[num_cols+1];
		
		$[0] = Integer.toString(key);
		
		for(int i=1; i <= num_cols; ++i)
			switch (serialTypeCode[i - 1]) {
			case 0x00:
				filePointer.readByte();
				$[i] = "null";
				break;
			case 0x01:
				filePointer.readShort();
				$[i] = "null";
				break;
			case 0x02:
				filePointer.readInt();
				$[i] = "null";
				break;
			case 0x03:
				filePointer.readLong();
				$[i] = "null";
				break;
			case 0x04:
				$[i] = Integer.toString(filePointer.readByte());
				break;
			case 0x05:
				$[i] = Integer.toString(filePointer.readShort());
				break;
			case 0x06:
				$[i] = Integer.toString(filePointer.readInt());
				break;
			case 0x07:
				$[i] = Long.toString(filePointer.readLong());
				break;
			case 0x08:
				$[i] = String.valueOf(filePointer.readFloat());
				break;
			case 0x09:
				$[i] = String.valueOf(filePointer.readDouble());
				break;
			case 0x0A:
				Long temp = filePointer.readLong();
				Date dateTime = new Date(temp);
				$[i] = dateFormat.format(dateTime);
				break;
			case 0x0B:
				temp = filePointer.readLong();
				Date date = new Date(temp);
				$[i] = dateFormat.format(date).substring(0, 10);
				break;
			default:
				int len = new Integer(serialTypeCode[i - 1] - 0x0C);
				byte[] bytes = new byte[len];
				filePointer.read(bytes);
				$[i] = new String(bytes);
				break;
			}

	}catch(Exception e){
		e.printStackTrace();
	}

	return $;
}
	
	public static int numberOfPages(RandomAccessFile f){
		int $ = 0;
		try{
			$ = (int)(f.length()/(new Long(DavisBaseConstants.pageSize)));
		}catch(Exception e){
			System.out.println(e);
		}

		return $;
	}
	public static boolean compCheck(String[] values, int rowid, String[] cmp, String[] columnNamesList){

		boolean $ = false;
		
		if(cmp.length == 0)
			$ = true;
		else{
			int columnPosition = 1;
			for(int i = 0; i < columnNamesList.length; ++i)
				if (columnNamesList[i].equals(cmp[0])) {
					columnPosition = i + 1;
					break;
				}
			
			if (columnPosition != 1)
				$ = cmp[2].equals(values[columnPosition - 1]);
			else {
				int val = Integer.parseInt(cmp[2]);
				switch (cmp[1]) {
				case "=":
					$ = rowid == val;
					break;
				case ">":
					$ = rowid > val;
					break;
				case ">=":
					$ = rowid >= val;
					break;
				case "<":
					$ = rowid < val;
					break;
				case "<=":
					$ = rowid <= val;
					break;
				case "!=":
					$ = rowid != val;
					break;
				}
			}
		}
		return $;
	}
	

	
	public static String[] getNullable(String table){
		String[] $ = new String[0];
		try{
			RandomAccessFile file = new RandomAccessFile(DavisBaseConstants.DEFAULT_DATA_DIRNAME + "/" + DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME+DavisBaseConstants.FILE_EXTENSION, "rw");
			BufferManager buffer = new BufferManager();
			String[] columnNameList = {"rowid", "table_name", "column_name", "data_type", "ordinal_position", "is_nullable"};
			String[] cmp = {"table_name","=",table};
			filterRecords(file, cmp, columnNameList, buffer);
			HashMap<Integer, String[]> buffercontent = buffer.contentMap;
			ArrayList<String> array = new ArrayList<String>();
			for(String[] contentValues : buffercontent.values())
				array.add(contentValues[5]);
			int size=array.size();
			$ = array.toArray(new String[size]);
			file.close();
			return $;
		}catch(Exception e){
			e.printStackTrace();
		}
		return $;
	}
	
	public static int searchKeyPage(RandomAccessFile f, int key){
		int value = 1;
		try{
			int numPages = numberOfPages(f);
			for(int page = 1; page <= numPages; ++page){
				f.seek((page - 1)*DavisBaseConstants.pageSize);
				if (f.readByte() == 0x0D) {
					int[] keysList = Page.getRecordsrowIds(f, page);
					if (keysList.length == 0)
						return 0;
					if (keysList[0] <= key && key <= keysList[keysList.length - 1]
							|| Page.getRightMostPage(f, page) == 0 && keysList[keysList.length - 1] < key)
						return page;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return value;
	}
	
	public static boolean tableExists(String tablename){
		tablename = tablename+DavisBaseConstants.FILE_EXTENSION;
		
		try {
			File dataDir = new File(DavisBaseConstants.DEFAULT_DATA_DIRNAME);
			String[] ListOfExistingTables = dataDir.list();
			for (int i=0; i<ListOfExistingTables.length; ++i)
				if (ListOfExistingTables[i].equals(tablename))
					return true;
		}
		catch (SecurityException se) {
			System.out.println("Unable to create data container directory");
			System.out.println(se);
		}

		return false;
	}
	
	public static int calPayloadSize(String table, String[] vals, byte[] stc){
		String[] dataTypeList = getDataTypeList(table);
		int $ = dataTypeList.length;
		for(int i = 1; i < dataTypeList.length; ++i){
			stc[i - 1]= getSerialTypeCode(vals[i], dataTypeList[i]);
			$ = $ + fieldLenInBytes(stc[i - 1]);
		}
		return $;
	}
	
	public static byte getSerialTypeCode(String value, String dataType){
		if (!value.equals("null"))
			switch (dataType) {
			case "TINYINT":
				return DavisBaseConstants.TINY_INT_SERIAL_TYPE_CODE;
			case "SMALLINT":
				return DavisBaseConstants.SMALL_INT_SERIAL_TYPE_CODE;
			case "INT":
				return DavisBaseConstants.INT_SERIAL_TYPE_CODE;
			case "BIGINT":
				return DavisBaseConstants.BIG_INT_SERIAL_TYPE_CODE;
			case "REAL":
				return DavisBaseConstants.REAL_SERIAL_TYPE_CODE;
			case "DOUBLE":
				return DavisBaseConstants.DOUBLE_SERIAL_TYPE_CODE;
			case "DATETIME":
				return DavisBaseConstants.DATE_TIME_SERIAL_TYPE_CODE;
			case "DATE":
				return DavisBaseConstants.DATE_SERIAL_TYPE_CODE;
			case "TEXT":
				return (byte) (value.length() + DavisBaseConstants.TEXT_SERIAL_TYPE_CODE);
			default:
				return DavisBaseConstants.ONE_BYTE_NULL_SERIAL_TYPE_CODE;
			}
		else
			switch (dataType) {
			case "TINYINT":
				return DavisBaseConstants.ONE_BYTE_NULL_SERIAL_TYPE_CODE;
			case "SMALLINT":
				return DavisBaseConstants.TWO_BYTE_NULL_SERIAL_TYPE_CODE;
			case "INT":
				return DavisBaseConstants.FOUR_BYTE_NULL_SERIAL_TYPE_CODE;
			case "BIGINT":
				return DavisBaseConstants.EIGHT_BYTE_NULL_SERIAL_TYPE_CODE;
			case "REAL":
				return DavisBaseConstants.FOUR_BYTE_NULL_SERIAL_TYPE_CODE;
			case "DOUBLE":
				return DavisBaseConstants.EIGHT_BYTE_NULL_SERIAL_TYPE_CODE;
			case "DATETIME":
				return DavisBaseConstants.EIGHT_BYTE_NULL_SERIAL_TYPE_CODE;
			case "DATE":
				return DavisBaseConstants.EIGHT_BYTE_NULL_SERIAL_TYPE_CODE;
			case "TEXT":
				return DavisBaseConstants.EIGHT_BYTE_NULL_SERIAL_TYPE_CODE;
			default:
				return DavisBaseConstants.ONE_BYTE_NULL_SERIAL_TYPE_CODE;
			}
	}
	
	public static short fieldLenInBytes(byte serialTypeCode){
		switch(serialTypeCode){
			case 0x00: return 1;
			case 0x01: return 2;
			case 0x02: return 4;
			case 0x03: return 8;
			case 0x04: return 1;
			case 0x05: return 2;
			case 0x06: return 4;
			case 0x07: return 8;
			case 0x08: return 4;
			case 0x09: return 8;
			case 0x0A: return 8;
			case 0x0B: return 8;
			default:   return (short)(serialTypeCode - 0x0C);
		}
	}
	
}
