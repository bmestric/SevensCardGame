module hr.bmestric.sevenscardgame {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.slf4j;
    requires java.rmi;
    requires java.naming;
    requires java.xml;
    requires java.logging;
    requires javafx.graphics;


    opens hr.bmestric.sevenscardgame to javafx.fxml;
    opens hr.bmestric.sevens.network.rmi to javafx.graphics,javafx.fxml;
    opens hr.bmestric.sevens.ui.controller to javafx.fxml;

    exports hr.bmestric.sevenscardgame;
    exports hr.bmestric.sevens.network.rmi.interfaces to java.rmi;
    exports hr.bmestric.sevens.network.chat.interfaces to java.rmi;
}