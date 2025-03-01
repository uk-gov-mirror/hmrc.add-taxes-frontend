import sbt._

private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-27"    % "4.1.0",
    "uk.gov.hmrc" %% "simple-reactivemongo"          % "7.31.0-play-27",
    "uk.gov.hmrc" %% "logback-json-logger"           % "5.1.0",
    "uk.gov.hmrc" %% "govuk-template"                % "5.65.0-play-27",
    "uk.gov.hmrc" %% "play-health"                   % "3.16.0-play-27",
    "uk.gov.hmrc" %% "play-ui"                       % "9.1.0-play-27",
    "uk.gov.hmrc" %% "http-caching-client"           % "9.3.0-play-27",
    "uk.gov.hmrc" %% "play-language"                 % "4.12.0-play-27",
    "uk.gov.hmrc" %% "play-partials"                 % "8.0.0-play-27"
  )

  abstract class TestDependencies(scope: String) {
    lazy val test : Seq[ModuleID] = Seq(
      "org.scalatest"           %% "scalatest"          % "3.0.9" % scope,
      "org.scalatestplus.play"  %% "scalatestplus-play" % "4.0.3" % scope,
      "org.pegdown"             %  "pegdown"            % "1.6.0" % scope,
      "org.jsoup"               %  "jsoup"              % "1.13.1" % scope,
      "com.typesafe.play"       %% "play-test"          % PlayVersion.current % scope,
      "org.mockito"             %  "mockito-core"       % "3.8.0" % scope,
      "org.scalacheck"          %% "scalacheck"         % "1.15.3" % scope,
      "com.github.tomakehurst"  % "wiremock-jre8"        % "2.27.2" % scope
      )
    }

  object Test extends TestDependencies("test")
  object ItTest extends TestDependencies("it")

  def apply(): Seq[ModuleID] = compile ++ Test.test ++ ItTest.test
}
