package com.fawaz.notesapp.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fawaz.notesapp.R

class GameFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Menyambungkan class Kotlin ini ke file desain fragment_game.xml
        return inflater.inflate(R.layout.fragment_game, container, false)
    }
}