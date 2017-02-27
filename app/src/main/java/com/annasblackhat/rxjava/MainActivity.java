package com.annasblackhat.rxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subscription = getRepositoryObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Repository>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: "+e.getMessage());
                        ((TextView)findViewById(R.id.textview)).setText(e.getMessage());
                    }

                    @Override
                    public void onNext(List<Repository> repositories) {
                        StringBuffer sb = new StringBuffer();
                        for(Repository repo : repositories){
                            sb.append(repo.getName());
                            sb.append("\n");
                            sb.append(repo.getHtmlUrl());
                            sb.append("\n\n");
                        }
                        ((TextView)findViewById(R.id.textview)).setText(sb.toString());
                    }
                });
    }

    private Observable<List<Repository>> getRepositoryObservable(){
        return Observable.defer(new Func0<Observable<List<Repository>>>() {
            @Override
            public Observable<List<Repository>> call() {
                try {
                    return Observable.just(getRepository());
                } catch (IOException e) {
                   return null;
                }
            }
        });
    }

    @Nullable
    public List<Repository> getRepository() throws IOException{
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.github.com/users/annasblackhat/repos")
                .build();
        Response response = client.newCall(request).execute();

        if(response.isSuccessful()){
            List<Repository> repositories = new Gson().fromJson(response.body().charStream(), new TypeToken<List<Repository>>(){}.getType());
            return repositories;
        }
        return null;
    }

}
