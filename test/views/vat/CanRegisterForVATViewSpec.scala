/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views.vat

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.vat.canRegisterForVAT

class CanRegisterForVATViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "canRegisterForVAT"

  def createView = () => canRegisterForVAT(frontendAppConfig)(HtmlFormat.empty)(fakeRequest, messages)

  "CanRegisterForVAT view" must {
    behave like normalPage(createView, messageKeyPrefix)

    "contain heading ID" in {
      val doc = asDocument(createView())
      doc.getElementsByTag("h1").attr("id") mustBe "can-register-for-vat"
    }

    "include paragraph 'Based on your answers, you can register for VAT using the online service.'" in {
      val doc = asDocument(createView())
      assertContainsText(doc, "Based on your answers, you can register for VAT using the online service.")
    }

    "Render the correct content" in {
      val doc = asDocument(createView())
      val view = doc.text()

      assertLinkById(
        doc,
        "continue",
        "Continue to VAT registration",
        "http://localhost:8080/portal/business-registration/select-taxes?lang=eng",
        "CanRegisterForVAT:Click:Continue"
      )
    }
  }
}
