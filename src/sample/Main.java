package sample;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("imageProgramMain.fxml")));
        primaryStage.setTitle("Image Compressor");
        primaryStage.setScene(new Scene(root, 1600, 800));
//        ObservableList<Node> paneChildren = ((AnchorPane) root.getChildrenUnmodifiable().get(3)).getChildren();
//        System.out.println(paneChildren.get(0));
//        root.getChildrenUnmodifiable().get(3).setDisable(true);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
