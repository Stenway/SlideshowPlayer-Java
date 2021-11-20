package com.mycompany.openjfx;

public class PlayerImage {
	public String FilePath;
	public double StartTime;
	public Double FadeInDuration;
	public Double FadeOutDuration;
	public double EndTime;
	
	public PlayerImage(String filePath, double startTime, Double fadeInDuration, Double fadeOutDuration, double endTime) {
		FilePath = filePath;
		StartTime = startTime;
		FadeInDuration = fadeInDuration;
		FadeOutDuration = fadeOutDuration;
		EndTime = endTime;
	}
}
