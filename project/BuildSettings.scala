import sbt._
import sbt.Keys._

object BuildSettings {

  val liftVersion = SettingKey[String]("liftVersion", "Version number of the Lift Web Framework")
  val liftEdition = SettingKey[String]("liftEdition", "Lift Edition (short version number to append to artifact name)")

  val basicSettings = Defaults.defaultSettings ++ Seq(
    moduleName := "squerylauth",
    organization := "net.liftmodules",
    version := "0.1-SNAPSHOT",
    liftVersion <<= liftVersion ?? "2.6-M2",
    liftEdition <<= liftVersion apply { _.substring(0,3) },
    moduleName <<= (name, liftEdition) { (n, e) =>  n + "_" + e },
    scalaVersion := "2.10.4",
    crossScalaVersions := Seq("2.9.2", "2.9.1", "2.9.1-1", "2.10.4", "2.11.2"),
    scalacOptions <<= scalaVersion map { sv: String =>
      if (sv.startsWith("2.10."))
        Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions")
      else
        Seq("-deprecation", "-unchecked")
    }
  )

  val publishSettings = seq(
    resolvers += "CB Central Mirror" at "http://repo.cloudbees.com/content/groups/public",
    resolvers += "Sonatype OSS Release" at "http://oss.sonatype.org/content/repositories/releases",
    resolvers += "Sonatype Snapshot" at "http://oss.sonatype.org/content/repositories/snapshots",

    publishTo <<= version { _.endsWith("SNAPSHOT") match {
      case true  => Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
      case false => Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
    }},

    credentials += Credentials( file("sonatype.credentials") ),
    credentials += Credentials( file("/private/liftmodules/sonatype.credentials") ),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    pomExtra := (
      <url>https://github.com/jgenso/squeryl-auth</url>
      <licenses>
        <license>
            <name>Apache 2.0 License</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
            <distribution>repo</distribution>
          </license>
       </licenses>
       <scm>
          <url>git@github.com:jgenso/squeryl-auth-module.git</url>
          <connection>scm:git:git@github.com:jgenso/squeryl-auth-module.git</connection>
       </scm>
       <developers>
          <developer>
            <id>apolinar</id>
            <name>Apolinar Linares</name>
            <url>http://genso.com.bo/</url>
        </developer>
        <developer>
            <id>jgenso</id>
            <name>Juan Jose Olivera Rodriguez</name>
            <url>http://genso.com.bo/</url>
        </developer>
       </developers>
     )
  )
}
