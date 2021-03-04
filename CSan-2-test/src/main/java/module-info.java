module by.bsuir.m0rk.csan.task.second {
    requires javafx.controls;
    requires javafx.fxml;

    opens by.bsuir.m0rk4.csan.task.second.controller to javafx.fxml;
    exports by.bsuir.m0rk4.csan.task.second;
}