package io.model;
import java.io.RandomAccessFile;
import common.DavisBaseConstants;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Page{

	public static int createInteriorPage(RandomAccessFile filePointer){
		int num_of_pages = 0;
		try{
			num_of_pages = (int)(filePointer.length()/(new Long(DavisBaseConstants.pageSize)));
			num_of_pages = num_of_pages + 1;
			filePointer.setLength(DavisBaseConstants.pageSize * num_of_pages);
			filePointer.seek((num_of_pages-1)*DavisBaseConstants.pageSize);
			filePointer.writeByte(0x05); 
		}catch(Exception e){
			System.out.println(e);
		}

		return num_of_pages;
	}

	public static int createLeafPage(RandomAccessFile filePointer){
		int num_of_pages = 0;
		try{
			num_of_pages = (int)(filePointer.length()/(new Long(DavisBaseConstants.pageSize)));
			num_of_pages = num_of_pages + 1;
			filePointer.setLength(DavisBaseConstants.pageSize * num_of_pages);
			filePointer.seek((num_of_pages-1)*DavisBaseConstants.pageSize);
			filePointer.writeByte(0x0D); 
		}catch(Exception e){
			System.out.println(e);
		}

		return num_of_pages;

	}

	public static int findMiddleKey(RandomAccessFile filepointer, int page){
		int value = 0;
		try{
			filepointer.seek((page-1)*DavisBaseConstants.pageSize);
			byte pageType = filepointer.readByte();
			int numOfCells = getNumofCells(filepointer, page);
			int midcell = (int) Math.ceil((double) numOfCells / 2);
			long location = getLocOfCell(filepointer, page, midcell-1);
			filepointer.seek(location);

			switch(pageType){
				case 0x05:
					filepointer.readInt(); 
					value = filepointer.readInt();
					break;
				case 0x0D:
					filepointer.readShort();
					value = filepointer.readInt();
					break;
			}

		}catch(Exception e){
			e.printStackTrace();
		}

		return value;
	}

	
	public static void splitLeafPg(RandomAccessFile filePointer, int curPage, int newPage){
		try{
			int numCells = getNumofCells(filePointer, curPage);
			int midpoint = (int) Math.ceil((double) numCells / 2);
			int CellA = midpoint - 1;
			int CellB = numCells - CellA;
			int contentSize = DavisBaseConstants.pageSize;

			for(int i = CellA; i < numCells; i++){
				long loc = getLocOfCell(filePointer, curPage, i);
				filePointer.seek(loc);
				int cellSize = filePointer.readShort()+6;
				contentSize = contentSize - cellSize;
				filePointer.seek(loc);
				byte[] cell = new byte[cellSize];
				filePointer.read(cell);
				filePointer.seek((newPage-1)*DavisBaseConstants.pageSize+contentSize);
				filePointer.write(cell);
				setCellOffsetValue(filePointer, newPage, i - CellA, contentSize);
			}

			
			filePointer.seek((newPage-1)*DavisBaseConstants.pageSize+2);
			filePointer.writeShort(contentSize);

			
			short offset = getCellOffsetValue(filePointer, curPage, CellA-1);
			filePointer.seek((curPage-1)*DavisBaseConstants.pageSize+2);
			filePointer.writeShort(offset);

			
			int rightMost = getRightMostPage(filePointer, curPage);
			setRightMostPage(filePointer, newPage, rightMost);
			setRightMostPage(filePointer, curPage, newPage);

			
			int parent = getParentNode(filePointer, curPage);
			setParentNode(filePointer, newPage, parent);

			
			byte number = (byte) CellA;
			setCellNumber(filePointer, curPage, number);
			number = (byte) CellB;
			setCellNumber(filePointer, newPage, number);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void splitInteriorPage(RandomAccessFile filepointer, int curPage, int newPage){
		try{
			
			int numCells = getNumofCells(filepointer, curPage);
			
			int mid = (int) Math.ceil((double) numCells / 2);

			int cellA = mid - 1;
			int cellB = numCells - cellA - 1;
			short content = 512;

			for(int i = cellA+1; i < numCells; i++){
				long loc = getLocOfCell(filepointer, curPage, i);
				short cellSize = 8;
				content = (short)(content - cellSize);
				filepointer.seek(loc);
				byte[] cell = new byte[cellSize];
				filepointer.read(cell);
				filepointer.seek((newPage-1)*DavisBaseConstants.pageSize+content);
				filepointer.write(cell);
				filepointer.seek(loc);
				int page = filepointer.readInt();
				setParentNode(filepointer, page, newPage);
				setCellOffsetValue(filepointer, newPage, i - (cellA + 1), content);
			}
			
			int tmp = getRightMostPage(filepointer, curPage);
			setRightMostPage(filepointer, newPage, tmp);
			
			long midLoc = getLocOfCell(filepointer, curPage, mid - 1);
			filepointer.seek(midLoc);
			tmp = filepointer.readInt();
			setRightMostPage(filepointer, curPage, tmp);
			
			filepointer.seek((newPage-1)*DavisBaseConstants.pageSize+2);
			filepointer.writeShort(content);
			
			short offset = getCellOffsetValue(filepointer, curPage, cellA-1);
			filepointer.seek((curPage-1)*DavisBaseConstants.pageSize+2);
			filepointer.writeShort(offset);

			
			int parent = getParentNode(filepointer, curPage);
			setParentNode(filepointer, newPage, parent);
			
			byte num = (byte) cellA;
			setCellNumber(filepointer, curPage, num);
			num = (byte) cellB;
			setCellNumber(filepointer, newPage, num);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	public static void splitLeaf(RandomAccessFile filepointer, int page){
		int newPage = createLeafPage(filepointer);
		int midKey = findMiddleKey(filepointer, page);
		splitLeafPg(filepointer, page, newPage);
		int parent = getParentNode(filepointer, page);
		if(parent == 0){
			int rootPage = createInteriorPage(filepointer);
			setParentNode(filepointer, page, rootPage);
			setParentNode(filepointer, newPage, rootPage);
			setRightMostPage(filepointer, rootPage, newPage);
			insertInteriorCellAt(filepointer, rootPage, page, midKey);
		}else{
			long ploc = getPointerLocation(filepointer, page, parent);
			setPointerLocation(filepointer, ploc, parent, newPage);
			insertInteriorCellAt(filepointer, parent, page, midKey);
			sortCellsArray(filepointer, parent);
			while(checkInteriorFreeSpace(filepointer, parent)){
				parent = splitInter(filepointer, parent);
			}
		}
	}

	public static int splitInter(RandomAccessFile filepointer, int page){
		int newPage = createInteriorPage(filepointer);
		int midKey = findMiddleKey(filepointer, page);
		splitInteriorPage(filepointer, page, newPage);
		int parent = getParentNode(filepointer, page);
		if(parent == 0){
			int rootPage = createInteriorPage(filepointer);
			setParentNode(filepointer, page, rootPage);
			setParentNode(filepointer, newPage, rootPage);
			setRightMostPage(filepointer, rootPage, newPage);
			insertInteriorCellAt(filepointer, rootPage, page, midKey);
			return rootPage;
		}else{
			long ploc = getPointerLocation(filepointer, page, parent);
			setPointerLocation(filepointer, ploc, parent, newPage);
			insertInteriorCellAt(filepointer, parent, page, midKey);
			sortCellsArray(filepointer, parent);
			return parent;
		}
	}

	
	public static void sortCellsArray(RandomAccessFile filepointer, int page){
		 byte num = getNumofCells(filepointer, page);
		 int[] recordsRowIdsList = getRecordsrowIds(filepointer, page);
		 short[] cellArray = getCellArrayList(filepointer, page);
		 int ltmp;
		 short rtmp;

		 for (int i = 1; i < num; i++) {
            for(int j = i ; j > 0 ; j--){
                if(recordsRowIdsList[j] < recordsRowIdsList[j-1]){

                    ltmp = recordsRowIdsList[j];
                    recordsRowIdsList[j] = recordsRowIdsList[j-1];
                    recordsRowIdsList[j-1] = ltmp;

                    rtmp = cellArray[j];
                    cellArray[j] = cellArray[j-1];
                    cellArray[j-1] = rtmp;
                }
            }
         }

         try{
        	 filepointer.seek((page-1)*DavisBaseConstants.pageSize+12);
         	for(int i = 0; i < num; i++){
         		filepointer.writeShort(cellArray[i]);
			}
         }catch(Exception e){
         	System.out.println("Error at sortCellArray");
         }
	}

	public static int[] getRecordsrowIds(RandomAccessFile filepointer, int page){
		int num = new Integer(getNumofCells(filepointer, page));
		int[] array = new int[num];

		try{
			filepointer.seek((page-1)*DavisBaseConstants.pageSize);
			byte pageType = filepointer.readByte();
			byte offset = 0;
			switch(pageType){
			    case 0x0d:
				    offset = 2;
				    break;
				case 0x05:
					offset = 4;
					break;
				default:
					offset = 2;
					break;
			}

			for(int i = 0; i < num; i++){
				long loc = getLocOfCell(filepointer, page, i);
				filepointer.seek(loc+offset);
				array[i] = filepointer.readInt();
			}

		}catch(Exception e){
			e.printStackTrace();
		}

		return array;
	}
	
	public static short[] getCellArrayList(RandomAccessFile filepointer, int page){
		int num = new Integer(getNumofCells(filepointer, page));
		short[] arr = new short[num];

		try{
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+12);
			for(int i = 0; i < num; i++){
				arr[i] = filepointer.readShort();
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return arr;
	}

	
	public static long getPointerLocation(RandomAccessFile filepointer, int page, int parent){
		long val = 0;
		try{
			int numCells = new Integer(getNumofCells(filepointer, parent));
			for(int i=0; i < numCells; i++){
				long loc = getLocOfCell(filepointer, parent, i);
				filepointer.seek(loc);
				int childPage = filepointer.readInt();
				if(childPage == page){
					val = loc;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return val;
	}

	public static void setPointerLocation(RandomAccessFile filepointer, long loc, int parent, int page){
		try{
			if(loc == 0){
				filepointer.seek((parent-1)*DavisBaseConstants.pageSize+4);
			}else{
				filepointer.seek(loc);
			}
			filepointer.writeInt(page);
		}catch(Exception e){
			e.printStackTrace();
		}
	} 

	
	public static void insertInteriorCellAt(RandomAccessFile filepointer, int page, int child, int key){
		try{
			
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+2);
			short content = filepointer.readShort();
			
			if(content == 0)
				content = DavisBaseConstants.pageSize;
			
			content = (short)(content - 8);
			
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+content);
			filepointer.writeInt(child);
			filepointer.writeInt(key);
			
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+2);
			filepointer.writeShort(content);
			
			byte num = getNumofCells(filepointer, page);
			setCellOffsetValue(filepointer, page ,num, content);
			
			num = (byte) (num + 1);
			setCellNumber(filepointer, page, num);

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void insertLeafCellAt(RandomAccessFile filepointer, int page, int offset, short plsize, int key, byte[] stc, String[] vals){
		try{
			String s;
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+offset);
			filepointer.writeShort(plsize);
			filepointer.writeInt(key);
			int col = vals.length - 1;
			filepointer.writeByte(col);
			filepointer.write(stc);
			for(int i = 1; i < vals.length; i++){
				switch(stc[i-1]){
					case DavisBaseConstants.ONE_BYTE_NULL_SERIAL_TYPE_CODE:
						filepointer.writeByte(0);
						break;
					case DavisBaseConstants.TWO_BYTE_NULL_SERIAL_TYPE_CODE:
						filepointer.writeShort(0);
						break;
					case DavisBaseConstants.FOUR_BYTE_NULL_SERIAL_TYPE_CODE:
						filepointer.writeInt(0);
						break;
					case DavisBaseConstants.EIGHT_BYTE_NULL_SERIAL_TYPE_CODE:
						filepointer.writeLong(0);
						break;
					case DavisBaseConstants.TINY_INT_SERIAL_TYPE_CODE:
						filepointer.writeByte(new Byte(vals[i]));
						break;
					case DavisBaseConstants.SMALL_INT_SERIAL_TYPE_CODE:
						filepointer.writeShort(new Short(vals[i]));
						break;
					case DavisBaseConstants.INT_SERIAL_TYPE_CODE:
						filepointer.writeInt(new Integer(vals[i]));
						break;
					case DavisBaseConstants.BIG_INT_SERIAL_TYPE_CODE:
						filepointer.writeLong(new Long(vals[i]));
						break;
					case DavisBaseConstants.REAL_SERIAL_TYPE_CODE:
						filepointer.writeFloat(new Float(vals[i]));
						break;
					case DavisBaseConstants.DOUBLE_SERIAL_TYPE_CODE:
						filepointer.writeDouble(new Double(vals[i]));
						break;
					case DavisBaseConstants.DATE_TIME_SERIAL_TYPE_CODE:
						s = vals[i];
						Date temp = new SimpleDateFormat(DavisBaseConstants.DATEPATTERN).parse(s.substring(1, s.length()-1));
						long time = temp.getTime();
						filepointer.writeLong(time);
						break;
					case DavisBaseConstants.DATE_SERIAL_TYPE_CODE:
						s = vals[i];
						s = s.substring(1, s.length()-1);
						s = s+"_00:00:00";
						Date temp2 = new SimpleDateFormat(DavisBaseConstants.DATEPATTERN).parse(s);
						long time2 = temp2.getTime();
						filepointer.writeLong(time2);
						break;
					default:
						filepointer.writeBytes(vals[i]);
						break;
				}
			}
			int n = getNumofCells(filepointer, page);
			byte tmp = (byte) (n+1);
			setCellNumber(filepointer, page, tmp);
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+12+n*2);
			filepointer.writeShort(offset);
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+2);
			int content = filepointer.readShort();
			if(content >= offset || content == 0){
				filepointer.seek((page-1)*DavisBaseConstants.pageSize+2);
				filepointer.writeShort(offset);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static boolean checkInteriorFreeSpace(RandomAccessFile filepointer, int page){
		byte numCells = getNumofCells(filepointer, page);
		if(numCells > 30)
			return true;
		else
			return false;
	}

	public static int checkLeafFreeSpace(RandomAccessFile filepointer, int page, int size){
		int val = -1;

		try{
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+2);
			int content = filepointer.readShort();
			if(content == 0)
				return DavisBaseConstants.pageSize - size;
			int numCells = getNumofCells(filepointer, page);
			int space = content - 20 - 2*numCells;
			if(size < space)
				return content - size;
			
		}catch(Exception e){
			e.printStackTrace();
		}

		return val;
	}

	
	public static int getParentNode(RandomAccessFile filepointer, int page){
		int val = 0;

		try{
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+8);
			val = filepointer.readInt();
		}catch(Exception e){
			e.printStackTrace();
		}

		return val;
	}

	public static void setParentNode(RandomAccessFile filepointer, int page, int parent){
		try{
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+8);
			filepointer.writeInt(parent);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static int getRightMostPage(RandomAccessFile filepointer, int page){
		int rl = 0;

		try{
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+4);
			rl = filepointer.readInt();
		}catch(Exception e){
			System.out.println("Error at getRightMost");
		}

		return rl;
	}

	public static void setRightMostPage(RandomAccessFile filepointer, int page, int rightLeaf){

		try{
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+4);
			filepointer.writeInt(rightLeaf);
		}catch(Exception e){
			System.out.println("Error at setRightMost");
		}

	}

	public static boolean keyExists(RandomAccessFile filepointer, int page, int key){
		int[] recordsRowIdsList = getRecordsrowIds(filepointer, page);
		for(int i : recordsRowIdsList)
			if(key == i)
				return true;
		return false;
	}
	
	public static long getLocOfCell(RandomAccessFile filepointer, int page, int id){
		long loc = 0;
		try{
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+12+id*2);
			short offset = filepointer.readShort();
			long orig = (page-1)*DavisBaseConstants.pageSize;
			loc = orig + offset;
		}catch(Exception e){
			System.out.println(e);
		}
		return loc;
	}

	public static byte getNumofCells(RandomAccessFile filepointer, int page){
		byte val = 0;

		try{
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+1);
			val = filepointer.readByte();
		}catch(Exception e){
			System.out.println(e);
		}

		return val;
	}

	public static void setCellNumber(RandomAccessFile filepointer, int page, byte num){
		try{
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+1);
			filepointer.writeByte(num);
		}catch(Exception e){
			System.out.println(e);
		}
	}
	
	public static short getCellOffsetValue(RandomAccessFile filepointer, int page, int id){
		short offset = 0;
		try{
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+12+id*2);
			offset = filepointer.readShort();
		}catch(Exception e){
			System.out.println(e);
		}
		return offset;
	}

	public static void setCellOffsetValue(RandomAccessFile filepointer, int page, int id, int offset){
		try{
			filepointer.seek((page-1)*DavisBaseConstants.pageSize+12+id*2);
			filepointer.writeShort(offset);
		}catch(Exception e){
			System.out.println(e);
		}
	}
    
	public static byte getPgType(RandomAccessFile filepointer, int page){
		byte pageType=0x05;
		try {
			filepointer.seek((page-1)*DavisBaseConstants.pageSize);
			pageType = filepointer.readByte();
		} catch (Exception e) {
			System.out.println(e);
		}
		return pageType;
	}
}















