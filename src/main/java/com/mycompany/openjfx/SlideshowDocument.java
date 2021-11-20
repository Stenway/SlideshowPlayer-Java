package com.mycompany.openjfx;

import com.stenway.sml.SmlAttribute;
import com.stenway.sml.SmlDocument;
import com.stenway.sml.SmlElement;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class SlideshowDocument {
	public double DefaultDuration = 10;
	public double DefaultTransitionDuration = 2;
	public TransitionType DefaultTransitionType = TransitionType.CROSSFADE;
	
	public ArrayList<Object> Elements = new ArrayList<>();
	
	private void loadSml(String filePath) throws IOException {
		String directoryPath = Paths.get(filePath).getParent().toString();
		SmlDocument document = SmlDocument.load(filePath);
		SmlElement rootElement = document.getRoot();
		if (!rootElement.hasName("Slideshow")) {
			throw new IllegalStateException("Document is not a slideshow format");
		}
		if (rootElement.hasElement("Default")) {
			SmlElement defaultElement = rootElement.element("Default");
			
			DefaultDuration = defaultElement.getDouble("Duration", 10);
			if (defaultElement.hasAttribute("Transition")) {
				SmlAttribute defaultTransitionAttribute = defaultElement.attribute("Transition");
				DefaultTransitionType = getTransitionType(defaultTransitionAttribute.getString(0));
				if (defaultTransitionAttribute.getValues().length > 1) {
					DefaultTransitionDuration = defaultTransitionAttribute.getDouble(1);
				} else {
					DefaultTransitionDuration = 0;
				}
			}
		}
		for (SmlAttribute attribute : rootElement.attributes()) {
			if (attribute.hasName("Image")) {
				String imageFilePath = attribute.getString(0);
				if (!Paths.get(imageFilePath).isAbsolute()) {
					imageFilePath = Paths.get(directoryPath, imageFilePath).toString();
				}
				Double imageDuration = null;
				if (attribute.getValues().length > 1) {
					imageDuration = attribute.getDouble(1);
				}
				addElement(new SlideshowImage(imageFilePath, imageDuration));
			} else if (attribute.hasName("CrossFade")) {
				Double duration = attribute.getDouble();
				addElement(new SlideshowTransition(TransitionType.CROSSFADE, duration));
			} else if (attribute.hasName("BlackFade")) {
				Double duration = attribute.getDouble();
				addElement(new SlideshowTransition(TransitionType.BLACKFADE, duration));
			} else if (attribute.hasName("Cut")) {
				addElement(new SlideshowTransition(TransitionType.CUT, null));
			} else {
				throw new IllegalStateException("Unknown attribute '"+attribute.getName()+"'");
			}
		}
	}
	
	private void addElement(Object element) {
		if (element instanceof SlideshowTransition && Elements.size() > 0) {
			if (Elements.get(Elements.size()-1) instanceof SlideshowTransition) {
				throw new IllegalStateException("Transition after transition is not allowed");
			}
		}
		Elements.add(element);
	}
	
	public PlayerImage[] convert() {
		ArrayList<PlayerImage> images = new ArrayList<>();
		double time = 0;
		SlideshowTransition lastTransition = null;
		PlayerImage lastPlayerImage = null;
		for (Object element : Elements) {
			if (element instanceof SlideshowTransition) {
				SlideshowTransition transition = (SlideshowTransition)element;
				lastTransition = transition;
			} else if (element instanceof SlideshowImage) {
				if (lastTransition == null) {
					lastTransition = new SlideshowTransition(DefaultTransitionType, DefaultTransitionDuration);
				}
				if (lastPlayerImage != null) {
					if (lastTransition.Type == TransitionType.CROSSFADE) {
						lastPlayerImage.EndTime += lastTransition.Duration;
					} else if (lastTransition.Type == TransitionType.BLACKFADE) {
						lastPlayerImage.FadeOutDuration = lastTransition.Duration / 2;
					}
				}
				SlideshowImage image = (SlideshowImage)element;
				double duration = image.Duration != null ? image.Duration : DefaultDuration;
				String filePath = image.FilePath;
				PlayerImage playerImage = new PlayerImage(filePath, time, 0.001, 0.001, time+duration);
				images.add(playerImage);
				lastPlayerImage = playerImage;
				time += duration;
				if (lastTransition.Type == TransitionType.CROSSFADE) {
					playerImage.EndTime += lastTransition.Duration;
					playerImage.FadeInDuration = lastTransition.Duration;
					time += lastTransition.Duration;
				} else if (lastTransition.Type == TransitionType.BLACKFADE) {
					playerImage.EndTime += lastTransition.Duration / 2;
					playerImage.FadeInDuration = lastTransition.Duration / 2;
					time += lastTransition.Duration / 2;
				} else if (lastTransition.Type == TransitionType.CUT) {
					playerImage.EndTime += 0.05;
				}
				
				lastTransition = null;
			}
		}
		if (lastPlayerImage != null) {
			if (lastTransition == null) {
				lastTransition = new SlideshowTransition(DefaultTransitionType, DefaultTransitionDuration);
			}
			if (lastTransition.Type == TransitionType.CROSSFADE) {
				lastPlayerImage.EndTime += lastTransition.Duration;
				lastPlayerImage.FadeOutDuration = lastTransition.Duration;
			} else if (lastTransition.Type == TransitionType.BLACKFADE) {
				lastPlayerImage.EndTime += lastTransition.Duration / 2;
				lastPlayerImage.FadeOutDuration = lastTransition.Duration / 2;
			}
		}
		return images.toArray(new PlayerImage[0]);
	}
	
	static TransitionType getTransitionType(String value) {
		if (value.equalsIgnoreCase("CrossFade")) {
			return TransitionType.CROSSFADE;
		} else if (value.equalsIgnoreCase("BlackFade")) {
			return TransitionType.BLACKFADE;
		} else if (value.equalsIgnoreCase("Cut")) {
			return TransitionType.CUT;
		} else {
			throw new IllegalArgumentException("Invalid transition type");
		}
	}
	
	public static SlideshowDocument load(String filePath) throws IOException {
		SlideshowDocument slideshow = new SlideshowDocument();
		slideshow.loadSml(filePath);
		return slideshow;
	}
}
