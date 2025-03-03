package com.example.java_game;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.shape.Circle;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.scene.control.Label;

public class GameView {

    @FXML
    private Circle circle_large;
    @FXML
    private Circle circle_small;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private Line horizontal;
    @FXML
    private Line border;
    @FXML
    private Line arrow;

    @FXML
    private Label scoreLabel;
    @FXML
    private Label shotsLabel;

    private Thread t = null;
    private boolean play = false;
    private boolean isPaused = false;
    private double speed_large = 2;
    private double speed_small = 4;
    private final double arrow_speed = 5;

    private Thread arrowThread = null;
    private boolean isShooting = false;
    private boolean wasPaused = false;
    private boolean isFirstStart = true;

    private int score = 0;
    private int shots = 0;

    private double initialLargeY;
    private double initialSmallY;
    private double initialArrowStartX;
    private double initialArrowEndX;

    @FXML
    void next() {
        if (!isPaused) {
            double y_large = circle_large.getLayoutY();
            double y_small = circle_small.getLayoutY();
            double horizontalY = horizontal.getLayoutY();

            if (y_large + speed_large >= horizontalY - circle_large.getRadius()) {
                speed_large = -speed_large;
            } else if (y_large + speed_large <= circle_large.getRadius()) {
                speed_large = -speed_large;
            }
            circle_large.setLayoutY(y_large + speed_large);

            if (y_small + speed_small >= horizontalY - circle_small.getRadius()) {
                speed_small = -speed_small;
            } else if (y_small + speed_small <= circle_small.getRadius()) {
                speed_small = -speed_small;
            }
            circle_small.setLayoutY(y_small + speed_small);
        }
    }

    @FXML
    void start() {
        if (isFirstStart) {
            initialLargeY = circle_large.getLayoutY();
            initialSmallY = circle_small.getLayoutY();
            initialArrowStartX = arrow.getStartX();
            initialArrowEndX = arrow.getEndX();
        }
        isFirstStart = false;
        if (t == null) {
            t = new Thread(() -> {
                play = true;
                while (play) {
                    Platform.runLater(this::next);
                    try {
                        Thread.sleep(10); // Задержка для плавного движения
                    } catch (InterruptedException e) {
                        play = false;
                        t = null;
                    }
                }
            });
            t.start();
        }
    }

    @FXML
    void stop() {
        play = false;
        isPaused = false;
        if (t != null) {
            t.interrupt();
            t = null;
        }

        score = 0;
        shots = 0;
        updateScoreLabel();
        updateShotsLabel();

        circle_large.setLayoutY(initialLargeY);
        circle_small.setLayoutY(initialSmallY);
        arrow.setStartX(initialArrowStartX);
        arrow.setEndX(initialArrowEndX);

        isShooting = false;
        if (arrowThread != null) {
            arrowThread.interrupt();
            arrowThread = null;
        }
    }

    @FXML
    void pause() {
        if (play){
            isPaused = true;
        }
    }

    @FXML
    void resume() {
        isPaused = false;
        wasPaused = true;
        if (isShooting) {
            shoot();
        }
        wasPaused = false;
    }

    @FXML
    void shoot() {
        if (play && !isPaused) {
            if (!wasPaused && !isShooting) {
                shots++;
            }
            updateShotsLabel();

            if (arrowThread == null || !arrowThread.isAlive()) {
                arrowThread = new Thread(() -> {
                    isShooting = true;
                    while (isShooting && !isPaused) {
                        Platform.runLater(() -> {
                            double arrowStartX = arrow.getStartX();
                            double arrowEndX = arrow.getEndX();
                            double borderX = border.getLayoutX() - initialArrowEndX;

                            if (arrowEndX + arrow_speed < borderX) {
                                arrow.setStartX(arrowStartX + arrow_speed);
                                arrow.setEndX(arrowEndX + arrow_speed);
                            } else {
                                isShooting = false;
                                arrow.setStartX(initialArrowStartX);
                                arrow.setEndX(initialArrowEndX);
                            }

                            double largeX = circle_large.getLayoutX();
                            double largeY = circle_large.getLayoutY();
                            double largeRadius = circle_large.getRadius();
                            double distanceToLarge = Math.sqrt(Math.pow(arrowEndX - largeX, 2) + Math.pow(arrow.getStartY() - largeY, 2));

                            if (distanceToLarge <= largeRadius) {
                                isShooting = false;
                                arrow.setStartX(initialArrowStartX);
                                arrow.setEndX(initialArrowEndX);
                                score += 1;
                                updateScoreLabel();
                            }

                            double smallX = circle_small.getLayoutX();
                            double smallY = circle_small.getLayoutY();
                            double smallRadius = circle_small.getRadius();
                            double distanceToSmall = Math.sqrt(Math.pow(arrowEndX - smallX, 2) + Math.pow(arrow.getStartY() - smallY, 2));

                            if (distanceToSmall <= smallRadius) {
                                isShooting = false;
                                arrow.setStartX(initialArrowStartX);
                                arrow.setEndX(initialArrowEndX);
                                score += 2;
                                updateScoreLabel();
                            }
                        });
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            isShooting = false;
                            arrowThread = null;
                        }
                    }
                });
                arrowThread.start();
            }
        }
    }

    private void updateScoreLabel() {
        scoreLabel.setText(String.valueOf(score));
    }

    private void updateShotsLabel() {
        shotsLabel.setText(String.valueOf(shots));
    }
}
