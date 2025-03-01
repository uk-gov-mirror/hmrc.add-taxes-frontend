/*
 * Copyright 2021 HM Revenue & Customs
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

package utils

import config.FrontendAppConfig
import javax.inject.Inject
import models.sa.{KnownFacts, KnownFactsPostcode}
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import uk.gov.hmrc.play.validators.Validators.isPostcodeLengthValid

class KnownFactsFormValidator @Inject()() {

  protected val postCodeFormat = "(([gG][iI][rR] {0,}0[aA]{2})|((([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y]?[0-9][0-9]?)|(([a-pr-uwyzA-PR-UWYZ][0-9][a-hjkstuwA-HJKSTUW])|([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y][0-9][abehmnprv-yABEHMNPRV-Y]))) {0,}[0-9][abd-hjlnp-uw-zABD-HJLNP-UW-Z]{2}))"
  protected val ninoRegex = """(?i)(^$|^(?!BG|GB|KN|NK|NT|TN|ZZ)([A-Z]{2})[0-9]{6}[A-D]?$)"""

  def validatePostcode(postcodeKey: String, blankPostcodeMessageKey: String,
                       invalidPostcodeMessageKey: String)(implicit appConfig: FrontendAppConfig) = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]) = {
      val value = data.getOrElse(key, "")
      value match {
        case a if a.length == 0 => Left(Seq(FormError(postcodeKey, blankPostcodeMessageKey)))
        case a if a.length > 0 && !containsValidPostCodeCharacters(a) =>
          Left(Seq(FormError(postcodeKey, invalidPostcodeMessageKey)))
        case a if a.length > 0 && !knownFactsIsPostcodeLengthValid(a) =>
          Left(Seq(FormError(postcodeKey, invalidPostcodeMessageKey)))
        case a => Right(a)
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = {
      Map(key -> value.toString)
    }
  }

  def containsValidPostCodeCharacters(value: String): Boolean =
    postCodeFormat.r.findFirstIn(value).isDefined

  def knownFactsIsPostcodeLengthValid(value: String)(implicit appConfig: FrontendAppConfig): Boolean = {
    value.length <= appConfig.postcodeValidLength && isPostcodeLengthValid(value)
  }

  def ninoFormatter(ninoKey: String, blankMessageKey: String, lengthMessageKey: String, formatMessageKey: String)
                   (implicit appConfig: FrontendAppConfig): Formatter[String] =
    new Formatter[String] {
      override def bind(key: String, data: Map[String, String]) = {
        val value = data.getOrElse(key, "")
        lazy val valueMinusSpaces = value.replaceAll("\\s", "")
        value match {
          case n if n.isEmpty => Left(Seq(FormError(ninoKey, blankMessageKey)))
          case _ if valueMinusSpaces.length < appConfig.validationMinLengthNINO ||
            valueMinusSpaces.length > appConfig.validationMaxLengthNINO => Left(Seq(FormError(ninoKey, lengthMessageKey)))
          case _ if !valueMinusSpaces.matches(ninoRegex) => Left(Seq(FormError(ninoKey, formatMessageKey)))
          case n => Right(n)
        }
      }

      override def unbind(key: String, value: String): Map[String, String] = {
        Map(key -> value.toString)
      }
    }

  def stringFormatter
  (implicit appConfig: FrontendAppConfig): Formatter[String] =
    new Formatter[String] {
      override def bind(key: String, data: Map[String, String]) = {
        val value = data.getOrElse(key, "")
        value match {
          case "Y" => Right("Y")
        }
      }

      override def unbind(key: String, value: String): Map[String, String] = {
        Map(key -> value.toString)
      }
    }

}
