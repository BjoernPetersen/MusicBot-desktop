package com.github.bjoernpetersen.deskbot.view.config


import com.github.bjoernpetersen.jmusicbot.config.Config
import com.github.bjoernpetersen.jmusicbot.config.ConfigListener
import java.lang.ref.WeakReference

class WeakConfigListener(private val entry: Config.ReadOnlyStringEntry, wrapped: ConfigListener<String?>) :
    ConfigListener<String?> {

  private val wrappedRef: WeakReference<ConfigListener<String?>> = WeakReference(wrapped)

  override fun invoke(oldValue: String?, newValue: String?) {
    val wrapped = wrappedRef.get()
    if (wrapped == null) {
      entry.removeListener(this)
    } else {
      wrapped(oldValue, newValue)
    }
  }
}
