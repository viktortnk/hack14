import AssemblyKeys._ // put this at the top of the file

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case PathList(ps @ _*) if ps.last endsWith "ECLIPSEF.RSA" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "mailcap" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "plugin.properties" => MergeStrategy.concat
  case PathList(ps @ _*) if ps.last endsWith "pom.properties" => MergeStrategy.concat
  case PathList(ps @ _*) if ps.last endsWith "pom.xml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
  case x => old(x)
}
}
