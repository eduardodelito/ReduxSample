package com.enaz.redux.sample

//From the example https://medium.com/swlh/how-to-implement-redux-in-kotlin-part-1-the-basics-db2854613079

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

// 1. define the entities we're working with

interface State
interface Action

typealias Reducer <S> = (S, Action) -> S
typealias StoreSubscriber <S> = (S) -> Unit

interface Store <S: State> {
    fun dispatch(action: Action)
    fun add(subscriber: StoreSubscriber <S>): Boolean
    fun remove(subscriber: StoreSubscriber <S>): Boolean
    fun getCurrent(): S
}

// 2. implement the entities for our needs

data class CounterState(
    val value: Int = 0
): State

sealed class CounterActions: Action {
    object Init: CounterActions()
    object Increment: CounterActions()
    object Decrement: CounterActions()
}

val CounterStateReducer: Reducer<CounterState> = { old, action ->
    when (action) {
        is CounterActions.Init -> CounterState()
        is CounterActions.Increment -> old.copy(value = old.value + 1)
        is CounterActions.Decrement -> old.copy(value = old.value - 1)
        else -> old
    }
}

class DefaultStore <S: State>(
    initialState: S,
    private val reducer: Reducer<S>
): Store<S> {

    private val subscribers = mutableSetOf<StoreSubscriber<S>>()

    private var state: S = initialState
        set(value) {
            field = value
            subscribers.forEach { it(value) }
        }

    override fun dispatch(action: Action) {
        state = reducer(state, action)
    }

    override fun add(subscriber: StoreSubscriber<S>) = subscribers.add(element = subscriber)

    override fun remove(subscriber: StoreSubscriber<S>) = subscribers.remove(element = subscriber)

    override fun getCurrent(): S = state
}

// 3. use all of the above in our app

object DI {
    val store = DefaultStore(initialState = CounterState(), reducer = CounterStateReducer)
}

class CounterActivity : AppCompatActivity() {

    private val decrementButton by lazy { findViewById<Button>(R.id.decrement_button) }
    private val incrementButton by lazy { findViewById<Button>(R.id.increment_button) }
    private val counterText by lazy { findViewById<TextView>(R.id.counter_text_view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DI.store.add {
            counterText.text = "${it.value}"
        }

        DI.store.dispatch(action = CounterActions.Init)

        decrementButton.setOnClickListener {
            DI.store.dispatch(action = CounterActions.Decrement)
        }

        incrementButton.setOnClickListener {
            DI.store.dispatch(action = CounterActions.Increment)
        }
    }
}
