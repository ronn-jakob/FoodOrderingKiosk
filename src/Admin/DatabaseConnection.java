/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    public static Connection connectDatabase(){
        Connection conn = null;
        try {    
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/kioskEDP",
            "root",
            "@Fifteen200515"
            );
            System.out.println("Succesfully Connected");
        } catch (ClassNotFoundException ex) {
            System.out.println("Missing Driver: " + ex);
            ex.printStackTrace();
//            System.getLogger(DatabaseCon.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (SQLException ex) {
            System.out.println("Not Connected: " + ex);
            ex.printStackTrace();
//            System.getLogger(DatabaseCon.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return conn;
    }
    
}
