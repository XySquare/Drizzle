package org.drizzle.drizzle;

import android.support.v4.util.LruCache;

/**
 * Created by ttc on 2017/3/16.
 */

 class Cache {

     static LruCache<String, Boolean> ShotLikeCache = new LruCache<>(128);
     static LruCache<String, Boolean> CommentLikeCache = new LruCache<>(128);

}

