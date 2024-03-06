package com.mpbp3.project3;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.sql.DriverManager;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class SQLClientCon {

    @FXML
    private ComboBox<String> DBURLProp;
    @FXML
    private ComboBox<String> UserProp;
    @FXML
    private TextField username;
    @FXML
    private TextField password;
    @FXML
    private TextArea SQLExecResult;
    @FXML
    private TextArea SQLCommand;
    @FXML
    private Button connect;
    @FXML
    private Button disconnect;
    @FXML
    private Button executeSQL;
    @FXML
    private Button clearResultButton;
    @FXML
    private Button clearSQL;
    @FXML
    private TextField connectionURL;

    private Connection connection = null;




    public void initialize()   {

        DBURLProp.getItems().addAll(dbProps());
        UserProp.getItems().addAll(userProps());

        System.out.println("Hello");

    }




    private List<String> dbProps()   {
        File file = new File("src/main/resources/com/mpbp3/project3/dbProps");
        File[] files = file.listFiles((d, name) -> name.endsWith(".properties"));
        List<String> fileNames = new ArrayList<>();

        if(files != null)   {
            for(int i = 0; i < files.length; i++)   {
                fileNames.add(files[i].getName());
            }
        }
        System.out.println(fileNames);

        return fileNames;
    }

    private List<String> userProps()   {
        File file = new File("src/main/resources/com/mpbp3/project3/userProps");
        File[] files = file.listFiles((d, name) -> name.endsWith(".properties"));
        List<String> fileNames = new ArrayList<>();

        if(files != null)   {
            for(int i = 0; i < files.length; i++)   {
                fileNames.add(files[i].getName());
            }
        }

        return fileNames;
    }



    @FXML
    private void handleConnection(ActionEvent event)   {
        try   {
            String userPropPath = "src/main/resources/com/mpbp3/project3/userProps/" + UserProp.getSelectionModel().getSelectedItem();
            String dbPropPath = "src/main/resources/com/mpbp3/project3/dbProps/" + DBURLProp.getSelectionModel().getSelectedItem();

            Properties userProps = new Properties();
            userProps.load(new FileInputStream(userPropPath));

            String dbUser = userProps.getProperty("username");
            String dbPassword = userProps.getProperty("password");

            if(!username.getText().equals(dbUser) || !password.getText().equals(dbPassword))   {
                connectionURL.setText("NOT CONNECTED - User Credentials Do Not Match Properties File!");
            }

            Properties dbProps = new Properties();
            dbProps.load(new FileInputStream(dbPropPath));

            String dbURL = dbProps.getProperty("url");
            String dbDriver = dbProps.getProperty("driver");

            Class.forName(dbDriver);

            connection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
            connectionURL.setText("CONNECTED TO " + connection.getMetaData().getURL());




        }
        catch(Exception e)   {
            e.printStackTrace();
        }
    }

    public void handleDisconnect(ActionEvent event) {
        try {
            connection.close();
            connection = null;
            connectionURL.setText("Disconnected");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleClearResult(ActionEvent event) {
        SQLExecResult.clear();
    }

    @FXML
    private void handleClearSQL(ActionEvent event) {
        SQLCommand.clear();
    }



}