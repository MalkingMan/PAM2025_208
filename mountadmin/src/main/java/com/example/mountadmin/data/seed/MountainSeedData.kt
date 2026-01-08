package com.example.mountadmin.data.seed

import com.example.mountadmin.data.model.HikingRoute
import com.example.mountadmin.data.model.Mountain

/**
 * Canonical seed dataset for Firestore collection `mountains`.
 *
 * IMPORTANT:
 * - Field names and types must match MountainRepository.createMountain() mapping.
 * - Do NOT include: Merbabu, Rinjani, Slamet, Semeru.
 * - Difficulty must be one of: Easy, Moderate, Difficult, Expert.
 */
object MountainSeedData {

    /**
     * Seed dataset for Firestore collection `mountains`.
     */
    fun build(): List<Mountain> = listOf(
        // HAPUS 16 gunung seed lama (Kerinci, Leuser, Sibayak, Dempo, Gede, Pangrango, Ciremai,
        // Sumbing, Sindoro, Lawu, Bromo, Ijen, Agung, Tambora, Klabat, Latimojong).
        // Dataset sekarang berisi hanya gunung tambahan yang baru.

        Mountain(
            name = "Gunung Prau",
            province = "Jawa Tengah",
            elevation = 2565,
            description = "Salah satu gunung paling populer untuk pendaki pemula-menengah. Terkenal sunrise dan hamparan savana di puncak (Dieng).",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Patak Banteng", difficulty = HikingRoute.DIFFICULTY_EASY, estimatedTime = "1-2 hari", distance = "±7-10 km PP"),
                HikingRoute(name = "Dwarawati", difficulty = HikingRoute.DIFFICULTY_MODERATE, estimatedTime = "1-2 hari", distance = "±9-12 km PP")
            )
        ),
        Mountain(
            name = "Gunung Andong",
            province = "Jawa Tengah",
            elevation = 1726,
            description = "Gunung favorit pendaki pemula untuk latihan. Jalur relatif pendek dengan puncak terbuka dan view pegunungan sekitar Magelang.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Sawit", difficulty = HikingRoute.DIFFICULTY_EASY, estimatedTime = "half-day", distance = "±4-6 km PP"),
                HikingRoute(name = "Pendem", difficulty = HikingRoute.DIFFICULTY_EASY, estimatedTime = "half-day", distance = "±4-6 km PP")
            )
        ),
        Mountain(
            name = "Gunung Ungaran",
            province = "Jawa Tengah",
            elevation = 2050,
            description = "Gunung dekat Semarang dengan jalur beragam. Cocok untuk pendaki yang ingin latihan dengan variasi hutan dan jalur terbuka.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Promasan", difficulty = HikingRoute.DIFFICULTY_MODERATE, estimatedTime = "1-2 hari", distance = "±10-14 km PP"),
                HikingRoute(name = "Perantunan", difficulty = HikingRoute.DIFFICULTY_MODERATE, estimatedTime = "1-2 hari", distance = "±10-14 km PP")
            )
        ),
        Mountain(
            name = "Gunung Papandayan",
            province = "Jawa Barat",
            elevation = 2665,
            description = "Gunung favorit di Garut dengan kawah aktif, hutan mati, dan area camp yang nyaman. Cocok untuk pemula dan keluarga.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Camp David (Cisurupan)", difficulty = HikingRoute.DIFFICULTY_EASY, estimatedTime = "1-2 hari", distance = "±8-12 km PP"),
                HikingRoute(name = "Tegal Alun", difficulty = HikingRoute.DIFFICULTY_MODERATE, estimatedTime = "2 hari", distance = "±10-14 km PP")
            )
        ),
        Mountain(
            name = "Gunung Batur",
            province = "Bali",
            elevation = 1717,
            description = "Salah satu sunrise trek paling populer di Indonesia. Jalur relatif pendek dengan pemandangan kaldera dan danau.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Toya Bungkah", difficulty = HikingRoute.DIFFICULTY_EASY, estimatedTime = "half-day", distance = "±6-8 km PP"),
                HikingRoute(name = "Pura Jati", difficulty = HikingRoute.DIFFICULTY_MODERATE, estimatedTime = "half-day", distance = "±7-9 km PP")
            )
        ),
        Mountain(
            name = "Gunung Arjuno",
            province = "Jawa Timur",
            elevation = 3339,
            description = "Gunung besar di Jatim dengan trek panjang, banyak pos, dan jalur punggungan. Sering dipaketkan dengan Welirang.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Tretes", difficulty = HikingRoute.DIFFICULTY_DIFFICULT, estimatedTime = "2-3 hari", distance = "±20-30 km PP"),
                HikingRoute(name = "Purwosari", difficulty = HikingRoute.DIFFICULTY_DIFFICULT, estimatedTime = "2-3 hari", distance = "±22-32 km PP")
            )
        ),
        Mountain(
            name = "Gunung Welirang",
            province = "Jawa Timur",
            elevation = 3156,
            description = "Satu massif dengan Arjuno. Dikenal dengan area belerang dan rute panjang yang membutuhkan stamina baik.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Tretes (via Arjuno-Welirang)", difficulty = HikingRoute.DIFFICULTY_EXPERT, estimatedTime = "2-3 hari", distance = "±24-34 km PP"),
                HikingRoute(name = "Lawang", difficulty = HikingRoute.DIFFICULTY_EXPERT, estimatedTime = "2-3 hari", distance = "±24-34 km PP")
            )
        ),
        Mountain(
            name = "Gunung Penanggungan",
            province = "Jawa Timur",
            elevation = 1653,
            description = "Gunung favorit untuk latihan pendakian di Jatim. Banyak situs sejarah dan jalur beragam, cocok untuk sunrise hike.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Tamiajeng", difficulty = HikingRoute.DIFFICULTY_EASY, estimatedTime = "half-day", distance = "±4-6 km PP"),
                HikingRoute(name = "Jolotundo", difficulty = HikingRoute.DIFFICULTY_MODERATE, estimatedTime = "half-day", distance = "±5-7 km PP")
            )
        ),
        Mountain(
            name = "Gunung Argopuro",
            province = "Jawa Timur",
            elevation = 3088,
            description = "Dikenal sebagai salah satu trek terpanjang di Jawa. Rute Baderan–Bremi populer dengan tantangan logistik.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Baderan → Bremi (lintas)", difficulty = HikingRoute.DIFFICULTY_EXPERT, estimatedTime = "4-6 hari", distance = "±50-70 km"),
                HikingRoute(name = "Bremi → Baderan (lintas)", difficulty = HikingRoute.DIFFICULTY_EXPERT, estimatedTime = "4-6 hari", distance = "±50-70 km")
            )
        ),
        Mountain(
            name = "Gunung Raung",
            province = "Jawa Timur",
            elevation = 3332,
            description = "Gunung dengan kaldera besar dan jalur punggungan yang menantang. Beberapa rute memerlukan pengalaman dan kewaspadaan tinggi.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Kalibaru", difficulty = HikingRoute.DIFFICULTY_EXPERT, estimatedTime = "2-4 hari", distance = "±24-40 km PP"),
                HikingRoute(name = "Sumberwringin", difficulty = HikingRoute.DIFFICULTY_EXPERT, estimatedTime = "2-4 hari", distance = "±24-40 km PP")
            )
        ),
        Mountain(
            name = "Gunung Inerie",
            province = "Nusa Tenggara Timur",
            elevation = 2245,
            description = "Gunung kerucut ikonik di Bajawa (Flores). Trek relatif pendek namun tanjakan tajam dengan batuan lepas.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Watumeze", difficulty = HikingRoute.DIFFICULTY_DIFFICULT, estimatedTime = "half-day", distance = "±6-8 km PP"),
                HikingRoute(name = "Tololela (varian)", difficulty = HikingRoute.DIFFICULTY_DIFFICULT, estimatedTime = "half-day", distance = "±6-10 km PP")
            )
        ),
        Mountain(
            name = "Gunung Marapi",
            province = "Sumatera Barat",
            elevation = 2891,
            description = "Salah satu gunung paling sering didaki di Sumbar. Jalur relatif singkat tetapi perlu memantau status aktivitas vulkanik.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Koto Baru", difficulty = HikingRoute.DIFFICULTY_MODERATE, estimatedTime = "1 hari", distance = "±8-12 km PP"),
                HikingRoute(name = "Batu Palano", difficulty = HikingRoute.DIFFICULTY_MODERATE, estimatedTime = "1 hari", distance = "±8-12 km PP")
            )
        ),
        Mountain(
            name = "Gunung Singgalang",
            province = "Sumatera Barat",
            elevation = 2877,
            description = "Gunung dengan danau kawah (Danau Dewi) dan trek hutan yang asri, populer di Sumbar.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Padang Panjang", difficulty = HikingRoute.DIFFICULTY_DIFFICULT, estimatedTime = "2 hari", distance = "±14-18 km PP"),
                HikingRoute(name = "Koto Baru", difficulty = HikingRoute.DIFFICULTY_DIFFICULT, estimatedTime = "2 hari", distance = "±14-18 km PP")
            )
        ),
        Mountain(
            name = "Gunung Talang",
            province = "Sumatera Barat",
            elevation = 2597,
            description = "Gunung api di Solok dengan view Danau Kembar. Jalur relatif singkat dengan tanjakan yang konsisten.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Aie Batumbuak", difficulty = HikingRoute.DIFFICULTY_MODERATE, estimatedTime = "1 hari", distance = "±8-12 km PP"),
                HikingRoute(name = "Bukit Sileh", difficulty = HikingRoute.DIFFICULTY_MODERATE, estimatedTime = "1 hari", distance = "±8-12 km PP")
            )
        ),
        Mountain(
            name = "Gunung Bawakaraeng",
            province = "Sulawesi Selatan",
            elevation = 2830,
            description = "Salah satu gunung paling populer di Sulsel. Jalur menanjak panjang, sering didaki untuk latihan dan camping.",
            imageUrl = "",
            routes = listOf(
                HikingRoute(name = "Lembanna (Malino)", difficulty = HikingRoute.DIFFICULTY_DIFFICULT, estimatedTime = "2 hari", distance = "±16-22 km PP"),
                HikingRoute(name = "Bilu (varian)", difficulty = HikingRoute.DIFFICULTY_EXPERT, estimatedTime = "2-3 hari", distance = "±20-26 km PP")
            )
        )
    )
}
