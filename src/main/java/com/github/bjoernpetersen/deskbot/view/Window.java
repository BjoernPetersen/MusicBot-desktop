package com.github.bjoernpetersen.deskbot.view;

import javafx.stage.Stage;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
interface Window {

  void showOnStage(Stage stage);
}
