package common;

public final class DavisBaseConstants {
      private DavisBaseConstants(){
    	  
      }
      public static final String PROMPT = "davisql> ";
      public static final int pageSize = 512;
      public static final String DEFAULT_DATA_DIRNAME = "data";
      public static final String SYSTEM_TABLES_TABLENAME = "davisbase_tables";
      public static final String SYSTEM_COLUMNS_TABLENAME = "davisbase_columns";
      public static final String FILE_EXTENSION = ".tbl";
      public static final String DATEPATTERN = "yyyy-MM-dd_HH:mm:ss";
      
      public static final byte INVALID_CLASS = -1;
      public static final byte TINYINT = 0;
      public static final byte SMALLINT = 1;
      public static final byte INT = 2;
      public static final byte BIGINT = 3;
      public static final byte REAL = 4;
      public static final byte DOUBLE = 5;
      public static final byte DATE = 6;
      public static final byte DATETIME = 7;
      public static final byte TEXT = 8;
      
      //Serial Code DatabaseConstants
      public static final byte ONE_BYTE_NULL_SERIAL_TYPE_CODE = 0x00;
      public static final byte TWO_BYTE_NULL_SERIAL_TYPE_CODE = 0x01;
      public static final byte FOUR_BYTE_NULL_SERIAL_TYPE_CODE = 0x02;
      public static final byte EIGHT_BYTE_NULL_SERIAL_TYPE_CODE = 0x03;
      public static final byte TINY_INT_SERIAL_TYPE_CODE = 0x04;
      public static final byte SMALL_INT_SERIAL_TYPE_CODE = 0x05;
      public static final byte INT_SERIAL_TYPE_CODE = 0x06;
      public static final byte BIG_INT_SERIAL_TYPE_CODE = 0x07;
      public static final byte REAL_SERIAL_TYPE_CODE = 0x08;
      public static final byte DOUBLE_SERIAL_TYPE_CODE = 0x09;
      public static final byte DATE_TIME_SERIAL_TYPE_CODE = 0x0A;
      public static final byte DATE_SERIAL_TYPE_CODE = 0x0B;
      public static final byte TEXT_SERIAL_TYPE_CODE = 0x0C;
}
