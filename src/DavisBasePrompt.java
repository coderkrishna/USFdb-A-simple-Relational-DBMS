import java.util.Scanner;

import common.DavisBaseConstants;
import query.QueryParser;

/**
 * Created by Yamini Thota on 16/4/2020.
 */

public class DavisBasePrompt {
	
	private static Scanner scanner = new Scanner(System.in).useDelimiter(";");
	
	public static void splashScreen() {
		System.out.println(QueryParser.line("-",80));
        System.out.println("Welcome to DavisBaseLite"); // Display the string.
		System.out.println("DavisBaseLite Version " + QueryParser.getVersion());
		System.out.println(QueryParser.getCopyright());
		System.out.println("\nType \"help;\" to display supported commands.");
		System.out.println(QueryParser.line("-",80));
	}
		

	public static void main(String[] args) {
		DatabaseHelper.InitializeDatabase();
		splashScreen();
		 while(!QueryParser.isExit) {
	            System.out.print(DavisBaseConstants.PROMPT);
	            QueryParser.parseUserCommand(scanner.next().replace("\n", "").replace("\r", " ").trim().toLowerCase());
	        }
		 System.out.println("Exiting...");
	}

}
