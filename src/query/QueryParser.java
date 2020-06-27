package query;
import static java.lang.System.out;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.DavisBaseConstants;
import io.IOManager;
import model.table.Column;

public class QueryParser {
	public static String version = "v1.0";
	public static String copyright = "Yamini Ratna Thota";
	public static boolean isExit = false;
	
	public static String getVersion() {
		return version;
	}
	
	public static String getCopyright() {
		return copyright;
	}
	
	public static void displayVersion() {
		System.out.println("DavisBaseLite Version " + getVersion());
		System.out.println(getCopyright());
	}
	
	public static String line(String s,int num) {
		String $ = "";
		for(int i=0;i<num;++i)
			$ += s;
		return $;
	}
	
	public static void help() {
		out.println(line("*",80));
		out.println("SUPPORTED COMMANDS\n");
		out.println("CREATE TABLE table_name (<column_name> <datatype> [PRIMARY KEY | NOT NULL]);");
		out.println("Creates a table in database");
		out.println("All commands below are case insensitive\n");
		out.println("SHOW TABLES;");
		out.println("\tDisplay the names of all tables.\n");
		out.println("SELECT <column_list> FROM <table_name> [WHERE <condition>];");
		out.println("\tDisplay table records whose optional <condition>");
		out.println("\tis <column_name> = <value>.\n");
		out.println("DROP TABLE <table_name>;");
		out.println("\tRemove table data (i.e. all records) and its schema.\n");
		out.println("INSERT INTO <table_name> (column1, column2, column3, ...) VALUES (value1, value2, value3, ...);");
		out.println("\tInsert a record into the table\n");
		out.println("VERSION;");
		out.println("\tDisplay the program version.\n");
		out.println("HELP;");
		out.println("\tDisplay this help information.\n");
		out.println("EXIT;");
		out.println("\tExit the program.\n");
		out.println(line("*",80));
	}
	
	public static void parseUserCommand (String userCommand) {
		
		switch ((new ArrayList<String>(Arrays.asList(userCommand.split(" ")))).get(0)) {
		case "show":
			System.out.println("CASE: SHOW");
			ShowTables.showTables();
			break;
		case "select":
			System.out.println("CASE: SELECT");
			parseSelectQuery(userCommand);
			break;
		case "drop":
			System.out.println("CASE: DROP");
			parseDropTable(userCommand);
			break;
		case "create":
			System.out.println("CASE: CREATE");
			parseCreateTable(userCommand);
			break;
		case "insert":
			System.out.println("CASE: INSERT");
			parseInsertInto(userCommand);
			break;
		case "help":
			help();
			break;
		case "version":
			displayVersion();
			break;
		case "exit":
			isExit = true;
			break;
		case "quit":
			isExit = true;
		default:
			System.out.println("I didn't understand the command: \"" + userCommand + "\"");
			break;
		}
	}
	
	 private static boolean comparePartOfQuery(String userCommand, String expectedCommand) {
        String[] usrParts = userCommand.toLowerCase().split(" ");
        String[] actuaParts = expectedCommand.toLowerCase().split(" ");

        for(int i=0;i<actuaParts.length;++i)
			if (!actuaParts[i].equals(usrParts[i]))
				return false;

        return true;
    }
	
	public static void parseCreateTable(String createTableString) {
		  if(!comparePartOfQuery(createTableString, QueryParserHelper.CREATE_TABLE_COMMAND)){
			  System.out.println("Unrecognised command");
              return;
          }

          int indexOpenBracket = createTableString.toLowerCase().indexOf("(");
          if(indexOpenBracket == -1) {
              System.out.println( "Expected (");
              return;
          }

          if(!createTableString.endsWith(")")){
              System.out.println("Missing )");
              return;
          }
          String tableName = createTableString.substring(QueryParserHelper.CREATE_TABLE_COMMAND.length(), indexOpenBracket).trim();
          String columnsPart = createTableString.substring(indexOpenBracket + 1, createTableString.length()-1);
          QueryParserHelper queryhelper = new QueryParserHelper();
          List<Column> columns = queryhelper.createTableHandler(tableName, columnsPart);
          if(columns!=null)
			CreateTable.createTable(tableName, columns);
	}
	
	public static void parseDropTable(String dropTableString){
            if(!comparePartOfQuery(dropTableString, QueryParserHelper.DROP_TABLE_COMMAND)){
            	System.out.println("Unrecognised command");
                return;
            }
            
        	String[] tokens=dropTableString.split(" ");
        	String tableName = tokens[2];
        	if (!IOManager.tableExists(tableName))
				System.out.println("Table " + tableName + " does not exist.");
			else if (!tableName.equals(DavisBaseConstants.SYSTEM_TABLES_TABLENAME)
					&& !tableName.equals(DavisBaseConstants.SYSTEM_COLUMNS_TABLENAME))
				DropTable.drop(tableName);
			else
				System.out.println("Operation cannot be performed as you are trying to drop system tables");
	}
	
	public static void parseSelectQuery(String selectString){
		if(!comparePartOfQuery(selectString, QueryParserHelper.SELECT_COMMAND)){
			System.out.println("Unrecognised command");
            return;
        }

        int index = selectString.toLowerCase().indexOf("from");
        if(index == -1) {
            System.out.println("Expected FROM keyword");
            return;
        }

        String attributeList = selectString.substring(QueryParserHelper.SELECT_COMMAND.length(), index).trim();
        String restUserQuery = selectString.substring(index + "from".length());

        index = restUserQuery.toLowerCase().indexOf("where");
        QueryParserHelper queryhelper = new QueryParserHelper();
        if(index == -1) {
            String tableName = restUserQuery.trim();
            queryhelper.selectHandler(attributeList.split(","), tableName, "");
            return;
        }

        queryhelper.selectHandler(attributeList.split(","), restUserQuery.substring(0, index).trim(),
				restUserQuery.substring(index + "where".length()));
	}
	
	public static void parseInsertInto(String insertQuery){
		if(!comparePartOfQuery(insertQuery, QueryParserHelper.INSERT_COMMAND)){
			System.out.println("Unrecognised command");
            return;
        }

        String tableName = "";
        int valuesIndex = insertQuery.toLowerCase().indexOf("values");
        if(valuesIndex == -1) {
            System.out.println("Expected VALUES keyword");
            return;
        }

        String columnOptions = insertQuery.toLowerCase().substring(0, valuesIndex);
        int openBracketIndex = columnOptions.indexOf("(");

        if(openBracketIndex != -1) {
            tableName = insertQuery.substring(QueryParserHelper.INSERT_COMMAND.length(), openBracketIndex).trim();
            int closeBracketIndex = insertQuery.indexOf(")");
            if(closeBracketIndex == -1) {
                System.out.println("Expected ')'");
                return;
            }
        }

        if(tableName.equals(""))
			tableName = insertQuery.substring(QueryParserHelper.INSERT_COMMAND.length(), valuesIndex).trim();

        String valuesList = insertQuery.substring(valuesIndex + "values".length()).trim();
        if(!valuesList.startsWith("(")){
            System.out.println("Expected '('");
            return;
        }

        if(!valuesList.endsWith(")")){
        	System.out.println("Expected ')'");
            return;
        }

        valuesList = valuesList.substring(1, valuesList.length()-1);
        
        String[] insert_vals = valuesList.split(",");
        
		for(int i = 0; i < insert_vals.length; ++i)
			insert_vals[i] = insert_vals[i].trim();
        
    	if (IOManager.tableExists(tableName))
			InsertInto.insertInto(tableName, insert_vals);
		else
			System.out.println("Table " + tableName + " does not exist.");
	}
}
