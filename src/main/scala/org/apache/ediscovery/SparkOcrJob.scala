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

import org.apache.ediscovery.ocr.{OCR, OpenPdfTextConverter, TesarrectOcr}
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.common.SolrInputDocument
import org.apache.spark.input.PortableDataStream
import org.apache.spark.{SparkConf, SparkContext}
import org.ghost4j.document.PDFDocument

object SparkOcrJob {
//  val ocr: OCR = new TesarrectOcr("./data/", "eng", 300)
  val ocr: OCR = new OpenPdfTextConverter()

  val usage = s"""
Sample usage:

 $$SPARK_HOME/bin/spark-submit
    --class ${SparkOcrJob.getClass.getName}
    --driver-memory 512m
    --executor-memory 341m
    --master spark://master:6066
    --deploy-mode cluster
    /vagrant/spark-ocr-assembly-0.1.jar
    hdfs://c7401:8020/user/root/*.pdf
"""

  def main(args: Array[String]): Unit = {
    if (args.length == 0) {
      println(usage)
      System.exit(1)
    }
    val hdfsPath = args(0)
    println(s"Converting PDF to text in ${hdfsPath}")
    val sparkContext = new SparkContext(sparkConf)
    sparkContext
      .binaryFiles(hdfsPath)
      .map(convertToString)
      .foreach(storeInSolr)
  }

  private def sparkConf = {
    new SparkConf()
      .setAppName("EDiscovery Spark OCR")
      .set("spark.executor.memory", "1g")
  }

  def convertToString(binaryFile: (String, PortableDataStream)): (String, String) = {
    val (name, content) = binaryFile
    val stream = content.open
    try {
      (name, ocr.recognize(read(stream)))
    } finally {
      stream.close
    }
  }

  private def read(stream: DataInputStream) = {
    val document: PDFDocument = new PDFDocument()
    document.load(stream)
    document
  }

  def storeInSolr(nameAndContent: (String, String)) = {
    val (name, content) = nameAndContent
    println(s"Storing ${name}..")
    val solr = new HttpSolrClient.Builder("http://c7401:8886/solr/documents").build // TODO read from command line
    try {

      val document = new SolrInputDocument
      document.addField("filename", name)
      document.addField("text", content)
      println(solr.add(document))
      solr.commit()
    } finally {
      solr.close()
    }
  }
}
