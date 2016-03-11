package com.lguipeng.notes.injector.module;

import android.content.Context;

import com.lguipeng.notes.injector.Activity;
import com.lguipeng.notes.injector.ContextLifeCycle;

import dagger.Module;
import dagger.Provides;

/**
 * 约定@Provides函数以provide作为前缀， @Module类以Module作为后缀。
 * Created by lgp on 2015/5/26.
 */
@Module
public class ActivityModule {
    private final android.app.Activity activity;
    public ActivityModule(android.app.Activity activity) {
        this.activity = activity;
    }

    @Provides @Activity
    android.app.Activity provideActivity() {
        return activity;
    }

    @Provides @Activity @ContextLifeCycle("Activity")
    Context provideContext() {
        return activity;
    }
}
