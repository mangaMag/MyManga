package com.freddieptf.mangatest.api;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.freddieptf.mangatest.API_KEYS;
import com.freddieptf.mangatest.R;
import com.freddieptf.mangatest.beans.MangaDetailsObject;
import com.freddieptf.mangatest.data.Contract;
import com.freddieptf.mangatest.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by fred on 7/26/15.
 */
public class GetManga {

    HttpURLConnection httpURLConnection;
    BufferedReader bufferedReader;
    String LOG_TAG = getClass().getSimpleName();
    String result, mangaId, source;
    Context context;

    public GetManga(Context context){
        this.context = context;
    }

    public MangaDetailsObject getManga(final String mangaId, final String source){
        this.mangaId = mangaId;
        this.source = source;

        try {
            URL readerUrl = new URL
                    ("https://doodle-manga-scraper.p.mashape.com/mangareader.net/manga/" + mangaId + "/");
            URL foxUrl = new URL
                    ("https://doodle-manga-scraper.p.mashape.com/mangafox.me/manga/" + mangaId + "/");

            if(source.equals(context.getString(R.string.pref_manga_reader)))
                result = getResultString(readerUrl);
            else result = getResultString(foxUrl);

            if(result.isEmpty() || result.equals("")) throw new NullPointerException();
            Log.d(LOG_TAG, result);

            return processResults(result);
        } catch (MalformedURLException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }

        finally {
            if(httpURLConnection != null) httpURLConnection.disconnect();
            if(bufferedReader != null) try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public String getResultString(URL baseUrl){
        try {
            Utilities.Log(LOG_TAG, "Url: " + baseUrl);
            httpURLConnection = (HttpURLConnection)baseUrl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.addRequestProperty("X-Mashape-Key", API_KEYS.API_KEY);
            httpURLConnection.connect();

            int statusCode = httpURLConnection.getResponseCode();

            if(statusCode != 200) return "";

            Log.i(LOG_TAG, "Status Code: " + statusCode);

            InputStream inputStream = httpURLConnection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line);
            }
            if(stringBuilder.toString() == null || stringBuilder.toString().equals("null")
                    || stringBuilder.length() <= 10) return "";

            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, e.getMessage());
            return "";
        }

    }

    private MangaDetailsObject processResults(String string){
        String MANGA_NAME = "name",
                MANGA_AUTHOR = "author",
                MANGA_INFO = "info",
                MANGA_STATUS = "status",
                MANGA_COVER = "cover",
                MANGA_LAST_UPDATE = "lastUpdate",
                MANGA_CHAPTERS = "chapters";

        MangaDetailsObject mangaDetailsObject = new MangaDetailsObject();

        try {
            String name, status, info, cover, author, chapters, lastUpdate;
            JSONObject mainJsonObject = new JSONObject(string);

            name = mainJsonObject.getString(MANGA_NAME);
            mangaDetailsObject.setName(name);

            if(!mainJsonObject.has(MANGA_STATUS))status = "wakaranai";
            else status = mainJsonObject.getString(MANGA_STATUS);
            mangaDetailsObject.setStatus(status);


            if(!mainJsonObject.has(MANGA_INFO))info = "No description";
            else info = mainJsonObject.getString(MANGA_INFO);
            mangaDetailsObject.setInfo(info);


            if(!mainJsonObject.has(MANGA_AUTHOR))author = "Some Mangaka";
            else {
                try {
                    author = mainJsonObject.getJSONArray(MANGA_AUTHOR).getString(0);
                }catch(NullPointerException | ArrayIndexOutOfBoundsException | JSONException e){
                    try {
                        author = mainJsonObject.getJSONArray("artist").getString(0);
                    }catch (NullPointerException | ArrayIndexOutOfBoundsException | JSONException ex){
                        author = "Some Mangaka";
                    }
                }
            }
            mangaDetailsObject.setAuthor(author);


            if(!mainJsonObject.has(MANGA_LAST_UPDATE)) lastUpdate = "";
            else lastUpdate = mainJsonObject.getString(MANGA_LAST_UPDATE);
            mangaDetailsObject.setLastUpdate(lastUpdate);

            cover = mainJsonObject.getString(MANGA_COVER);
            chapters = mainJsonObject.getJSONArray(MANGA_CHAPTERS).toString();
            mangaDetailsObject.setCover(cover);
            mangaDetailsObject.setChapters(chapters);

            Log.d(LOG_TAG, "Manga Name: " + name);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mangaDetailsObject;

    }

    public static void insertToDb(Context context, MangaDetailsObject mangaDetailsObject, String mangaId, String source){
        Uri uri = Contract.MyManga.buildMangaWithNameUri(mangaDetailsObject.getName());
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if(cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.MyManga.COLUMN_MANGA_NAME, mangaDetailsObject.getName());
        contentValues.put(Contract.MyManga.COLUMN_MANGA_ID, mangaId);
        contentValues.put(Contract.MyManga.COLUMN_MANGA_AUTHOR, mangaDetailsObject.getAuthor());
        contentValues.put(Contract.MyManga.COLUMN_MANGA_INFO, mangaDetailsObject.getInfo());
        contentValues.put(Contract.MyManga.COLUMN_MANGA_COVER, mangaDetailsObject.getCover());
        contentValues.put(Contract.MyManga.COLUMN_MANGA_STATUS, mangaDetailsObject.getStatus());
        contentValues.put(Contract.MyManga.COLUMN_MANGA_CHAPTER_JSON, mangaDetailsObject.getChapters());
        contentValues.put(Contract.MyManga.COLUMN_MANGA_SOURCE, source);
        contentValues.put(Contract.MyManga.COLUMN_MANGA_LAST_UPDATE, mangaDetailsObject.getLastUpdate());

        JSONArray array;
        try {
            array = new JSONArray(mangaDetailsObject.getChapters());
            JSONObject object = array.getJSONObject(array.length() - 1);
            contentValues.put(Contract.MyManga.COLUMN_MANGA_LATEST_CHAPTER, object.getString("chapterId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        context.getContentResolver().insert(Contract.MyManga.CONTENT_URI, contentValues);

    }


}
