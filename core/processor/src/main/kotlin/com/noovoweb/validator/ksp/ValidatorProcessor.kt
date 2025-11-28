package com.noovoweb.validator.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * KSP Processor for @Validated annotations.
 *
 * This is the entry point for the Kotlin Symbol Processing API.
 * It finds all classes annotated with @Validated and generates validators for them.
 *
 * Processing Steps:
 * 1. Find all @Validated classes
 * 2. Parse each class using AnnotationParser
 * 3. Generate validator code using ValidatorClassGenerator
 * 4. Write generated files
 */
class ValidatorProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {
    private val annotationParser = AnnotationParser(logger)
    private val fieldValidatorCodeGenerator = FieldValidatorCodeGenerator()
    private val validatorClassGenerator = ValidatorClassGenerator(fieldValidatorCodeGenerator)

    private var invoked = false

    /**
     * Process all @Validated annotations.
     *
     * @param resolver Provides access to compiler analysis
     * @return List of deferred symbols (symbols that couldn't be processed yet)
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Prevent multiple invocations in the same round
        if (invoked) {
            return emptyList()
        }
        invoked = true

        logger.info("ValidatorProcessor started")

        // Find all classes annotated with @Validated
        val validatedClasses =
            resolver
                .getSymbolsWithAnnotation("com.noovoweb.validator.Validated")
                .filterIsInstance<KSClassDeclaration>()

        if (!validatedClasses.iterator().hasNext()) {
            logger.info("No @Validated classes found")
            return emptyList()
        }

        var processedCount = 0
        var errorCount = 0
        val deferred = mutableListOf<KSAnnotated>()

        validatedClasses.forEach { classDeclaration ->
            try {
                // Skip if not valid yet (e.g., missing dependencies)
                if (!classDeclaration.validate()) {
                    logger.warn("Deferring validation for ${classDeclaration.qualifiedName?.asString()}")
                    deferred.add(classDeclaration)
                    return@forEach
                }

                // Only process data classes
                if (Modifier.DATA !in classDeclaration.modifiers) {
                    logger.error(
                        "@Validated can only be applied to data classes",
                        classDeclaration,
                    )
                    errorCount++
                    return@forEach
                }

                logger.info("Processing @Validated class: ${classDeclaration.qualifiedName?.asString()}")

                // Parse the class and extract validation validators
                val classInfo = annotationParser.parse(classDeclaration)
                if (classInfo == null) {
                    logger.warn("No validation validators found in ${classDeclaration.simpleName.asString()}")
                    return@forEach
                }

                // Generate validator code
                val fileSpec = validatorClassGenerator.generate(classInfo)

                // Write the generated file
                val dependencies =
                    Dependencies(
                        aggregating = true,
                        sources = arrayOf(classDeclaration.containingFile!!),
                    )

                codeGenerator.createNewFile(
                    dependencies = dependencies,
                    packageName = fileSpec.packageName,
                    fileName = fileSpec.name,
                ).use { outputStream ->
                    OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                        fileSpec.writeTo(writer)
                    }
                }

                logger.info("Generated validator: ${classInfo.validatorClassName}")
                processedCount++
            } catch (e: Exception) {
                logger.error(
                    "Failed to process ${classDeclaration.qualifiedName?.asString()}: ${e.message}",
                    classDeclaration,
                )
                logger.exception(e)
                errorCount++
            }
        }

        logger.info("ValidatorProcessor completed: $processedCount validators generated, $errorCount errors")

        return deferred
    }

    /**
     * Called when processing finishes.
     */
    override fun finish() {
        logger.info("ValidatorProcessor finished")
    }

    /**
     * Called when an error occurred during processing.
     */
    override fun onError() {
        logger.error("ValidatorProcessor encountered errors")
    }
}

/**
 * Provider for the ValidatorProcessor.
 *
 * This is discovered by KSP via Java ServiceLoader.
 * Must be registered in META-INF/services.
 */
class ValidatorProcessorProvider : SymbolProcessorProvider {
    /**
     * Create a new ValidatorProcessor instance.
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ValidatorProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options,
        )
    }
}
