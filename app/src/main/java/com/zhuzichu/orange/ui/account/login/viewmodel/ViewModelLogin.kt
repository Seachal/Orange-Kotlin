package com.zhuzichu.orange.ui.account.login.viewmodel

import androidx.lifecycle.MutableLiveData
import com.uber.autodispose.android.lifecycle.autoDispose
import com.zhuzichu.base.base.BaseViewModel
import com.zhuzichu.base.binding.BindingCommand
import com.zhuzichu.base.ext.*
import com.zhuzichu.orange.R
import com.zhuzichu.orange.manager.AccountManager
import com.zhuzichu.orange.repository.NetRepository
import javax.inject.Inject

class ViewModelLogin @Inject constructor(
    private val netRepository: NetRepository,
    private val accountManager: AccountManager
) : BaseViewModel() {

    val username = MutableLiveData("18229858146")
    val password = MutableLiveData("18229858146")

    val onClickLogin = BindingCommand<Any>({
        netRepository.login(username.value!!, password.value!!.md5())
            .bindToSchedulers()
            .bindToException()
            .autoLoading(this)
            .autoDispose(lifecycleOwner)
            .subscribe({
                accountManager.isLogin.value = true
                accountManager.userInfo.value = it.data.userInfo
                it.data.token.toast()
                back()
            }, {
                handleThrowable(it)
            })
    })
}