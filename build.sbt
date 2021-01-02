import Dependencies._

ThisBuild / scalaVersion     := "2.12.11"
ThisBuild / version          := "0.2.0-SNAPSHOT"
ThisBuild / organization     := "com.github.tnielens"
ThisBuild / organizationName := "tnielens"

lazy val root = (project in file("."))
  .settings(
    name := "intervalops",
    libraryDependencies += scalaTest % Test
  )
