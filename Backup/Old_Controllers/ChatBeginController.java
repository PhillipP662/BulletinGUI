package bulletingui.bulletingui.Controllers;

import bulletingui.bulletingui.AppContext;
import bulletingui.bulletingui.Client.Client;
import bulletingui.bulletingui.Commen.CryptoUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.io.IOException;

public class ChatBeginController {
    // Window attributes
    @FXML
    private Label aliceLabel;

    @FXML
    private Label bobLabel;
    @FXML
    private Label aliceReceiveMessage;

    @FXML
    private Label bobReceiveMessage;

    @FXML
    private TextField aliceInputField;
    @FXML
    private TextField bobInputField;

    @FXML
    private TextArea aliceChatArea;
    @FXML
    private TextArea bobChatArea;

    // Controller attributes
    private Stage stage;
    private Scene scene;

    @FXML
    public void initialize() throws Exception {
        // GUI logic --------------------------------------------
        // Handle pressing <enter> in input field
        aliceInputField.setOnAction(event -> {
            try {
                sendAliceMessage(event); // Call the existing method
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        bobInputField.setOnAction(event -> {
            try {
                sendBobMessage(event); // Call the existing method
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Send ----------------------------------------------------------------

    public void sendAliceMessage(ActionEvent actionEvent) throws Exception {
        // Get the user information
        String user1Name = aliceLabel.getText();
        String user2Name = bobLabel.getText();
        Client user1 = AppContext.getClient(user1Name);
        Client user2 = AppContext.getClient(user2Name);

        // Send message
        String message = aliceInputField.getText();
        aliceInputField.clear();
        if(!message.isEmpty()) user1.send(user2, message);

        // Show locally copy of message
        if(!message.isEmpty()) {
            aliceChatArea.appendText(user1Name + ": \t" + message + "\n");
        }

        // Remove the error message if needed
        aliceReceiveMessage.setVisible(false);
    }

    public void sendBobMessage(ActionEvent actionEvent) throws Exception {
        // Get the user information
        String user1Name = aliceLabel.getText();
        String user2Name = bobLabel.getText();
        Client user1 = AppContext.getClient(user1Name);
        Client user2 = AppContext.getClient(user2Name);

        // Send message
        String message = bobInputField.getText();
        bobInputField.clear();
        if(!message.isEmpty()) user2.send(user1, message);

        // Show locally copy of message
        if(!message.isEmpty()) {
            bobChatArea.appendText(user2Name + ": \t" + message + "\n");
        }

        // Remove the error message if needed
        bobReceiveMessage.setVisible(false);
    }

    // Receive ----------------------------------------------------------------
    public void receiveAliceMessage(ActionEvent actionEvent) throws Exception {
        try{
            // Get the user information
            String user1Name = aliceLabel.getText();
            String user2Name = bobLabel.getText();
            Client user1 = AppContext.getClient(user1Name);
            Client user2 = AppContext.getClient(user2Name);

            // Receive message
            String message = user1.recieve(user2);
            if(!message.isEmpty()) {
                aliceChatArea.appendText(user2Name + ": \t" + message + "\n");
            } else {
                aliceReceiveMessage.setText("No message found...");
                aliceReceiveMessage.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                aliceReceiveMessage.setVisible(true);
            }
            aliceReceiveMessage.setText("");
        } catch (IllegalArgumentException e) {
            aliceReceiveMessage.setText("No message found...");
            aliceReceiveMessage.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            aliceReceiveMessage.setVisible(true);
        }


    }
    public void receiveBobMessage(ActionEvent actionEvent) throws Exception {
        try{
            // Get the user information
            String user1Name = aliceLabel.getText();
            String user2Name = bobLabel.getText();
            Client user1 = AppContext.getClient(user1Name);
            Client user2 = AppContext.getClient(user2Name);

            // Receive message
            String message = user2.recieve(user1);
            if(!message.isEmpty()) {
                bobChatArea.appendText(user1Name + ": \t" + message + "\n");
            } else {
                bobReceiveMessage.setText("No message found...");
                bobReceiveMessage.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                bobReceiveMessage.setVisible(true);
            }
            bobReceiveMessage.setText("");
        } catch (IllegalArgumentException e) {
            bobReceiveMessage.setText("No message found...");
            bobReceiveMessage.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            bobReceiveMessage.setVisible(true);
        }
    }

    // Menu ---------------------------------------------------------
    public void menuHomePress(ActionEvent event) throws IOException {
        // Code logic
        System.out.println("Going back to home");
        AppContext.reset();

        // GUI logic
        Parent root = FXMLLoader.load(getClass().getResource("/bulletingui/bulletingui/home-view.fxml"));
        stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/home-view.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    public void menuRestartPress(ActionEvent event) throws Exception {
        // Code logic
        System.out.println("Restarting...");
        AppContext.reset();
        aliceInputField.clear();
        bobInputField.clear();
        aliceChatArea.clear();
        bobChatArea.clear();

        // Create a shared key
        restartChat();
    }
    public void menuClearPress(ActionEvent event) throws IOException {
        // Code logic
        System.out.println("Fields cleared");
        aliceInputField.clear();
        bobInputField.clear();
        aliceChatArea.clear();
        bobChatArea.clear();

        // Make the error messages invisible
        aliceReceiveMessage.setVisible(false);
        bobReceiveMessage.setVisible(false);
    }

    public void restartChat() {
        // Code logic
        try{
            // Create the shared key
            SecretKey sharedKey = CryptoUtils.generateKey();
            AppContext.setSharedKey(sharedKey);
            if(AppContext.getSharedKey() != null) System.out.println("SharedKey initialized.");

            // Initialize Alice and Bob (or other clients)
            String user1Name = aliceLabel.getText();
            String user2Name = bobLabel.getText();
            Client user1 = new Client(user1Name, sharedKey);
            Client user2 = new Client(user2Name, sharedKey);
            AppContext.setClient(user1Name, user1);
            AppContext.setClient(user2Name, user2);

            // Initialize phonebooks
            user1.InitiliaseClient(user2);
            user2.InitiliaseClient(user1);
            // Add the new contact to your phonebook
            user1.updatePhonebook(user2, user2.getPhonebook());
            user2.updatePhonebook(user1, user1.getPhonebook());

            // Create private keys
            user1.setOtherClientPublicKey(user2.getPublicKey());
            user2.setOtherClientPublicKey(user1.getPublicKey());
        } catch (Exception e) {
            System.out.println("Server offline");
        }
    }
}