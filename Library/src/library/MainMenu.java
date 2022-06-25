package library;

import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public class MainMenu{
    static final String dbAddress="jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db65";
    static final String dbUsername ="Group65";
    static final String dbPassword = "csci3170";
    static Connection conn = null;
    public static void main(String[] args) throws Exception {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }
        
        int choice=-1;
        Scanner keyboard=new Scanner(System.in);
        do{
            ShowMainMenu();
            choice=keyboard.nextInt();
            switch(choice){
                case 1: AdminOption.AdminChoice();break;
                case 2: UserOption.UserChoice();break;
                case 3: LibOption.LibChoice();break;
                case 4: break;
            }
        }while(choice!=4);
    }
    
    //Show Main Menu Option
    public static void ShowMainMenu(){
        System.out.println("Welcome to Library Inquiry System\n");
        System.out.println("-----Main menu-----");
        System.out.println("What kinds of operations would you like to perform?");
        System.out.println("1. Operations for Administrator");
        System.out.println("2. Operations for Library User");
        System.out.println("3. Operations for Librarian");
        System.out.println("4. Exit this program");
        System.out.print("Enter Your choice: ");
    }

}