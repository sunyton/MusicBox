package com.sample.sunyton.musicbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sample.sunyton.musicbox.model.Audio;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Utils {

    private static final String AUDIO_INDEX = "audioIndex";
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private final String STORAGE = "com.sample.sunyton.musicbox.STORAGE";
    private final String AUDIO_LISTS = "audioLists";



    private Utils(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
    }

    public static Utils with(Context context) {
        return new Utils(context);
    }

    /**
     * 存取音乐对象集合
     * @param arrayList
     * @return
     */
    public Utils storeAudio(ArrayList<Audio> arrayList) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String json = new Gson().toJson(arrayList);
        editor.putString(AUDIO_LISTS, json);
        editor.apply();

        return this;
    }

    public ArrayList<Audio> loadAudio() {
        Gson gson = new Gson();
        String json = mSharedPreferences.getString(AUDIO_LISTS, null);
        Type type = new TypeToken<ArrayList<Audio>>() {
        }.getType();
        return gson.fromJson(json, type);/*type?*/

    }

    public Utils storeAudioIndex(int index) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(AUDIO_INDEX, index);
        editor.apply();

        return this;

    }

    public int loadAudioIndex() {
        return mSharedPreferences.getInt(AUDIO_INDEX, -1);

    }

    public Utils clear() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
        return this;
    }

}
