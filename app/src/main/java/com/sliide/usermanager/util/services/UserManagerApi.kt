package com.sliide.usermanager.util.services

import com.google.gson.Gson
import com.sliide.usermanager.model.*
import com.sliide.usermanager.util.Constants
import com.sliide.usermanager.util.interfaces.ApiInterface
import com.sliide.usermanager.util.interfaces.StatusInterface
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException

class UserManagerApi(
    private val baseUrl: String,
    private val subscribeSchedule: Scheduler,
    private val observeSchedule: Scheduler
) {

    private val apiInterface by lazy { ClientService.create(baseUrl, ApiInterface::class.java) }
    private val compositeSubscription by lazy { CompositeDisposable() }

    /* Get users from last page */
    fun getLastPage(statusInterface: StatusInterface<UsersData>) {
        compositeSubscription.add(
            getUsers(1)
                .flatMap {
                    getUsers(it.meta.pagination.pages)
                }
                .subscribeOn(subscribeSchedule)
                .observeOn(observeSchedule)
                .subscribe(statusInterface::success) { throwable ->
                    val message = handleError(throwable)
                    statusInterface.fail(message)
                }
        )
    }

    private fun getUsers(page: Int): Single<UsersData> = apiInterface.getUsers(page)

    /* Create new user */
    fun createUser(
        name: String,
        email: String,
        gender: String,
        status: String,
        statusInterface: StatusInterface<CreateUserResponse>
    ) {
        compositeSubscription.add(
            apiInterface.createUser(
                Constants.ACCESS_TOKEN,
                CreateUser(
                    name,
                    email,
                    gender,
                    status
                )
            )
                .subscribeOn(subscribeSchedule)
                .observeOn(observeSchedule)
                .subscribe(statusInterface::success) { throwable ->
                    val message = handleError(throwable)
                    statusInterface.fail(message)
                }
        )
    }

    /* Remove user */
    fun removeUser(user: User, statusInterface: StatusInterface<Unit?>) {
        compositeSubscription.add(
            apiInterface.removeUser(
                Constants.ACCESS_TOKEN,
                user.id
            )
                .subscribeOn(subscribeSchedule)
                .observeOn(observeSchedule)
                .subscribe({ statusInterface.success(null) }, { throwable ->
                    val message = handleError(throwable)
                    statusInterface.fail(message)
                })
        )
    }

    /* Parse error from API */
    private fun handleError(throwable: Throwable): String {
        return (throwable as? HttpException)?.response()?.let { response ->
            response.errorBody()?.string()?.let { responseStr ->
                when (response.code()) {
                    400, 401 -> {
                        Gson().fromJson(responseStr, GeneralApiError::class.java).data.message
                    }
                    422 -> {
                        val apiError = Gson().fromJson(responseStr, InvalidDataApiError::class.java)
                        apiError.data.joinToString("\n") { apiErrorData ->
                            "${apiErrorData.field} ${apiErrorData.message}"
                        }
                    }
                    else -> null
                }
            }
        } ?: throwable.localizedMessage ?: "unknown error"
    }

    companion object {

        @Volatile
        private var INSTANCE: UserManagerApi? = null

        fun getInstance(
            baseUrl: String = Constants.BASE_URL,
            subscribeSchedule: Scheduler = Schedulers.io(),
            observeSchedule: Scheduler = AndroidSchedulers.mainThread()
        ): UserManagerApi {
            return INSTANCE ?: synchronized(this) {
                val instance = UserManagerApi(baseUrl, subscribeSchedule, observeSchedule)
                INSTANCE = instance
                instance
            }
        }
    }
}