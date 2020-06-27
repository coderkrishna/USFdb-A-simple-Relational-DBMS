## To start the java application
    run DavisBasePrompt.java
	
## List of supported commands

•	HELP : This will list all the commands which are supported

	davisql> help;
			******************************************************************************
			SUPPORTED COMMANDS

			CREATE TABLE table_name (<column_name> <datatype> [PRIMARY KEY | NOT NULL]);
			Creates a table in database
			All commands below are case insensitive

			SHOW TABLES;
				Display the names of all tables.

			SELECT <column_list> FROM <table_name> [WHERE <condition>];
				Display table records whose optional <condition>
				is <column_name> = <value>.

			DROP TABLE <table_name>;
				Remove table data (i.e. all records) and its schema.

			INSERT INTO <table_name> (column1, column2, column3, ...) VALUES (value1, value2, value3, ...);
				Insert a record into the table

			VERSION;
				Display the program version.

			HELP;
				Display this help information.

			EXIT;
				Exit the program.
			******************************************************************************
•	SHOW TABLES : Shows the list of tables

davisql> show tables;
			--------------------
			table_name          |
			--------------------
			davisbase_tables    |
			davisbase_columns   |
			persons1            |
			persons2            |
			persons3            |

•	DROP TABLE :  Drops the table which is specified

davisql> drop table persons3;

davisql> show tables;

			CASE: SHOW
			--------------------
			table_name          |
			--------------------
			davisbase_tables    |
			davisbase_columns   |
			persons1            |
			persons2            |
   Drop in case of table not exists gives a error message:
      davisql> drop table test;

   CASE: DROP
   Table test does not exist.

•	CREATE TABLE :  Creates the table with given table description

davisql> create table persons3 ( id INT PRIMARY KEY, NAME TEXT NOT NULL, AGE INT NOT NULL, ADDRESS TEXT, SALARY INT );

davisql> show tables;

			CASE: SHOW
			--------------------
			table_name          |
			--------------------
			davisbase_tables    |
			davisbase_columns   |
			persons1            |
			persons2            |
			persons3            |
			
•	INSERT INTO :  Interest the row Into given table

davisql> INSERT INTO persons1 (id, NAME, AGE,ADDRESS,SALARY) VALUES (1, yamini, 25, Richardson, 1000);

if inserting the row with duplicate primary key will give Uniqueness constraint violation
Uniqueness constraint violation

•	SELECT : select * will display all the columns in a table without any condition.

davisql>  select * from persons1;
			CASE: SELECT
			------------------------------------------
			id   |name     |age   |address      |salary   |
			------------------------------------------
			1    |yamini   |25    |richardson   |1000     |
			2    |ratna    |25    |dallas       |1000     |
			3    |thota    |25    |texas        |1000     |
			
davisql>  select name from persons1;
				Note: only one attribute is supported

			CASE: SELECT
			---------
			name     |
			---------
			yamini   |
			ratna    |
			thota    |
			
davisql> select * from persons1 where id = 1;

			CASE: SELECT
			------------------------------------------
			id   |name     |age   |address      |salary   |
			------------------------------------------
			1    |yamini   |25    |richardson   |1000     |

•	Version : This command displays the version number

davisql> version;

			  DavisBaseLite Version v1.0
			  Yamini Ratna Thota

•	EXIT : we will exit from the terminal
davisql> exit;
			  Exiting...

