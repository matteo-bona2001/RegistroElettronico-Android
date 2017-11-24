package com.sharpdroid.registroelettronico.api.spaggiari.v2

import android.util.Log
import android.util.SparseArray
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sharpdroid.registroelettronico.Info.API_URL
import com.sharpdroid.registroelettronico.api.spaggiari.v2.deserializer.DateDeserializer
import com.sharpdroid.registroelettronico.api.spaggiari.v2.deserializer.LongDeserializer
import com.sharpdroid.registroelettronico.database.entities.LoginRequest
import com.sharpdroid.registroelettronico.database.entities.LoginResponse
import com.sharpdroid.registroelettronico.database.entities.Profile
import com.sharpdroid.registroelettronico.database.room.DatabaseHelper
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class Spaggiari(val profile: Profile?) {

    companion object {
        private val cache = SparseArray<SpaggiariAPI>()
    }

    private val loginInterceptor by lazy {
        Interceptor { chain ->
            val original = chain.request()
            if (profile != null && original.url().toString() != API_URL + "auth/login" && profile.expire.time < System.currentTimeMillis()) {
                Log.d("LOGIN INTERCEPTOR", "TOKEN EXPIRED, REQUESTING NEW TOKEN")

                val loginRes = chain.proceed(original.newBuilder()
                        .url(API_URL + "auth/login")
                        .method("POST",
                                RequestBody.create(
                                        MediaType.parse("application/json"),
                                        LoginRequest(profile.password, profile.username, profile.ident).toString() //properly override to provide a json-like string
                                )
                        )
                        .header("User-Agent", "zorro/1.0")
                        .header("Z-Dev-Apikey", "+zorro+")
                        .build())

                if (loginRes.isSuccessful) {
                    val loginResponse = Gson().fromJson(loginRes.body()?.string(), LoginResponse::class.java)

                    Log.d("LOGIN INTERCEPTOR", "UPDATE TOKEN: " + loginResponse.token)

                    profile.expire = loginResponse.expire ?: throw IllegalStateException("Cannot achieve expire.time from:\n${loginRes.body()?.string()}")
                    profile.token = loginResponse.token ?: throw IllegalStateException("Cannot achieve token from:\n${loginRes.body()?.string()}")

                    DatabaseHelper.database.profilesDao().update(profile)
                }
            }
            try {
                return@Interceptor chain.proceed(original)
            } catch (err: Error) {
                return@Interceptor null
            }
        }
    }

    private val zorro by lazy {
        Interceptor { chain ->
            val original = chain.request()

            val request = original.newBuilder()
                    .header("User-Agent", "zorro/1.0")
                    .header("Z-Dev-Apikey", "+zorro+")
                    .header("Z-Auth-Token", profile?.token.orEmpty())
                    .method(original.method(), original.body())
                    .url(original.url().toString().replace("%7BstudentId%7D", profile?.id.toString()))
                    .build()

            try {
                return@Interceptor chain.proceed(request)
            } catch (err: Error) {
                return@Interceptor null
            }
        }
    }

    private val okHttp by lazy {
        OkHttpClient.Builder()
                .addInterceptor(loginInterceptor)
                .addInterceptor(zorro)
                .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create(
                        GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                                .registerTypeAdapter(Date::class.java, DateDeserializer())
                                .registerTypeAdapter(Long::class.java, LongDeserializer())
                                .create()))

                .baseUrl(API_URL)
                .client(okHttp)
                .build()
    }

    fun api(): SpaggiariAPI {
        if (cache.get(profile?.id?.toInt() ?: 0, null) != null) return cache[profile?.id?.toInt() ?: 0]
        val retrofit = retrofit.create(SpaggiariAPI::class.java)
        cache.put(profile?.id?.toInt() ?: 0, retrofit)
        return retrofit
    }
}