//package bulletingui.bulletingui.Controllers;
//
//import bulletingui.bulletingui.AppContext;
//import bulletingui.bulletingui.Client.Client;
//import bulletingui.bulletingui.Commen.CryptoUtils;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.Label;
//import javafx.stage.Stage;
//
//import javax.crypto.SecretKey;
//import java.rmi.ConnectException;
//
//public class HomeController {
//    // Window attributes
//    @FXML
//    private Label aliceHomeLabel;
//
//    @FXML
//    private Label bobHomeLabel;
//
//    @FXML
//    private Label serverOfflineLabel;
//
//    // Controller attributes
//    private Stage stage;
//    private Scene scene;
//
//    // Methods
//    @FXML
//    public void initialize() {
//        // Runs when home-view opens
//        serverOfflineLabel.setVisible(false);
//    }
//
//    public void initiateButton(ActionEvent event) throws Exception {
//        // Code logic
//        try{
//            // Create the shared key
//            SecretKey sharedKey = CryptoUtils.generateKey();
//            AppContext.setSharedKey(sharedKey);
//            if(AppContext.getSharedKey() != null) System.out.println("SharedKey initialized.");
//
//            // Initialize Alice and Bob (or other clients)
//            String user1Name = aliceHomeLabel.getText();
//            String user2Name = bobHomeLabel.getText();
//            Client user1 = new Client(user1Name, sharedKey);
//            Client user2 = new Client(user2Name, sharedKey);
//            AppContext.setClient(user1Name, user1);
//            AppContext.setClient(user2Name, user2);
//
//            // Initialize phonebooks
//            user1.InitiliaseClient(user2);
//            user2.InitiliaseClient(user1);
//            // Add the new contact to your phonebook
//            user1.updatePhonebook(user2, user2.getPhonebook());
//            user2.updatePhonebook(user1, user1.getPhonebook());
//
//            // Create private keys
//            user1.setOtherClientPublicKey(user2.getPublicKey());
//            user2.setOtherClientPublicKey(user1.getPublicKey());
//
//            // GUI logic
//            Parent root = FXMLLoader.load(getClass().getResource("/bulletingui/bulletingui/chat-begin-view.fxml"));
//            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
//            scene = new Scene(root);
//            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
//            scene.getStylesheets().add(getClass().getResource("/css/chat-begin-view.css").toExternalForm());
//            stage.setScene(scene);
//            stage.show();
//        } catch (ConnectException e) {
//            serverOfflineLabel.setVisible(true);
//        }
//    }
//}
