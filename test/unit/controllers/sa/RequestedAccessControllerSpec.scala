package controllers.sa


import controllers.Assets.{OK, SEE_OTHER}
import controllers.ControllerSpecBase
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation, status}
import play.twirl.api.HtmlFormat
import views.html.sa.requestedAccess

class RequestedAccessControllerSpec extends ControllerSpecBase with MockitoSugar {
  val view: requestedAccess = injector.instanceOf[requestedAccess]
  val btaOrigin: String = "bta-sa"

  def controller(pinInPostFeature: Boolean = true): RequestedAccessController = {
    new RequestedAccessController(
      FakeAuthAction,
      FakeServiceInfoAction,
      frontendAppConfig,
      mcc,
      view
    ){
      override val pinAndPostFeatureToggle: Boolean = pinInPostFeature
    }
  }

  def viewAsString(origin: String, returnUrl: Option[String] = None): String =
    new requestedAccess(mainTemplate)(frontendAppConfig, origin)(HtmlFormat.empty)(fakeRequest, messages).toString

  "EnrolmentSuccess Controller" must {
    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(btaOrigin)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(btaOrigin)
    }

    "redirect to BTA home page when the toggle is set to false" in {
      val result = controller(pinInPostFeature = false).onPageLoad(btaOrigin)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(frontendAppConfig.getBusinessAccountUrl("home"))
    }
  }

}