package library;

import java.util.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData ;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.io.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.sql.Connection;
import java.util.stream.IntStream;


import java.text.ParseException;

public class LibOption extends MainMenu {

    public static String[] TableList={
        "user_category", 
        "libuser",
        "book_category",
        "book",
        "copy",
        "borrow",
        "authorship"
    };
    //Show Librarian Menu
    public static void ShowLibMenu(){
        System.out.println("\n-----Operations for librarian menu-----");
        System.out.println("What kind of operation would you like to perform?");
        System.out.println("1. Book Borrowing");
        System.out.println("2. Book Returning");
        System.out.println("3. List all un-returned book copies which are checked-out within a period");
        System.out.println("4. Return to the main menu");
        
        System.out.print("Enter Your Choice: ");
    }

    //Get User Input for Admin Operation
    public static void LibChoice() throws IOException {
        Scanner keyboard=new Scanner(System.in);
        int choice;
        do{
            ShowLibMenu();
            choice = keyboard.nextInt();
            switch(choice){
                case 1:bookBorrowing();break;
                case 2:bookReturning();break;
                case 3:unreturnBook();break;
                case 4:break;
            }
        
        
        
        }while(choice!=4);
        
       
    }

    
    public static void bookBorrowing() {
        Scanner keyboard=new Scanner(System.in);
        System.out.print("Enter The User ID: ");
        String uid = keyboard.nextLine();
        System.out.print("Enter The Call Number: ");
        String callNum = keyboard.nextLine();
        System.out.print("Enter The Copy Number: ");
        int copyNum = keyboard.nextInt();

        Connection con = null;
            try {
                // input passcode in connection
                con = DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
                Statement stmt = null;

                // Zeroth sql statment : Check whether the user ID existed
                String sql0 = "SELECT libuid FROM libuser WHERE libuser.libuid = '%s' ";
                sql0 = String.format(sql0, uid);
                // run sql in jdbc
                stmt = con.createStatement();
                ResultSet rs0 = stmt.executeQuery(sql0);
                String UserID = "";

                while (rs0.next()) {   
                    UserID = rs0.getString("libuid");
                }

                //-------------------------------------------------------------------------------------------------------------------------------------------
                // debugging
                // String sql5 = "SELECT * FROM borrow ";
                                                
                // sql5 = String.format(sql5);      // need to fix
                //    // new

                // stmt = con.createStatement();       // new
                // ResultSet rs5 = stmt.executeQuery(sql5); 

                // while (rs5.next()) {                          //new 
                //     String userid5 = rs5.getString("libuid");
                //     String Callnum5 = rs5.getString("callnum");
                //     String copynum5 = rs5.getString("copynum");
                //     String checkout5 = rs5.getString("checkout");
                //     String return5 = rs5.getString("returnon");

                //     System.out.print("Borrowed: |" + userid5 + "|" + Callnum5 + "|" + copynum5 +  "|" + checkout5 + "|" + "|" + return5 + "|\n") ;   //debugging
    
                // }
                // System.out.println(UserID);     //debugging
                 //-------------------------------------------------------------------------------------------------------------------------------------------

                if (!(UserID != null && !UserID.equals("")))  // User not existed
                    System.out.println("[Error] This User ID is not existed");
                else { 
             
                 
                    try {
                        con = DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
                        // First sql statement : Check whether the callnum exist
                        // sql to output query of the record 
                        String sql = "SELECT DISTINCT book.callnum, copy.copynum FROM book "
                                        + "INNER JOIN copy ON copy.callnum=book.callnum "         
                                        + "WHERE book.callnum like '%s' "
                                        + "ORDER BY book.callnum ASC" ;

                        // input callnum into sql statement
                        sql = String.format(sql, callNum);

                        // run sql in jdbc
                        stmt = con.createStatement();
                        ResultSet rs = stmt.executeQuery(sql);
                        
                        // init output variables for sql1
                        String callnum1 = "";
                        int copynum1 = -1;
                        int MaxCopy = 0;

                        while (rs.next()) {   
                            callnum1 = rs.getString("book.callnum");
                            copynum1 = rs.getInt("copy.copynum"); 

                            if (copynum1 > MaxCopy) MaxCopy = copynum1; 

                            // System.out.println("|" + callnum1 + "|" + copynum1 + "|" + MaxCopy + "|");
                        }


                        // System.out.println("|" + callnum1 + "|" + copynum1 + "|");   //degugging
                    




                            // if callnum existed then 
                            if (!(callnum1 != null && !callnum1.equals(""))) {
                                System.out.println("[Error] This callnum is not existed");
                            } else {

                                    try {
                                        con = DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
                                        String sql2 = "SELECT borrow.callnum, borrow.copynum, borrow.returnon FROM borrow "     // new add
                                                            + "INNER JOIN book ON book.callnum=borrow.callnum "
                                                            + "WHERE book.callnum like '%s' AND borrow.returnon is NULL ";
                                        
                                        sql2 = String.format(sql2, callnum1);      
                                        
                                        // For second sql ----------------------------------------------------------------------------------------------------------
                                        // run sql in jdbc
                                        stmt = con.createStatement();       // new
                                        ResultSet rs2 = stmt.executeQuery(sql2);       // new

                                        //init output varibles for sql2
                                        String callnum2 = "";                         //new
                                        int Copy2[] = new int[100000];
                                        String Return2 = "";     
                                        int notreturnCounter = 0; 

                                        // get output query 
                                        int i=0;
                                        while (rs2.next()) {                          //new 
                                            callnum2 = rs2.getString("callnum");
                                            Copy2[i] = rs2.getInt("borrow.copynum");
                                            Return2 = rs2.getString("returnon");   
                                            notreturnCounter++;

                                            // System.out.print("Borrowed Copy is  : |" + callnum2 + "|" + Copy2[i] + "|" + Return2 + "|\n") ;        //debugging
                                            i++;
                                        }


                                        i = 0 ;
                                        for ( i=0 ; i<Copy2.length; i++) {
                                            if (Copy2[i] != 0) {
                                                // System.out.println(Copy2[i]);   // debuggung
                                            }
                                        }

                                        boolean contains = IntStream.of(Copy2).anyMatch(x -> x == copyNum);
                                        if (contains == true || copyNum > MaxCopy) {  // the book cannot be borrowed
                                            System.out.println("Book borrowing performed unsuccessfully. This book is borrowed by others and not yet returned or no such copy number of this book");
                                        } else { // the book can be borrowed 

                                            try {
                                                con = DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
                                                System.out.println("Book borrowing performed successfully");

                                                // updated table: insert record in BORROW with new libuid, callnum, copynum, checkout, return(NULL) 
                                                String sql4 = "INSERT INTO borrow (libuid, callnum, copynum, checkout, returnon) " 
                                                                + "VALUES ('%s', '%s', %d, CURDATE(), NULL) ";
                                                
                                                sql4 = String.format(sql4, UserID, callNum ,copyNum);      // need to fix
                                                stmt = con.createStatement();       // new
                                                stmt.executeUpdate(sql4);      // new
                                            
                                                // // debugging
                                                // String sql5 = "SELECT * FROM borrow";
                                                
                                                // sql5 = String.format(sql5);      // need to fix
                                                //    // new

                                                // stmt = con.createStatement();       // new
                                                // ResultSet rs5 = stmt.executeQuery(sql5); 

                                                // while (rs5.next()) {                          //new 
                                                //     String userid5 = rs5.getString("libuid");
                                                //     String Callnum5 = rs5.getString("callnum");
                                                //     String copynum5 = rs5.getString("copynum");
                                                //     String checkout5 = rs5.getString("checkout");
                                                //     String return5 = rs5.getString("returnon");
        
                                                //     System.out.print("Borrowed: |" + userid5 + "|" + Callnum5 + "|" + copynum5 +  "|" + checkout5 + "|" + "|" + return5 + "|\n") ;   //debugging
                                        
                                                // }

                                            } catch (SQLException ex) {
                                                // handle any errors
                                                System.out.println("SQLException: " + ex.getMessage());
                                                System.out.println("SQLState: " + ex.getSQLState());
                                                System.out.println("VendorError: " + ex.getErrorCode());
                                            } 

                                        } // end of if-statement updating record 
                                

                                    } catch (SQLException ex) {
                                        // handle any errors
                                        System.out.println("SQLException: " + ex.getMessage());
                                        System.out.println("SQLState: " + ex.getSQLState());
                                        System.out.println("VendorError: " + ex.getErrorCode());
                                    } // end of try in if callnum is existed
                    } // end of if-statement if callnum is existed
            


                    
                } catch (SQLException ex) {
                        // handle any errors
                        System.out.println("SQLException: " + ex.getMessage());
                        System.out.println("SQLState: " + ex.getSQLState());
                        System.out.println("VendorError: " + ex.getErrorCode());
                    } // end of try in if userid is existed
                } // end of if-statement if userid is existed




            } catch (SQLException ex) {
                // handle any errors
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            } // end of the largest end 




    } // end of function 













    public static void bookReturning(){
        Scanner keyboard=new Scanner(System.in);
        String uid,callNum;
        int copyNum=0,tborrowed=0;
        float userRating=0,rating=0;
        System.out.print("Enter The User ID: ");
        uid = keyboard.nextLine();
        System.out.print("Enter The Call Number: ");
        callNum = keyboard.nextLine();
        System.out.print("Enter The Copy Number: ");
        copyNum = keyboard.nextInt();
        System.out.print("Enter Your Rating of the Book: ");
        userRating = keyboard.nextFloat();
       
        try {
            conn =
               DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
            Statement stmt = conn.createStatement();
            //Check whether the record exist
            String verify="SELECT libuid, callnum, copynum, returnon "+
                            "FROM borrow "+
                            "WHERE libuid='"+uid+"' "+
                            "AND callnum='"+callNum+"' "+
                            "AND copynum="+copyNum+" "+
                            "AND returnon IS NULL";
            ResultSet resultSet=stmt.executeQuery(verify);
            if(!resultSet.isBeforeFirst()){
                System.out.println("No such record, returning to previous menu...");
            }else{
                //get old rating and tborrowed
                String getBookRating="SELECT rating,tborrowed "+ 
                                        "FROM book "+
                                        "WHERE callnum='"+callNum+"'";
                resultSet=stmt.executeQuery(getBookRating);

                if(!resultSet.isBeforeFirst()){
                    System.out.println("Wrong callnum");
                }else{
                    if(resultSet.next()){
                        rating=resultSet.getFloat("rating");
                        tborrowed=resultSet.getInt("tborrowed");

                        //new book rating caculation
                        rating=(rating*tborrowed+userRating)/(tborrowed+1);
                        tborrowed+=1;
                        //Update table borrow
                        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
                        Date date = new Date(System.currentTimeMillis());
                        String serverDate=formatter.format(date);
                        //System.out.println(serverDate);
                        //Update table borrow
                        PreparedStatement pstmt=
                        conn.prepareStatement("UPDATE borrow "+
                                                "SET returnon='"+serverDate+"' "+
                                                "WHERE libuid = '"+uid+"' "+
                                                "AND callnum = '"+callNum+"' "+
                                                "AND copynum = "+copyNum+" "+
                                                "AND returnon IS NULL");
                        pstmt.executeUpdate();

                        //Update table book
                        pstmt=conn.prepareStatement("UPDATE book "+
                                                    "SET tborrowed="+tborrowed+", "+
                                                    "rating="+rating+" "+
                                                    "WHERE callnum = '"+callNum+"'");

                        pstmt.executeUpdate();


                        System.out.println("Book returning performed successfully.");
                    }
                }

            }
                       
        } catch (SQLException ex) {
            String state=ex.getSQLState();
            int errcode=ex.getErrorCode();

            if(state.equals("08S01")){
                System.out.println("[Error]: Failed to connect library database");
            }else if(state.equals("42000")&&errcode==1064){
                System.out.println("[Error]: An matching borrow record is not found. The book has not been returned yet");
            }else{
                // handle any errors
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        }
    }
    public static void unreturnBook() {
        Scanner keyboard=new Scanner(System.in);
        java.sql.Date startDate,endDate;
        String startDateString,endDateString;
        SimpleDateFormat formatter1=new SimpleDateFormat("dd/MM/yyyy");  
        System.out.print("Type in the starting date[dd/mm/yyyy]: ");
        startDateString = keyboard.nextLine();
        System.out.print("Type in the ending date[dd/mm/yyyy]: ");
        endDateString = keyboard.nextLine();

        try {
            startDate = new java.sql.Date(formatter1.parse(startDateString).getTime());
            endDate = new java.sql.Date(formatter1.parse(endDateString).getTime());

            try {
                conn = DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
                Statement stmt = conn.createStatement();
                String query="SELECT libuid, callnum, copynum, checkout FROM borrow WHERE returnon IS NULL AND checkout BETWEEN '" + startDate + "' AND '" + endDate + "'";
                ResultSet rs = stmt.executeQuery(query);
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsNumber = rsmd.getColumnCount();
                System.out.print("|");
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print(rsmd.getColumnName(i)+"|");
                }
                System.out.println("");
                while (rs.next()) {
                    System.out.print("|");
                    for (int i = 1; i <= columnsNumber; i++) {
                        String columnValue = rs.getString(i);
                        System.out.print(columnValue+"|");
                    }
                    System.out.println("");
                }
                System.out.println("End of Query");
                
               
            } catch (SQLException ex) {
                // handle any errors
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        } catch (ParseException e) {
            System.out.println("[Error]: " + e.getMessage());
        }

    }

}
