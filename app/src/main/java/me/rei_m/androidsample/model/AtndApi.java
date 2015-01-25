package me.rei_m.androidsample.model;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import me.rei_m.androidsample.entity.AtndEvent;
import me.rei_m.androidsample.util.HttpAsyncLoader;

/**
 * Atnd APIを管理するモデル.
 * APIにリクエストを飛ばし、取得した結果をリストで保持して、取得完了のイベントを通知する
 *
 * Created by rei_m on 2015/01/25.
 */
public class AtndApi extends Observable
        implements LoaderManager.LoaderCallbacks<String>{



    public static AtndApi createInstance(){
        AtndApi instance = new AtndApi();
        instance.mList = new ArrayList<>();
        return instance;
    }

    private final int LOADER_ID = 1;

    private AtndApi(){}

    private Context mContext;
    private Loader mLoader;

    private List<AtndEvent> mList;

    public List<AtndEvent> getList(){
        return mList;
    }

    /**
     * Loaderの初期化を行い、APIへリクエストを飛ばす
     *
     * @param context Context
     * @param lm LoaderManager
     */
    public void initLoader(Context context, LoaderManager lm){
        mContext = context;
        mLoader = lm.initLoader(LOADER_ID, null, this);
        mLoader.forceLoad();
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        HttpAsyncLoader loader = new HttpAsyncLoader(mContext,
                "https://api.atnd.org/events/?keyword_or=google,cloud&format=json");
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {

        if(loader.getId() == LOADER_ID){
            try {
                // APIの取得結果をEventオブジェクトに変換して格納する
                JSONObject json = new JSONObject(data);
                int evCnt = json.getInt("results_returned");
                if(evCnt > 0){
                    JSONArray events = json.getJSONArray("events");
                    for(int i=0;i<evCnt;i++){
                        JSONObject ev = events.getJSONObject(i).getJSONObject("event");
                        AtndEvent atndEvent = new AtndEvent();
                        atndEvent.setId(ev.getString("event_id"));
                        atndEvent.setTitle(ev.getString("title"));
                        mList.add(atndEvent);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Observerに更新を通知する
        LoaderEvent event = new LoaderEvent(this);
        setChanged();
        notifyObservers(event);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

}