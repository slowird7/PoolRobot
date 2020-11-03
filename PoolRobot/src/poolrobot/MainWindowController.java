/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poolrobot;

import model.Ball;
import model.Cue;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Pocket;
import model.Screen;
import org.opencv.core.Point;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import model.Plan;

/**
 * FXML Controller class
 *
 * @author otsuka
 */
public class MainWindowController implements Initializable {

    public Stage me;
    private Tool tool;
    private BufferedImage imageScreen;
    private Image imageScreenGray;
    private Mat matScreen;
    private Mat matScreenGray;
    private Mat matResult;
    private Point3D[] circles;
    private Cue theCue;
    public MatViewer layerController;
    private Stage layerWindow;
    private MatViewer cvController;
    private Stage cvWindow;
    private Plan thePlan;
    private java.awt.Point mouseClickedPos;

    private Task<String> taskAutoRun = new Task<String>() {
        boolean done = false;

        @Override
        protected String call() throws Exception {
            while (true) {
//                Platform.runLater(() -> {
                if (isCancelled()) {
                    break;
                }
                if (Screen.isNewFrame()) {
                    tool.clickButton(Screen.btnNewFrame);
                    Screen.waitStill();
                } else if (Screen.isOpenBreakShot()) {
                    tool.shoot(40);
                    Screen.waitStill();
                } else if (Screen.isFrameOver()) {
                    tool.clickButton(Screen.btnFrameOver);
                    Screen.waitStill();
                } else {
                    System.out.println("makePlan");
                    updateMessage("makePlan");
                    makePlan();
                    if (isCancelled()) {
                        break;
                    }
                    System.out.println("aim");
                    updateMessage("aim");
                    aim();
                    if (isCancelled()) {
                        break;
                    }
                    updateMessage("topView");
                    topView();
                    if (isCancelled()) {
                        break;
                    }
                    updateMessage("shoot");
                    shoot();
                    Screen.waitStill();
                }
            }
            return "";
        }

        @Override

        protected void cancelled() {
            done = true;
            super.cancelled();
            lblStatus.textProperty().unbind();
        }

    };

    private void topView() {
        Screen.switchToTopView();
    }

    @FXML
    private ComboBox<String> selGame;
    @FXML
    private AnchorPane mainWindow;
    @FXML
    private ToggleButton btnLayer;
    @FXML
    private ToggleButton btnView;
    @FXML
    private Button btnTryPlan;
    @FXML
    private Button btnAutoPlay;
    @FXML
    private ToggleButton btnAutoRun;
    @FXML
    private ChoiceBox<Integer> cbTarget;
    @FXML
    private ChoiceBox<Integer> cbPocket;
    @FXML
    private Slider sldSpin;
    @FXML
    private Slider threshMax;
    @FXML
    private ImageView imageView;
    @FXML
    private TextArea txtConsole;
    @FXML
    private ListView<Plan> lstPlan;
    @FXML
    private Label lblAction;
    @FXML
    private Label lblStatus;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
        cbPocket.setItems(FXCollections.observableArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27));
        selGame.setItems(FXCollections.observableArrayList("9Ball", "Bank"));
        selGame.setValue("");
        onSelGameSelected(null);
        
        matResult = new Mat();

        Screen.rectPoolWindow = tool.findPoolWindow();
        layerWindow = new Stage();
        //layerWindow.initOwner((Stage)mainWindow.getScene().getWindow());
        layerWindow.initStyle(StageStyle.TRANSPARENT);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MatViewer.fxml"));
            Parent x = loader.load();
            Scene y = new Scene(x);
            y.setFill(Color.TRANSPARENT);
            layerWindow.setScene(y);
            layerWindow.setFullScreen(true);
            layerController = loader.getController();
        } catch (IOException ex) {
            System.out.println("Failed to create screenViewer.");

        }

        theCue = new Cue();

        layerController.add(theCue);
        Pocket.initPockets();
        for (Pocket pocket : Pocket.realPockets) {
            layerController.add(pocket.rect);
        }

        lstPlan.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Plan>() {
            @Override
            public void changed(ObservableValue<? extends Plan> observable, Plan oldValue, Plan newValue) {
                thePlan = newValue;
                cbTarget.setValue(newValue.ball.ballNo);
                cbPocket.setValue(newValue.pocket.pocketNo);
                makeCall();

            }
        }
        );

        Platform.runLater(() -> {
            layerWindow.initOwner(mainWindow.getScene().getWindow());
        });

    }

    private void setupFor9BallGame() {
        Ball.noOfBalls = 10;
        Ball.initBalls(selGame.getValue());
        cbTarget.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        try {
            tool = new Tool9Ball();
        } catch (AWTException ex) {
            System.out.println("Failed to create Robot.");
            return;
        }

    }
    
    private void setupForBankPoolGame() {
        Ball.noOfBalls = 6;
        Ball.initBalls(selGame.getValue());
        cbTarget.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        try {
            tool = new ToolBank();
        } catch (AWTException ex) {
            System.out.println("Failed to create Robot.");
            return;
        }
    }

    private void pushMouseCursorPos() {
        PointerInfo pI = MouseInfo.getPointerInfo();
        mouseClickedPos = pI.getLocation();
    }

    private void popMouseCursorPos() {
        tool.mouseMove(mouseClickedPos.x, mouseClickedPos.y);
    }

    private MatViewer layerWindow() {
        if (layerWindow == null) {
            layerWindow = new Stage();
            layerWindow.initOwner((Stage) mainWindow.getScene().getWindow());
            layerWindow.initStyle(StageStyle.TRANSPARENT);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MatViewer.fxml"));
                Parent x = loader.load();
                Scene y = new Scene(x);
                layerWindow.setScene(y);
                layerWindow.setFullScreen(true);
                layerController = loader.getController();
            } catch (IOException ex) {
                System.out.println("Failed to create screenViewer.");

            }
        }
        return layerController;
    }

    private void hide() {
//        ((Stage) mainWindow.getScene().getWindow()).toBack();
    }

    private void show() {
        ((Stage) mainWindow.getScene().getWindow()).toFront();
        ((Stage) mainWindow.getScene().getWindow()).setAlwaysOnTop(true);
    }

    private void captureScreen() {
        // これで画面キャプチャ
        try {
//            hide();
//            layerWindow.hide();
            matScreen = Screen.getScreenMat();
            matScreen.copyTo(matResult);
//            screenViewer().showImage(matResult);
        } finally {
//            show();
//            mainWindow.toFront();
        }
    }

    private void makePlan() {
        System.out.println("makePlan>");
        Screen.switchToTopView();
        System.out.println("makePlan.captureScreen");
        captureScreen();
        System.out.println("makePlan.recognizeBallInTopView");
        Mat matScreen = Screen.getPoolMat();
        // for debug
        for (int i = 0; i < Ball.noOfBalls; i++) {
            tool.recognizeBallInTopView(Ball.balls[i], matScreen, matResult);
            Imgcodecs.imwrite(String.format("dadada%d.png", i), matScreen);
        }

        System.out.println("makePlan.findExecutablePlans");
        List<Plan> plans = tool.findExecutablePlans();
        lstPlan.getItems().clear();
        for (int i = 0; i < Math.min(5, plans.size()); i++) {
            lstPlan.getItems().add(plans.get(i));
        }
        if (plans.size() > 0) {
            thePlan = plans.get(0);
            System.out.println("makePlan.makeCall");
            makeCall();
        }

    }

    private void makeCall() {
        System.out.println("makeCall");
        Point cueEndPoint = new Point(0, 0);
        System.out.println("callBallAndPocket");
        tool.callBallAndPocket(thePlan);
        System.out.println("aim");
        tool.aim(thePlan, cueEndPoint);

        Screen.switchToTopView();
        layerWindow().aimPoint.setTranslateX(thePlan.aimPoint.getX());
        layerWindow().aimPoint.setTranslateY(thePlan.aimPoint.getY());
        layerWindow().cueEndPoint.setTranslateX(cueEndPoint.x);
        layerWindow().cueEndPoint.setTranslateY(cueEndPoint.y);
        layerWindow().pathToAimPoint.setStartX(Ball.cueBall.sx);
        layerWindow().pathToAimPoint.setStartY(Ball.cueBall.sy);
        layerWindow().pathToAimPoint.setEndX(thePlan.aimPoint.getX());
        layerWindow().pathToAimPoint.setEndY(thePlan.aimPoint.getY());
        layerWindow().pathToCushion.setStartX(thePlan.ball.sx);
        layerWindow().pathToCushion.setStartY(thePlan.ball.sy);
        layerWindow().pathToCushion.setEndX(thePlan.pocket.sx);
        layerWindow().pathToCushion.setEndY(thePlan.pocket.sy);
//        layerWindow().pathToPocket.setStartX(Ball.cueBall.sx);
//        layerWindow().pathToPocket.setStartY(Ball.cueBall.sy);
//        layerWindow().pathToPocket.setEndX(thePlan.aimPoint.getX());
//        layerWindow().pathToPocket.setEndY(thePlan.aimPoint.getY());
    }

    private void aim() {
        if (thePlan == null) {
            new Alert(Alert.AlertType.WARNING, "No plan is selected.").showAndWait();
            return;
        }
        tool.aimInParseView(thePlan.ball, thePlan.offset, sldSpin.getValue(), txtConsole);
    }

    private void shoot() {
        tool.shoot((int) Math.sqrt(thePlan.distance) / 2);
    }

    private void undo() {
        hide();
        Screen.undo();
        show();
    }

    //=======================================================================
    // Event ahndler
    //=======================================================================
    @FXML
    private void onSelGameSelected(ActionEvent ev) {
        if (selGame.getValue().startsWith("9Ball")) {
            setupFor9BallGame();
        } else if (selGame.getValue().startsWith("Bank")) {
            setupForBankPoolGame();
        } else {
            try {
               tool = new Tool();
            } catch (AWTException ex) {
               System.out.println("Failed to create Robot.");
               return;
           }
       }
//        for (Ball ball : Ball.balls) {
//            layerController.add(ball.contour);
//            layerController.add(ball.horz);
//            layerController.add(ball.vert);
//            layerController.add(ball.a);
//        }
   }
    
    @FXML
    private void onBtnViewClicked(ActionEvent ev) {
        pushMouseCursorPos();
        tool.clickButton(Screen.btnSwitchView);
        tool.clickButton(Screen.btnSwitchView);
//        captureScreen();
//        layerWindow.show();
//        layerWindow.toFront();
        hide();
        show();
        mainWindow.toFront();
        popMouseCursorPos();
    }

    @FXML
    private void onBtnLayerClicked(ActionEvent ev) {
        pushMouseCursorPos();
        if (btnLayer.isSelected()) {
            layerWindow.show();
            layerWindow.toFront();
            hide();
            show();
            mainWindow.toFront();
        } else {
            layerWindow.toBack();
            layerWindow.hide();
        }
        popMouseCursorPos();
    }

    @FXML
    private void onBtnRecognizeClicked(ActionEvent ev) {
        pushMouseCursorPos();
        Screen.switchToTopView();
        captureScreen();
        if (Screen.isTopView()) {
            Mat matScreen = Screen.getPoolMat();
            for (int i = 0; i < Ball.noOfBalls; i++) {
                tool.recognizeBallInTopView(Ball.balls[i], matScreen, matResult);
            }
        } else {
            for (int i = 0; i < Ball.noOfBalls; i++) {
                tool.recognizeBallInParseView(Ball.balls[i], matResult);
            }
        }
        layerWindow.show();
        layerWindow.toFront();
        hide();
        show();
        mainWindow.toFront();
        popMouseCursorPos();
    }

    @FXML
    private void onBtnPlanClicked(ActionEvent ev) {
        pushMouseCursorPos();

        makePlan();
        layerWindow.show();
        layerWindow.toFront();
        hide();
        show();
        mainWindow.toFront();

        popMouseCursorPos();
    }

    @FXML
    private void onBtnUndoClicked(ActionEvent ev) {
        pushMouseCursorPos();
        undo();
        show();
        mainWindow.toFront();
        popMouseCursorPos();
    }

    @FXML
    private void onBtnShootClicked(ActionEvent ev) {
        pushMouseCursorPos();
        hide();
        layerWindow.hide();
        Screen.switchToTopView();
        shoot();
        show();
        mainWindow.toFront();
        popMouseCursorPos();
    }

    @FXML
    private void onBtnTryPlanClicked(ActionEvent ev) {
        pushMouseCursorPos();
        thePlan = new Plan();
        thePlan.ball = Ball.balls[cbTarget.getValue()];
        thePlan.pocket = Pocket.imaginaryPockets[cbPocket.getValue()];
        if (tool.isExecutablePlan(thePlan)) {
            tool.evaluatePlan(thePlan);
            txtConsole.setText(String.format("the plan is available. score=%f", thePlan.score));
        } else {
            txtConsole.setText(String.format("the plan is NOT available. cause=%s", thePlan.cause));
        }
        show();
        mainWindow.toFront();
        popMouseCursorPos();
    }

    @FXML
    private void onLstPlanSelected(ActionEvent ev) {
        thePlan = lstPlan.getSelectionModel().getSelectedItem();
        cbTarget.setValue(thePlan.ball.ballNo);
        cbPocket.setValue(thePlan.pocket.pocketNo);
        makeCall();
        show();
        mainWindow.toFront();
    }

    @FXML
    private void onBtnAimClicked(ActionEvent ev) {
        pushMouseCursorPos();
        hide();
        aim();
        layerWindow.show();
        layerWindow.toFront();
        show();
        mainWindow.toFront();
        popMouseCursorPos();
    }

    @FXML
    private void onBtnAutoPlayClicked(ActionEvent ev) {
        pushMouseCursorPos();
        makePlan();
        aim();
        topView();
        shoot();
        hide();
        show();
        mainWindow.toFront();
        popMouseCursorPos();
    }

    @FXML
    private void onBtnAutoRunClicked(ActionEvent ev) {
        pushMouseCursorPos();
        hide();
        if (btnAutoRun.isSelected()) {
            Thread th = new Thread(taskAutoRun);
            th.setDaemon(true);
            lblStatus.textProperty().bind(taskAutoRun.messageProperty());
            th.start();
        } else {
            taskAutoRun.cancel();
        }
        show();
        mainWindow.toFront();
        popMouseCursorPos();
    }
    
    @FXML
    private void onKeyPressed(KeyEvent ev) {
        if (ev.getCharacter().equals(" ")) {
            taskAutoRun.cancel();
        }
    }

}
