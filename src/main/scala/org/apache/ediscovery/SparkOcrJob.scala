/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.apache.ediscovery

import java.io.DataInputStream

import org.apache.ediscovery.ocr.{OCR, TesarrectOcr}
import org.apache.spark.{SparkConf, SparkContext}
import org.ghost4j.document.PDFDocument

object SparkOcrJob {
  val ocr: OCR = new TesarrectOcr("./data/", "eng", 300)

  def main(args: Array[String]): Unit = {
    val sparkContext = new SparkContext(sparkConf)
    sparkContext
      .binaryFiles("./sample.pdf")
      .map(convertToString)
      .foreach(println)
  }

  private def sparkConf = {
    new SparkConf()
      .setAppName("EDiscovery Spark OCR")
      .setMaster("local[2]")
      .set("spark.executor.memory", "1g")
  }

  def convertToString(file: (String, org.apache.spark.input.PortableDataStream)): String = {
    val stream = file._2.open
    try {
      ocr.recognize(read(stream))
    } finally {
      stream.close()
    }
  }

  private def read(stream: DataInputStream) = {
    val document: PDFDocument = new PDFDocument()
    document.load(stream)
    document
  }
}
