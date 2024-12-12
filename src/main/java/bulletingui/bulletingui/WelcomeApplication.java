package bulletingui.bulletingui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class WelcomeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try{
            URL resourceUrl = getClass().getResource("welcome-view.fxml");
            if (resourceUrl == null) {
                System.out.println("Resource not found: welcome-view.fxml");
            }

            Parent root = FXMLLoader.load(resourceUrl);

            Scene scene = new Scene(root);
            String css1 = getClass().getResource("/css/application.css").toExternalForm();
            String css2 = getClass().getResource("/css/welcome-view.css").toExternalForm();
            scene.getStylesheets().add(css1);
            scene.getStylesheets().add(css2);

            stage.setTitle("HelloFX");
            // Prevent resizing
            stage.setResizable(false);

            setIcon(stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setIcon(Stage stage) {
        // Load the icon using getClass().getResource()
        URL iconUrl = getClass().getResource("/Images/distr_icon4.png");
        if (iconUrl == null) throw new IllegalArgumentException("Icon resource not found: /Images/distr_icon2.png");
        Image icon = new Image(iconUrl.toString());
        stage.getIcons().add(icon);
    }

    public static void main(String[] args) {
        launch();
    }
}