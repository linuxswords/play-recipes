name := "play-recipes"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)     

play.Project.playScalaSettings

com.jamesward.play.BrowserNotifierPlugin.livereload