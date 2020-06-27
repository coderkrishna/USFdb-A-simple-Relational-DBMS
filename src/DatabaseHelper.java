import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import common.DavisBaseConstants;

public class DatabaseHelper {
	
    public static void InitializeDatabase() {
    	
		try {
	        File baseDir = new File(DavisBaseConstants.DEFAULT_DATA_DIRNAME);
	        if(!baseDir.exists()) {
	                if(baseDir.mkdirs()) {
	                	System.out.println("Initializing Database");
						System.out.println();
	                    createSystemTables();
	                }
	        }else{
				String[] existingTables = baseDir.list();
				boolean hasSystemTable = false;
				boolean hasColumnTable = false;
				for (int i=0; i<existingTables.length; ++i) {
					if(existingTables[i].equals(DavisBaseConstants.SYSTEM_TABLES_TABLENAME+DavisBaseConstants.FILE_EXTENSION))
						hasSystemTable = true;
					if(existingTables[i].equals(DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME+DavisBaseConstants.FILE_EXTENSION))
						hasColumnTable = true;
				}
				
				if(!hasSystemTable)
					createSystemTables();
				
				if(!hasColumnTable)
					createSystemTables();
	        }
		}
		catch (SecurityException e) {
			System.out.println(e);
		}

    }
    
    public static void createSystemTables(){

		
		try {
			//cleaning the database which is in unstable state
			File dataDir = new File(DavisBaseConstants.DEFAULT_DATA_DIRNAME);
			dataDir.mkdir();
			String[] oldTableFilesList = dataDir.list();
			for (int i=0; i<oldTableFilesList.length; ++i)
				(new File(dataDir, oldTableFilesList[i])).delete();
		}
		catch (SecurityException e) {
			System.out.println(e);
		}
		createDavisTableCatalog();
		createDavisColumnsCatalog();
	    
    }
    public static void createDavisTableCatalog(){
    	File file = new File( DavisBaseConstants.DEFAULT_DATA_DIRNAME + "/" + DavisBaseConstants.SYSTEM_TABLES_TABLENAME+DavisBaseConstants.FILE_EXTENSION);
	    try {
			if (file.createNewFile()) {
			    RandomAccessFile davisbase_tables_catalog = new RandomAccessFile(file, "rw");
			    davisbase_tables_catalog.setLength(DavisBaseConstants.pageSize);
			    davisbase_tables_catalog.seek(0);
			    davisbase_tables_catalog.write(0x0D); //leaf page of b-tree
			    davisbase_tables_catalog.writeByte(0x02); // 2 records on the page
				
				 /* davisbase_tables schema
				  * rowid INT
				  * table_name TEXT
				  * record_count INT*/ 
				int lengthOfTableRecord=24;
				
				int lengthOfcolumnRecord=25;
				
				int offsetT=DavisBaseConstants.pageSize-lengthOfTableRecord;  // 512-24 = 488
				int offsetC=offsetT-lengthOfcolumnRecord;	// 488-25 = 463
				
				davisbase_tables_catalog.writeShort(offsetC); 
				davisbase_tables_catalog.writeInt(0);
				davisbase_tables_catalog.writeInt(0);
				davisbase_tables_catalog.writeShort(offsetT);
				davisbase_tables_catalog.writeShort(offsetC);
				
				davisbase_tables_catalog.seek(offsetT);
				davisbase_tables_catalog.writeShort(20);
				davisbase_tables_catalog.writeInt(1); 
				davisbase_tables_catalog.writeByte(1);
				davisbase_tables_catalog.writeByte(28);
				davisbase_tables_catalog.writeBytes(DavisBaseConstants.SYSTEM_TABLES_TABLENAME);
				
				davisbase_tables_catalog.seek(offsetC);
				davisbase_tables_catalog.writeShort(21);
				davisbase_tables_catalog.writeInt(2); 
				davisbase_tables_catalog.writeByte(1);
				davisbase_tables_catalog.writeByte(29);
				davisbase_tables_catalog.writeBytes(DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME);
				
				davisbase_tables_catalog.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    public static void createDavisColumnsCatalog(){
    	File fileCol = new File( DavisBaseConstants.DEFAULT_DATA_DIRNAME + "/" + DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME+DavisBaseConstants.FILE_EXTENSION);
		try {
			if (fileCol.createNewFile()) {
				RandomAccessFile davisbase_columns_catalog = new RandomAccessFile(fileCol, "rw");
				davisbase_columns_catalog.setLength(DavisBaseConstants.pageSize);
				davisbase_columns_catalog.seek(0);       
				davisbase_columns_catalog.writeByte(0x0D); //leaf table of b-tree
				davisbase_columns_catalog.writeByte(0x08);
				
				/* davisbase_columns schema
				 *  rowid            INT
				 *  table_name       TEXT
				 *  column_name      TEXT
				 *  data_type        TEXT
				 *  ordinal_position TINYINT
				 *  is_nullable      TEXT*/
				
				int[] offset=new int[10];
				offset[0]=DavisBaseConstants.pageSize-43;
				offset[1]=offset[0]-47;
				offset[2]=offset[1]-44;
				offset[3]=offset[2]-48;
				offset[4]=offset[3]-49;
				offset[5]=offset[4]-47;
				offset[6]=offset[5]-57;
				offset[7]=offset[6]-49;
				
				davisbase_columns_catalog.writeShort(offset[7]); 
				davisbase_columns_catalog.writeInt(0); 
				davisbase_columns_catalog.writeInt(0); 
				
				for(int i=0;i<8;++i)
					davisbase_columns_catalog.writeShort(offset[i]);
	
				
				davisbase_columns_catalog.seek(offset[0]);
				davisbase_columns_catalog.writeShort(33);
				davisbase_columns_catalog.writeInt(1); 
				davisbase_columns_catalog.writeByte(5);
				davisbase_columns_catalog.writeByte(28);
				davisbase_columns_catalog.writeByte(17);
				davisbase_columns_catalog.writeByte(15);
				davisbase_columns_catalog.writeByte(4);
				davisbase_columns_catalog.writeByte(14);
				davisbase_columns_catalog.writeBytes(DavisBaseConstants.SYSTEM_TABLES_TABLENAME); 
				davisbase_columns_catalog.writeBytes("rowid"); 
				davisbase_columns_catalog.writeBytes("INT"); 
				davisbase_columns_catalog.writeByte(1); 
				davisbase_columns_catalog.writeBytes("NO"); 	
				
				davisbase_columns_catalog.seek(offset[1]);
				davisbase_columns_catalog.writeShort(39); 
				davisbase_columns_catalog.writeInt(2); 
				davisbase_columns_catalog.writeByte(5);
				davisbase_columns_catalog.writeByte(28);
				davisbase_columns_catalog.writeByte(22);
				davisbase_columns_catalog.writeByte(16);
				davisbase_columns_catalog.writeByte(4);
				davisbase_columns_catalog.writeByte(14);
				davisbase_columns_catalog.writeBytes(DavisBaseConstants.SYSTEM_TABLES_TABLENAME); 
				davisbase_columns_catalog.writeBytes("table_name"); 
				davisbase_columns_catalog.writeBytes("TEXT"); 
				davisbase_columns_catalog.writeByte(2);
				davisbase_columns_catalog.writeBytes("NO"); 
				
				davisbase_columns_catalog.seek(offset[2]);
				davisbase_columns_catalog.writeShort(34); 
				davisbase_columns_catalog.writeInt(3); 
				davisbase_columns_catalog.writeByte(5);
				davisbase_columns_catalog.writeByte(29);
				davisbase_columns_catalog.writeByte(17);
				davisbase_columns_catalog.writeByte(15);
				davisbase_columns_catalog.writeByte(4);
				davisbase_columns_catalog.writeByte(14);
				davisbase_columns_catalog.writeBytes(DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME);
				davisbase_columns_catalog.writeBytes("rowid");
				davisbase_columns_catalog.writeBytes("INT");
				davisbase_columns_catalog.writeByte(1);
				davisbase_columns_catalog.writeBytes("NO");
				
				davisbase_columns_catalog.seek(offset[3]);
				davisbase_columns_catalog.writeShort(40);
				davisbase_columns_catalog.writeInt(4); 
				davisbase_columns_catalog.writeByte(5);
				davisbase_columns_catalog.writeByte(29);
				davisbase_columns_catalog.writeByte(22);
				davisbase_columns_catalog.writeByte(16);
				davisbase_columns_catalog.writeByte(4);
				davisbase_columns_catalog.writeByte(14);
				davisbase_columns_catalog.writeBytes(DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME);
				davisbase_columns_catalog.writeBytes("table_name");
				davisbase_columns_catalog.writeBytes("TEXT");
				davisbase_columns_catalog.writeByte(2);
				davisbase_columns_catalog.writeBytes("NO");
				
				davisbase_columns_catalog.seek(offset[4]);
				davisbase_columns_catalog.writeShort(41);
				davisbase_columns_catalog.writeInt(5); 
				davisbase_columns_catalog.writeByte(5);
				davisbase_columns_catalog.writeByte(29);
				davisbase_columns_catalog.writeByte(23);
				davisbase_columns_catalog.writeByte(16);
				davisbase_columns_catalog.writeByte(4);
				davisbase_columns_catalog.writeByte(14);
				davisbase_columns_catalog.writeBytes(DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME);
				davisbase_columns_catalog.writeBytes("column_name");
				davisbase_columns_catalog.writeBytes("TEXT");
				davisbase_columns_catalog.writeByte(3);
				davisbase_columns_catalog.writeBytes("NO");
				
				davisbase_columns_catalog.seek(offset[5]);
				davisbase_columns_catalog.writeShort(39);
				davisbase_columns_catalog.writeInt(6); 
				davisbase_columns_catalog.writeByte(5);
				davisbase_columns_catalog.writeByte(29);
				davisbase_columns_catalog.writeByte(21);
				davisbase_columns_catalog.writeByte(16);
				davisbase_columns_catalog.writeByte(4);
				davisbase_columns_catalog.writeByte(14);
				davisbase_columns_catalog.writeBytes(DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME);
				davisbase_columns_catalog.writeBytes("data_type");
				davisbase_columns_catalog.writeBytes("TEXT");
				davisbase_columns_catalog.writeByte(4);
				davisbase_columns_catalog.writeBytes("NO");
				
				davisbase_columns_catalog.seek(offset[6]);
				davisbase_columns_catalog.writeShort(49); 
				davisbase_columns_catalog.writeInt(7); 
				davisbase_columns_catalog.writeByte(5);
				davisbase_columns_catalog.writeByte(29);
				davisbase_columns_catalog.writeByte(28);
				davisbase_columns_catalog.writeByte(19);
				davisbase_columns_catalog.writeByte(4);
				davisbase_columns_catalog.writeByte(14);
				davisbase_columns_catalog.writeBytes(DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME);
				davisbase_columns_catalog.writeBytes("ordinal_position");
				davisbase_columns_catalog.writeBytes("TINYINT");
				davisbase_columns_catalog.writeByte(5);
				davisbase_columns_catalog.writeBytes("NO");
				
				davisbase_columns_catalog.seek(offset[7]);
				davisbase_columns_catalog.writeShort(41); 
				davisbase_columns_catalog.writeInt(8); 
				davisbase_columns_catalog.writeByte(5);
				davisbase_columns_catalog.writeByte(29);
				davisbase_columns_catalog.writeByte(23);
				davisbase_columns_catalog.writeByte(16);
				davisbase_columns_catalog.writeByte(4);
				davisbase_columns_catalog.writeByte(14);
				davisbase_columns_catalog.writeBytes(DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME);
				davisbase_columns_catalog.writeBytes("is_nullable");
				davisbase_columns_catalog.writeBytes("TEXT");
				davisbase_columns_catalog.writeByte(6);
				davisbase_columns_catalog.writeBytes("NO");
				
				davisbase_columns_catalog.close();
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}
    }
}
