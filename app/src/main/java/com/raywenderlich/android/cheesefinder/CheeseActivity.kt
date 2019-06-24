/*
 * Copyright (c) 2019 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.cheesefinder

import android.text.Editable
import android.text.TextWatcher
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_cheeses.queryEditText

class CheeseActivity : BaseSearchActivity() {

  override fun onStart() {
    super.onStart()

    val searchTextObservable = createTextChangeObservable()

    searchTextObservable
        // 1 - Code should start on the Main thread since it works with View
        .subscribeOn(AndroidSchedulers.mainThread())
        // 2 - Show progress every time a new item is emitted
        .doOnNext { showProgress() }
        .observeOn(Schedulers.io())
        .map { cheeseSearchEngine.search(it) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          // 3 - and hide it when we're ready to display the result
          hideProgress()
          showResult(it)
        }
  }

  // 1 - Returns an observable, but instead of button clicks, it's text changes
  private fun createTextChangeObservable(): Observable<String> {
    // 2 - return the Observable that takes an ObservableOnSubscribe
    return Observable.create { emitter ->
      // 3 - when an Observer makes a subscription, first thing to do is create a TextWatcher
      val textWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable?) = Unit

        override fun beforeTextChanged(
          s: CharSequence?,
          start: Int,
          count: Int,
          after: Int
        ) = Unit

        // 4 - when the user types (and onTextChanged is [triggered]) we pass the new text value
        override fun onTextChanged(
          s: CharSequence?,
          start: Int,
          before: Int,
          count: Int
        ) {
          s?.toString()
              ?.let { emitter.onNext(it) }
        }
      }
      // 5 - we add the watcher to the TextView (Woah....)
      queryEditText.addTextChangedListener(textWatcher)

      // 6 - remove the watcher
      emitter.setCancellable {
        queryEditText.removeTextChangedListener(textWatcher)
      }
    }

  }


}
