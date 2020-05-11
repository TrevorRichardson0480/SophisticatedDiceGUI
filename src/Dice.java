import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

public class Dice extends Application {
    int numFaces = 6;
    int startingVal = 1;
    int maxInRange = 6;
    boolean firstIteration = true;
    boolean ignoreEven = false;
    boolean ignoreOdd = false;
    boolean valueMinError = false;
    boolean valueMaxError = false;


    public void start(Stage appStage) {
        // Show a loading windows to indicate that the program is running. In most cases, the loading window will not be seen.
        Stage loadingStage = showLoadingWindow();

        // Set layout and initial states
        int textFieldWidth = 15;
        int gridPadding = 10;
        int verticalGap = 10;
        int horizontalGap = 10;
        int sliderMin = 1;
        int sliderMax = 20;
        int sliderInit = 6;
        int sliderMinorTickCount = sliderMax - 1;

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
            calculate(currValField, lowestValField, highestValField, pastVals);

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
            showHistoryWindow(appStage, pastVals, paddingField, verticalGap, horizontalGap);

        });

        //
        optionsButton.setOnAction(event -> {
            expandOptions(appStage, gridPane, paddingField, verticalGap, horizontalGap, sidesSlider, optionsButton, numFacesLabel);

        });

        sidesSlider.valueProperty().addListener(e -> {
            numFaces = (int) sidesSlider.getValue();
            numFacesLabel.setText("Number of Faces: " + numFaces);

        });

        appStage.setScene(scene);
        appStage.setTitle("Dice");
        appStage.show();

        loadingStage.close();

        appStage.setWidth(appStage.getWidth() + 50);
        appStage.setResizable(false);
        numFacesLabel.setPrefWidth(numFacesLabel.getWidth() + 25);

    }

    public static Stage showLoadingWindow() {
        int gridPadding = 10;
        int verticalGap = 10;
        int horizontalGap = 10;

        GridPane gridPane = new GridPane();

        ProgressIndicator loadingIndicator = new ProgressIndicator(-1.0);
        Label loadingLabel = new Label("Loading...");

        Insets paddingField = new Insets(gridPadding, gridPadding, gridPadding, gridPadding);

        gridPane.setPadding(paddingField);
        gridPane.setHgap(verticalGap);
        gridPane.setVgap(horizontalGap);

        gridPane.add(loadingIndicator, 0, 0);
        gridPane.add(loadingLabel, 1, 0);

        Scene scene = new Scene(gridPane);
        Stage loadingStage = new Stage();
        loadingStage.setScene(scene);
        loadingStage.setResizable(false);
        loadingStage.show();

        return loadingStage;

    }

    public void showHistoryWindow(Stage appStage, ArrayList<Integer> pastVals, Insets paddingField, int verticalGap, int horizontalGap) {
        try {
            String data = "";
            String average = "";
            int highestVal = pastVals.get(0);
            int lowestVal = pastVals.get(0);
            int sum = 0;

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
            historyStage.show();

            cancelButton.setOnAction(event1 -> {
                historyStage.close();

            });

            String finalData = data;

            exportButton.setOnAction(event1 -> {
                showExportWindow(historyStage, appStage, paddingField, verticalGap, horizontalGap, finalData);

            });


            clearButton.setOnAction(event1 -> {
                clearHistory(pastVals, historyStage);

            });

        } catch (IndexOutOfBoundsException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No past values to show! Try clicking \"Roll\" first.");
            alert.showAndWait();

        }

    }

    public static void showExportWindow(Stage historyStage, Stage appStage, Insets paddingField, int verticalGap, int horizontalGap, String data) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(historyStage);

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
        exportStage.show();

        saveButton.setOnAction(event2 -> {
            saveExport(selectedDirectory, fileName, exportStage, data);

        });

        cancelButtonExport.setOnAction(event2 -> {
            exportStage.close();

        });

    }

    public static void saveExport(File selectedDirectory, TextField fileName, Stage exportStage, String data) {
        try {
            File tempFile = new File(selectedDirectory.getAbsolutePath() + "\\" + fileName.getText() + ".txt");

            if (tempFile.exists()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "This file already exists! Do you wish to overwrite?");
                alert.setHeaderText("WARNING");
                Optional<ButtonType> result = alert.showAndWait();

                if (result.get() == ButtonType.CANCEL) {
                    exportStage.close();

                } else {
                    FileWriter fileWriter = new FileWriter(selectedDirectory.getAbsolutePath() + "\\" + fileName.getText() + ".txt");
                    fileWriter.write(data);
                    fileWriter.close();
                    alert = new Alert(Alert.AlertType.INFORMATION, "The data was successfully exported.");
                    alert.showAndWait();
                    exportStage.close();

                }

            } else {
                FileWriter fileWriter = new FileWriter(selectedDirectory.getAbsolutePath() + "\\" + fileName.getText() + ".txt");
                fileWriter.write(data);
                fileWriter.close();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "The data was successfully exported.");
                alert.showAndWait();
                exportStage.close();

            }

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "There has been a directory error! Data was not successfully exported.");
            e.printStackTrace();
            alert.showAndWait();

        }
    }

    public void clearHistory(ArrayList<Integer> pastVals, Stage historyStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Erase all data?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            pastVals.clear();
            alert = new Alert(Alert.AlertType.INFORMATION, "Past values have been cleared.");
            alert.setHeaderText("Got it");
            alert.showAndWait();
            historyStage.close();
        }

    }

    public void expandOptions(Stage appStage, GridPane gridPane, Insets paddingField, int verticalGap, int horizontalGap, Slider sidesSlider, Button optionsButton, Label numFacesLabel) {
        Label minLabel = new Label("Minimum Value:");
        Label maxLabel = new Label("Maximum Value:");

        TextField minValField = new TextField("1");
        TextField maxValField = new TextField(Integer.toString((int) sidesSlider.getValue()));

        CheckBox ignoreOddCheck = new CheckBox("Ignore Odd Numbers");
        CheckBox ignoreEvenCheck = new CheckBox("Ignore Even Numbers");

        Button aboutButton = new Button("About");
        Button cancelOptionsButton = new Button("Cancel");

        gridPane.add(minLabel, 0, 5);
        gridPane.add(maxLabel, 0, 6);
        gridPane.add(aboutButton, 0, 7);
        gridPane.add(minValField, 1, 5);
        gridPane.add(maxValField, 1, 6);
        gridPane.add(ignoreOddCheck, 2, 5);
        gridPane.add(ignoreEvenCheck, 2, 6);
        gridPane.add(cancelOptionsButton, 2, 7);

        sidesSlider.setDisable(true);
        optionsButton.setDisable(true);

        appStage.setHeight(appStage.getHeight() * 1.5);

        valueMinError = setValueField(minValField, maxValField, numFacesLabel);
        valueMaxError = setValueField(minValField, maxValField, numFacesLabel);

        minValField.textProperty().addListener(e -> {
            valueMinError = setValueField(minValField, maxValField, numFacesLabel);

        });

        maxValField.textProperty().addListener(e -> {
            valueMaxError = setValueField(minValField, maxValField, numFacesLabel);

        });

        ignoreEvenCheck.selectedProperty().addListener(e -> {
            if (ignoreEvenCheck.isSelected()) {
                ignoreOddCheck.setDisable(true);
                ignoreEven = true;

            } else {
                ignoreOddCheck.setDisable(false);
                ignoreEven = false;

            }
        });

        ignoreOddCheck.selectedProperty().addListener(e -> {
            if (ignoreOddCheck.isSelected()) {
                ignoreEvenCheck.setDisable(true);
                ignoreOdd = true;

            } else {
                ignoreEvenCheck.setDisable(false);
                ignoreOdd = false;

            }
        });

        aboutButton.setOnAction(e -> {
            showAboutWindow(paddingField, verticalGap, horizontalGap);

        });

        cancelOptionsButton.setOnAction(e -> {
            gridPane.getChildren().remove(minLabel);
            gridPane.getChildren().remove(maxLabel);
            gridPane.getChildren().remove(minValField);
            gridPane.getChildren().remove(maxValField);
            gridPane.getChildren().remove(ignoreEvenCheck);
            gridPane.getChildren().remove(ignoreOddCheck);
            gridPane.getChildren().remove(cancelOptionsButton);

            sidesSlider.setDisable(false);
            optionsButton.setDisable(false);

            ignoreEven = false;
            ignoreOdd = false;

            appStage.setHeight((appStage.getHeight() * 2) / 3);

            startingVal = 1;
            numFaces = (int) sidesSlider.getValue();
            numFacesLabel.setText("Number of Faces: " + numFaces);

        });
    }

    public boolean setValueField(TextField minValField, TextField maxValField, Label numFacesLabel) {
        int difference;
        int max;
        int min;

        try {
            max = Integer.parseInt(maxValField.getText());
            min = Integer.parseInt(minValField.getText());
            difference = Math.abs(max - min) + 1;

            numFacesLabel.setText("Number of Faces: " + difference);

            numFaces = difference;
            startingVal = min;
            maxInRange = max;

            return false;

        } catch (NumberFormatException exception) {
            numFacesLabel.setText("Number of Faces: ?");
            return true;

        }
    }

    public void showAboutWindow(Insets paddingField, int verticalGap, int horizontalGap) {
        GridPane aboutPane = new GridPane();

        Label titleLabel = new Label("Sophisticated Dice Thing");
        Label versionNumAndDateLabel = new Label("v1.1.1 - 5/11/2020");
        Label copyRightLabel = new Label("Copyright 2020, Trevor Richardson, All rights reserved.");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        versionNumAndDateLabel.setMaxWidth(Double.MAX_VALUE);
        copyRightLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);
        versionNumAndDateLabel.setAlignment(Pos.CENTER);
        copyRightLabel.setAlignment(Pos.CENTER);

        Button closeButton = new Button("Okay");
        closeButton.setAlignment(Pos.BASELINE_RIGHT);

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

        closeButton.setOnAction(event -> {
            aboutStage.close();

        });

    }

    public void calculate(TextField currValField, TextField lowestValField, TextField highestValField, ArrayList<Integer> pastVals) {
        int lowestVal;
        int highestVal;
        int newVal = newVal();

        try {
            if (startingVal > maxInRange) {
                throw new InputMismatchException("Min value is greater than max value!");

            }

            if (valueMaxError || valueMinError) {
                throw new NumberFormatException();
            }

            if (firstIteration) {
                lowestVal = newVal;
                highestVal = newVal;
                firstIteration = false;

            } else {
                lowestVal = Integer.parseInt(lowestValField.getText());
                highestVal = Integer.parseInt(highestValField.getText());

            }

            if (newVal >= highestVal) {
                highestValField.setText(String.valueOf(newVal));

            }

            if (newVal <= lowestVal) {
                lowestValField.setText(String.valueOf(newVal));

            }

            currValField.setText(String.valueOf(newVal));
            pastVals.add(newVal);

        } catch (Exception e) {
            if (startingVal > maxInRange) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Custom min value is greater than max value!");
                alert.showAndWait();

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please check your custom values. Only whole numbers are allowed.");
                alert.showAndWait();

            }
        }
    }

    public int newVal() {
        int result = 0;

        if (startingVal < 0 && maxInRange > 0) {
            startingVal--;
            numFaces++;

        } else if (maxInRange < 0) {
            startingVal--;

        }

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

        if (startingVal < 0 && maxInRange > 0) {
            startingVal++;
            numFaces--;

        } else if (maxInRange < 0) {
            startingVal++;

        }

        return result;
    }

    public static void main(String args[]) {
        launch(args);

    }
}