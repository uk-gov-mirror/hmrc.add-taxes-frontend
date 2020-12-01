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

import config.FrontendAppConfig
import controllers.actions.{AuthAction, ServiceInfoAction}
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.sa.groupIdError

import scala.concurrent.ExecutionContext

class GroupIdFoundController @Inject()(authenticate: AuthAction,
                                       serviceInfoData: ServiceInfoAction,
                                       appConfig: FrontendAppConfig,
                                       mcc: MessagesControllerComponents,
                                       groupIdError: groupIdError) extends FrontendController(mcc) with I18nSupport {

  implicit val ec: ExecutionContext = mcc.executionContext
  val pinAndPostFeatureToggle = appConfig.pinAndPostFeatureToggle

  def onPageLoad(): Action[AnyContent] = (authenticate andThen serviceInfoData) {
    implicit request =>
      if (pinAndPostFeatureToggle) {
        Ok(groupIdError(appConfig)(request.serviceInfoContent))
      } else {
        Redirect(Call("GET", appConfig.getBusinessAccountUrl("home")))
      }
  }

}
