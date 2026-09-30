package com.example.utils

data class SampleVideo(
    val title: String,
    val funnyPhrase: String,
    val url: String
)

object SampleData {
    val samples = listOf(
        SampleVideo(
            title = "Big Buck Bunny (4K 60FPS)",
            funnyPhrase = "🐇 Quantum Bunny Hop",
            url = "https://www.youtube.com/watch?v=aqz-KE-bpKQ"
        ),
        SampleVideo(
            title = "Blender Open Movie: Sintel",
            funnyPhrase = "🐉 Dragon Fire Symphony",
            url = "https://www.youtube.com/watch?v=eRsGyueVLvQ"
        ),
        SampleVideo(
            title = "Never Gonna Give You Up",
            funnyPhrase = "🕺 Cosmic Rickroll Matrix",
            url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        ),
        SampleVideo(
            title = "Lofi Hip Hop Beats to Relax",
            funnyPhrase = "☕ Cybernetic Chill Cat",
            url = "https://www.youtube.com/watch?v=jfKfPfyJRdk"
        ),
        SampleVideo(
            title = "Tears of Steel (Sci-Fi Short)",
            funnyPhrase = "🤖 Robo Laser Disco",
            url = "https://www.youtube.com/watch?v=R6MlUcmOul8"
        )
    )

    fun getRandomSample(): SampleVideo {
        return samples.random()
    }
}
