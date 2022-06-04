package com.yc.lifehelper.application;

import android.content.Context;
import android.util.Log;

import com.yc.appstart.AppTaskDispatcher;
import com.yc.library.base.app.LibApplication;
import com.yc.localelib.service.LocaleService;
import com.yc.tracesdk.TimeTrace;
import com.yc.tracesdk.TimeTraceFactory;

import com.yc.ycthreadpoollib.ScheduleTask;


/**
 * <pre>
 *     @author      杨充
 *     blog         https://www.jianshu.com/p/53017c3fc75d
 *     time         2015/08/22
 *     desc         Application
 *     revise
 *     GitHub       https://github.com/yangchong211
 * </pre>
 */
public class MainApplication extends LibApplication {



    /**
     * 程序启动的时候执行
     */
    @Override
    public void onCreate() {
        Log.d("Application : ", "onCreate");
        super.onCreate();
        TimeTrace timeTrace = TimeTraceFactory.countByNanoseconds("MainApplication");
        timeTrace.step("start onCreate: %s");
        //执行task任务
        AppTaskDispatcher.create()
                .setShowLog(true)
                .addAppStartTask(new AppCoreTask())
                .addAppStartTask(new AppMonitorTask())
                .addAppStartTask(new AppDelayTask())
                .addAppStartTask(new AppThreadTask())
                .addAppStartTask(new AppLazyTask())
                .start()
                .await();
        timeTrace.step("App Task Dispatcher: %s");
        timeTrace.end();
        ScheduleTask.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                //创建一个核心线程池管理线程

            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        // 绑定语种
        super.attachBaseContext(LocaleService.getInstance().attachBaseContext(base));
    }


}


