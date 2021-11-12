package com.sliide.usermanager.util.interfaces

import com.sliide.usermanager.model.CreateUser
import com.sliide.usermanager.model.CreateUserResponse
import com.sliide.usermanager.model.UsersData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface ApiInterface {

    @GET("/public/v1/users")
    fun getUsers(@Query("page") page: Int): Single<UsersData>

    @Headers("Content-Type: application/json")
    @POST("/public/v1/users")
    fun createUser(@Header("Authorization") token: String, @Body body: CreateUser): Single<CreateUserResponse>

    @DELETE("/public/v1/users/{id}")
    fun removeUser(@Header("Authorization") token: String, @Path("id") userId: Int): Completable
}