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
import kotlinx.android.synthetic.main.activity_cheeses.searchButton
import java.util.concurrent.TimeUnit.MILLISECONDS

class CheeseActivity : BaseSearchActivity() {

  override fun onStart() {
    super.onStart()

    val textStream = createTextChangeObservable()
    val buttonClickStream = createButtonClickObservable()

    val searchTextObservable = Observable.merge<String>(buttonClickStream, textStream)

    searchTextObservable
        // Code should start on the Main thread since it works with View
        .observeOn(AndroidSchedulers.mainThread())
        // Show progress every time a new item is emitted
        .doOnNext { showProgress() }
        .observeOn(Schedulers.io())
        .map { cheeseSearchEngine.search(it) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          // and hide it when we're ready to display the result
          hideProgress()
          showResult(it)
        }
  }

  // Returns an observable, but instead of button clicks, it's text changes
  private fun createTextChangeObservable(): Observable<String> {

    // Creating the Observable first
    val textChangedObservable = Observable.create<String> { emitter ->
      // when an Observer makes a subscription, first thing to do is create a TextWatcher
      val textWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable?) = Unit

        override fun beforeTextChanged(
          s: CharSequence?,
          start: Int,
          count: Int,
          after: Int
        ) = Unit

        // when the user types (and onTextChanged is [triggered]) we pass the new text value
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
      // we add the watcher to the TextView (Woah....)
      queryEditText.addTextChangedListener(textWatcher)

      // remove the watcher
      emitter.setCancellable {
        queryEditText.removeTextChangedListener(textWatcher)
      }
    }
    return textChangedObservable
        .filter { it.length >= 3 }
        .debounce(1000, MILLISECONDS)
  }

  // 1 - Declaring a function that returns an Observable emitting Strings
  private fun createButtonClickObservable(): Observable<String> {
    // 2 - creating an Observable. Supplying it with a new ObservableOnSubscribe
    return Observable.create { emitter ->
      // 3 - An OnClickListener (I know, not exactly reactive. Bear with me)
      searchButton.setOnClickListener{
        // 4 - call onNext and pass the text Value
        emitter.onNext(queryEditText.text.toString())
      }
      // 5 - A good habit to remove Listeners as soon as they are not needed.
      emitter.setCancellable {
        searchButton.setOnClickListener(null)
      }
    }

  }

}
