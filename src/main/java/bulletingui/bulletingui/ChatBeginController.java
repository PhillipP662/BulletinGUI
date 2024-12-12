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
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import javax.swing.text.WrappedPlainView;
import java.io.IOException;

public class ChatBeginController {
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
    private Label aliceReceiveMessage;

    @FXML
    private Label bobReceiveMessage;

    @FXML
    private Button aliceSendButton;
    @FXML
    private Button bobSendButton;
    @FXML
    private MenuItem menuHomeButton;
    @FXML
    private MenuItem menuRestartButton;
    @FXML
    private MenuItem menuClearButton;

    @FXML
    private TextField aliceInputField;
    @FXML
    private TextField bobInputField;

    @FXML
    private TextArea aliceChatArea;
    @FXML
    private TextArea bobChatArea;


    @FXML
    private Line separatorLine;

    // Controller attributes
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    public void initialize() throws Exception {
        // Code logic
        // Create the shared key
        AppContext.setSharedKey(CryptoUtils.generateKey());
        if(AppContext.getSharedKey() != null) System.out.println("SharedKey initialized.");

        // Initialize Alice and Bob (or other clients)
        String user1Name = aliceLabel.getText();
        String user2Name = bobLabel.getText();
        Client user1 = new Client(user1Name, AppContext.getSharedKey());
        Client user2 = new Client(user2Name, AppContext.getSharedKey());
        AppContext.setClient(user1Name, user1);
        AppContext.setClient(user2Name, user2);

        // Initialize phonebooks
        user1.InitiliaseClient(user2);
        user2.InitiliaseClient(user1);
        // Add the new contact to your phonebook
        user1.updatePhonebook(user2, user2.getPhonebook());
        user2.updatePhonebook(user1, user1.getPhonebook());

        // GUI logic
        // Handle pressing enter
        aliceInputField.setOnAction(event -> {
            try {
                sendAliceMessage(event); // Call the existing method
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        bobInputField.setOnAction(event -> {
            try {
                sendAliceMessage(event); // Call the existing method
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
        if(!message.isEmpty()) user1.send(AppContext.getBulletinBoard(), user2, message);

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
        if(!message.isEmpty()) user2.send(AppContext.getBulletinBoard(), user1, message);

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
            String message = user1.recieve(AppContext.getBulletinBoard(), user2);
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
            String message = user2.recieve(AppContext.getBulletinBoard(), user1);
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
        AppContext.reset();

        // GUI logic
        Parent root = FXMLLoader.load(getClass().getResource("home-view.fxml"));
        stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/home-view.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    public void menuRestartPress(ActionEvent event) throws Exception {
        // Code logic
        AppContext.reset();
        aliceInputField.clear();
        bobInputField.clear();

        // Create a new bulletin board and shared key
        AppContext.setBulletinBoard( new BulletinBoardImpl(10));
        if(AppContext.getBulletinBoard() != null) System.out.println("BulletinBoard initialized.");
        initialize();

    }
    public void menuClearPress(ActionEvent event) throws IOException {
        // Code logic
        aliceInputField.clear();
        bobInputField.clear();

        // Make the error messages invisible
        aliceReceiveMessage.setVisible(false);
        bobReceiveMessage.setVisible(false);
    }
}