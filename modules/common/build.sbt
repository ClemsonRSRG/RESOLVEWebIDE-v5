name := "RESOLVEWebIDE-common"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-feature" // Shows warnings in detail in the stdout
)

// Javac compiler warning
javacOptions ++= Seq(
  "-Xlint:unchecked",
  "-Xlint:deprecation",
  "-Xdiags:verbose"
)

libraryDependencies ++= Seq(
  "com.feth" %% "play-authenticate" % "0.7.1",
  "be.objectify" %% "deadbolt-java" % "2.4.4",
  "org.hibernate" % "hibernate-entitymanager" % "5.0.7.Final",
  "mysql" % "mysql-connector-java" % "5.1.38"
)

routesGenerator := InjectedRoutesGenerator