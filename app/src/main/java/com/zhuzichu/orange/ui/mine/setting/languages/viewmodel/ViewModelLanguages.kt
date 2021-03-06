package com.zhuzichu.orange.ui.mine.setting.languages.viewmodel

import androidx.lifecycle.MutableLiveData
import com.zhuzichu.base.ext.map
import com.zhuzichu.base.base.BaseViewModel
import com.zhuzichu.base.binding.BindingCommand
import com.zhuzichu.base.common.prefs.UserStorage
import com.zhuzichu.base.event.SingleLiveEvent
import com.zhuzichu.orange.BR
import com.zhuzichu.orange.R
import me.tatarka.bindingcollectionadapter2.itembindings.OnItemBindClass
import javax.inject.Inject

class ViewModelLanguages @Inject constructor(
    private val userStorage: UserStorage
) : BaseViewModel() {

    companion object {
        private const val LOCAL_EN = "en"
        private const val LOCAL_ZH = "zh"
        private const val LOCAL_AR = "ar"
    }

    val languagesChangeEvent = SingleLiveEvent<String>()

    val items = MutableLiveData<List<Any>>()

    fun loadSectionLable() {
        val locale = userStorage.locale
        items.value = listOf(
            ViewModelItemLanguage(this, "English", LOCAL_EN, locale == LOCAL_EN),
            ViewModelItemLanguage(this, "中文", LOCAL_ZH, locale == LOCAL_ZH),
            ViewModelItemLanguage(this, "عربى", LOCAL_AR, locale == LOCAL_AR)
        )
    }

    val itemBinding = OnItemBindClass<Any>().apply {
        map<ViewModelItemLanguage>(BR.item, R.layout.item_language)
    }

    val onClickNavigation = BindingCommand<Any>({
        back()
    })
}