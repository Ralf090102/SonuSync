package com.example.sonusync.data.typeconverter

import androidx.room.TypeConverter
import com.example.sonusync.data.model.Music
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class MusicListTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromMusicList(musicList: List<Music>?): String? {
        return musicList?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toMusicList(musicListString: String?): List<Music>? {
        val listType = object : TypeToken<List<Music>>() {}.type
        return musicListString?.let { gson.fromJson(it, listType) }
    }
}