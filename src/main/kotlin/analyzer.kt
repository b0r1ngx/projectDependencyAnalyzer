import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.regex.Pattern

// TODO: Start video from ~ 10 minutes
// TODO: 1. Add features of how deep analyzer should show dependencies
// TODO: 1.1 Project dependencies to inner project files
// TODO: 1.2 1.1 + Dependencies that is used by third-party libraries
// TODO: 1.3 Std libs and deep of it
// TODO: 1.4 All of dependencies to the ground

// TODO: 2. Call some renderer library to show graph of dependencies
fun main() {
    val fileTree = mutableMapOf<String, MutableSet<String>>()
    val srcDirPath = Paths.get("./src/main")

    Files.walkFileTree(srcDirPath, object : FileVisitor<Path> {
        private val packagePattern = Pattern.compile("^package\\s+(.*)$")
        private val importPattern = Pattern.compile("^import\\s+(.*)$")

        override fun visitFile(file: Path?, p1: BasicFileAttributes?): FileVisitResult {
            if (file != null && file.toString().endsWith(".kt")) {
                var currentPackage: String? = null

                file.toFile().readLines().forEach { line ->
                    var matcher = packagePattern.matcher(line)
                    if (matcher.find()) currentPackage = matcher.group(1)
                    matcher = importPattern.matcher(line)

                    if (currentPackage != null && matcher.find()) {
                        val importedPackage = matcher.group(1)
                            .split('.')
                            .dropLast(1)
                            .joinToString(".")

                        fileTree.getOrPut(currentPackage!!) {
                            mutableSetOf()
                        }.add(importedPackage)
                    }
                }
            }
            return FileVisitResult.CONTINUE
        }

        override fun visitFileFailed(p0: Path?, p1: IOException?) = FileVisitResult.CONTINUE
        override fun postVisitDirectory(p0: Path?, p1: IOException?) = FileVisitResult.CONTINUE
        override fun preVisitDirectory(p0: Path?, p1: BasicFileAttributes?) = FileVisitResult.CONTINUE
    })

    // Print out the results
    fileTree.forEach { (pkg, imports) ->
        println("Package $pkg:")
        imports.forEach {
            println("\t imports $it")
        }
    }
}