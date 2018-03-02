package com.imadeddinelakhbizi.tictactoe_local

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TableRow
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.rate_layout.view.*
import kotlinx.android.synthetic.main.win_layout.view.*

class MainActivity : AppCompatActivity() {

    var mFirebaseAnalytics: FirebaseAnalytics? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText.setBackgroundResource(R.drawable.score_panel_active_background)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        //update score panels
        val Prefs = getSharedPreferences("mypref", 0)
        aiScore = Prefs.getInt("aiScore", 0)
        huScore = Prefs.getInt("huScore", 0)
        editText.setText(huScore.toString())
        editText2.setText(aiScore.toString())

        //load banner ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        //load interstitial ad
        interstitialAd.adUnitId = "interstitual_ad_unit_id"
        loadInterstitial(interstitialAd)
        interstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                loadInterstitial(interstitialAd)
            }
        }
    }

    fun buClick(view: View) {

        var buSelected = view as Button
        var CellID: Int = 1
        ActivePlayer = "O"
        when (buSelected.id) {
            R.id.button -> CellID = 0
            R.id.button2 -> CellID = 1
            R.id.button3 -> CellID = 2
            R.id.button4 -> CellID = 3
            R.id.button5 -> CellID = 4
            R.id.button6 -> CellID = 5
            R.id.button7 -> CellID = 6
            R.id.button8 -> CellID = 7
            R.id.button9 -> CellID = 8
        }
        PlayGame(CellID, buSelected)
        editText.setBackgroundResource(R.drawable.score_panel_background)
        editText2.setBackgroundResource(R.drawable.score_panel_active_background)

        move(CellID, huPlayer)
    }

    var player1 = ArrayList<Int>()
    var player2 = ArrayList<Int>()
    var ActivePlayer: String = ""

    fun PlayGame(CellID: Int, buSelected: Button) {

        if (ActivePlayer == "X") {
            buSelected.setBackgroundResource(R.drawable.nought)
            player1.add(CellID)
            ActivePlayer = "O"

        } else {
            buSelected.setBackgroundResource(R.drawable.circle)
            player2.add(CellID)
            ActivePlayer = "X"
        }
        buSelected.isEnabled = false
    }

    fun AutoPlay(CellID: Int): Button {
        val buSelect: Button
        when (CellID) {
            0 -> buSelect = button
            1 -> buSelect = button2
            2 -> buSelect = button3
            3 -> buSelect = button4
            4 -> buSelect = button5
            5 -> buSelect = button6
            6 -> buSelect = button7
            7 -> buSelect = button8
            8 -> buSelect = button9
            else -> {
                buSelect = button
            }
        }
        return buSelect
    }

    fun reset() {
        round = 0
        origBoard = arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8)
        for (j in 0 until tableLayout.childCount) {
            val view: TableRow = tableLayout.getChildAt(j) as TableRow
            for (i in 0 until view.childCount) {
                val _view: View = view.getChildAt(i)
                if (_view is Button) {
                    _view.isEnabled = true
                    _view.setBackgroundResource(R.color.colorColdYellow)
                    _view.text = ""
                }
            }
        }
    }


    fun move(CellID: Int, player: String) {
        if (origBoard[CellID] != "X" && origBoard[CellID] != "O") {
            round++
            origBoard[CellID] = player
            if (winning(origBoard, player)) {
                huScore++
                UpdateScore(aiScore, huScore)
                GamePlay++
                checkGamesCount(GamePlay)
                showAlert("Congrat! you win!", getEmojiByUnicode(0x1F601))

            } else if (round > 8) {
                GamePlay++
                checkGamesCount(GamePlay)
                showAlert("Tie !", getEmojiByUnicode(0x1F612))

            } else {
                round++
                //creating implementing a little pause after hu plays
                val handler = Handler()
                val r = Runnable {
                    run {
                        val index = minimax(origBoard, aiPlayer).index
                        PlayGame(index!! as Int, AutoPlay(index as Int))
                        editText2.setBackgroundResource(R.drawable.score_panel_background)
                        editText.setBackgroundResource(R.drawable.score_panel_active_background)
                        origBoard[index] = aiPlayer
                        if (winning(origBoard, aiPlayer)) {
                            aiScore++
                            UpdateScore(aiScore, huScore)
                            GamePlay++
                            checkGamesCount(GamePlay)
                            showAlert("You lose!", getEmojiByUnicode(0x1F614))

                        } else if (round == 0) {
                            GamePlay++
                            checkGamesCount(GamePlay)
                            showAlert("Tie !", getEmojiByUnicode(0x1F612))

                        }
                    }
                }
                handler.postDelayed(r, 1300)

            }
        }
    }

    //interstitial ad
    var interstitialAd = InterstitialAd(this)
    //nomber of games played
    var GamePlay = 0
    var huScore = 0
    var aiScore = 0
    var round = 0
    // human
    var huPlayer = "O"
    // ai
    var aiPlayer = "X"
    var origBoard: Array<Any> = arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8)
    var fc = 0

    fun minimax(newBoard: Array<Any>, player: String): Move {

        //add one to function calls
        fc++

        //available spots
        val availSpots = emptyIndexies(newBoard)

        // checks for the terminal states such as win, lose, and tie and returning a value accordingly
        if (winning(newBoard, huPlayer)) {
            val move = Move()
            move.score = -10
            return move
        } else if (winning(newBoard, aiPlayer)) {
            val move = Move()
            move.score = 10
            return move
        } else if (availSpots.isEmpty()) {
            val move = Move()
            move.score = 0
            return move
        }


        val moves = ArrayList<Move>()
        // loop through available spots
        for (i in availSpots.indices) {
            //create an object for each and store the index of that spot that was stored as a number in the object's index key

            val move = Move()
            move.index = newBoard[availSpots[i] as Int]

            // set the empty spot to the current player
            newBoard[availSpots[i] as Int] = player
            //if collect the score resulted from calling minimax on the opponent of the current player
            if (player == aiPlayer) {
                val result = minimax(newBoard, huPlayer)
                move.score = result.score
            } else {
                val result = minimax(newBoard, aiPlayer)
                move.score = result.score
            }

            //reset the spot to empty
            newBoard[availSpots[i] as Int] = move.index!!

            // push the object to the array
            moves.add(move)
        }

// if it is the computer's turn loop over the moves and choose the move with the highest score
        var bestMove = 0
        if (player === aiPlayer) {
            var bestScore: Int = -10000
            for (i in moves.indices) {
                if (moves[i].score > bestScore) {
                    bestScore = moves[i].score
                    bestMove = i
                }
            }
        } else {

// else loop over the moves and choose the move with the lowest score
            var bestScore = 10000
            for (i in moves.indices) {
                if (moves[i].score < bestScore) {
                    bestScore = moves[i].score
                    bestMove = i
                }
            }
        }
        ActivePlayer = "X"
// return the chosen move (object) from the array to the higher depth
        return moves[bestMove]
    }

    fun emptyIndexies(board: Array<Any>): List<Any> {
        val list: List<Any>
        list = board.filter { s -> s != "O" }
        return list.filter { s -> s != "X" }
    }

    fun winning(board: Array<Any>, player: String): Boolean {
        if ((board[0] == player && board[1] == player && board[2] == player) ||
                (board[3] == player && board[4] == player && board[5] == player) ||
                (board[6] == player && board[7] == player && board[8] == player) ||
                (board[0] == player && board[3] == player && board[6] == player) ||
                (board[1] == player && board[4] == player && board[7] == player) ||
                (board[2] == player && board[5] == player && board[8] == player) ||
                (board[0] == player && board[4] == player && board[8] == player) ||
                (board[2] == player && board[4] == player && board[6] == player)) {
            return true
        } else {
            return false
        }
    }

    class Move {
        var index: Any? = null
        var score: Int = 0
    }

    fun showAlert(alertMessage: String, imoji: String) {
        val view: View = layoutInflater.inflate(R.layout.win_layout, null)
        val builder = AlertDialog.Builder(this).create()
        view.textView.text = alertMessage
        view.textView2.text = imoji
        view.textView2.textSize = 24F
        view.textView3.text = "Do you want to replay?"
        builder.setCanceledOnTouchOutside(false)
        builder.setButton(AlertDialog.BUTTON_POSITIVE, "yes", { dialogInterface, i ->
            reset()

        })
        builder.setView(view)
        builder.show()

    }

    fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    fun UpdateScore(aiScore: Int, huScore: Int) {
        editText.setText(huScore.toString())
        editText2.setText(aiScore.toString())
        val Prefs = getSharedPreferences("mypref", 0)
        val editor = Prefs.edit()
        editor.putInt("aiScore", aiScore)
        editor.putInt("huScore", huScore)
        editor.commit()
    }

    fun checkGamesCount(gamePlay: Int) {
        if (gamePlay == 4) {
            val pref = getSharedPreferences("myPref", 0)
            val bool = pref.getBoolean("isAppRated", false)
            if (interstitialAd.isLoaded) {
                interstitialAd.show()
                GamePlay=0
            } else {
                Log.w("interstitial_ad", "ad isn't loaded yet!")
            }
            if (!bool) {
                val view: View = layoutInflater.inflate(R.layout.rate_layout, null)
                val builder = AlertDialog.Builder(this).create()
                view.textView4.text = "made with love"
                view.textView5.text = "want to rate this app?"
                view.button10.setOnClickListener {
                    this.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + applicationContext.packageName)))
                    //remember the app was rated
                    val pref = getSharedPreferences("myPref", 0)
                    val editor = pref.edit()
                    editor.putBoolean("isAppRated", true)
                    editor.commit()
                    builder.dismiss()
                    reset()
                }
                builder.setView(view)
                builder.show()
            }


        }
    }

    fun loadInterstitial(interstitialAd: InterstitialAd) {

        interstitialAd.loadAd(AdRequest.Builder().build())
    }
}
