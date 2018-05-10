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

package org.apache.ediscovery.ocr

import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

import javax.imageio.ImageIO
import org.bytedeco.javacpp.lept.{PIX, pixDestroy, pixReadMem}
import org.bytedeco.javacpp.tesseract
import org.bytedeco.javacpp.tesseract.TessBaseAPI
import org.ghost4j.document.PDFDocument
import org.ghost4j.renderer.SimpleRenderer

class TesarrectOcr(dataPath: String, language: String, dpi: Int) extends OCR {
  private def createAPI = {
    val api = tesseract.TessBaseAPICreate
    if (tesseract.TessBaseAPIInit3(api, dataPath, language) != 0) {
      tesseract.TessBaseAPIDelete(api)
      throw new RuntimeException("Error while initializing Tesseract")
    }
    api
  }

  def recognize(document: PDFDocument): String = {
    val api = createAPI
    try {
      return renderImages(document).toArray
        .map(image => toText(image, api))
        .mkString("")
    } finally {
      api.End
    }
  }

  private def toText(image: AnyRef, api: TessBaseAPI) = {
    val pix: PIX = toPIX(image)
    try {
      api.SetImage(pix)
      api.GetUTF8Text.getString
    } finally {
      pixDestroy(pix)
    }
  }

  private def toPIX(image: AnyRef) = {
    val imageByteStream = new ByteArrayOutputStream()
    ImageIO.write(image.asInstanceOf[RenderedImage], "png", imageByteStream)
    pixReadMem(
      ByteBuffer.wrap(imageByteStream.toByteArray()).array(),
      ByteBuffer.wrap(imageByteStream.toByteArray()).capacity()
    )
  }

  private def renderImages(document: PDFDocument) = {
    val renderer: SimpleRenderer = new SimpleRenderer()
    renderer.setResolution(dpi)
    renderer.render(document)
  }
}
