resolvers += Resolver.url("scalasbt", new URL("https://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(
  Resolver.ivyStylePatterns
)

resolvers += Classpaths.sbtPluginReleases

resolvers += "jgit-repo".at("https://download.eclipse.org/jgit/maven")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
