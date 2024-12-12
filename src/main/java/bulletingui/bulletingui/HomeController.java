package bulletingui.bulletingui;

import bulletingui.bulletingui.Client.Client;
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
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import javax.crypto.SecretKey;

public class HomeController {
    // Window attributes
    @FXML
    private AnchorPane homeRootPane;

    @FXML
    private Label homeTitle;

    @FXML
    private Label aliceLabel;

    @FXML
    private Label bobLabel;

    @FXML
    private Button initiateButton;

    @FXML
    private Line separatorLine;

    // Controller attributes
    private Stage stage;
    private Scene scene;
    private Parent root;

    // Methods
    @FXML
    public void initialize() {
        // Code logic
        AppContext.setBulletinBoard( new BulletinBoardImpl(10));
        if(AppContext.getBulletinBoard() != null) System.out.println("BulletinBoard initialized.");
    }

    public void initiateButton(ActionEvent event) throws Exception {
        // GUI logic
        Parent root = FXMLLoader.load(getClass().getResource("chat-begin-view.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/chat-begin-view.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
