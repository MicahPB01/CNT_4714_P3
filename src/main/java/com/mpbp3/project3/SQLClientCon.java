/*
Name: Micah Puccio-Ball
Course: CNT 4714 Spring 2024
Assignment title: Project 3 – A Two-tier Client-Server Application
Date: March 7, 2024
Class: SQLClientCon
*/

package com.mpbp3.project3;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
    private TextArea SQLCommand;
    @FXML
    private TextField connectionURL;

    private Connection connection = null;
    @FXML
    private TableView<ObservableList<String>> SQLExecResult;

    private Connection operationsConnection;





    public void initialize() throws SQLException, IOException, ClassNotFoundException {

        DBURLProp.getItems().addAll(dbProps());
        UserProp.getItems().addAll(userProps());

        operationsConnection = operationDB();


    }




    private List<String> dbProps()   {
        File file = new File("src/main/resources/com/mpbp3/project3/dbProps");
        File[] files = file.listFiles((d, name) -> name.endsWith(".properties"));
        List<String> fileNames = new ArrayList<>();

        if(files != null)   {

            for(int i = 0; i < files.length; i++)   {
                if(files[i].getName().equals("operationslog.properties"))   {
                    continue;
                }
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
                if(files[i].getName().equals("theaccountant.properties"))   {
                    continue;
                }


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
            else {

                Properties dbProps = new Properties();
                dbProps.load(new FileInputStream(dbPropPath));

                String dbURL = dbProps.getProperty("url");
                String dbDriver = dbProps.getProperty("driver");

                Class.forName(dbDriver);

                connection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
                connectionURL.setText("CONNECTED TO " + connection.getMetaData().getURL());

            }


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
        SQLExecResult.getItems().clear();
        SQLExecResult.getColumns().clear();
    }

    @FXML
    private void handleClearSQL(ActionEvent event) {
        SQLCommand.clear();
    }

    @FXML
    public void handleExecute(ActionEvent event) {
        boolean query = true;

        String sql = SQLCommand.getText();

        try   {

            Statement statement = connection.createStatement();

            if(sql.toUpperCase().startsWith("SELECT"))   {
                ResultSet resultSet = statement.executeQuery(sql);


                displayResults(resultSet);
            }
            else   {
                int rowsAffected = statement.executeUpdate(sql);
                query = false;
                showAlert("Successful Update", rowsAffected + " rows updated.", Alert.AlertType.INFORMATION);


            }


            if(query)   {
                String updateQuery = "INSERT INTO operationscount (login_username, num_queries, num_updates) VALUES (?, 1, 0) ON DUPLICATE KEY UPDATE num_queries = num_queries + 1";
                try (PreparedStatement pstmt = operationsConnection.prepareStatement(updateQuery)) {
                    pstmt.setString(1, username.getText());
                    pstmt.executeUpdate();
                }
                catch(SQLException e) {
                    e.printStackTrace();
                }
            }
            else   {
                String updateQuery = "INSERT INTO operationscount (login_username, num_queries, num_updates) VALUES (?, 0, 1) ON DUPLICATE KEY UPDATE num_updates = num_updates + 1";
                try (PreparedStatement pstmt = operationsConnection.prepareStatement(updateQuery)) {
                    pstmt.setString(1, username.getText());
                    pstmt.executeUpdate();
                }
                catch(SQLException e) {
                    e.printStackTrace();
                }
            }


        }
        catch (Exception e) {
            String error = e.getMessage();
            showAlert("Database error", error, Alert.AlertType.ERROR);
        }

    }








    private void displayResults(ResultSet resultSet) throws SQLException   {
        SQLExecResult.getColumns().clear();
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        for(int i = 1; i <= columnCount; i++)   {
            int j = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(metaData.getColumnName(i));
            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j - 1)));
            SQLExecResult.getColumns().add(column);
        }

        while(resultSet.next())   {
            ObservableList<String> row = FXCollections.observableArrayList();
            for(int i = 1; i <= columnCount; i++)   {

                row.add(resultSet.getString(i));

            }
            data.add(row);
        }
        SQLExecResult.setItems(data);
    }

    private void showAlert(String title, String content, Alert.AlertType type)   {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Connection operationDB() throws ClassNotFoundException, IOException, SQLException {
        Properties DBProps = new Properties();
        Properties userProps = new Properties();

        try(FileInputStream DBStream = new FileInputStream("src/main/resources/com/mpbp3/project3/dbProps/operationslog.properties"))   {
            DBProps.load(DBStream);
        }

        try(FileInputStream userStream = new FileInputStream("src/main/resources/com/mpbp3/project3/userProps/project3app.properties"))   {
            userProps.load(userStream);
        }

        String dbURL = DBProps.getProperty("url");
        String dbDriver = DBProps.getProperty("driver");
        String username = userProps.getProperty("username");
        String password = userProps.getProperty("password");


        Class.forName(dbDriver);

        return DriverManager.getConnection(dbURL, username, password);

    }


}