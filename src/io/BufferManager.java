package io;
import java.util.HashMap;

import query.QueryParser;

public class BufferManager{
	
	public int number_row; 
	public HashMap<Integer, String[]> contentMap;
	public String[] columnName; 
	public int[] displayformat; 
	
	public BufferManager(){
		number_row = 0;
		contentMap = new HashMap<Integer, String[]>();
	}

	public void add_vals(int rowid, String[] value){
		contentMap.put(rowid, value);
		number_row = number_row + 1;
	}

	public String formatString(int length, String s) {
		return String.format("%-"+(length+3)+"s", s);
	}


	public void displayContent(String[] col){
		
		if(number_row == 0)
			System.out.println("Empty set.");
		else{
			for(int i = 0; i < displayformat.length; ++i)
				displayformat[i] = columnName[i].length();
			for(String[] i : contentMap.values())
				for(int j = 0; j < i.length; ++j)
					if(displayformat[j] < i[j].length())
						displayformat[j] = i[j].length();
			
			if(col[0].equals("*")){
				
				for(int l: displayformat)
					System.out.print(QueryParser.line("-", l+3));
				
				System.out.println();
				
				for(int i = 0; i< columnName.length; ++i)
					System.out.print(formatString(displayformat[i], columnName[i])+"|");
				
				System.out.println();
				
				for(int len: displayformat)
					System.out.print(QueryParser.line("-", len+3));
				
				System.out.println();

				for(String[] i : contentMap.values()){
					for(int j = 0; j < i.length; ++j)
						System.out.print(formatString(displayformat[j], i[j])+"|");
					System.out.println();
				}
			
			}
			else{
				int[] controlList = new int[col.length];
				for(int j = 0; j < col.length; ++j)
					for(int i = 0; i < columnName.length; ++i)
						if(col[j].equals(columnName[i]))
							controlList[j] = i;

				for(int j = 0; j < controlList.length; ++j)
					System.out.print(QueryParser.line("-", displayformat[controlList[j]]+3));
				
				System.out.println();
				
				for(int j = 0; j < controlList.length; ++j)
					System.out.print(formatString(displayformat[controlList[j]], columnName[controlList[j]])+"|");
				
				System.out.println();
				
				for(int j = 0; j < controlList.length; ++j)
					System.out.print(QueryParser.line("-", displayformat[controlList[j]]+3));
				
				System.out.println();
				
				for(String[] i : contentMap.values()){
					for(int j = 0; j < controlList.length; ++j)
						System.out.print(formatString(displayformat[controlList[j]], i[controlList[j]])+"|");
					System.out.println();
				}
				System.out.println();
			}
		}
	}
}