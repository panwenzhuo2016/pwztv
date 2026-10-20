package com.fongmi.android.tv.player;

import android.net.Uri;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Episode;
import com.fongmi.android.tv.bean.Flag;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.player.extractor.Force;
import com.fongmi.android.tv.player.extractor.Push;
import com.fongmi.android.tv.player.extractor.Strm;
import com.fongmi.android.tv.player.extractor.TVBus;
import com.fongmi.android.tv.player.extractor.Video;
import com.fongmi.android.tv.player.extractor.Youtube;
import com.fongmi.android.tv.exception.ExtractException;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Source {

    private final List<Extractor> extractors;

    private static class Loader {
        // 注册保留的播放地址解析器。
        static volatile Source INSTANCE = new Source();
    }

    public static Source get() {
        return Loader.INSTANCE;
    }

    // 注册保留的播放地址解析器。
    public Source() {
        extractors = new ArrayList<>();
        extractors.add(new Force());
        extractors.add(new Push());
        extractors.add(new Strm());
        extractors.add(new TVBus());
        extractors.add(new Video());
        extractors.add(new Youtube());
    }

    private Extractor getExtractor(Uri uri) {
        return extractors.stream().filter(extractor -> extractor.match(uri)).findFirst().orElse(null);
    }

    // 仅为需要展开的在线视频创建解析任务。
    private void addCallable(Iterator<Episode> iterator, List<Callable<List<Episode>>> items) {
        String url = iterator.next().getUrl();
        if (Youtube.Parser.match(url)) {
            items.add(Youtube.Parser.get(url));
            iterator.remove();
        }
    }

    public void parse(Vod vod) throws Exception {
        try (ExecutorService executor = Executors.newCachedThreadPool()) {
            for (Flag flag : vod.getFlags()) {
                List<Callable<List<Episode>>> items = new ArrayList<>();
                Iterator<Episode> iterator = flag.getEpisodes().iterator();
                // 仅为需要展开的在线视频创建解析任务。
                while (iterator.hasNext()) addCallable(iterator, items);
                for (Future<List<Episode>> future : executor.invokeAll(items, 30, TimeUnit.SECONDS)) flag.getEpisodes().addAll(future.get());
            }
        }
    }

    // 解析播放地址并明确拒绝已移除的协议。
    public String fetch(Result result) throws Exception {
        Uri uri = result.getUrl().uri();
        String url = result.getUrl().v();
        // 识别已移除的下载与P2P播放地址。
        if (PlaybackSupport.removedUrl(url)) throw new ExtractException(ResUtil.getString(R.string.error_p2p_removed));
        Extractor extractor = getExtractor(uri);
        if (extractor != null) result.setParse(0);
        if (extractor instanceof Video) result.setParse(1);
        return extractor == null ? url : extractor.fetch(url);
    }

    public void stop() {
        if (extractors == null) return;
        extractors.forEach(Extractor::stop);
    }

    public void exit() {
        if (extractors == null) return;
        App.execute(() -> extractors.forEach(Extractor::exit));
    }

    public interface Extractor {

        String fetch(String url) throws Exception;

        boolean match(Uri uri);

        void stop();

        void exit();
    }
}
