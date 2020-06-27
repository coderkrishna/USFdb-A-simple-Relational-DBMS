package model.table;

public class Column {
    public String name;
    public String type;
    public boolean isNull;
    
    private Column(String name,String type,boolean isNull){
    	this.name = name;
    	this.type = type;
    	this.isNull = isNull;
    }
    
    public static Column createColumnList(String column){
        String primaryKey = "primary key";
        String notNull = "not null";
        boolean isNull = true;
        if(column.toLowerCase().endsWith(primaryKey))
			column = column.substring(0, column.length() - primaryKey.length()).trim();
		else if(column.toLowerCase().endsWith(notNull)){
            column = column.substring(0, column.length() - notNull.length()).trim();
            isNull = false;
        }

        String[] partsList = column.split(" ");
        String name;
        if(partsList.length > 2){
            System.out.println("Expected column format <name> <datatype> [PRIMARY KEY | NOT NULL]");
            return null;
        }

        if(partsList.length > 1){
            name = partsList[0].trim();
            String type = partsList[1].trim();
            if (type != null)
				return new Column(name, type, isNull);
			System.out.println(column + "Unrecognised data type " + partsList[1]);
			return null;
        }

        System.out.println(column + "Expected column format <name> <datatype> [PRIMARY KEY | NOT NULL]");
        return null;
    }
}
