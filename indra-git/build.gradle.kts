import net.kyori.indra.self.declarePlugin

dependencies {
  compileOnlyApi("org.checkerframework:checker-qual:3.10.0")
  api("org.eclipse.jgit:org.eclipse.jgit:5.10.0.+")
}

declarePlugin(
  id = "indra.git",
  mainClass = "git.GitPlugin",
  displayName = "Indra Git",
  description = "Expose the Git repository that may contain the active project",
  tags = listOf("vcs", "git", "indra")
)
