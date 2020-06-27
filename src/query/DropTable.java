package query;

import java.io.File;
import java.io.RandomAccessFile;

import common.DavisBaseConstants;
import io.IOManager;
import io.model.Page;

public class DropTable {
	public static void drop(String tableName){
		try{
			RandomAccessFile SystemTableTablesFile = new RandomAccessFile(DavisBaseConstants.DEFAULT_DATA_DIRNAME+"/"+DavisBaseConstants.SYSTEM_TABLES_TABLENAME+DavisBaseConstants.FILE_EXTENSION, "rw");
			int numOfPages = IOManager.numberOfPages(SystemTableTablesFile);
			for(int page = 1; page <= numOfPages; ++page) {
				SystemTableTablesFile.seek((page - 1) * DavisBaseConstants.pageSize);
				if (SystemTableTablesFile.readByte() != 0x0D)
					continue;
				short[] cellAddressList = Page.getCellArrayList(SystemTableTablesFile, page);
				int k = 0;
				for (int i = 0; i < cellAddressList.length; ++i)
					if (!IOManager.retrieveValuesList(SystemTableTablesFile,
							Page.getLocOfCell(SystemTableTablesFile, page, i))[1].equals(tableName)) {
						Page.setCellOffsetValue(SystemTableTablesFile, page, k, cellAddressList[i]);
						++k;
					}
				Page.setCellNumber(SystemTableTablesFile, page, (byte) k);
			}

			RandomAccessFile SystemTableColumnsFile = new RandomAccessFile(DavisBaseConstants.DEFAULT_DATA_DIRNAME+"/"+DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME + DavisBaseConstants.FILE_EXTENSION, "rw");
			numOfPages = IOManager.numberOfPages(SystemTableColumnsFile);
			for(int page = 1; page <= numOfPages; ++page) {
				SystemTableColumnsFile.seek((page - 1) * DavisBaseConstants.pageSize);
				if (SystemTableColumnsFile.readByte() != 0x0D)
					continue;
				short[] cellAddressList = Page.getCellArrayList(SystemTableColumnsFile, page);
				int k = 0;
				for (int i = 0; i < cellAddressList.length; ++i)
					if (!IOManager.retrieveValuesList(SystemTableColumnsFile,
							Page.getLocOfCell(SystemTableColumnsFile, page, i))[1].equals(tableName)) {
						Page.setCellOffsetValue(SystemTableColumnsFile, page, k, cellAddressList[i]);
						++k;
					}
				Page.setCellNumber(SystemTableColumnsFile, page, (byte) k);
			}

			(new File(DavisBaseConstants.DEFAULT_DATA_DIRNAME, tableName + DavisBaseConstants.FILE_EXTENSION)).delete();
		}catch(Exception e){
			System.out.println(e);
		}

	}

}
