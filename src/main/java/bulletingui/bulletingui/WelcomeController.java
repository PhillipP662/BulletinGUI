package bulletingui.bulletingui;

import bulletingui.bulletingui.Commen.BulletinBoardTest;
import bulletingui.bulletingui.Commen.CryptoUtils;
import bulletingui.bulletingui.Server.BulletinBoardImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.io.IOException;

public class WelcomeController {
    // Window attributes
    @FXML
    private AnchorPane welcomeRootPane;
    @FXML
    private Label welcomeText;
    @FXML
    private Button switchToHomeButton;

    // Controller attributes
    private Stage stage;
    private Scene scene;
    private Parent root;

    public void switchToHome(ActionEvent event) throws Exception {
        // GUI logic
        Parent root = FXMLLoader.load(getClass().getResource("home-view.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/home-view.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}