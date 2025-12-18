module hr.bmestric.sevenscardgame {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.slf4j;


    opens hr.bmestric.sevenscardgame to javafx.fxml;
    exports hr.bmestric.sevenscardgame;
}