package net.bjoernpetersen.deskbot.view.property

import javafx.scene.control.Control
import mu.KotlinLogging
import net.bjoernpetersen.musicbot.api.config.Config
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.Validator

class ValidatorAdapter<T>(
    private val entry: Config.Entry<T>) : Validator<T> {

    private val logger = KotlinLogging.logger {}

    override fun apply(t: Control, u: T): ValidationResult {
        // TODO maybe expose checker from entry?
        val message = entry.checkError()
        return message?.let { ValidationResult.fromError(t, it) } ?: ValidationResult()
    }

}
