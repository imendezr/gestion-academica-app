package com.example.gestionacademicaapp.utils

import androidx.appcompat.widget.SearchView

fun setupSearchView(
    searchView: SearchView,
    hint: String,
    onQueryTextChange: (String?) -> Unit
) {
    searchView.apply {
        isIconified = false
        clearFocus()
        queryHint = hint
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                onQueryTextChange(newText)
                return true
            }
        })
    }
}
