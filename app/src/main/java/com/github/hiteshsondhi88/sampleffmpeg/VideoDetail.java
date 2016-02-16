package com.github.hiteshsondhi88.sampleffmpeg;

/**
 * android
 * <p/>
 * Created by wangalbert on 2/16/16.
 * Copyright (c) 2016 MobiusBobs Inc. All rights reserved.
 */
public class VideoDetail {

  private int width;
  private int height;
  private int rotation;
  private String fileName;
  private String fileTmpName;

  // Constructor
  public VideoDetail(String fileName) {
    this.fileName = fileName;
    fileTmpName = fileName.replace(".mp4", "Tmp.mp4");
  }

  // tostring
  public  String toString() {
    String msg = "fileName = " + fileName + "\n";
    msg += "width = " + width + ", height = " + height + "\n";
    msg += "rotation = " + rotation;
    return msg;
  }

  // Getter and Setter
  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getRotation() {
    return rotation;
  }

  public void setRotation(int rotation) {
    this.rotation = rotation;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFileTmpName() {
    return fileTmpName;
  }

  public void setFileTmpName(String fileTmpName) {
    this.fileTmpName = fileTmpName;
  }
}
