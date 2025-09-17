package com.thales.server;

import com.thales.server.controller.AppController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/server.fxml"));

        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("VoteFlix");
        stage.show();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                AppController controller = loader.getController();
                controller.getServerListener().close();
                Platform.exit();
                System.exit(0);
            }
        });  
    }

    public static void main(String[] args) {
        launch(args);
    }
}