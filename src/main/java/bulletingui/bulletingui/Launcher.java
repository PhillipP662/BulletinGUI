package bulletingui.bulletingui;

import bulletingui.bulletingui.Server.Server;
import javafx.application.Application;


public class Launcher {
    public static void main(String[] args) {
        try {
            // Start the server
            System.out.println("Starting the Server...");
            Server.main(new String[0]); // Call Server's main method

            // Launch the GUI
            System.out.println("Launching WelcomeApplication...");
            //WelcomeApplication.main(new String[0]); // Call WelcomeApplication's main method
            Application.launch(WelcomeApplication.class, args);
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
