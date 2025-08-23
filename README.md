# BulletinGUI

## Overview
BulletinGUI is a graphical interface for interacting with a secure and privacy-friendly bulletin board system. This project demonstrates the implementation of key concepts from the "Privately (and Unlinkably) Exchanging Messages Using a Public Bulletin Board" protocol.

## How to Run
0. **Entry Point**  
   References to files are relative to the following entry point:
   `src/main/java/bulletingui/bulletingui`


1. **Run the Launcher**  
   Simply execute the `Launcher` class to start both the server and the GUI in one step. This will:
   - Start the server.
   - Launch the `WelcomeApplication` GUI.


2. **Optional: Run Individually**  
   If you prefer to run the components separately:
   - Start the server by running `Server` (in the `/Server` package).
   - Launch the GUI by running `WelcomeApplication`.


3. **Explore Without Server**  
   You can also launch the GUI (`WelcomeApplication`) without starting the server to observe how it behaves without an active backend.
 
## Features
- One-step execution with `Launcher`.
- Privacy-friendly message exchange simulation.
- Server-client architecture using Java RMI.
- A simple and intuitive GUI for testing and interaction.

## Requirements
- Java 8 or higher
- Ensure all dependencies are included in the classpath

## Notes
- Using the `Launcher` class is the recommended method for running the application.
- If running components individually, ensure the server is started before launching the GUI for full functionality.

Enjoy exploring BulletinGUI!
