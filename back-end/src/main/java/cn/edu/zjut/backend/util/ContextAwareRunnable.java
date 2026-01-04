package cn.edu.zjut.backend.util;

import io.jsonwebtoken.Claims;

// 封装上下文传递工具（有些导入/组卷的任务，是异步的，会开启一个新的线程，UserContext会上下文丢失，这里封装一个上下文传递工具，从父线程中传claims到子线程）
public class ContextAwareRunnable implements Runnable {
    private final Runnable task;
    private final Claims claims;

    public ContextAwareRunnable(Runnable task) {
        this.task = task;
        this.claims = UserContext.getClaims();
    }

    @Override
    public void run() {
        try {
            if (claims != null) {
                UserContext.setClaims(claims);
            }
            task.run();
        } finally {
            UserContext.clear();
        }
    }
}
