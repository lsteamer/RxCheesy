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

import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_cheeses.*

class CheeseActivity : BaseSearchActivity() {

  override fun onStart() {
    super.onStart()

    // 1 - Creating an Observable
    val searchTextObservable = createButtonClickObservable()
    // 2 - Subscribing to that observable supplying a simple consumer
    searchTextObservable.subscribe { query ->
      // 3 - Perform the search and show the results
      showResult(cheeseSearchEngine.search(query))
    }
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
