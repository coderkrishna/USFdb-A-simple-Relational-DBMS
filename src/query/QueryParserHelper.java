package query;

import java.util.ArrayList;
import java.util.List;

import io.IOManager;
import model.table.Column;

public class QueryParserHelper {
	static final String CREATE_TABLE_COMMAND = "CREATE TABLE";
	static final String SELECT_COMMAND = "SELECT";
	static final String INSERT_COMMAND = "INSERT INTO";
	static final String DROP_TABLE_COMMAND = "DROP TABLE";
	
	public List<Column> createTableHandler(String tableName,String columnsPart){
		if(IOManager.tableExists(tableName)){
			System.out.println("Table "+tableName+" already exists.");
			return null;
		}
		ArrayList<Column> columnList = new ArrayList<>();
        String[] columnsList = columnsPart.split(",");
        
        for(String columnEntry : columnsList){
            Column column = Column.createColumnList(columnEntry.trim());
            if(column == null) 
            	return null;
            columnList.add(column);
        }
        return columnList;
	}
	
	public void selectHandler(String[] attributes, String tableName, String conditionString){    
        if (!IOManager.tableExists(tableName))
			System.out.println("Table " + tableName + " does not exist.");
		else
			Select.select(tableName, attributes,
					(conditionString.length() <= 0 ? new String[0] : parseWhereClause(conditionString)));
        
	}
	
	public static String[] parseWhereClause(String conditionsString){
		String listOfStrings[] = new String[3];
		String operators[] = new String[2];
		if(conditionsString.contains("=")) {
			operators = conditionsString.split("=");
			listOfStrings[0] = operators[0].trim();
			listOfStrings[1] = "=";
			listOfStrings[2] = operators[1].trim();
		}

		if(conditionsString.contains("<")) {
			operators = conditionsString.split("<");
			listOfStrings[0] = operators[0].trim();
			listOfStrings[1] = "<";
			listOfStrings[2] = operators[1].trim();
	
		}
		
		if(conditionsString.contains(">")) {
			operators = conditionsString.split(">");
			listOfStrings[0] = operators[0].trim();
			listOfStrings[1] = ">";
			listOfStrings[2] = operators[1].trim();
		}
		
		if(conditionsString.contains("<=")) {
			operators = conditionsString.split("<=");
			listOfStrings[0] = operators[0].trim();
			listOfStrings[1] = "<=";
			listOfStrings[2] = operators[1].trim();
		}

		if(conditionsString.contains(">=")) {
			operators = conditionsString.split(">=");
			listOfStrings[0] = operators[0].trim();
			listOfStrings[1] = ">=";
			listOfStrings[2] = operators[1].trim();
		}
		return listOfStrings;
	}
}
