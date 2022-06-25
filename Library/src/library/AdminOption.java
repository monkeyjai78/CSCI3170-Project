package library;

import java.util.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.io.*;
import java.sql.Date;

public class AdminOption extends MainMenu{
    public static String[] TableList={
        "user_category", 
        "libuser",
        "book_category",
        "book",
        "copy",
        "borrow",
        "authorship"
    };
    //Show Admin Menu
    public static void ShowAdminMenu(){
        System.out.println("\n-----Operations for administrator menu-----");
        System.out.println("What kind of operation would you like to perform?");
        System.out.println("1. Create all tables");
        System.out.println("2. Delete all tables");
        System.out.println("3. Load from datafile");
        System.out.println("4. Show number of records in each table");
        System.out.println("5. Return to the main menu");
        System.out.print("Enter Your Choice: ");
    }

    //Get User Input for Admin Operation
    public static void AdminChoice() throws IOException, SQLException, ParseException{
        Scanner keyboard=new Scanner(System.in);
        int choice;
        do{
            ShowAdminMenu();
            choice = keyboard.nextInt();
            switch(choice){
                case 1:CreateTable();break;
                case 2:DeleteTables();break;
                case 3:LoadData();break;
                case 4:ShowNumRec();break;
                case 5:break;
            }
        
        
        
        }while(choice!=5);
        
    }
    public static void CreateTable(){
        System.out.print("Processing...");
        String[] tableCreateCMD=new String[7];

        try {
            conn =
               DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
            Statement stmt = conn.createStatement();
            CreateCMD(tableCreateCMD);

            for(int i=0;i<7;i++){
                stmt.executeUpdate(tableCreateCMD[i]);
            }
            System.out.println("Done. Database is initialized.");
           
        } catch (SQLException ex) {
            // handle any errors
            String state=ex.getSQLState();
            int errcode=ex.getErrorCode();
            if(state.equals("08S01")){
                System.out.println("\n[Error]: Failed to connect library database.");
            }else if(state.equals("42S01")&&errcode==1050){
                System.out.println("\n[Error]: Table already exists.");
            }else{
                System.out.println("\nSQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        }

    }
    
    public static void DeleteTables(){
        System.out.print("Processing...");
        try {
            conn =
               DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
            Statement stmt = conn.createStatement();
            int []order={6,5,4,1,3,0,2};
            for(int i=0;i<7;i++){
                String temp="DROP TABLE IF EXISTS "+TableList[order[i]]+";";
                stmt.executeUpdate(temp);
            }


            System.out.println("Done. Database is removed.");
           
        } catch (SQLException ex) {
            // handle any errors
            String state=ex.getSQLState();
            if(state.equals("08S01")){
                System.out.println("\n[Error]: Failed to connect library database.");
            }else{
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        }

    }
    
    public static void LoadData() throws IOException,SQLException, ParseException{
        try{
            System.out.print("\nType in the Source Data Folder Path: ");
            Scanner keyboard=new Scanner(System.in);
            String folderPath=keyboard.nextLine();
            System.out.print("Processing...");
            conn =
               DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
            int error=0;
            error+=LoadBookCategory(folderPath);        
            error+=LoadUserCategory(folderPath);
            error+=LoadUser(folderPath);
            error+=LoadBook(folderPath);
            error+=LoadCheckOut(folderPath);

            if(error==0)
                System.out.println("Done. Data is inputted to the database");
            else System.out.println("\n[Error]: Failed to retrieve data from some of the file.");


        }catch(IOException io){
            System.out.println("[Error]: Invalid Character Received");

        }catch(SQLException ex){
            String state=ex.getSQLState();
            if(state.equals("08S01")){
                System.out.println("\n[Error]: Failed to connect library database.");
            }else{
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        }
    }
    
    public static int LoadBookCategory(String path) throws IOException,SQLException{
        try {
            //conn =
              // DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
            File book_category=new File(path+"/book_category.txt");
            Scanner markFile=new Scanner(book_category);
            
            PreparedStatement pstmt=
                conn.prepareStatement("INSERT IGNORE INTO book_category values (?,?)");
            
            
            while(markFile.hasNextLine()){
                String aLine=markFile.nextLine();
                String[] info = aLine.split("\t",2);
                //int bcid=markFile.nextInt();
            
                //String bcname=markFile.nextLine();

                pstmt.setInt(1,Integer.parseInt(info[0]));
                pstmt.setString(2,info[1]);
                pstmt.executeUpdate();
            }
            return 0;
           
        } catch (SQLException ex) {
            // handle any errors
            String state=ex.getSQLState();
            if(state.equals("08S01")){
                System.out.println("\n[Error]: Failed to connect library database.");
            }else{
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
            return 1;
        }catch (IOException io){
            //System.out.println("[Error]: Failed to retrieve data from book_category.txt");
            return 1;
        }

    }
    
    public static int LoadBook(String path) throws IOException,SQLException, ParseException{
        try {
            //conn =
               //DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
            File book=new File(path+"/book.txt");
            Scanner markFile=new Scanner(book);
                        
            
            while(markFile.hasNextLine()){
                String aLine=markFile.nextLine();
                String[] info = aLine.split("\t",8);
                
                
                PreparedStatement pstmt=
                    conn.prepareStatement("INSERT IGNORE INTO book values (?,?,?,?,?,?)");

                String []datePart=info[4].split("/",3);
                
                pstmt.setString(1,info[0]);
                pstmt.setString(2,info[2]);
                pstmt.setDate(3,Date.valueOf(datePart[2]+"-"+datePart[1]+"-"+datePart[0]));
                if(info[5].equals("null")){
                    pstmt.setNull(4, java.sql.Types.FLOAT);
                }else pstmt.setFloat(4, Float.parseFloat(info[5]));
                pstmt.setInt(5,Integer.parseInt(info[6]));
                pstmt.setInt(6,Integer.parseInt(info[7]));
                pstmt.executeUpdate();

                pstmt=
                    conn.prepareStatement("INSERT IGNORE INTO authorship values (?,?)");
                String[]author= info[3].split(",",0);
                for(int j=0;j<author.length;j++){
                    pstmt.setString(2,info[0]);
                    pstmt.setString(1,author[j]);
                    pstmt.executeUpdate();
                }

                pstmt=
                    conn.prepareStatement("INSERT IGNORE INTO copy values (?,?)");
                for(int j=1;j<=Integer.parseInt(info[1]);j++){
                    pstmt.setString(1, info[0]);
                    pstmt.setInt(2,j);
                    pstmt.executeUpdate();
                }

            }
            return 0;
        } catch (SQLException ex) {
            // handle any errors
            String state=ex.getSQLState();
            if(state.equals("08S01")){
                System.out.println("\n[Error]: Failed to connect library database.");
            }else{
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
            return 1;
        }catch (IOException io){
            //System.out.println("[Error]: Failed to retrieve data from book.txt");
            return 1;
        }
        
    }
    
    public static int LoadUser(String path) throws IOException,SQLException{
        try {
            //conn =
               //DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
            File user=new File(path+"/user.txt");
            Scanner markFile=new Scanner(user);
            
            PreparedStatement pstmt=
                conn.prepareStatement("INSERT IGNORE INTO libuser values (?,?,?,?,?)");            
            
            while(markFile.hasNextLine()){
                String aLine=markFile.nextLine();
                String[] info = aLine.split("\t",5);
  
                pstmt.setString(1,info[0]);
                pstmt.setString(2,info[1]);
                pstmt.setInt(3,Integer.parseInt(info[2]));
                pstmt.setString(4,info[3]);
                pstmt.setInt(5,Integer.parseInt(info[4]));
                pstmt.executeUpdate();
            }
            return 0;
           
        } catch (SQLException ex) {
            // handle any errors
            String state=ex.getSQLState();
            if(state.equals("08S01")){
                System.out.println("\n[Error]: Failed to connect library database.");
            }else{
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
            return 1;
        }catch (IOException io){
            //System.out.println("[Error]: Failed to retrieve data from user.txt");
            return 1;
        }

    }

    public static int LoadCheckOut(String path) throws IOException,SQLException{
        try {
            //conn =
               //DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
            File check_out=new File(path+"/check_out.txt");
            Scanner markFile=new Scanner(check_out);
            
            PreparedStatement pstmt=
                conn.prepareStatement("INSERT IGNORE INTO borrow values (?,?,?,?,?)");
            
            
            while(markFile.hasNextLine()){
                String aLine=markFile.nextLine();
                String[] info = aLine.split("\t",5);
                String []datePartA=info[3].split("/",3);
                String []datePartB=info[4].split("/",3);

                pstmt.setString(1,info[2]);
                pstmt.setString(2,info[0]);
                pstmt.setInt(3,Integer.valueOf(info[1]));
                //yyyy-mm-dd
                pstmt.setDate(4,Date.valueOf(datePartA[2]+"-"+datePartA[1]+"-"+datePartA[0]));

                if(info[4].equals("null")){
                    pstmt.setNull(5, java.sql.Types.DATE);
                }else pstmt.setDate(5,Date.valueOf(datePartB[2]+"-"+datePartB[1]+"-"+datePartB[0]));

                pstmt.executeUpdate();
            }
            return 0;
           
        } catch (SQLException ex) {
            // handle any errors
            String state=ex.getSQLState();
            if(state.equals("08S01")){
                System.out.println("\n[Error]: Failed to connect library database.");
            }else{
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
            return 1;
        }catch (IOException io){
            //System.out.println("[Error]: Failed to retrieve data from check_out.txt");
            return 1;
        }

    }

    public static int LoadUserCategory(String path) throws IOException,SQLException{
        try {
            //conn =
              // DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
            File user_category=new File(path+"/user_category.txt");
            Scanner markFile=new Scanner(user_category);
            
            PreparedStatement pstmt=
                conn.prepareStatement("INSERT IGNORE INTO user_category values (?,?,?)");
            
            while(markFile.hasNextLine()){
                String aLine=markFile.nextLine();
                String[] info = aLine.split("\t",3);

                pstmt.setInt(1,Integer.parseInt(info[0]));
                pstmt.setInt(2,Integer.parseInt(info[1]));
                pstmt.setInt(3,Integer.parseInt(info[2]));
                pstmt.executeUpdate();
            }
            return 0;
           
        } catch (SQLException ex) {
            // handle any errors
            String state=ex.getSQLState();
            if(state.equals("08S01")){
                System.out.println("\n[Error]: Failed to connect library database.");
            }else{
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
            return 1;
        }catch (IOException io){
            //System.out.println("[Error]: Failed to retrieve data from user_category.txt");
            return 1;
        }

    }

    public static void ShowNumRec(){
        try {
            conn =
               DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
            Statement stmt = conn.createStatement();

            System.out.println("\nNumber of records in each table:");
            for(int i=0;i<7;i++){
                String query="SELECT COUNT(*) FROM "+TableList[i];
                ResultSet rs = stmt.executeQuery(query);
                rs.next();
                System.out.println(TableList[i]+": "+rs.getInt(1));
            }
           
        } catch (SQLException ex) {
            // handle any errors
            String state=ex.getSQLState();
            int errcode=ex.getErrorCode();
            if(state.equals("08S01")){
                System.out.println("[Error]: Failed to connect library database.");
            }else if(state.equals("42S02")&& errcode==1146){
                System.out.println("[Error]: No table exists in the database.");
            }
            else{
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        }
        
     
    }

    public static void CreateCMD(String x[]){
        x[0]="CREATE TABLE user_category"+
                "( ucid INTEGER NOT NULL,"+
                "max INTEGER NOT NULL,"+
                "period INTEGER NOT NULL,"+
                "PRIMARY KEY(ucid),"+
                "CHECK ( ID >= 1 AND ID <= 9 "+
                    "AND max >= 1 AND max <= 99 "+
                    "AND period >= 1 AND period <= 99 "+
                    "AND ucid >=1 AND ucid <=9))";

        x[4]="CREATE TABLE libuser"+
                "( libuid VARCHAR(10) NOT NULL,"+
                "name VARCHAR(25) NOT NULL,"+
                "age INTEGER NOT NULL,"+
                "address VARCHAR(100) NOT NULL,"+
                "ucid INTEGER NOT NULL,"+
                "PRIMARY KEY(libuid),"+
                "FOREIGN KEY(ucid) REFERENCES user_category(ucid),"+
                "CHECK( age>=1 AND age <= 999 ))";                    ;
        
        x[1]="CREATE TABLE book_category"+
                "( bcid INTEGER NOT NULL,"+
                "bcname VARCHAR(30) NOT NULL,"+
                "PRIMARY KEY(bcid),"+
                "CHECK( bcid >= 1 AND bcid <=9 ))";
        
        x[2]="CREATE TABLE book"+
                "( callnum VARCHAR(8) NOT NULL,"+
                "title VARCHAR(30) NOT NULL,"+
                "publish DATE,"+
                "rating FLOAT,"+
                "tborrowed INTEGER NOT NULL,"+
                "bcid INTEGER NOT NULL,"+
                "PRIMARY KEY(callnum),"+
                "FOREIGN KEY(bcid) REFERENCES book_category(bcid),"+
                "CHECK( rating >= 0 "+
                    "AND tborrowed >= 0 AND tborrowed<=99 ))";
        
        //double check this
        x[5]="CREATE TABLE copy"+
                "( callnum VARCHAR(8) NOT NULL,"+
                "copynum INTEGER NOT NULL,"+
                "PRIMARY KEY(callnum, copynum),"+
                "FOREIGN KEY(callnum) REFERENCES book(callnum))";

        //this too
        x[6]="CREATE TABLE borrow"+
                "( libuid VARCHAR(10) NOT NULL,"+
                "callnum VARCHAR(8) NOT NULL,"+
                "copynum INTEGER NOT NULL,"+
                "checkout DATE NOT NULL,"+
                "returnon DATE,"+
                "PRIMARY KEY(libuid, callnum, copynum, checkout),"+
                "FOREIGN KEY(callnum, copynum) REFERENCES copy(callnum, copynum),"+
                "FOREIGN KEY(libuid) REFERENCES libuser(libuid))";

        x[3]="CREATE TABLE authorship"+
                "( aname VARCHAR(25) NOT NULL,"+
                "callnum VARCHAR(8) NOT NULL,"+
                "PRIMARY KEY(aname, callnum),"+
                "FOREIGN KEY(callnum) REFERENCES book(callnum))";
                
    }
}
