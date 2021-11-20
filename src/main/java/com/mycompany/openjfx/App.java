package com.mycompany.openjfx;

import java.nio.file.Paths;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;


public class App extends Application {

	@Override
	public void start(Stage primaryStage) {
		SlideshowDocument slideshow;
		try {
			slideshow = SlideshowDocument.load(filePath);
		} catch (Exception e) {
			throw new IllegalStateException("Could not load slideshow", e);
		}
		PlayerImage[] images = slideshow.convert();
		
		Group root = new Group();
		for (PlayerImage playerImage : images) {
			String src = Paths.get(playerImage.FilePath).toUri().toString();
			Image image = new Image(src);
			ImageView imageView = new ImageView(image);  
			
			imageView.setX(0);  
			imageView.setY(0); 
			imageView.setPreserveRatio(true);
			imageView.fitWidthProperty().bind(primaryStage.widthProperty());
			imageView.setOpacity(0.0);
			
			FadeTransition fadeIn = new FadeTransition();
			fadeIn.setDelay(Duration.seconds(playerImage.StartTime));
			fadeIn.setDuration(Duration.seconds(playerImage.FadeInDuration));
			fadeIn.setFromValue(0);
			fadeIn.setToValue(1.0);
			fadeIn.setCycleCount(1);
			fadeIn.setAutoReverse(false);
			fadeIn.setNode(imageView);
			fadeIn.play();
			
			FadeTransition fadeOut = new FadeTransition();
			fadeOut.setDelay(Duration.seconds(playerImage.EndTime-playerImage.FadeOutDuration));
			fadeOut.setDuration(Duration.seconds(playerImage.FadeOutDuration));
			fadeOut.setFromValue(1);
			fadeOut.setToValue(0.0);
			fadeOut.setCycleCount(1);
			fadeOut.setAutoReverse(false);
			fadeOut.setNode(imageView);
			fadeOut.play();
			
			root.getChildren().add(imageView);
		}
		
		Scene scene = new Scene(root,800,450);
		scene.setFill(Color.BLACK);
		primaryStage.setScene(scene);  
		primaryStage.setFullScreenExitHint("");
		primaryStage.setFullScreen(true);
		
		primaryStage.setTitle("SML Slideshow Player");  
		primaryStage.show(); 
	}

	static String filePath;
   
	public static void main(String[] args) {
		filePath = "d:\\Slideshow\\Slideshow.sml";
		launch();
	}

}