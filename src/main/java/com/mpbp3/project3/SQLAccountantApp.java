/*
Name: <your name goes here>
Course: CNT 4714 Spring 2024
Assignment title: Project 3 – A Specialized Accountant Application
Date: March 7, 2024
Class: SQLAccountantApp
*/
package com.mpbp3.project3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SQLAccountantApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SQLAccountantApp.class.getResource("SQLAccountantView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("SQL Accountant Application");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}