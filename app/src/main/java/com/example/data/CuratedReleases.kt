package com.example.data

data class ReleaseItem(
    val title: String,
    val platform: String, // "Netflix" or "Prime Video"
    val releaseDate: String,
    val imageUrl: String? = null,
    val genre: String,
    val synopsis: String,
    val durationOrSeasons: String
)

object CuratedReleases {
    val list = listOf(
        // NETFLIX
        ReleaseItem(
            title = "Wednesday (Season 2)",
            platform = "Netflix",
            releaseDate = "Scheduled: late 2026",
            imageUrl = "https://images.unsplash.com/photo-1509248961158-e54f6934749c?auto=format&fit=crop&w=400&q=80",
            genre = "Fantasy / Dark Comedy",
            synopsis = "Wednesday Addams returns to solve a new set of dark, supernatural mysteries at Nevermore Academy, full of quirky, gothic twists.",
            durationOrSeasons = "8 Episodes"
        ),
        ReleaseItem(
            title = "Squid Game (Season 2)",
            platform = "Netflix",
            releaseDate = "December 2024",
            imageUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?auto=format&fit=crop&w=400&q=80",
            genre = "Thriller / Drama",
            synopsis = "Three years after winning Squid Game, Player 456 remains determined to find those behind the deadly game and put an end to their sport.",
            durationOrSeasons = "6 Episodes"
        ),
        ReleaseItem(
            title = "Fallout (Season 1)",
            platform = "Prime Video",
            releaseDate = "April 2024",
            imageUrl = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?auto=format&fit=crop&w=400&q=80",
            genre = "Sci-Fi / Action",
            synopsis = "Based on one of the greatest video game franchises of all time, Fallout is the story of haves and have-nots in a world in which there’s almost nothing left to have.",
            durationOrSeasons = "8 Episodes"
        ),
        ReleaseItem(
            title = "The Boys (Season 4)",
            platform = "Prime Video",
            releaseDate = "June 2024",
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=400&q=80",
            genre = "Action / Dark Hero",
            synopsis = "The world is on the brink. Victoria Neuman is closer than ever to the Oval Office and under the muscled thumb of Homelander, who is consolidating his power.",
            durationOrSeasons = "8 Episodes"
        ),
        ReleaseItem(
            title = "Stranger Things (Season 5)",
            platform = "Netflix",
            releaseDate = "Expected: late 2026",
            imageUrl = "https://images.unsplash.com/photo-1542204172-e7052809a86e?auto=format&fit=crop&w=400&q=80",
            genre = "Sci-Fi / Adventure",
            synopsis = "The final battle begins. Eleven and the gang unite to destroy Vecna once and for all and seal the rift between Hawkins and the Upside Down.",
            durationOrSeasons = "8 Episodes"
        ),
        ReleaseItem(
            title = "The Lord of the Rings: The Rings of Power (Season 2)",
            platform = "Prime Video",
            releaseDate = "August 2024",
            imageUrl = "https://images.unsplash.com/photo-1461360228754-6e81c478b882?auto=format&fit=crop&w=400&q=80",
            genre = "Fantasy / Adventure",
            synopsis = "Sauron has returned. Cast out by Galadriel, without army or ally, the rising Dark Lord must now rely on his own cunning to rebuild his power.",
            durationOrSeasons = "8 Episodes"
        ),
        ReleaseItem(
            title = "3 Body Problem",
            platform = "Netflix",
            releaseDate = "March 2024",
            imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=400&q=80",
            genre = "Sci-Fi / Mystery",
            synopsis = "Across continents and decades, five brilliant friends make earth-shattering discoveries as the laws of science unravel and an existential threat emerges.",
            durationOrSeasons = "1 Season"
        ),
        ReleaseItem(
            title = "Road House",
            platform = "Prime Video",
            releaseDate = "March 2024",
            imageUrl = "https://images.unsplash.com/photo-1585647347483-22b66260dfff?auto=format&fit=crop&w=400&q=80",
            genre = "Action / Thriller",
            synopsis = "An ex-UFC fighter takes a job as a bouncer at a rough Florida Keys roadhouse, only to discover that this paradise is not all it seems.",
            durationOrSeasons = "121 Mins"
        ),
        ReleaseItem(
            title = "Bridgerton (Season 3)",
            platform = "Netflix",
            releaseDate = "May 2024",
            imageUrl = "https://images.unsplash.com/photo-1513151233558-d860c5398176?auto=format&fit=crop&w=400&q=80",
            genre = "Romance / Drama",
            synopsis = "As Penelope Featherington finally gives up on her long-held crush on Colin Bridgerton, she decides it's time to take a husband.",
            durationOrSeasons = "8 Episodes"
        ),
        ReleaseItem(
            title = "Mr. & Mrs. Smith",
            platform = "Prime Video",
            releaseDate = "February 2024",
            imageUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=400&q=80",
            genre = "Action / Comedy",
            synopsis = "Two lonely strangers land jobs with a mysterious spy agency that offers them a glamorous life of espionage, wealth, and world travel in Manhattan.",
            durationOrSeasons = "8 Episodes"
        ),
        ReleaseItem(
            title = "Glass Onion: A Knives Out Mystery",
            platform = "Netflix",
            releaseDate = "December 2022",
            imageUrl = "https://images.unsplash.com/photo-1605810230434-7631ac76ec81?auto=format&fit=crop&w=400&q=80",
            genre = "Mystery / Thriller",
            synopsis = "Famed Southern detective Benoit Blanc travels to Greece to peel back the layers of a mystery involving a tech billionaire and his eclectic crew of friends.",
            durationOrSeasons = "139 Mins"
        ),
        ReleaseItem(
            title = "Hazbin Hotel",
            platform = "Prime Video",
            releaseDate = "January 2024",
            imageUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=400&q=80",
            genre = "Animated / Fantasy",
            synopsis = "Charlie Morningstar, the princess of Hell, opens a rehabilitation hotel to help demons find redemption and reduce Hell's overpopulation humanely.",
            durationOrSeasons = "8 Episodes"
        )
    )
}
