resolvers ++= Seq(
	"Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
	"embedemongo" at "http://oss.sonatype.org/content/repositories/snapshots",
  "spray domain.repo" at "http://domain.repo.spray.io",
  Resolver.url(
    "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
    Resolver.ivyStylePatterns),
  "spray nightlies" at "http://nightlies.spray.io"
    )

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")

logLevel := Level.Warn

resolvers +=  "Kamon Repository Snapshots"  at "http://snapshots.kamon.io"

addSbtPlugin("com.typesafe.sbt" % "sbt-aspectj" % "0.10.6")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.5.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.4")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.1.0")

addSbtPlugin("com.github.fedragon" % "sbt-todolist" % "0.6")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.10")

addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.5")

addSbtPlugin("com.thoughtworks.sbt-api-mappings" % "sbt-api-mappings" % "0.2.2")

addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.14")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.4")
