package com.transmanagerB.domain;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * 	해상도	픽셀 수	포맷
 * SD	720 X 480	345,600	480p
 * HD	1,280 X 720	921,600	720p
 * FHD	1,920 X 1,080	2,073,600	1080p / 1080i
 * QHD	3,840 X 2,160	8,294,400	2160p
 * UHD	4,096 X 2,160	8,847,360	2160p
 * @author user
 */
@Data
public class TransmanagerBCommand {

	private int priority;
	
	private String correlation_ID; // 키 값

	@NotNull @Size(min=1)
	private String srcfile;
	
	private String destfile1; // base_Adaptive_30fps_320x180_0300K;
	
	private String destfile2; // base_Adaptive_30fps_640x360_0600K;
	
	private String destfile3; // base_Adaptive_30fps_640x360_0900K;
	
	private String destfile4; // base_Adaptive_30fps_960x540_1500K;
	
	private String destfile5; // base_Adaptive_30fps_1280x720_3000K;
	
//	private String destfile6; // base_Adaptive_30fps_1920x1080_5000K;
	
	private double progress;
}
