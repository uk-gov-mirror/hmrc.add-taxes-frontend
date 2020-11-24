/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.sa

import connectors.DataCacheConnector
import controllers.Assets.{OK, SEE_OTHER}
import controllers.ControllerSpecBase
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.sa.retryKnownFacts
import scala.concurrent.ExecutionContext.Implicits.global


class RetryKnownFactsControllerSpec extends ControllerSpecBase with MockitoSugar {

  val view: retryKnownFacts = injector.instanceOf[retryKnownFacts]
  val mockDataCacheConnector = mock[DataCacheConnector]

  def controller(pinInPostFeature: Boolean = true): RetryKnownFactsController = {
    new RetryKnownFactsController(
      frontendAppConfig,
      FakeAuthAction,
      FakeServiceInfoAction,
      mockDataCacheConnector,
      mcc,
      view
    ) {
      override val pinAndPostFeatureToggle = pinInPostFeature
    }
  }

  def viewAsString(): String =
    new retryKnownFacts(formWithCSRF, mainTemplate)(frontendAppConfig)(HtmlFormat.empty)(fakeRequest, messages).toString

  "TryPinInPost Controller" must {
    "return OK and the correct view for a GET when feature toggle is set to true" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to BTA homepage when feature toggle set to false" in {
      val result = controller(false).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(frontendAppConfig.getBusinessAccountUrl("home"))
    }

    "redirect to enter SaUTR page" in {
      val result = controller().onSubmit()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe routes.EnterSAUTRController.onPageLoad().url
    }
  }

}
