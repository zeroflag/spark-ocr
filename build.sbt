name := "spark-ocr"

version := "0.1"

scalaVersion := "2.11.8"

val sparkVersion = "2.3.0"

lazy val javaCppVer = "1.4.1"
lazy val tessaVer = s"3.05.01-$javaCppVer"
lazy val leptoVer = s"1.75.3-$javaCppVer"
lazy val os = "macosx-x86_64"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.ghost4j" % "ghost4j" % "1.0.0",
  "org.bytedeco" % "javacpp" % javaCppVer,
  "org.bytedeco.javacpp-presets" % "tesseract" % tessaVer,
  "org.bytedeco.javacpp-presets" % "tesseract" % tessaVer classifier os,
  "org.bytedeco.javacpp-presets" % "leptonica" % leptoVer,
  "org.bytedeco.javacpp-presets" % "leptonica" % leptoVer classifier os
)