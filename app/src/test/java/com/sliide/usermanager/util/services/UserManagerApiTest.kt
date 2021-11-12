package com.sliide.usermanager.util.services

import com.sliide.usermanager.model.CreateUserResponse
import com.sliide.usermanager.model.User
import com.sliide.usermanager.model.UsersData
import com.sliide.usermanager.util.interfaces.StatusInterface
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.then
import java.util.*

class UserManagerApiTest {

    private var mockWebServer = MockWebServer()
    private val mockUsersDataCallbacks: StatusInterface<UsersData> = mock()
    private val mockCreateUserCallbacks: StatusInterface<CreateUserResponse> = mock()
    private val mockRemoveUserCallbacks: StatusInterface<Unit?> = mock()
    private val target = UserManagerApi(
        mockWebServer.url("").toUrl().toString(),
        Schedulers.trampoline(),
        Schedulers.trampoline()

    )

    @Test
    fun `getLastPage should return error message when first page request is failed`() {
        target.getLastPage(mockUsersDataCallbacks)

        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        Thread.sleep(100)

        assertEquals(mockWebServer.takeRequest().path, "/public/v1/users?page=1")

        then(mockUsersDataCallbacks).should().fail("HTTP 500 Server Error")
    }

    @Test
    fun `getLastPage should return error message when last page request is failed`() {
        target.getLastPage(mockUsersDataCallbacks)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(readMockData("get_users_first_page_success.json"))
        )
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        Thread.sleep(100)

        assertEquals(mockWebServer.takeRequest().path, "/public/v1/users?page=1")
        assertEquals(mockWebServer.takeRequest().path, "/public/v1/users?page=72")

        then(mockUsersDataCallbacks).should().fail("HTTP 500 Server Error")
    }

    @Test
    fun `getLastPage should return users data when api calls are succeeded`() {
        target.getLastPage(mockUsersDataCallbacks)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(readMockData("get_users_first_page_success.json"))
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(readMockData("get_users_last_page_success.json"))
        )

        Thread.sleep(100)

        assertEquals(mockWebServer.takeRequest().path, "/public/v1/users?page=1")
        assertEquals(mockWebServer.takeRequest().path, "/public/v1/users?page=72")

        then(mockUsersDataCallbacks).should().success(any())
    }

    @Test
    fun `createUser should return error message when api call is failed`() {
        target.createUser(
            "test",
            "test@test.com",
            "male",
            "inactive",
            mockCreateUserCallbacks
        )

        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        Thread.sleep(100)

        assertEquals(mockWebServer.takeRequest().path, "/public/v1/users")

        then(mockCreateUserCallbacks).should().fail("HTTP 500 Server Error")
    }

    @Test
    fun `createUser should return user data when api call are succeeded`() {
        target.createUser(
            "test",
            "test@test.com",
            "male",
            "inactive",
            mockCreateUserCallbacks
        )

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(readMockData("create_user_success.json"))
        )

        Thread.sleep(100)

        assertEquals(mockWebServer.takeRequest().path, "/public/v1/users")

        then(mockCreateUserCallbacks).should().success(any())
    }

    @Test
    fun `removeUser should return error message when api call is failed`() {
        target.removeUser(
            User(
                1234,
                "test",
                "test@test.com",
                "male",
                "inactive"
            ),
            mockRemoveUserCallbacks
        )

        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        Thread.sleep(100)

        assertEquals(mockWebServer.takeRequest().path, "/public/v1/users/1234")

        then(mockRemoveUserCallbacks).should().fail("HTTP 500 Server Error")
    }

    @Test
    fun `removeUser should return user data when api call are succeeded`() {
        target.removeUser(
            User(
                1234,
                "test",
                "test@test.com",
                "male",
                "inactive"
            ),
            mockRemoveUserCallbacks
        )

        mockWebServer.enqueue(
            MockResponse().setResponseCode(204)
        )

        Thread.sleep(100)

        assertEquals(mockWebServer.takeRequest().path, "/public/v1/users/1234")

        then(mockRemoveUserCallbacks).should().success(null)
    }

    private fun readMockData(filename: String): String {
        val inputStream = javaClass.classLoader!!.getResourceAsStream(filename)
        val scanner = Scanner(inputStream, "UTF-8").useDelimiter("\\A")
        return scanner.next()
    }
}