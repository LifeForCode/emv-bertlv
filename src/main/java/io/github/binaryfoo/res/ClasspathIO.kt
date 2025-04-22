package io.github.binaryfoo.res

import java.io.InputStream

object ClasspathIO {

    fun readLines(fileName: String): List<String> =
        try {
            open(fileName).bufferedReader().use {
                it.readLines()
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to read $fileName", e)
        }

    fun open(fileName: String): InputStream =
        this::class.java.classLoader.getResourceAsStream(fileName)
            ?: throw IllegalStateException("Cannot open file $fileName")
}
