name := "RESOLVEWebIDE-byDesign"

version := "1.0"

scalacOptions ++= Seq(
  "-feature", // Shows warnings in detail in the stdout
  "-language:reflectiveCalls"
)

// Javac compiler warning
javacOptions ++= Seq(
  "-Xlint:unchecked",
  "-Xlint:deprecation",
  "-Xdiags:verbose"
)

libraryDependencies ++= Seq(
  // Apache Commons CSV
  "org.apache.commons" % "commons-csv" % "1.4"
)

routesGenerator := InjectedRoutesGenerator