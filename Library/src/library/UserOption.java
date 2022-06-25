package library;

import java.util.Scanner;
import java.util.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.io.*;
import java.sql.Date;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

// Linux Command 
/*
scp -r ./src cschiu0@linux5.cse.cuhk.edu.hk:/uac/y20/cschiu0
linux5:~> cd src
linux5:~/src> ls
./  ../  .DS_Store  library/  mysql-connector-java-5.1.47.jar  sample_data/
linux5:~/src> cd library/
linux5:~/src/library> ls
./  ../  AdminOption.java  .DS_Store  LibOption.java  MainMenu.java  mysql-connector-java-5.1.47.jar  sample_data/  UserOption.java
linux5:~/src/library> cd ..
linux5:~/src> javac *.java
javac: file not found: *.java
Usage: javac <options> <source files>
use -help for a list of possible options
linux5:~/src> javac library/*.java
linux5:~/src> java -cp .:mysql-connector-java-5.1.47.jar library/MainMenu
Welcome to Library Inquiry System

-----Main menu-----
What kinds of operations would you like to perform?
1. Operations for Administrator
2. Operations for Library User
3. Operations for Librarian
4. Exit this program
Enter Your choice: 2
*/

public class UserOption extends MainMenu{
    public static String[] TableList={
        "user_category", 
        "libuser",
        "book_category",
        "book",
        "copy",
        "borrow",
        "authorship"
    };

    //Show User Menu
    public static void ShowUserMenu(){
        System.out.println("\n-----Operations for library user menu-----");
        System.out.println("What kind of operation would you like to perform?");
        System.out.println("1. Search for Books");
        System.out.println("2. Show loan record of a user");
        System.out.println("3. Return to the main menu");
        
        System.out.print("Enter Your Choice: ");
    }



    //Get User Input for Admin Operation
    public static void UserChoice(){
        Scanner keyboard = new Scanner(System.in);
        int choice;
        do{
            ShowUserMenu();
            choice = keyboard.nextInt();
            switch(choice){
                case 1:SearchBooks();break;
                case 2:ShowLoan();break;
                case 3:break;
            }

        }while(choice!=3);
    }




// missing output : ava. no of copy
    public static void SearchBooks(){
        System.out.println("Choose the search criterion: ");
        System.out.println("1. call number");
        System.out.println("2. title");
        System.out.println("3. authour");
        System.out.print("Choose the search criterion: ");

        // Read user's input
        Scanner keyboard =new Scanner(System.in);
        int choice = keyboard.nextInt();


        // User wants to search books by call number 
        if (choice == 1) 
        {
            // connect to jdbc in try catch 
            Connection con = null;
            try {
                // input passcode in connection
                con = DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
                Statement stmt = null;
                
                // read callnumber of the book
                Scanner Keyboard= new Scanner(System.in);  
                System.out.printf("Type in the Search Keyword: ");          //skills: need printf   // no println or print
                String CallNum = Keyboard.next();  

       


                /* old SQL1 statement: 
                "SELECT book.callnum, book.title, book.rating, book_category.bcname, authorship.aname FROM book "
                    + "INNER JOIN book_category ON book.bcid=book_category.bcid " 
                    + "INNER JOIN authorship ON book.callnum=authorship.callnum " 
                    // + "INNER JOIN copy ON copy.callnum=book.callnum "          // need to fix corrected
                    // + "INNER JOIN borrow ON borrow.callnum=book.callnum "    // need to fix
                    + "WHERE book.callnum like '%s' "
                    + "ORDER BY book.callnum ASC" ;
                */

                // String sql = "SELECT returnno FROM borrow";
                // System.out.print("Callnum is " + CallNum + "\n");     //debugging
                // borrow.returnon  copy.copynum, MAX(copy.copynum)

                // sql to output query of the record 
                String sql = "SELECT DISTINCT book.callnum, book.title, book.rating, book_category.bcname, authorship.aname, copy.copynum FROM book "
                                 + "INNER JOIN book_category ON book.bcid=book_category.bcid " 
                                + "INNER JOIN authorship ON book.callnum=authorship.callnum " 
                                + "INNER JOIN copy ON copy.callnum=book.callnum "          // need to fix corrected
                                + "WHERE book.callnum like '%s' "
                                + "ORDER BY book.callnum, aname ASC" ;
             



                // sql to output the checkout record of each books' copy in order to calculate the no. of ava. copy 
                // only return the record that is NOT RETURN YET 
                String sql2 = "SELECT borrow.callnum, borrow.copynum, borrow.returnon FROM borrow "     // new add
                                + "INNER JOIN book ON book.callnum=borrow.callnum "
                                + "WHERE book.callnum like '%s' AND borrow.returnon is NULL ";
                                

                // input callnum into sql statement
                sql = String.format(sql, CallNum);
                sql2 = String.format(sql2, CallNum);        // new add


                System.out.println("|Call Num|Tilte|Book Category|Author|Rating|Avaliable No. of Copy|");
       




        // For First sql ----------------------------------------------------------------------------------------------------------
                // run sql in jdbc
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                
                // init output variables for sql1
                String callnum = "";
                String title = "";
                String BookCat = "";
                    // Since the output query will not re-arrange the author name
                String Author[] = new String[1000];
                int i = 0; 
                String Rating = ""; 
                int Copy = -1;
                int MaxCopy = -1; // MaxCopy = Max no. of copy of the book 
                  
                // read in output results from sql 
                while (rs.next()) {   
                    callnum = rs.getString("callnum");
                    title = rs.getString("title");
                    BookCat = rs.getString("bcname"); 
                    Author[i] = rs.getString("aname");
                    Rating = rs.getString("rating");
                    //Copy = rs.getString("MAX(copy.copynum)");       //need to fix
                    Copy = rs.getInt("copy.copynum"); 
                    //Return = rs.getString("returnon");    //need to fix

                    // System.out.println("Copy is " + Copy);              //debugging
                    // System.out.println("Copy Temp is " + MaxCopy);      //debugging

                    
                    // Returm max no. of copy of the book
                    if (Copy > MaxCopy) MaxCopy = Copy; 

                    // System.out.println("After : Copy Temp is " + MaxCopy);   //debugging
                    
                    //System.out.println("Author["+ i + "] is " + Author[i]);   //debugging
                    //System.out.print(callnum + "|" + title + "|" + BookCat + "|" + Author[i] + "|" + Rating + "|"   + "|\n") ;  //debugging
                    i++;
                }
              

                // Error Handling 
                if (callnum == null || callnum.isEmpty() ) {
                    System.out.println("[Error]:  This call number is not found. The call number is not exist");
                } else { 


        // For second sql ----------------------------------------------------------------------------------------------------------
                // run sql in jdbc
                stmt = con.createStatement();       // new
                ResultSet rs2 = stmt.executeQuery(sql2);        // new

                //init output varibles for sql2
                String callnum2 = "";                         //new
                int Copy2 = -1;
                String Return2 = "";     
                int notreturnCounter = 0; 

                // get output query 
                while (rs2.next()) {                          //new 
                    callnum2 = rs2.getString("callnum");
                    Copy2 = rs2.getInt("copynum");
                    Return2 = rs2.getString("returnon");   
                    notreturnCounter++;

                    //System.out.print("Borrowed Copy is  : |" + callnum2 + "|" + Copy2 + "|" + Return2 + "|\n") ;        //debugging
                }




     // Output result code ----------------------------------------------------------------------------------------------------------

                
                // Remove duplicated author names caused by extra column (copy.copynum)
                Author = Arrays.stream(Author).distinct().toArray(String[]::new);
                // System.out.println("Array after removing duplicates: " + Arrays.toString(Author));   //debugging

                // Combine seprated author name into one single line
                i = 0;
                String SumAuthor = "";
                for (i=0 ; i<Author.length ; i++) { 
                    if (Author[i] != null && !Author[i].equals("") ) {
                        SumAuthor = SumAuthor + Author[i] + "," ;
                        // System.out.println(Author[i]);      //debugging
                    }
                }
                //Elimitate the last , in SumAuthor
                SumAuthor = SumAuthor.substring(0, SumAuthor.length() - 1);


                int avacopy = 0;
                // Calculate how many no. of ava. copy the book has
                if (callnum2 == null || callnum2.isEmpty() ) {      // no record in borrow relation
                    avacopy = MaxCopy;
                } else {    // have record in borrow and not return yet 
                    avacopy = MaxCopy - notreturnCounter;
                }



                // System.out.println(Return);  //debugging
                System.out.print("|" + callnum + "|" + title + "|" + BookCat + "|" + SumAuthor + "|" + Rating + "|" + avacopy + "|\n") ; 
                // System.out.print("MaxCopy is : |" + callnum2  + "|" + MaxCopy + "|\n") ;        //debugging
            
            } // end of error handling

            } catch (SQLException ex) {
                    // handle any errors
                    System.out.println("SQLException: " + ex.getMessage());
                    System.out.println("SQLState: " + ex.getSQLState());
                    System.out.println("VendorError: " + ex.getErrorCode());
                }
            }
            

        // Users want to search book by title
        if (choice == 2) {
            
            Connection con = null;
            try {
                con = DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
                Statement stmt = null;
                
                Scanner Keyboard= new Scanner(System.in);  //System.in is a standard input stream  
                System.out.printf("Type in the Search Keyword: ");          //skills: need printf   // no println or print
                String Title = Keyboard.next(); 
                // System.out.println(Title);   //debugging
                


                String sql = "SELECT callnum, title FROM book "
                                + "WHERE title like '%s %%' OR title like '%% %s' OR title like '%% %s %%' or title like '%s' "
                                + "ORDER BY book.callnum ASC" ;

                sql = String.format(sql, Title, Title, Title, Title);
                System.out.println("|Call Num|Tilte|Book Category|Author|Rating|Avaliable No. of Copy|");

                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                String callnum[] = new String[100000];
                String title = "";
               // String Return = "";             
            
                int i = 0;

                while (rs.next()) {
                    title = rs.getString("title");
                    callnum[i] = rs.getString("callnum");
                
                    // System.out.print(callnum[i] + "|" + title + "\n");       //debugging 
                    i++;
                } // end of while

                i = 0;
                for (i=0; i<callnum.length ; i++) {
                    if (callnum[i] != null && !callnum[i].equals("") ) {
                    //    System.out.println(callnum[i]);       //debugging
                    }
                } // end of for loop
 
            // Error Handling 
                if (callnum[0] == null || callnum[0].isEmpty() ) {
                    System.out.println("[Error]:  There is no matching book with this title");
                } else {

                // for each callnum, output result like what searching by callnum does
                try {
                    con = DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
            
                    i = 0;
                    for (i=0 ; i<callnum.length ; i++) {

                            if (callnum[i] != null && !callnum[i].equals("") )  {
                        

                                // sql to output query of the record 
                                String sql2 = "SELECT DISTINCT book.callnum, book.title, book.rating, book_category.bcname, authorship.aname, copy.copynum FROM book "
                                                + "INNER JOIN book_category ON book.bcid=book_category.bcid " 
                                                + "INNER JOIN authorship ON book.callnum=authorship.callnum " 
                                                + "INNER JOIN copy ON copy.callnum=book.callnum "          // need to fix corrected
                                                + "WHERE book.callnum like '%s' "
                                                + "ORDER BY aname ASC" ;



                                // sql to output the checkout record of each books' copy in order to calculate the no. of ava. copy 
                                // only return the record that is NOT RETURN YET 
                                String sql3 = "SELECT borrow.callnum, borrow.copynum, borrow.returnon FROM borrow "     // new add
                                                + "INNER JOIN book ON book.callnum=borrow.callnum "
                                                + "WHERE book.callnum like '%s' AND borrow.returnon is NULL ";




                                sql2 = String.format(sql2, callnum[i]);
                                sql3 = String.format(sql3, callnum[i]);
                                //System.out.printf("callnum[" + i + "] is " + callnum[i] + "  ");      //debugging
                              

                                stmt = con.createStatement();
                                ResultSet rs2 = stmt.executeQuery(sql2);


                        // For first sql : ----------------------------------------------------------------------------------------

                                String callnum2 = "";
                                String title2 = "";
                                String BookCat2 = "";
                                String Author2[] = new String[1000];
                                int j = 0;
                                String Rating2 = "";
                                int Copy2 = -1;  
                                int MaxCopy2 = -1; // MaxCopy = Max no. of copy of the book


                                while (rs2.next()) {   
                                    callnum2 = rs2.getString("callnum");
                                    title2 = rs2.getString("title");
                                    BookCat2 = rs2.getString("bcname");
                                    Author2[j] = rs2.getString("aname");
                                    Rating2 = rs2.getString("rating");
                                    Copy2 = rs2.getInt("copynum");
                                    
                                    if (Copy2 > MaxCopy2) MaxCopy2 = Copy2; 
                                    //System.out.println("Author["+ j + "] is " + Author2[j]);
                                    j++;

                                } // end of while 

                        // For second sql ------------------------------------------------------------------------
                                        // run sql in jdbc
                                        stmt = con.createStatement();       // new
                                        ResultSet rs3 = stmt.executeQuery(sql3);        // new

                                        //init output varibles for sql2
                                        String callnum3 = "";                         //new
                                        int Copy3 = -1;
                                        String Return3 = "";     
                                        int notreturnCounter = 0; 

                                        // get output query 
                                        while (rs3.next()) {                          //new 
                                            callnum3 = rs3.getString("callnum");
                                            Copy3 = rs3.getInt("copynum");
                                            Return3 = rs3.getString("returnon");   
                                            notreturnCounter++;

                                            // System.out.print("Borrowed Copy is  : |" + callnum3 + "|" + Copy3 + "|" + Return3 + "|\n") ;        //debugging
                                        }

                        // Output the query --------------------------------------------------------------------------------------------------
                                // Remove duplicated author names caused by extra column (copy.copynum)
                                Author2 = Arrays.stream(Author2).distinct().toArray(String[]::new);
                                // System.out.println("Array2 after removing duplicates: " + Arrays.toString(Author2));   //debugging


                                j = 0;
                                String SumAuthor2 = "";
                                for (j=0 ; j<Author2.length ; j++) { 
                                    if (Author2[j] != null && !Author2[j].equals("") ) {
                                        SumAuthor2 = SumAuthor2 + Author2[j] + "," ;
                                    }
                                }

                                SumAuthor2 = SumAuthor2.substring(0, SumAuthor2.length() - 1);

                                
                                int avacopy = 0;
                                // Calculate how many no. of ava. copy the book has
                                if (callnum2 == null || callnum2.isEmpty() ) {      // no record in borrow relation
                                    avacopy = MaxCopy2;
                                } else {    // have record in borrow and not return yet 
                                    avacopy = MaxCopy2 - notreturnCounter;
                                }

 

                                // System.out.println(Return);
                                System.out.print("|" + callnum2 + "|" + title2 + "|" + BookCat2 + "|" + SumAuthor2 + "|" + Rating2 + "|" + avacopy + "|\n") ; 
                                // System.out.print("MaxCopy is : |" + callnum3  + "|" + MaxCopy2 + "|\n") ;        //debugging

                            } // end of if
                        
                    } // end of for loop
               

                } catch (SQLException ex) {
                    // handle any errors
                    System.out.println("SQLException: " + ex.getMessage());
                    System.out.println("SQLState: " + ex.getSQLState());
                    System.out.println("VendorError: " + ex.getErrorCode());
                     
                } // end of small try 
            } // end of error handling 




                // System.out.print(callnum + "|" + title + "|" + BookCat + "|" + SumAuthor + "|" + Rating + "|"  + Copy + "|\n") ; 
                //System.out.print(callnum + "|" + title);

            } catch (SQLException ex) {
                // handle any errors
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
                 
            }// end of big try
                        

        } // end of if (chioce ==2)




        // User want to search book by Author
        if (choice == 3) {

            Connection con = null;
            try {
                con = DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
                Statement stmt = null;
                
                Scanner Keyboard= new Scanner(System.in);  //System.in is a standard input stream  
                System.out.printf("Type in the Search Keyword: ");
                String Author = Keyboard.next(); 
 


                String sql = "SELECT authorship.callnum, aname FROM authorship "
                                + "WHERE authorship.aname LIKE '%s %%' OR authorship.aname like '%% %s' OR aname like '%% %s %%' OR aname like '%s' "
                                + "ORDER BY callnum ASC" ;
                sql = String.format(sql, Author, Author, Author, Author);
                System.out.println("|Call Num|Tilte|Book Category|Author|Rating|Avaliable No. of Copy|");

                // no. of ava copy = no. of copy - copy number

                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
     

                String callnum[] = new String[100000];
                String author = "";
               // String Return = "";             
            
                int i = 0;

                while (rs.next()) {
                    author = rs.getString("aname");
                    callnum[i] = rs.getString("callnum");
                
                    // System.out.print(callnum[i] + "|"  + author + "\n");     //debugging
                    i++;
                } // end of while

                i = 0;
                for (i=0; i<callnum.length ; i++) {
                    if (callnum[i] != null && !callnum[i].equals("") ) {
                    //    System.out.println(callnum[i]);     // debugging
                    }
                } // end of for loop
 
                
            // Error Handling 
            if (callnum[0] == null || callnum[0].isEmpty() ) {
                System.out.println("[Error]:  There is no author named " + Author + " found");
            } else {
                

                try {
                    con = DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
                 //   Statement stmt2 = null;
                    i = 0;
                    for (i=0 ; i<callnum.length ; i++) {

                            if (callnum[i] != null && !callnum[i].equals("") )  {
                                // sql to output query of the record 
                                String sql2 = "SELECT DISTINCT book.callnum, book.title, book.rating, book_category.bcname, authorship.aname, copy.copynum FROM book "
                                                + "INNER JOIN book_category ON book.bcid=book_category.bcid " 
                                                + "INNER JOIN authorship ON book.callnum=authorship.callnum " 
                                                + "INNER JOIN copy ON copy.callnum=book.callnum "          // need to fix corrected
                                                + "WHERE book.callnum like '%s' "
                                                + "ORDER BY aname ASC" ;



                                // sql to output the checkout record of each books' copy in order to calculate the no. of ava. copy 
                                // only return the record that is NOT RETURN YET 
                                String sql3 = "SELECT borrow.callnum, borrow.copynum, borrow.returnon FROM borrow "     // new add
                                                + "INNER JOIN book ON book.callnum=borrow.callnum "
                                                + "WHERE book.callnum like '%s' AND borrow.returnon is NULL ";




                                sql2 = String.format(sql2, callnum[i]);
                                sql3 = String.format(sql3, callnum[i]);
                                //System.out.printf("callnum[" + i + "] is " + callnum[i] + "  ");      //debugging
                              

                                stmt = con.createStatement();
                                ResultSet rs2 = stmt.executeQuery(sql2);


                        // For first sql : ----------------------------------------------------------------------------------------

                                String callnum2 = "";
                                String title2 = "";
                                String BookCat2 = "";
                                String Author2[] = new String[1000];
                                int j = 0;
                                String Rating2 = "";
                                int Copy2 = -1;  
                                int MaxCopy2 = -1; // MaxCopy = Max no. of copy of the book


                                while (rs2.next()) {   
                                    callnum2 = rs2.getString("callnum");
                                    title2 = rs2.getString("title");
                                    BookCat2 = rs2.getString("bcname");
                                    Author2[j] = rs2.getString("aname");
                                    Rating2 = rs2.getString("rating");
                                    Copy2 = rs2.getInt("copynum");
                                    
                                    if (Copy2 > MaxCopy2) MaxCopy2 = Copy2; 
                                    //System.out.println("Author["+ j + "] is " + Author2[j]);
                                    j++;

                                } // end of while 

                        // For second sql ------------------------------------------------------------------------
                                        // run sql in jdbc
                                        stmt = con.createStatement();       // new
                                        ResultSet rs3 = stmt.executeQuery(sql3);        // new

                                        //init output varibles for sql2
                                        String callnum3 = "";                         //new
                                        int Copy3 = -1;
                                        String Return3 = "";     
                                        int notreturnCounter = 0; 

                                        // get output query 
                                        while (rs3.next()) {                          //new 
                                            callnum3 = rs3.getString("callnum");
                                            Copy3 = rs3.getInt("copynum");
                                            Return3 = rs3.getString("returnon");   
                                            notreturnCounter++;

                                            // System.out.print("Borrowed Copy is  : |" + callnum3 + "|" + Copy3 + "|" + Return3 + "|\n") ;        //debugging
                                        }

                        // Output the query --------------------------------------------------------------------------------------------------
                                // Remove duplicated author names caused by extra column (copy.copynum)
                                Author2 = Arrays.stream(Author2).distinct().toArray(String[]::new);
                                // System.out.println("Array2 after removing duplicates: " + Arrays.toString(Author2));   //debugging


                                j = 0;
                                String SumAuthor2 = "";
                                for (j=0 ; j<Author2.length ; j++) { 
                                    if (Author2[j] != null && !Author2[j].equals("") ) {
                                        SumAuthor2 = SumAuthor2 + Author2[j] + "," ;
                                    }
                                }

                                SumAuthor2 = SumAuthor2.substring(0, SumAuthor2.length() - 1);

                                
                                int avacopy = 0;
                                // Calculate how many no. of ava. copy the book has
                                if (callnum2 == null || callnum2.isEmpty() ) {      // no record in borrow relation
                                    avacopy = MaxCopy2;
                                } else {    // have record in borrow and not return yet 
                                    avacopy = MaxCopy2 - notreturnCounter;
                                }

 

                                // System.out.println(Return);
                                System.out.print("|" + callnum2 + "|" + title2 + "|" + BookCat2 + "|" + SumAuthor2 + "|" + Rating2 + "|" + avacopy + "|\n") ; 
                                // System.out.print("MaxCopy is : |" + callnum3  + "|" + MaxCopy2 + "|\n") ;        //debugging

                        
                            } // end of if
                        
                    } // end of for loop


                } catch (SQLException ex) {
                    // handle any errors
                    System.out.println("SQLException: " + ex.getMessage());
                    System.out.println("SQLState: " + ex.getSQLState());
                    System.out.println("VendorError: " + ex.getErrorCode());
                     
                } // end of small try 


            } // end of error handling




            } catch (SQLException ex) {
                    // handle any errors
                    System.out.println("SQLException: " + ex.getMessage());
                    System.out.println("SQLState: " + ex.getSQLState());
                    System.out.println("VendorError: " + ex.getErrorCode());
                } // end of big try 

        }

        System.out.println("End of Query\n");

    }







    public static void ShowLoan() {

       Connection con = null;
        try {
            con = DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
            Statement stmt = null;
            
            Scanner Keyboard= new Scanner(System.in);  //System.in is a standard input stream  
            System.out.printf("Enter The user ID: ");
            String UserID = Keyboard.next(); 
            System.out.println("Loan Record:");

            // Each book copy has a unique pair of call number and copy number and they can be used jointly to identify a book copy
            // authorship.aname,
            String sql = "SELECT borrow.callnum, borrow.copynum, book.title, borrow.checkout, borrow.returnon  FROM libuser "
                            + "INNER JOIN borrow ON borrow.libuid = libuser.libuid "
                            + "INNER JOIN book ON book.callnum = borrow.callnum "
                            + "WHERE libuser.libuid = '%s' "
                            + "ORDER BY borrow.checkout DESC";
            sql = String.format(sql, UserID);  ////////
            System.out.println("|CallNum|CopyNum|Title|Author|Check-out|Returned?|");
      
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
            String callnum[] = new String[100000];
            int i = 0;
            // String Copy = "";
            // String title = "";
            // int i = 0;
            // String Author[] = new String[10];
            // String checkout = "";
            // String Return = "";
            while (rs.next()) {
               
                callnum[i] = rs.getString("callnum");
                // Copy = rs.getString("copynum");
                // title = rs.getString("title");
                // Author[i] = rs.getString("aname");
                // checkout = rs.getString("checkout");
                // Return = rs.getString("returnon");
                 
              
                // if (Return != null && !Return.equals("") ) {  // return is not null 
                //     Return = "Yes";
                // } else Return = "No";
                i++;
                //System.out.println(callnum+ "|" + Copy + "|" + title + "|" + Author[i] + "|" + checkout + "|" + Return + "|") ; 
             }
            
            
            i = 0;
            for (i=0 ; i<callnum.length ; i++) { 
                if (callnum[i] != null && !callnum[i].equals("") ) {
                //    System.out.println(callnum[i]);           //debugging
                }
            }

            
    // Error Handling 
            if (callnum[0] == null || callnum[0].isEmpty() ) {
                System.out.println("[Error]:  This User's Loan is not found. The User ID has no checkout record or is not exist");
            }




            try {
                con = DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
             //   Statement stmt2 = null;
                i = 0;
                for (i=0 ; i<callnum.length ; i++) {

                        if (callnum[i] != null && !callnum[i].equals("") )  {
                            
                            String sql2 = "SELECT borrow.callnum, borrow.copynum, book.title, authorship.aname, borrow.checkout, borrow.returnon  FROM libuser "
                                                + "INNER JOIN borrow ON borrow.libuid = libuser.libuid "
                                                + "INNER JOIN book ON book.callnum = borrow.callnum "
                                                + "INNER JOIN authorship ON authorship.callnum = borrow.callnum "
                                                + "WHERE libuser.libuid = '%s' AND borrow.callnum = '%s' ";
                            sql2 = String.format(sql2, UserID,  callnum[i]);
                            //System.out.printf("callnum[" + i + "] is " + callnum[i] + "  ");      //debugging
                            // System.out.println("|Call Num|Tilte|Book Category|Author|Rating|Avaliable No. of Copy|");

                            stmt = con.createStatement();
                            ResultSet rs2 = stmt.executeQuery(sql2);

                            String callnum2 = "";
                            String Copy = "";
                            String title = "";                           
                            String Author[] = new String[1000];
                            int j = 0;
                            String checkout = "";
                            String Return = "";  

            

                            while (rs2.next()) {   
                                callnum2 = rs2.getString("callnum");
                                Copy = rs2.getString("copynum");
                                title = rs2.getString("title");
                                Author[j] = rs2.getString("aname");
                                checkout = rs2.getString("checkout");
                                Return = rs2.getString("returnon");

                                if (Return != null && !Return.equals("") ) {  // return is not null 
                                    Return = "Yes";
                                } else Return = "No";

                                // System.out.println("Author["+ j + "] is " + Author[j]);  //debugging
                                j++;

                            } // end of while 


                            j = 0;
                            String SumAuthor2 = "";
                            for (j=0 ; j<Author.length ; j++) { 
                                if (Author[j] != null && !Author[j].equals("") ) {
                                        SumAuthor2 = SumAuthor2 + Author[j] + "," ;
                                }
                            }
                            SumAuthor2 = SumAuthor2.substring(0, SumAuthor2.length() - 1);
                            

                            // System.out.println(Return);
                            System.out.println("|" + callnum2 + "|" + Copy + "|" + title + "|" + SumAuthor2 + "|" + checkout + "|" + Return + "|") ; 
                    
                        } // end of if
                    
                } // end of for loop


            } catch (SQLException ex) {
                // handle any errors
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
                 
            }   // end of small try






                // i = 0;
                // String SumAuthor = "";
                // for (i=0 ; i<Author.length ; i++) { 
                //     if (Author[i] != null && !Author[i].equals("") ) {
                //         SumAuthor = SumAuthor + Author[i] + "," ;
                //     }
                // }
                
                // System.out.println(callnum+ "|" + Copy + "|" + title + "|" + SumAuthor + "|" + checkout + "|" + Return + "|") ; 
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
            } catch (SQLException ex) {
                // handle any errors
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            } // end of big try







/*
           if (isEmpty(userID) == true) {
                Connection conn = null;
                try {
                    String url = "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db65";
                    conn = DriverManager.getConnection(url);
                    Statement stmt = null;
               
                    String sql2 = "SELECT * FROM check_out INNER JOIN book ON book.callnum = check_out.callnum WHERE ucid = '%s'";
                    sql2 = String.format(sql2, UserID);  ////////

                    stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(sql2);

                    while (rs.next()) {
                       // String UserID = rs.getString("ucid");
                        String callnum = rs.getString("callnum");
                        String Copy = rs.getString("copynum");
                        String title = rs.getString("title");
                        String Author = rs.getString("aname");
                        String checkout = rs.getString("checkout");
                        String Return = rs.getString("return");
                        
                     
        
                        if (Return == "null") Return = "No"; else Return = "Yes";  
                        
                        System.out.print(callnum+ "|" + Copy + "|" + title + "|" + Author + "|" + checkout + "|" + checkout + "|" + Return + "|") ; 
                    }




                } catch (SQLException ex) {
                    // handle any errors
                    System.out.println("SQLException: " + ex.getMessage());
                    System.out.println("SQLState: " + ex.getSQLState());
                    System.out.println("VendorError: " + ex.getErrorCode());
        }

*/

          //  System.out.print(callnum+ "|" + Copy + "|" + title + "|" + checkout + "|" + checkout + "|" + Return + "|") ; 

     
//---------------------------------------------------to be edited
        



        System.out.println("End of Query");
    }


}