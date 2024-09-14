package com.miekir.mvvm.task.net;

import com.miekir.mvvm.context.GlobalContext;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * @author : 詹子聪
 * 网络请求进度
 * @date : 2021/4/5 14:04
 */
public class NetProgress {

    public Interceptor getInter(Listener listener) {
        return new Interceptor(listener);
    }

    public static class Interceptor implements okhttp3.Interceptor {
//        final Listener progressListener = new Listener() {
//            @Override public void update(long bytesRead, long contentLength, boolean done) {
//                System.out.println(bytesRead);
//                System.out.println(contentLength);
//                System.out.println(done);
//                System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);
//            }
//        };

        private final Listener progressListener;

        public Interceptor(Listener listener) {
            this.progressListener = listener;
        }

        @NotNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .body(new NetProgress.ResponseBody(originalResponse.body(), progressListener))
                    .build();
        }
    }

    private static class ResponseBody extends okhttp3.ResponseBody {
        private final okhttp3.ResponseBody responseBody;
        private final Listener listener;
        private BufferedSource bufferedSource;

        public ResponseBody(okhttp3.ResponseBody responseBody, Listener listener) {
            this.responseBody = responseBody;
            this.listener = listener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(@NotNull Buffer sink, long byteCount) throws IOException {
                    final long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    GlobalContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                        }
                    });
                    return bytesRead;
                }
            };
        }
    }

    public interface Listener {
        void update(long bytesRead, long contentLength, boolean done);
    }
}

