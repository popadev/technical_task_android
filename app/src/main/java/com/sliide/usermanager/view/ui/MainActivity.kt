package com.sliide.usermanager.view.ui

import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sliide.usermanager.R
import com.sliide.usermanager.databinding.ActivityMainBinding
import com.sliide.usermanager.extension.isEmail
import com.sliide.usermanager.model.User
import com.sliide.usermanager.viewmodel.MainViewModel

class MainActivity : AppCompatActivity(), MainViewModel.View {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(MainViewModel::class.java)
        mainViewModel.view = this
        binding.mainVm = mainViewModel

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener {
            showDialogToCreateUser()
        }
    }

    private fun showDialogToCreateUser() {
        val alertDialog = MaterialAlertDialogBuilder(this)
            .setMessage(getString(R.string.add_user))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.ok), null)
            .setView(R.layout.add_user)
            .show()

        val nameEdit = alertDialog.findViewById<EditText>(R.id.edit_name)!!
        val emailEdit = alertDialog.findViewById<EditText>(R.id.edit_email)!!
        val maleRadioButton = alertDialog.findViewById<RadioButton>(R.id.radio_button_male)!!
        val activeCheckBox = alertDialog.findViewById<CheckBox>(R.id.check_active)!!
        val okButton = alertDialog.getButton(BUTTON_POSITIVE)
        okButton.isEnabled = false

        nameEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, star: Int, before: Int, count: Int) {
                okButton.isEnabled = nameEdit.text.isNotBlank() && emailEdit.text.toString().isEmail()
            }
        })
        emailEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, star: Int, before: Int, count: Int) {
                okButton.isEnabled = nameEdit.text.isNotBlank() && emailEdit.text.toString().isEmail()
            }
        })
        okButton.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val gender = if (maleRadioButton.isChecked) "male" else "female"
            val status = if (activeCheckBox.isChecked) "active" else "inactive"

            alertDialog.dismiss()

            mainViewModel.createUser(name, email, gender, status)
        }
    }

    override fun scrollToBottom() {
        binding.recyclerUsers.scrollToPosition(mainViewModel.getAdapter().itemCount - 1)
    }

    override fun showError(message: String, forceRetry: Boolean) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
            .setTitle(getString(R.string.error))
            .setMessage(message)
            .setPositiveButton(getString(if (forceRetry) R.string.retry else R.string.ok)) { _, _ ->
                if (forceRetry) {
                    mainViewModel.getUsers()
                }
            }
            .show()
    }

    override fun showDialogToRemoveUser(user: User) {
        MaterialAlertDialogBuilder(this)
            .setMessage(getString(R.string.remove_message))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                mainViewModel.removeUser(user)
            }
            .show()
    }
}