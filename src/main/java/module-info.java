module bulletingui.bulletingui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    requires java.desktop;

    opens bulletingui.bulletingui.Commen to java.rmi;
    exports bulletingui.bulletingui.Commen;

    opens bulletingui.bulletingui to javafx.fxml;
    exports bulletingui.bulletingui;
}