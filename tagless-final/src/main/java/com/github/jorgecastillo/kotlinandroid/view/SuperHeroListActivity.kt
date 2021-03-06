package com.github.jorgecastillo.kotlinandroid.view

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.github.jorgecastillo.kotlinandroid.R
import com.github.jorgecastillo.kotlinandroid.di.context.SuperHeroesContext
import com.github.jorgecastillo.kotlinandroid.di.context.SuperHeroesContext.GetHeroesContext
import com.github.jorgecastillo.kotlinandroid.functional.AsyncResult
import com.github.jorgecastillo.kotlinandroid.functional.AsyncResultKindPartial
import com.github.jorgecastillo.kotlinandroid.functional.ev
import com.github.jorgecastillo.kotlinandroid.presentation.SuperHeroesListView
import com.github.jorgecastillo.kotlinandroid.presentation.getSuperHeroes
import com.github.jorgecastillo.kotlinandroid.presentation.onHeroListItemClick
import com.github.jorgecastillo.kotlinandroid.view.adapter.HeroesCardAdapter
import com.github.jorgecastillo.kotlinandroid.view.viewmodel.SuperHeroViewModel
import kategory.HK
import kategory.effects.ev
import kategory.value
import kotlinx.android.synthetic.main.activity_main.heroesList

class SuperHeroListActivity : AppCompatActivity(), SuperHeroesListView {

  private lateinit var adapter: HeroesCardAdapter
  private lateinit var heroesContext: GetHeroesContext

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setupDependencyGraph()
    setupList()
  }

  private fun setupDependencyGraph() {
    heroesContext = GetHeroesContext(this, this)
  }

  private fun setupList() {
    heroesList.setHasFixedSize(true)
    heroesList.layoutManager = LinearLayoutManager(this)
    adapter = HeroesCardAdapter(itemClick = {
      onHeroListItemClick(it.heroId, AsyncResult<GetHeroesContext>()).unsafePerformEffects(heroesContext)
    })
    heroesList.adapter = adapter
  }

  override fun onResume() {
    super.onResume()
    getSuperHeroes(AsyncResult<GetHeroesContext>()).unsafePerformEffects(GetHeroesContext(this, this))
  }

  override fun drawHeroes(heroes: List<SuperHeroViewModel>) = runOnUiThread {
    adapter.characters = heroes
    adapter.notifyDataSetChanged()
  }

  override fun showNotFoundError() = runOnUiThread {
    Snackbar.make(heroesList, R.string.not_found, Snackbar.LENGTH_SHORT).show()
  }

  override fun showGenericError() = runOnUiThread {
    Snackbar.make(heroesList, R.string.generic, Snackbar.LENGTH_SHORT).show()
  }

  override fun showAuthenticationError() = runOnUiThread {
    Snackbar.make(heroesList, R.string.authentication, Snackbar.LENGTH_SHORT).show()
  }
}

fun <D : SuperHeroesContext> HK<AsyncResultKindPartial<D>, Unit>.unsafePerformEffects(ctx: D): Unit {
  this.ev().run(ctx).value().ev().unsafeRunAsync {}
}

