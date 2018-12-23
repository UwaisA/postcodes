package com.laundrapp.postcodes

internal class Formatter(private val validator: Validator) {

    private val nonAlphanumeric = "[^a-zA-Z0-9]".toRegex()
    private val leadingTrailingNonAlphanumeric = "(^[^a-zA-Z0-9]+)|([^a-zA-Z0-9]+$)".toRegex()

    fun format(postcode: CursoredString): CursoredString {
        val postcodeUpperCase = postcode.string.toUpperCase()
        val postcodeTrimmed = postcodeUpperCase.replace(leadingTrailingNonAlphanumeric, "")
        if (validator.partialValidate(postcodeTrimmed)) return CursoredString(postcodeTrimmed, 0)
        val postcodeStripped = postcodeUpperCase.replace(nonAlphanumeric, "")
        if (validator.partialValidate(postcodeStripped)) return CursoredString(postcodeStripped, 0)

        val separatorLoc = findSeparatorLocation(postcodeStripped)
        if (separatorLoc != -1) {
            val postcodeDashSeparated = postcodeStripped.insert("-", separatorLoc)
            if (validator.partialValidate(postcodeDashSeparated)) return CursoredString(postcodeDashSeparated, 0)

            val postcodeSpaceSeparated = postcodeStripped.insert(" ", separatorLoc)
            if (validator.partialValidate(postcodeSpaceSeparated)) return CursoredString(postcodeSpaceSeparated, 0)
        }

        throw CouldNotFormatException()
    }

    /**
     * Assuming there is a need for a separator, find it's location
     * @return The index of the character after which a separator needs to be added
     *          or -1 if no separator position could be found
     */
    private fun findSeparatorLocation(postcode: String): Int {
        var start = 0
        var end = postcode.length - 1

        while (start <= end) {
            val middle = (start + end) / 2

            val partialValidateMiddle = validator.partialValidate(postcode.substring(0, middle))
            if (!partialValidateMiddle) {
                end = middle - 1
                continue
            }
            val partialValidateMiddlePlusOne = validator.partialValidate(postcode.substring(0, middle + 1))

            if (partialValidateMiddlePlusOne) {
                start = middle + 1
                continue
            }

            return middle
        }
        return -1
    }

    class CouldNotFormatException : RuntimeException()

    private fun String.insert(string: String, location: Int): String {
        return substring(0, location) + string + substring(location)
    }
}