package org.drizzle.drizzle;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static org.drizzle.drizzle.NetworkConnector.*;


/**
 * 网络响应解释类
 * Created by ${XYY} on ${2015/11/20}.
 */

public class DrizzleFetchr {
    private static final String TAG = "DrizzleFetchr";

    private static final String REDIRECT_URI = "drizzle://phone-callback";
    private static final String CLIENT_ID = "40f5aec15cfbe1007abd9d3f8eb8824f66a6f435a4752be54ba921d80a4be32b";
    private static final String CLIENT_SECRET = "e12dc87db665db829c122029786b633aacb472d2e34bb7ea68895a0a84f8cce1";
    private static final String READ_ONLY_ACCESS_TOKEN = "a1a773a58a985f27a95b16aa9d8c31e46b13b46e0131bbfeadba1f68d6f5b80e";

    private static String AUTHORIZED_ACCESS_TOKEN = null;
    private static User AuthorizedUser = null;
    private static List<BucketItem> CachedBuckets = null;

    private static final int per_page = 12;

    static int perPage() {
        return per_page;
    }

    List<BucketItem> getCachedBuckets() {
        return CachedBuckets;
    }

    void setAccseeToken(String accessToken) {
        AUTHORIZED_ACCESS_TOKEN = accessToken;
    }

    User getAuthorizedUser() {
        return AuthorizedUser;
    }

    void setAuthorizedUser(String authorizedUserJsonString) {
        try {
            AuthorizedUser = new User();
            parseUser(AuthorizedUser, new JSONObject(authorizedUserJsonString));
        } catch (JSONException e) {
            Log.e(TAG, "Fail to parse JSON!");
        }
    }

    String getAuthorizeUrl(String state) {
        return Uri.parse("https://dribbble.com/oauth/authorize")
                .buildUpon()
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("redirect_uri", REDIRECT_URI)
                .appendQueryParameter("scope", "public write")
                .appendQueryParameter("state", state)
                .build().toString();
    }

    /**
     * 根据Code交换AccessToken
     */
    boolean exchangeAccessToken(String code, Context context) {
        try {
            String respond = postUrlString("https://dribbble.com/oauth/token", "client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&code=" + code + "&redirect_uri=" + REDIRECT_URI);
            Log.i(TAG, respond);
            JSONObject jsonObject = new JSONObject(respond);
            String access_token = jsonObject.optString("access_token", null);
            if (access_token == null) {
                Log.e(TAG, "ERROR: " + jsonObject.optString("error_description"));
            } else {
                //成功获得Access Token
                //TODO:Save Access Token
                DataPreference.setAuthorizedAccessToken(context, access_token);
                AUTHORIZED_ACCESS_TOKEN = access_token;
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network Connection ERROR!");
        } catch (JSONException e) {
            Log.e(TAG, "Fail to parse JSON!");
        }
        return false;
    }

    /**
     * 获取特定用户的信息
     */
    @Nullable
    User fetchUser(String userID) {
        User items = null;
        try {
            String url = Uri.parse("https://api.dribbble.com/v1/users/")
                    .buildUpon()
                    .appendPath(userID)
                    .appendQueryParameter("access_token", READ_ONLY_ACCESS_TOKEN)
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            items = new User();
            parseUser(items, new JSONObject(jsonString));
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return items;
    }

    /**
     * 获取登录用户的信息
     */
    @Nullable
    User fetchAuthorizedUser(Context context) {
        if (userNotAuthorized()) return null;

        User user = null;

        String url = Uri.parse("https://api.dribbble.com/v1/user")
                .buildUpon()
                .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                .build()
                .toString();

        try {
            String respond = getUrlString(url);
            Log.i(TAG, respond);
            user = new User();
            parseUser(user, new JSONObject(respond));
            DataPreference.setUserData(context, respond);
        } catch (IOException e) {
            Log.e(TAG, "Network Connection ERROR!");
        } catch (JSONException e) {
            Log.e(TAG, "Fail to parse JSON!");
        }
        AuthorizedUser = user;
        return user;
    }

    /**
     * 获取最近的Shot
     */
    @Nullable
    List<Shot> fetchShots(String pageNum) {
        List<Shot> shots = null;

        try {
            String url = Uri
                    .parse("https://api.dribbble.com/v1/")
                    .buildUpon()
                    .build()
                    .buildUpon()
                    .appendPath("shots")
                    .appendQueryParameter("access_token", READ_ONLY_ACCESS_TOKEN)
                    .appendQueryParameter("per_page", "12")
                    .appendQueryParameter("page", pageNum)
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            shots = new ArrayList<>();
            parseShots(shots, jsonString);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return shots;
    }

    /**
     * 获取指定的Shot
     */
    @Nullable
    Shot fetchShot(String shotId) {
        Shot shot = null;

        try {

            String url = Uri
                    .parse("https://api.dribbble.com/v1/")
                    .buildUpon()
                    .appendPath("shots")
                    .appendPath(shotId)
                    .appendQueryParameter("access_token", READ_ONLY_ACCESS_TOKEN)
                    .build()
                    .toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            shot = new Shot();
            parseShot(shot, new JSONObject(jsonString));
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
            return null;
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return shot;
    }

    /**
     * 获取指定用户的Shots
     */
    @Nullable
    List<Shot> fetchShotsOfUser(String userId, String page) {
        List<Shot> shots = null;
        try {
            String url = Uri.parse("https://api.dribbble.com/v1/users/")
                    .buildUpon()
                    .appendPath(userId)
                    .appendPath("shots")
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("per_page", "12")
                    .appendQueryParameter("access_token", READ_ONLY_ACCESS_TOKEN)
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON:" + jsonString);
            shots = new ArrayList<>();
            parseShots(shots, jsonString);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return shots;
    }

    /**
     * 获取指定Bucket的Shots
     */
    @Nullable
    List<Shot> fetchShotsOfBucket(String bucketId, String page) {
        return fetchShotsOfBucket(bucketId, String.valueOf(page), String.valueOf(per_page));
    }

    /**
     * 获取指定Bucket的第一张Shot
     */
    @Nullable
    List<Shot> fetchSingleShotOfBucket(String bucketId) {
        return fetchShotsOfBucket(bucketId, "1", String.valueOf(per_page));
    }

    @Nullable
    private List<Shot> fetchShotsOfBucket(String bucketId, String page, String per_page) {
        List<Shot> shots = null;

        try {
            String url = Uri
                    .parse("https://api.dribbble.com/v1/buckets/")
                    .buildUpon()
                    .appendPath(bucketId)
                    .appendPath("shots")
                    .appendQueryParameter("access_token", READ_ONLY_ACCESS_TOKEN)
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("per_page", String.valueOf(per_page))
                    .build()
                    .toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            shots = new ArrayList<>();
            parseShots(shots, jsonString);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return shots;
    }

    /**
     * 获取指定Shot的评论
     */
    @Nullable
    List<Comment> fetchComments(String shotId, int page) {
        List<Comment> comments = null;

        try {

            String url = Uri
                    .parse("https://api.dribbble.com/v1/")
                    .buildUpon()
                    .build().buildUpon()
                    .appendPath("shots")
                    .appendPath(shotId)
                    .appendPath("comments")
                    .appendQueryParameter("page", page + "")
                    .appendQueryParameter("per_page", "12")
                    .appendQueryParameter("access_token", READ_ONLY_ACCESS_TOKEN)
                    .build()
                    .toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            comments = new ArrayList<>();
            parseComments(comments, jsonString);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return comments;
    }

    /**
     * 获取登录用户的Buckets
     */
    @Nullable
    List<BucketItem> fetchMyBuckets(String page) {
        if (userNotAuthorized()) return null;

        List<BucketItem> bucketItems = null;

        String url = Uri.parse("https://api.dribbble.com/v1/user/buckets")
                .buildUpon()
                .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                .appendQueryParameter("page", page)
                .appendQueryParameter("per_page", "12")
                .build()
                .toString();
        try {
            String respond = getUrlString(url);
            Log.i(TAG, respond);
            bucketItems = new ArrayList<>();
            parseBuckets(bucketItems, respond);
            if (page.equals("1"))
                CachedBuckets = bucketItems;
            else
                CachedBuckets.addAll(bucketItems);
        } catch (IOException e) {
            Log.e(TAG, "Network Connection ERROR!");
        } catch (JSONException e) {
            Log.e(TAG, "Fail to parse JSON!");
        }

        return bucketItems;
    }

    /**
     * 获取指定用户正在关注的用户
     */
    @Nullable
    List<User> fetchFollowings(String userId, String pageNum) {
        List<User> followings = null;
        try {
            String url = Uri.parse("https://api.dribbble.com/v1/users/")
                    .buildUpon()
                    .appendPath(userId)
                    .appendPath("following")
                    .appendQueryParameter("page", pageNum)
                    .appendQueryParameter("per_page", "12")
                    .appendQueryParameter("access_token", READ_ONLY_ACCESS_TOKEN)
                    .build().toString();
            String jsonString = getUrlString(url);
            followings = new ArrayList<>();
            parseFollowings(followings, jsonString);
            Log.i(TAG, "Received JSON: following__" + jsonString);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return followings;
    }

    /**
     * 新建一个Bucket
     */
    boolean createBucket(String bucketName, String bucketDescription) {
        if (userNotAuthorized()) return false;

        boolean success = false;

        try {
            String respond = postUrlString("https://api.dribbble.com/v1/buckets", "name=" + bucketName + "&description=" + bucketDescription + "&access_token=" + AUTHORIZED_ACCESS_TOKEN);
            Log.i(TAG, respond);
            BucketItem bucketItem = new BucketItem();
            parseBucket(bucketItem, new JSONObject(respond));
            if (bucketItem.getName().equals(bucketName)) {
                success = true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network Connection ERROR!");
        } catch (JSONException e) {
            Log.e(TAG, "Fail to parse JSON!");
        }

        return success;
    }

    /**
     * 更新指定的Bucket
     */
    @Nullable
    BucketItem updateBucket(String bucketId, String name, String description) {
        if (userNotAuthorized()) return null;

        BucketItem bucketItem = null;

        String url = Uri
                .parse("https://api.dribbble.com/v1/buckets/")
                .buildUpon()
                .appendPath(bucketId)
                .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                .build()
                .toString();
        //FIXME: 过滤字符串
        String content = bucketJson(name, Util.replaceBlank(description));
        Log.i(TAG, "Content: " + content);
        try {
            String jsonString = putUrlString(url, content);
            Log.i(TAG, "Received JSON: " + jsonString);
            bucketItem = new BucketItem();
            parseBucket(bucketItem, new JSONObject(jsonString));
        } catch (IOException ioe) {
            Log.e(TAG, "Network Connection ERROR!");
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON");
        }
        return bucketItem;
    }

    /**
     * 构建Bucket的JSONString
     */
    private static String bucketJson(String name, String description) {
        String jsonResult = "";//定义返回字符串
        try {
            JSONObject jsonObj = new JSONObject();//pet对象，json形式
            jsonObj.put("name", name);//向pet对象里面添加值
            jsonObj.put("description", description);
            jsonResult = jsonObj.toString();//生成返回字符串
        } catch (JSONException e) {
            Log.e(TAG, "Fail to Create Json.");
        }
        return jsonResult;
    }

    /**
     * 移除指定的Bucket
     */
    boolean deleteBucket(String bucketId) {
        if (userNotAuthorized()) return false;

        boolean success = false;
        String url = Uri
                .parse("https://api.dribbble.com/v1/buckets/")
                .buildUpon()
                .appendPath(bucketId)
                .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                .build()
                .toString();
        try {
            success = deleteUrl(url);
            Log.i(TAG, "Delete Success: " + success);
        } catch (IOException ioe) {
            Log.e(TAG, "Network Connection ERROR!");
        }
        return success;
    }

    /**
     * 将制定的Shot放入指定的Bucket
     */
    boolean putAShotIntoABucket(String bucketId, String shotId) {
        if (userNotAuthorized()) return false;

        boolean success = false;

        String url = Uri
                .parse("https://api.dribbble.com/v1/buckets/")
                .buildUpon()
                .appendPath(bucketId)
                .appendPath("shots")
                .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                .build()
                .toString();
        //FIXME: 过滤字符串
        String content = "{" +
                "\"shot_id\" : \"" + shotId + "\"" +
                "}";
        Log.i(TAG, "Content: " + content);
        try {
            String jsonString = putUrlString(url, content);
            Log.i(TAG, "Received JSON: " + jsonString);
            success = true;
        } catch (IOException ioe) {
            Log.e(TAG, "Network Connection ERROR!");
        }

        return success;
    }

    /**
     * 检查用户是否Like一个指定的Shot
     */
    boolean isLiked(String shotId) {
        if (userNotAuthorized()) return false;

        LruCache<String, Boolean> cache = Cache.ShotLikeCache;
        if (cache.snapshot().containsKey(shotId)) {
            return cache.get(shotId);
        }

        try {
            String url = Uri
                    .parse("https://api.dribbble.com/v1/")
                    .buildUpon()
                    .build()
                    .buildUpon()
                    .appendPath("shots")
                    .appendPath(shotId)
                    .appendPath("like")
                    .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                    .build().toString();

            String jsonString = getUrlString(url);
            new JSONObject(jsonString);

            Log.i(TAG, "liked shot: " + shotId);
            cache.put(shotId, true);
            return true;

        } catch (IOException e) {
            Log.i(TAG, "Unliked shot: " + shotId);
            cache.put(shotId, false);
            return false;
        } catch (JSONException e) {
            Log.i(TAG, "Unliked shot: " + shotId);
            cache.put(shotId, false);
            return false;
        }
    }

    /**
     * Like一个指定的Shot
     */
    boolean like(String shotId) {
        if (userNotAuthorized()) return false;

        try {
            String url = Uri
                    .parse("https://api.dribbble.com/v1/")
                    .buildUpon()
                    .build()
                    .buildUpon()
                    .appendPath("shots")
                    .appendPath(shotId)
                    .appendPath("like")
                    .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                    .build().toString();

            postUrlString(url, "");
            Log.i(TAG, "Like shot: " + shotId);
            Cache.ShotLikeCache.put(shotId, true);
            AuthorizedUser.setLikesCount(AuthorizedUser.getLikesCount() + 1);
            return true;
        } catch (IOException e) {
            Log.i(TAG, "Like shot: " + shotId);

            return false;
        }

    }

    /**
     * UnLike一个指定的Shot
     */
    boolean unlike(String shotId) {
        if (userNotAuthorized()) return false;

        try {
            String url = Uri
                    .parse("https://api.dribbble.com/v1/")
                    .buildUpon()
                    .build()
                    .buildUpon()
                    .appendPath("shots")
                    .appendPath(shotId)
                    .appendPath("like")
                    .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                    .build().toString();

            boolean bool = deleteUrl(url);

            if (bool) {
                Log.i(TAG, "Unlike shot: " + shotId);
            } else {
                Log.e(TAG, "Failed to unlike shot: " + shotId);
            }
            Cache.ShotLikeCache.put(shotId, false);
            AuthorizedUser.setLikesCount(AuthorizedUser.getLikesCount() - 1);
            return bool;
        } catch (IOException e) {
            Log.e(TAG, "Failed to unlike shot: " + shotId);

            return false;
        }
    }

    /**
     * 检查用户是否Like一个指定的Comment
     */
    boolean isLikeComment(String shotId, String commentId) {
        if (userNotAuthorized()) return false;

        LruCache<String, Boolean> cache = Cache.CommentLikeCache;
        if (cache.snapshot().containsKey(commentId)) {
            return cache.get(commentId);
        }

        try {
            String url = Uri
                    .parse("https://api.dribbble.com/v1/")
                    .buildUpon()
                    .build()
                    .buildUpon()
                    .appendPath("shots")
                    .appendPath(shotId)
                    .appendPath("comments")
                    .appendPath(commentId)
                    .appendPath("like")
                    .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                    .build().toString();

            String jsonString = getUrlString(url);
            new JSONObject(jsonString);

            Log.i(TAG, "liked comment: " + commentId);
            cache.put(shotId, true);
            return true;

        } catch (IOException e) {
            Log.i(TAG, "Unliked comment: " + commentId);
            cache.put(shotId, false);
            return false;
        } catch (JSONException e) {
            Log.i(TAG, "Unliked comment: " + commentId);
            cache.put(shotId, false);
            return false;
        }
    }

    /**
     * Like一个指定的Comment
     */
    boolean likeComment(String shotId, String commentId) {
        if (userNotAuthorized()) return false;

        try {
            String url = Uri
                    .parse("https://api.dribbble.com/v1/")
                    .buildUpon()
                    .build()
                    .buildUpon()
                    .appendPath("shots")
                    .appendPath(shotId)
                    .appendPath("comments")
                    .appendPath(commentId)
                    .appendPath("like")
                    .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                    .build().toString();

            postUrlString(url, "");

            Log.i(TAG, "like comment: " + commentId);
            Cache.CommentLikeCache.put(commentId, true);
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Unlike comment IO ERROR: " + commentId);
            return false;
        }
    }

    /**
     * UnLike一个指定的Comment
     */
    boolean unlikeComment(String shotId, String commentId) {
        if (userNotAuthorized()) return false;

        try {
            String url = Uri
                    .parse("https://api.dribbble.com/v1/")
                    .buildUpon()
                    .build()
                    .buildUpon()
                    .appendPath("shots")
                    .appendPath(shotId)
                    .appendPath("comments")
                    .appendPath(commentId)
                    .appendPath("like")
                    .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                    .build().toString();

            boolean bool = deleteUrl(url);

            if (bool) {
                Log.i(TAG, "Unlike comment: " + commentId);
                Cache.CommentLikeCache.put(commentId, false);
            } else {
                Log.e(TAG, "Failed to like comment: " + commentId);
            }

            return bool;
        } catch (IOException e) {
            Log.e(TAG, "Failed to unlike comment IO ERROR: " + commentId);
            return false;
        }
    }

    /**
     * 获取登录用户Like的Shots
     */
    @Nullable
    List<Shot> fetchLikes(int page) {
        if (userNotAuthorized()) return null;

        List<Shot> shots = null;

        try {
            String url = Uri
                    .parse("https://api.dribbble.com/v1/")
                    .buildUpon()
                    .build()
                    .buildUpon()
                    .appendPath("user")
                    .appendPath("likes")
                    .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                    .appendQueryParameter("per_page", "12")
                    .appendQueryParameter("page", String.valueOf(page))
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            shots = new ArrayList<>();
            parseLikes(shots, jsonString);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return shots;
    }

    /**
     * Follow指定用户
     */
    boolean putFollow(String userID) {
        if (userNotAuthorized()) return false;

        String url = Uri.parse("https://api.dribbble.com/v1/users/")
                .buildUpon()
                .appendPath(userID)
                .appendPath("follow")
                .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                .build().toString();
        try {
            NetworkConnector.putUrlString(url, "{}");
            AuthorizedUser.setFollowingNum(AuthorizedUser.getFollowingNum() + 1);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Unfollow指定用户
     */
    boolean deleteFollow(String userID) {
        if (userNotAuthorized()) return false;

        String url = Uri.parse("https://api.dribbble.com/v1/users/")
                .buildUpon()
                .appendPath(userID)
                .appendPath("follow")
                .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                .build().toString();

        try {
            if (!deleteUrl(url)) {
                Log.e("response", "delete error");
                return false;
            } else {
                Log.d("response", "delete success");
                AuthorizedUser.setFollowingNum(AuthorizedUser.getFollowingNum() - 1);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查登录用户是否Follow一个指定的用户
     */
    boolean IfFollowingUser(String userID) {
        String url = Uri.parse("https://api.dribbble.com/v1/user/")
                .buildUpon()
                .appendPath("following")
                .appendPath(userID)
                .appendQueryParameter("access_token", AUTHORIZED_ACCESS_TOKEN)
                .build().toString();
        try {
            getUrlString(url);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void parseUser(User items, JSONObject jsonBody)
            throws JSONException {

        items.setId(jsonBody.optString("id"));
        items.setName(jsonBody.getString("name"));
        items.setUserName(jsonBody.optString("username"));
        items.setIntroduce(jsonBody.getString("bio"));
        items.setAvatarUrl(jsonBody.getString("avatar_url"));
        items.setLocation(jsonBody.getString("location"));
        items.setFollowingNum(jsonBody.getInt("followings_count"));
        items.setFollowerNum(jsonBody.getInt("followers_count"));
        items.setLikesCount(jsonBody.optInt("likes_count"));
        items.setProjectsNum(jsonBody.getString("projects_count"));
        items.setShotsNum(jsonBody.getInt("shots_count"));

    }

    private void parseBucket(BucketItem bucketItem, JSONObject bucketJsonObject) throws JSONException {
        bucketItem.setId(bucketJsonObject.optString("id"));
        bucketItem.setName(bucketJsonObject.optString("name"));
        bucketItem.setDescription(bucketJsonObject.optString("description"));
        bucketItem.setShotsCount(bucketJsonObject.optString("shots_count"));
        bucketItem.setCreatedAt(bucketJsonObject.optString("created_at"));
        bucketItem.setUpdatedAt(bucketJsonObject.optString("updated_at"));
    }

    private void parseBuckets(List<BucketItem> bucketItems, String respond) throws JSONException {
        JSONArray bucketsJsonArray = new JSONArray(respond);
        for (int i = 0; i < bucketsJsonArray.length(); i++) {
            JSONObject bucketJsonObject = bucketsJsonArray.getJSONObject(i);

            BucketItem bucketItem = new BucketItem();
            parseBucket(bucketItem, bucketJsonObject);

            bucketItems.add(bucketItem);
        }
    }

    private void parseShot(Shot shot, JSONObject shotJsonObject) throws JSONException {

        shot.setId(shotJsonObject.getString("id"));
        shot.setTitle(shotJsonObject.getString("title"));
        shot.setViewsCount(shotJsonObject.getInt("views_count"));
        shot.setCommentsCount(shotJsonObject.getInt("comments_count"));
        shot.setLikesCount(shotJsonObject.getInt("likes_count"));

        shot.setDescription(shotJsonObject.getString("description"));
        shot.setDateCreated(shotJsonObject.getString("created_at"));
        shot.setHtmlUrl(shotJsonObject.getString("html_url"));
        shot.setBucketsCount(shotJsonObject.getInt("buckets_count"));

        JSONObject designerJsonObject = shotJsonObject.optJSONObject("user");
        if(designerJsonObject!=null) {
            User user = new User();
            parseUser(user, designerJsonObject);
            shot.setDesigner(user);
        }

        JSONObject imagesJsonObject = shotJsonObject.getJSONObject("images");
        shot.setUrls(new String[]{imagesJsonObject.getString("hidpi"),
                imagesJsonObject.getString("normal"), imagesJsonObject.getString("teaser")});

    }

    private void parseShots(List<Shot> shots, String jsonBody) throws IOException, JSONException {
        JSONArray shotsJsonArray = new JSONArray(jsonBody);

        for (int i = 0; i < shotsJsonArray.length(); i++) {
            JSONObject shotJsonObject = shotsJsonArray.getJSONObject(i);

            Shot shot = new Shot();
            parseShot(shot, shotJsonObject);

            shots.add(shot);
        }
    }

    private void parseComments(List<Comment> shots, String jsonBody) throws IOException, JSONException {
        JSONArray shotsJsonArray = new JSONArray(jsonBody);

        for (int i = 0; i < shotsJsonArray.length(); i++) {
            JSONObject shotJsonObject = shotsJsonArray.getJSONObject(i);

            Comment comment = new Comment();
            comment.setId(shotJsonObject.getString("id"));
            comment.setBody(shotJsonObject.getString("body"));
            comment.setLikeCount(shotJsonObject.getInt("likes_count"));
            comment.setCreateDate(shotJsonObject.getString("created_at"));

            JSONObject userJsonObject = shotJsonObject.getJSONObject("user");
            User user = new User();
            parseUser(user, userJsonObject);
            comment.setUser(user);

            shots.add(comment);
        }
    }

    private void parseLikes(List<Shot> shots, String jsonString) throws JSONException {

        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            JSONObject shotJsonObject = jsonObject.getJSONObject("shot");
            Shot shot = new Shot();
            parseShot(shot, shotJsonObject);
            shot.setLiked(true);

            shots.add(shot);
        }
    }

    private void parseFollowings(List<User> followings, String jsonBody) throws IOException, JSONException {
        JSONArray followingsJsonArray = new JSONArray(jsonBody);

        for (int i = 0; i < followingsJsonArray.length(); i++) {
            JSONObject followingJsonObject = followingsJsonArray.getJSONObject(i);

            JSONObject followeeJsonObject = followingJsonObject.getJSONObject("followee");
            User following = new User();
            parseUser(following, followeeJsonObject);

            followings.add(following);
        }
    }

    private boolean userNotAuthorized() {
        boolean unAuth = AUTHORIZED_ACCESS_TOKEN == null;
        if (unAuth)
            Log.e(TAG, "User Not Authorized.");
        return unAuth;
    }

}
