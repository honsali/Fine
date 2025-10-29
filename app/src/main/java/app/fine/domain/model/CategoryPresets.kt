package app.fine.domain.model

object CategoryPresets {
    const val DefaultCategoryName: String = "Divers"

    val QuickCategoryNames: List<String> = listOf(
        "Nourriture",
        "Loyer",
        "Scolarité",
        "Carburant",
        "Payage",
        "Parking",
        "SDK",
        "NFK",
        "Aide",
        "Telephone",
        "Fibre",
        "EE",
        "Gaz",
        "Voiture",
        "Poche"
    )

    val ProtectedCategoryDisplayNames: List<String> =
        QuickCategoryNames + DefaultCategoryName

    val ProtectedCategoryNames: Set<String> =
        ProtectedCategoryDisplayNames.map { it.lowercase() }.toSet()

    fun isProtected(name: String): Boolean =
        ProtectedCategoryNames.contains(name.lowercase())
}
