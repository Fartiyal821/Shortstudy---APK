package com.example.data.model

enum class Category(val displayName: String, val slug: String, val iconRes: String) {
    ALL("All Topics", "all", "auto_awesome"),
    PYTHON("Python", "python", "terminal"),
    C_LANG("C Basics", "c", "code"),
    WEB_DEV("Web Dev", "web", "language"),
    ALGORITHMS("Algorithms", "algo", "account_tree"),
    ABOUT("About Platform", "about", "info");

    companion object {
        fun fromSlug(slug: String): Category {
            return entries.firstOrNull { it.slug.equals(slug, ignoreCase = true) } ?: ALL
        }

        fun fromLabel(label: String): Category {
            val lower = label.lowercase()
            return when {
                lower.contains("python") -> PYTHON
                lower.contains(" c ") || lower.startsWith("c ") || lower.contains("c-basics") || lower == "c" -> C_LANG
                lower.contains("web") || lower.contains("html") || lower.contains("css") || lower.contains("javascript") -> WEB_DEV
                lower.contains("algo") || lower.contains("data structure") || lower.contains("dsa") -> ALGORITHMS
                lower.contains("about") || lower.contains("short-study") -> ABOUT
                else -> ALL
            }
        }
    }
}
