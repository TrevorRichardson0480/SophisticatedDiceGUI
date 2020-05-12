// =================================
// Author:         Trevor Richardson
// Date Complete:  5/11/2020
// Version:        1.2.4
// =================================
// Dice Program
// =================================

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Optional;
import javafx.stage.Modality;

/* The Dice class will:
 * ------------------------
 * - Extend and make use of the JavaFX Application class
 * - Use JavaFX to create an application that will provide the user with a die roller
 *
 * Methods:
 * ------------------------
 * - start: loads primary stage
 * - showLoadingWindow: displays a loading screen while the program loads a section of code. Returns a Stage to be closed later.
 * - showHistoryWindow: loads the history windows to display past values with options.
 * - showExportWindow: if the export button is clicked within the history stage, showExportWindow will prompt the user to choose a directory and file name.
 * - saveExport: showExportWindow will call the saveExport method to save the file to the selected directory.
 * - clearHistory: will clear the pastVals array and subsequently empty the history window's text area.
 * - expandOptions: extends the primary window to display additional options, and greys out old options
 * - setValueField: when custom values are used, this sets values to be used in calculations later, returns true if there was an error (a non numerical value was inputted).
 * - showAboutWindow: displays a small window with program info.
 * - newVal: generates a new random number with given constraints (range, ignore even, ignore odd).
 * - main: launches the application.
 */

public class Dice extends Application {
    // Set layout and initial states
    int textFieldWidth = 15;
    int gridPadding = 10;
    int verticalGap = 10;
    int horizontalGap = 10;
    int sliderMin = 1;
    int sliderMax = 20;
    int sliderInit = 6;
    int sliderMinorTickCount = sliderMax - 1;
    int numFaces = 6;
    int startingVal = 1;
    int maxInRange = 6;
    int numOfDice = 1;
    int diceSliderMax = 100;
    int diceSliderMinorTickCount = diceSliderMax - 1;
    boolean clearPerRoll = false;
    boolean firstIteration = true;
    boolean ignoreEven = false;
    boolean ignoreOdd = false;
    boolean valueMinError = false;
    boolean valueMaxError = false;
    boolean calculationError = false;

    // Start the application
    public void start(Stage appStage) {
        // Show a loading windows to indicate that the program is running. In most cases, the loading window will not be seen.
        Stage loadingStage = showLoadingWindow();

        // Array list will be used to store history
        ArrayList<Integer> pastVals = new ArrayList<>();

        // Initialize primary window's grid, scene, labels, and text field for showing results.
        GridPane gridPane = new GridPane();
        Scene scene = new Scene(gridPane);

        Label valueLabel = new Label("Result:");
        Label highestValLabel = new Label("Highest Result:");
        Label lowestValLabel = new Label("Lowest Result:");
        Label numFacesLabel = new Label("Number of Faces: " + numFaces);

        TextField currValField = new TextField();
        currValField.setPrefColumnCount(textFieldWidth);
        currValField.setEditable(false);

        TextField highestValField = new TextField();
        highestValField.setPrefColumnCount(textFieldWidth);
        highestValField.setEditable(false);

        TextField lowestValField = new TextField();
        lowestValField.setPrefColumnCount(textFieldWidth);
        lowestValField.setEditable(false);

        // Slider will be used to set the number of sides the die should have.
        Slider sidesSlider = new Slider(sliderMin, sliderMax, sliderInit);
        sidesSlider.setMajorTickUnit(sliderMax);
        sidesSlider.setMinorTickCount(sliderMinorTickCount);
        sidesSlider.setShowTickMarks(true);
        sidesSlider.setShowTickLabels(true);
        sidesSlider.setShowTickLabels(true);
        sidesSlider.setSnapToTicks(true);
        sidesSlider.setBlockIncrement(1);
        numFacesLabel.setText("Number of Faces: " + (int) sidesSlider.getValue());

        // Initialize buttons
        Button calcButton = new Button("Roll");
        Button resetButton = new Button("Reset");
        Button historyButton = new Button("Past Results");
        Button optionsButton = new Button("Custom Options");

        // Set grid padding and gaps
        Insets paddingField = new Insets(gridPadding, gridPadding, gridPadding, gridPadding);
        gridPane.setPadding(paddingField);
        gridPane.setHgap(verticalGap);
        gridPane.setVgap(horizontalGap);

        // Add fields, labels, buttons, and slider to the pane
        gridPane.add(valueLabel, 0, 0);
        gridPane.add(highestValLabel, 0, 1);
        gridPane.add(lowestValLabel, 0, 2);
        gridPane.add(numFacesLabel, 0, 3);
        gridPane.add(currValField, 1, 0);
        gridPane.add(highestValField, 1, 1);
        gridPane.add(lowestValField, 1, 2);
        gridPane.add(sidesSlider, 1, 3);
        gridPane.add(calcButton, 2, 0);
        gridPane.add(resetButton, 2, 1);
        gridPane.add(historyButton, 2, 2);
        gridPane.add(optionsButton, 2, 3);

        // Get new random number when "Roll" button is clicked
        calcButton.setOnAction(event -> {
            // if there are multiple die, show loading window and go straight to history window
            if (numOfDice > 1) {
                loadingStage.show();

            }

            // if clear per roll is checked, clear the past vals before calculation
            if (clearPerRoll) {
                clearHistory(pastVals);

            }

            for (int i = 0; i < numOfDice; i++) {
                calculate(currValField, lowestValField, highestValField, pastVals);

                if (calculationError){
                    calculationError = false;
                    break;

                }
            }

            if (numOfDice > 1) {
                showHistoryWindow(appStage, pastVals, paddingField);
                loadingStage.close();

            }
        });

        // Clear text fields when "Reset" button is clicked
        resetButton.setOnAction(event -> {
            currValField.setText("");
            lowestValField.setText("");
            highestValField.setText("");

            firstIteration = true;

        });

        // Open history window when "Past Results" button is clicked
        historyButton.setOnAction(event -> {
            showHistoryWindow(appStage, pastVals, paddingField);

        });

        // Open additional options when "Custom Options" button is clicked
        optionsButton.setOnAction(event -> {
            expandOptions(appStage, gridPane, paddingField, sidesSlider, optionsButton, numFacesLabel);

        });

        // Updates settings when the slider is changed
        sidesSlider.valueProperty().addListener(e -> {
            numFaces = (int) sidesSlider.getValue();
            numFacesLabel.setText("Number of Faces: " + numFaces);

        });

        // Initialize stage and make small adjustments
        appStage.setScene(scene);
        appStage.setTitle("Dice");
        appStage.getIcons().add(image);
        appStage.setResizable(false);

        // Close loading window, open application windows
        loadingStage.close();

        appStage.show();
        appStage.setWidth(appStage.getWidth() + 50);
        numFacesLabel.setPrefWidth(numFacesLabel.getWidth() + 25);

    }

    // Method for displaying loading window
    public Stage showLoadingWindow() {

        // Initialize grid pane, loading indicator, label
        GridPane gridPane = new GridPane();

        ProgressIndicator loadingIndicator = new ProgressIndicator(-1.0);
        Label loadingLabel = new Label("Loading...");

        Insets paddingField = new Insets(gridPadding, gridPadding, gridPadding, gridPadding);

        gridPane.setPadding(paddingField);
        gridPane.setHgap(verticalGap);
        gridPane.setVgap(horizontalGap);

        gridPane.add(loadingIndicator, 0, 0);
        gridPane.add(loadingLabel, 1, 0);

        // Show loading Stage
        Scene scene = new Scene(gridPane);
        Stage loadingStage = new Stage();
        loadingStage.setScene(scene);
        loadingStage.setResizable(false);
        loadingStage.show();

        return loadingStage;

    }

    // Method for showing the history or "Past Values" windows
    public void showHistoryWindow(Stage appStage, ArrayList<Integer> pastVals, Insets paddingField) {
        Stage loadingStage = showLoadingWindow();

        try {
            String data = "";
            String average = "";
            int highestVal = pastVals.get(0);
            int lowestVal = pastVals.get(0);
            int sum = 0;

            // Get a string of data
            data += "Roll #\n";
            data += "\n";

            for (int i = 0; i < pastVals.size(); i++) {
                data += (i + 1) + ".     " + pastVals.get(i) + "\n";

                if (highestVal < pastVals.get(i)) {
                    highestVal = pastVals.get(i);

                }

                if (lowestVal > pastVals.get(i)) {
                    lowestVal = pastVals.get(i);

                }

                sum += pastVals.get(i);

            }

            data += "\n";
            data += "Lowest Value: " + lowestVal + "\n";
            data += "Highest Value: " + highestVal + "\n";
            average += (((double) sum) / ((double) pastVals.size()) + "000").substring(0, 5);
            data += "Average: " + average + "\n";

            // Initialize text Area, buttons, grid pane, scene, stage, and show window
            TextArea historyField = new TextArea(data);
            historyField.setEditable(false);
            Button cancelButton = new Button("Okay");
            Button exportButton = new Button("Export");
            Button clearButton = new Button("Clear History");

            GridPane historyPane = new GridPane();
            historyPane.setPadding(paddingField);
            historyPane.setHgap(verticalGap);
            historyPane.setVgap(horizontalGap);
            historyPane.add(historyField, 0, 0, 3, 1);
            historyPane.add(exportButton, 0, 1);
            historyPane.add(clearButton, 1, 1);
            historyPane.add(cancelButton, 2, 1);

            Scene historyScene = new Scene(historyPane);
            Stage historyStage = new Stage();

            historyStage.setScene(historyScene);
            historyStage.setTitle("Past Values");
            historyStage.initModality(Modality.WINDOW_MODAL);
            historyStage.initOwner(appStage);
            historyStage.setWidth(appStage.getWidth());
            historyStage.setY(appStage.getY());
            historyStage.setX(appStage.getX());
            loadingStage.close();
            historyStage.show();

            // Set cancel button to close window
            cancelButton.setOnAction(event1 -> {
                historyStage.close();

            });

            String finalData = data;

            // Set export button to open export window
            exportButton.setOnAction(event1 -> {
                showExportWindow(historyStage, appStage, paddingField, finalData);

            });

            // Set clear history button to clear the pastVals array
            clearButton.setOnAction(event1 -> {
                clearHistory(pastVals);
                historyStage.close();

            });

        // catch statement will display an alert when index does not exist (ie. when no values have been saved)
        } catch (IndexOutOfBoundsException e) {
            loadingStage.close();
            Alert alert = new Alert(Alert.AlertType.ERROR, "No past values to show! Try clicking \"Roll\" first.");
            alert.showAndWait();

        }
    }

    // Method will export data to selected directory
    public void showExportWindow(Stage historyStage, Stage appStage, Insets paddingField, String data) {
        Stage loadingStage = showLoadingWindow();

        try {
            // Get directory from user
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(historyStage);

            // Initialize labels, text field, buttons, grid pane, scene, and stage
            Label directoryLabel = new Label("Directory:  " + selectedDirectory.getAbsolutePath());
            Label fileNameLabel = new Label("File Name:");
            Label fileExtensionLabel = new Label(".txt");
            TextField fileName = new TextField("DiceRollData");
            Button saveButton = new Button("Save");
            Button cancelButtonExport = new Button("Close");

            GridPane exportPane = new GridPane();
            exportPane.setPadding(paddingField);
            exportPane.setHgap(verticalGap);
            exportPane.setVgap(horizontalGap);
            exportPane.add(directoryLabel, 0, 0, 3, 1);
            exportPane.add(fileNameLabel, 0, 1);
            exportPane.add(fileName, 1, 1);
            exportPane.add(fileExtensionLabel, 2, 1);
            exportPane.add(saveButton, 0, 2);
            exportPane.add(cancelButtonExport, 1, 2);

            Scene exportScene = new Scene(exportPane);

            Stage exportStage = new Stage();
            exportStage.setScene(exportScene);
            exportStage.setTitle("Export");
            exportStage.initModality(Modality.WINDOW_MODAL);
            exportStage.initOwner(historyStage);
            exportStage.setY(appStage.getY());
            exportStage.setX(appStage.getX());
            loadingStage.close();
            exportStage.show();

            // Set save button to save the data as a .txt to the desired directory
            saveButton.setOnAction(event2 -> {
                saveExport(selectedDirectory, fileName, exportStage, data);

            });

            // Cancel button will close the export stage
            cancelButtonExport.setOnAction(event2 -> {
                exportStage.close();

            });

        } catch (Exception e) {
            loadingStage.close();

        }
    }

    // When the "save" button is clicked, this method will save the file
    public void saveExport(File selectedDirectory, TextField fileName, Stage exportStage, String data) {
        Stage loadingStage = showLoadingWindow();

        try {
            File tempFile = new File(selectedDirectory.getAbsolutePath() + "\\" + fileName.getText() + ".txt");

            // Use the temp file to see if the file name already exists at the desired directory
            if (tempFile.exists()) {
                // if the file already exists, display an alert to inform the user
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "This file already exists! Do you wish to overwrite?");
                alert.setHeaderText("WARNING");
                Optional<ButtonType> result = alert.showAndWait();

                // if the cancel button was clicked, close the export stage
                if (result.get() == ButtonType.CANCEL) {
                    loadingStage.close();
                    exportStage.close();

                // Else, replace the file, inform the user of the file creation.
                } else {
                    FileWriter fileWriter = new FileWriter(selectedDirectory.getAbsolutePath() + "\\" + fileName.getText() + ".txt");
                    fileWriter.write(data);
                    fileWriter.close();
                    loadingStage.close();
                    alert = new Alert(Alert.AlertType.INFORMATION, "The data was successfully exported.");
                    alert.showAndWait();
                    exportStage.close();


                }

            // if the file does not exist, create the file, inform the user of the file creation.
            } else {
                FileWriter fileWriter = new FileWriter(selectedDirectory.getAbsolutePath() + "\\" + fileName.getText() + ".txt");
                fileWriter.write(data);
                fileWriter.close();
                loadingStage.close();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "The data was successfully exported.");
                alert.showAndWait();
                exportStage.close();

            }

        // if there was an OI error (ie. access denied), inform the user and abort
        } catch (IOException e) {
            loadingStage.close();
            Alert alert = new Alert(Alert.AlertType.ERROR, "There has been a directory error! Data was not successfully exported.");
            e.printStackTrace();
            alert.showAndWait();

        }
    }

    // Method will clear the pastVals, ask the user for confirmation, and inform the user that the data has been cleared
    public void clearHistory(ArrayList<Integer> pastVals) {
        // if clear per roll checkbox is selected, do not display dialogue boxes
        if (!clearPerRoll) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Erase all data?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
                pastVals.clear();
                alert = new Alert(Alert.AlertType.INFORMATION, "Past values have been cleared.");
                alert.setHeaderText("Got it");
                alert.showAndWait();

            }

        } else {
            pastVals.clear();

        }
    }

    // Method will expand the main window to show additional options
    public void expandOptions(Stage appStage, GridPane gridPane, Insets paddingField, Slider sidesSlider, Button optionsButton, Label numFacesLabel) {
        // Initialize labels, textfield, check boxes, buttons, slider, button grid, grid pane
        Label minLabel = new Label("Minimum Value:");
        Label maxLabel = new Label("Maximum Value:");
        Label multiDiceInfoLabel = new Label("The results of multiple die are displayed in the \"Past Results\" window.");

        TextField minValField = new TextField("1");
        TextField maxValField = new TextField(Integer.toString((int) sidesSlider.getValue())); // This field is set to the slider's value

        CheckBox ignoreOddCheck = new CheckBox("Ignore Odd Numbers");
        CheckBox ignoreEvenCheck = new CheckBox("Ignore Even Numbers");
        CheckBox clearPerRollCheck = new CheckBox("Clear History Per Roll");

        Slider numOfDiceSlider = new Slider(sliderMin, diceSliderMax, numOfDice);
        numOfDiceSlider.setMajorTickUnit(diceSliderMax);
        numOfDiceSlider.setMinorTickCount(diceSliderMinorTickCount);
        numOfDiceSlider.setShowTickMarks(true);
        numOfDiceSlider.setShowTickLabels(true);
        numOfDiceSlider.setShowTickLabels(true);
        numOfDiceSlider.setSnapToTicks(true);
        numOfDiceSlider.setBlockIncrement(1);
        Label numDiceLabel = new Label("Number of Dice: " + (int) numOfDiceSlider.getValue());

        Button aboutButton = new Button("About");
        Button cancelOptionsButton = new Button("Cancel");

        GridPane buttonGrid = new GridPane();
        buttonGrid.add(aboutButton, 0, 0);
        buttonGrid.add(cancelOptionsButton, 1, 0);
        buttonGrid.setVgap(verticalGap);
        buttonGrid.setHgap(horizontalGap);

        gridPane.add(minLabel, 0, 5);
        gridPane.add(maxLabel, 0, 6);
        gridPane.add(numDiceLabel, 0, 7);
        gridPane.add(minValField, 1, 5);
        gridPane.add(maxValField, 1, 6);
        gridPane.add(numOfDiceSlider, 1, 7);
        gridPane.add(ignoreOddCheck, 2, 5);
        gridPane.add(ignoreEvenCheck, 2, 6);
        gridPane.add(clearPerRollCheck, 2, 7);
        gridPane.add(buttonGrid, 2, 9);
        gridPane.add(multiDiceInfoLabel, 0, 8, 3, 1);

        // disable old options
        sidesSlider.setDisable(true);
        optionsButton.setDisable(true);

        // Initialize stage
        appStage.setHeight(appStage.getHeight() * 15 / 8);

        // Initialize to detect errors and set difference, these should return false most of the time
        valueMinError = setValueField(minValField, maxValField, numFacesLabel);
        valueMaxError = setValueField(minValField, maxValField, numFacesLabel);

        // If the minValField was changed, detect errors and set the difference
        minValField.textProperty().addListener(e -> {
            valueMinError = setValueField(minValField, maxValField, numFacesLabel);

        });

        // If the maxValField was changed, detect errors and set the difference
        maxValField.textProperty().addListener(e -> {
            valueMaxError = setValueField(minValField, maxValField, numFacesLabel);

        });

        // if the ignoreEvenCheck was checked, set ignore even equal to true and disable ignore odd
        ignoreEvenCheck.selectedProperty().addListener(e -> {
            if (ignoreEvenCheck.isSelected()) {
                ignoreOddCheck.setDisable(true);
                ignoreEven = true;

            } else {
                ignoreOddCheck.setDisable(false);
                ignoreEven = false;

            }
        });

        // Likewise for ignoreOddCheck
        ignoreOddCheck.selectedProperty().addListener(e -> {
            if (ignoreOddCheck.isSelected()) {
                ignoreEvenCheck.setDisable(true);
                ignoreOdd = true;

            } else {
                ignoreEvenCheck.setDisable(false);
                ignoreOdd = false;

            }
        });

        // if clear per roll checkbox is selected
        clearPerRollCheck.selectedProperty().addListener(e -> {
            if (clearPerRollCheck.isSelected()) {
                clearPerRoll = true;

            } else {
                clearPerRoll = false;

            }
        });

        // if the number of dice has been changed, update
        numOfDiceSlider.valueProperty().addListener(e -> {
            numOfDice = (int) numOfDiceSlider.getValue();
            numDiceLabel.setText("Number of Dice: " + numOfDice);

        });

        // if the about button was clicked, display the about window
        aboutButton.setOnAction(e -> {
            showAboutWindow(paddingField);

        });

        // set cancel button to remove the extra options, enable old options, reshape window, reset slider
        cancelOptionsButton.setOnAction(e -> {
            gridPane.getChildren().remove(minLabel);
            gridPane.getChildren().remove(maxLabel);
            gridPane.getChildren().remove(minValField);
            gridPane.getChildren().remove(maxValField);
            gridPane.getChildren().remove(ignoreEvenCheck);
            gridPane.getChildren().remove(ignoreOddCheck);
            gridPane.getChildren().remove(buttonGrid);
            gridPane.getChildren().remove(numDiceLabel);
            gridPane.getChildren().remove(numOfDiceSlider);
            gridPane.getChildren().remove(multiDiceInfoLabel);
            gridPane.getChildren().remove(clearPerRollCheck);

            sidesSlider.setDisable(false);
            optionsButton.setDisable(false);

            ignoreEven = false;
            ignoreOdd = false;
            numOfDice = 1;

            appStage.setHeight(appStage.getHeight() * 8 / 15);

            startingVal = 1;
            numFaces = (int) sidesSlider.getValue();
            numFacesLabel.setText("Number of Faces: " + numFaces);

        });
    }

    // Method will set value field ("Number of Faces: ...")
    public boolean setValueField(TextField minValField, TextField maxValField, Label numFacesLabel) {
        int difference;
        int max;
        int min;

        try {
            // find the difference to determine the number of faces on the die
            max = Integer.parseInt(maxValField.getText());
            min = Integer.parseInt(minValField.getText());
            difference = Math.abs(max - min) + 1;

            numFacesLabel.setText("Number of Faces: " + difference);

            // update values
            numFaces = difference;
            startingVal = min;
            maxInRange = max;

            return false;

        // if the user entered a non numerical string (NumberFormatException) return true to indicate an error has occurred
        } catch (NumberFormatException exception) {
            numFacesLabel.setText("Number of Faces: ?");
            return true;

        }
    }

    // Method will display the about window
    public void showAboutWindow(Insets paddingField) {
        // Initialize labels, button, grid pane, scene, and stage
        Label titleLabel = new Label("Sophisticated Dice Thing");
        Label versionNumAndDateLabel = new Label("v1.2.4 - 5/11/2020");
        Label copyRightLabel = new Label("Copyright 2020, Trevor Richardson, All rights reserved.");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        versionNumAndDateLabel.setMaxWidth(Double.MAX_VALUE);
        copyRightLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);
        versionNumAndDateLabel.setAlignment(Pos.CENTER);
        copyRightLabel.setAlignment(Pos.CENTER);

        Button closeButton = new Button("Okay");
        closeButton.setAlignment(Pos.BASELINE_RIGHT);

        GridPane aboutPane = new GridPane();
        aboutPane.setPadding(paddingField);
        aboutPane.setHgap(verticalGap);
        aboutPane.setVgap(horizontalGap);
        aboutPane.add(titleLabel, 0, 0);
        aboutPane.add(versionNumAndDateLabel, 0 ,1);
        aboutPane.add(copyRightLabel, 0, 2);
        aboutPane.add(closeButton, 0, 3);

        Scene aboutScene = new Scene(aboutPane);

        Stage aboutStage = new Stage();
        aboutStage.setScene(aboutScene);
        aboutStage.setResizable(false);
        aboutStage.show();

        // Close button will close the stage
        closeButton.setOnAction(event -> {
            aboutStage.close();

        });

    }

    // calculate will create a new random number given the constraints
    public void calculate(TextField currValField, TextField lowestValField, TextField highestValField, ArrayList<Integer> pastVals) {
        int lowestVal;
        int highestVal;
        int newVal = newVal();

        try {
            // Min value cannot be larger than max value
            if (startingVal > maxInRange) {
                throw new InputMismatchException("Min value is greater than max value!");

            }

            // if an error was detected earlier
            if (valueMaxError || valueMinError) {
                throw new NumberFormatException();
            }

            // This prevents a null pointer exception, creates data for the first iteration
            if (firstIteration) {
                lowestVal = newVal;
                highestVal = newVal;
                firstIteration = false;

            // Otherwise, use present data
            } else {
                lowestVal = Integer.parseInt(lowestValField.getText());
                highestVal = Integer.parseInt(highestValField.getText());

            }

            // if need be, update the highest value or lowest value variables
            if (newVal >= highestVal) {
                highestValField.setText(String.valueOf(newVal));

            }

            if (newVal <= lowestVal) {
                lowestValField.setText(String.valueOf(newVal));

            }

            // Update the current value and add to the history
            currValField.setText(String.valueOf(newVal));
            pastVals.add(newVal);

        // Two possible errors. If the min value is greater than the max value, ask the user to fix the issue. Otherwise ask user to check custom values.
        } catch (Exception e) {
            if (startingVal > maxInRange) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Custom min value is greater than max value!");
                alert.showAndWait();
                calculationError = true;

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please check your custom values. Only whole numbers are allowed. Custom values must be in the range -2,147,483,648 to 2,147,483,647. The maximum possible number of faces is 2,147,483,647");
                alert.showAndWait();
                calculationError = true;

            }
        }
    }

    // method will generate a random number with given constraints
    public int newVal() {
        int result = 0;

        // adjust the values based on their signs for proper math
        if (startingVal < 0 && maxInRange > 0) {
            startingVal--;
            numFaces++;

        } else if (maxInRange < 0) {
            startingVal--;

        }

        // if statement will determine if the ignore even or ignore odd switches are checked and get a result
        if (!ignoreEven && !ignoreOdd) {
            result = (int) (startingVal + (Math.random() * (numFaces)));

        } else if (ignoreOdd) {
            do {
                result = (int) (startingVal + (Math.random() * numFaces));

            } while (result % 2 == 1);

        } else if (ignoreEven) {
            do {
                result = (int) (startingVal + (Math.random() * numFaces));

            } while (result % 2 == 0);
        }

        // restore the values based on their signs
        if (startingVal < 0 && maxInRange > 0) {
            startingVal++;
            numFaces--;

        } else if (maxInRange < 0) {
            startingVal++;

        }

        return result;
    }

    // Launch app
    public static void main(String args[]) {
        launch(args);

    }
}