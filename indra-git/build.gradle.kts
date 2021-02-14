dependencies {
  compileOnlyApi("org.checkerframework:checker-qual:3.10.0")
  api("org.eclipse.jgit:org.eclipse.jgit:5.10.0.+")
}

indraPluginPublishing {
  plugin(
    "indra.git",
    "net.kyori.indra.git.GitPlugin",
    "Indra Git",
    "Expose the Git repository that may contain the active project",
    listOf("vcs", "git", "indra")
  )
}
