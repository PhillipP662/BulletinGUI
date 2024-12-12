package bulletingui.bulletingui.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WelcomeController {
    // Controller attributes
    private Stage stage;
    private Scene scene;

    public void switchToHome(ActionEvent event) throws Exception {
        // GUI logic
        Parent root = FXMLLoader.load(getClass().getResource("/bulletingui/bulletingui/home-view.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/home-view.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}