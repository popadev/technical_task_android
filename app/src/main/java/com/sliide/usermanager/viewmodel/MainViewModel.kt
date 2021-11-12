package com.sliide.usermanager.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.databinding.ObservableInt
import androidx.lifecycle.AndroidViewModel
import com.sliide.usermanager.R
import com.sliide.usermanager.model.CreateUserResponse
import com.sliide.usermanager.model.User
import com.sliide.usermanager.model.UsersData
import com.sliide.usermanager.util.interfaces.StatusInterface
import com.sliide.usermanager.util.services.UserManagerApi
import com.sliide.usermanager.view.adapters.UserAdapter

class MainViewModel(
    application: Application
) : AndroidViewModel(application), UserAdapter.OnLongClickListener {

    var view: View? = null
    var isLoading = ObservableInt(android.view.View.GONE)
    private val adapter = UserAdapter()
    private var users = ArrayList<User>()
    private val userManagerApi by lazy { UserManagerApi.getInstance() }

    init {
        adapter.onLongClickListener = this
        getUsers()
    }

    fun getAdapter(): UserAdapter {
        return adapter
    }

    /* Fetch users from API */
    fun getUsers() {
        // Check internet connection
        if (!isNetworkConnected()) {
            showError((getApplication() as Context).getString(R.string.no_internet), true)
            return
        }

        showLoading()

        // Call api to fetch users
        userManagerApi.getLastPage(object : StatusInterface<UsersData> {
            override fun success(response: UsersData) {
                updateUserList(response)
            }

            override fun fail(message: String) {
                showError(message, true)
            }

        })
    }

    /* Create user via API */
    fun createUser(name: String, email: String, gender: String, status: String) {
        // Check internet connection
        if (!isNetworkConnected()) {
            showError((getApplication() as Context).getString(R.string.no_internet), false)
            return
        }

        showLoading()

        // Call Api to create user
        userManagerApi.createUser(name, email, gender, status, object : StatusInterface<CreateUserResponse> {
            override fun success(response: CreateUserResponse) {
                addUserToList(response.data)
            }

            override fun fail(message: String) {
                showError(message, false)
            }

        })
    }

    /* Remove user via API */
    fun removeUser(user: User) {
        // Check internet connection
        if (!isNetworkConnected()) {
            showError((getApplication() as Context).getString(R.string.no_internet), false)
            return
        }

        showLoading()

        // Call Api to create user
        userManagerApi.removeUser(user, object : StatusInterface<Unit?> {
            override fun success(response: Unit?) {
                removeUserFromList(user)
            }

            override fun fail(message: String) {
                showError(message, false)
            }

        })
    }

    /* Check if internet connection exist */
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = (getApplication() as Context).getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }

    /* Show loading progress */
    private fun showLoading() {
        isLoading.set(android.view.View.VISIBLE)
    }

    /* Hide loading progress */
    private fun hideLoading() {
        isLoading.set(android.view.View.GONE)
    }

    /* Refresh user list with given data */
    private fun updateUserList(usersData: UsersData) {
        hideLoading()

        users = usersData.data.toCollection(ArrayList())

        if (users.isNotEmpty()) {
            with(adapter) {
                setUserList(users)
                notifyDataSetChanged()
            }
        }
    }

    /* Add user to list */
    private fun addUserToList(user: User) {
        hideLoading()

        users.add(user)

        with(adapter) {
            setUserList(users)
            notifyItemInserted(users.size - 1)
        }
        view?.scrollToBottom()
    }

    /* Remove user from list */
    private fun removeUserFromList(user: User) {
        hideLoading()

        val index = users.indexOf(user)
        users.remove(user)

        with(adapter) {
            setUserList(users)
            notifyItemRemoved(index)
        }
    }

    /* Show error message as alert */
    private fun showError(message: String, forceRetry: Boolean) {
        hideLoading()
        view?.showError(message, forceRetry)
    }

    override fun onLongClick(user: User) {
        view?.showDialogToRemoveUser(user)
    }

    interface View {
        fun scrollToBottom()
        fun showError(message: String, forceRetry: Boolean)
        fun showDialogToRemoveUser(user: User)
    }
}
