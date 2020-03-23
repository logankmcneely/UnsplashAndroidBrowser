package com.phorium.flickrimagebrowser

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import kotlin.properties.Delegates
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.next_ticket.view.*
import kotlinx.android.synthetic.main.next_with_prev_ticket.view.*
import kotlinx.android.synthetic.main.photo_ticket.view.ivPhoto
import kotlinx.android.synthetic.main.prev_ticket.view.*
import kotlinx.android.synthetic.main.random_ticket.view.*
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "MainActivity"


class MainActivity : AppCompatActivity() {


    private var downloadData: DownloadData? = null
    private var searchParams = SearchParams("random","all",1)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateSearchQuery(searchParams)

        Log.d(TAG, "onCreate() finished")
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()

        searchParams.systemUI = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "onCreateOptionsMenu called")
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)

        val searchView: SearchView?

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.app_bar_search).actionView  as SearchView
        searchView.queryHint = "Search for photos"
        val searchableInfo = searchManager.getSearchableInfo(componentName)
        searchView.setSearchableInfo(searchableInfo)

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(TAG, ".onQueryTestSubmit: called with querry $query")

                if (query!!.isNotEmpty()) {
                    searchParams.searchQuery = query
                    updateSearchQuery(searchParams)
                    hideSystemUI()
                }

                // Close and refocus
                searchView.onActionViewCollapsed()
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.mnuLandscape, R.id.mnuPortrait, R.id.mnuAll -> {
                if (!item.isChecked) {
                    item.isChecked = true
                    searchParams.orientation = item.title.toString().toLowerCase(Locale.ROOT)
                    updateSearchQuery(searchParams)
                }
            }
            R.id.mnuPopular -> {
                searchParams.searchQuery = "popular"
                updateSearchQuery(searchParams)
            }
            R.id.mnuLatest -> {
                searchParams.searchQuery = "latest"
                updateSearchQuery(searchParams)
            }
            R.id.mnuOldest -> {
                searchParams.searchQuery = "oldest"
                updateSearchQuery(searchParams)
            }
            R.id.mnuRandom -> {
                searchParams.searchQuery = "random"
                updateSearchQuery(searchParams)
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
        hideSystemUI()
        return true
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Closing Activity")
            .setMessage("Are you sure you want to close this activity?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }

    class DownloadData(context: Context, listView: ListView, searchParams: SearchParams, window: Window): AsyncTask<String, Void, String>() {

        private val photoList = ArrayList<Photo>()

        private var propContext: Context by Delegates.notNull()
        private var propListView: ListView by Delegates.notNull()
        private var propSearchParams: SearchParams by Delegates.notNull()
        private var propWindow: Window by Delegates.notNull()


        init {
            Log.d("DownloadData", "DownloadData class initialized")
            propContext = context
            propListView = listView
            propSearchParams = searchParams
            propWindow = window
        }

        override fun doInBackground(vararg url: String?): String {
            Log.d("DownloadData", "doInBackground() called")

            val result = URL(url[0]).readText()

            val jsonArray = try {
                val jsonObject = JSONObject(result)
                jsonObject.getJSONArray("results")
            } catch (e:Exception) {
                JSONArray(result)
            }

            for (i in 0 until jsonArray.length()) {
                val jsonPhoto = jsonArray.getJSONObject(i)

                val createdAt: String = jsonPhoto.getString("created_at")
                val urls = jsonPhoto.getJSONObject("urls")
                val urlRaw: String = urls.getString("raw")
                val urlFull: String = urls.getString("full")
                val urlRegular: String = urls.getString("regular")
                val urlSmall: String = urls.getString("small")
                val urlThumb: String = urls.getString("thumb")
                val width: Int = jsonPhoto.getInt("width")
                val height: Int = jsonPhoto.getInt("height")
                val color: String = jsonPhoto.getString("color")
                val id: String = jsonPhoto.getString("id")

                val description: String = jsonPhoto.getString("description")
                val user: String = jsonPhoto.getJSONObject("user").getString("name")

                val photoObject =
                    Photo(id, createdAt, urlRaw, urlFull, urlRegular, urlSmall, urlThumb, width, height, color, description, user)
                photoList.add(photoObject)
            }

            return ""
        }

        override fun onPostExecute(result: String?) {
            Log.d("DownloadData", "onPostExecute() called")

            val idParam =
                when {
                    photoList.size < 30 -> "end"
                    propSearchParams.searchQuery == "random" -> "random"
                    propSearchParams.pageNumber > 1 -> "both"
                    else -> "next"
                }
            photoList.add((Photo(idParam,"","","","","","",0,0,"","","")))

            val myAdapter = MyAdapter(propContext, photoList)
            propListView.adapter = myAdapter


        }

        inner class MyAdapter(context: Context, private var listOfPhotos: ArrayList<Photo>) : BaseAdapter() {

            private val context: Context? = context

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

                val photo = listOfPhotos[position]
                when (photo.id) {
                    "next" -> {

                        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val myView = inflater.inflate(R.layout.next_ticket, parent, false)
                        myView.ivNextSolo.setOnClickListener {
                            propSearchParams.pageNumber += 1
                            updateSearchQuery(propSearchParams)
                        }
                        return myView

                    }
                    "both" -> {
                        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val myView = inflater.inflate(R.layout.next_with_prev_ticket, parent, false)
                        myView.ivNext.setOnClickListener {
                            propSearchParams.pageNumber += 1
                            updateSearchQuery(propSearchParams)
                        }
                        myView.ivPrev.setOnClickListener {
                            propSearchParams.pageNumber -= 1
                            updateSearchQuery(propSearchParams)
                        }
                        return myView
                    }
                    "random" -> {
                        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val myView = inflater.inflate(R.layout.random_ticket, parent, false)
                        myView.ivRandom.setOnClickListener {
                            updateSearchQuery(propSearchParams)
                        }
                        return myView

                    }
                    "end" -> {
                        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val myView = inflater.inflate(R.layout.prev_ticket, parent, false)
                        myView.ivPrevSolo.setOnClickListener {
                            propSearchParams.pageNumber -= 1
                            updateSearchQuery(propSearchParams)
                        }
                        return myView
                    }
                    else -> {

                        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val myView = inflater.inflate(R.layout.photo_ticket, parent, false)


                        Picasso.get().load(photo.urlRegular)
                            .error(R.drawable.placeholder)
                            .placeholder(R.drawable.placeholder)
                            .into(myView.ivPhoto)

                        myView.ivPhoto.setOnClickListener {
                            if (!propSearchParams.systemUI) {
                                showSystemUI()
                            } else {
                                hideSystemUI()
                            }
                            propSearchParams.systemUI = !propSearchParams.systemUI
                        }
                        myView.ivPhoto.setOnLongClickListener {
                            val intent = Intent(context, PhotoView::class.java)
                            intent.putExtra("url", photo.urlRegular)
                            intent.putExtra("user", photo.user)
                            intent.putExtra("createdAt", photo.createdAt)
                            intent.putExtra("description", photo.description)
                            intent.putExtra("color", photo.color)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                            true
                        }

                        return myView
                    }
                }
            }

            override fun getItem(position: Int): Any {
                return listOfPhotos[position]
            }

            override fun getItemId(position: Int): Long {
                return position.toLong()
            }

            override fun getCount(): Int {
                return listOfPhotos.size
            }
        }

        private fun updateSearchQuery(searchParams: SearchParams) {
            var searchURL: String = when (searchParams.searchQuery) {
                "random" -> {
                    searchParams.randomURL
                }
                "latest", "popular", "oldest" -> {
                    searchParams.sortURL + "&order_by=${searchParams.searchQuery}"
                }
                else -> {
                    searchParams.searchURL +"&query=${searchParams.searchQuery}"
                }
            }
            if (searchParams.orientation != "all") {
                searchURL += "&orientation=${searchParams.orientation}"
            }

            searchURL += "&page=${searchParams.pageNumber}"


            DownloadData(propContext,propListView, propSearchParams, propWindow).execute(searchURL)
            Log.d("TEST", searchURL)
        }

        private fun hideSystemUI() {
            // Enables regular immersive mode.
            // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
            // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                propWindow.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
            }
        }

        // Shows the system bars by removing all the flags
        // except for the ones that make the content appear under the system bars.
        private fun showSystemUI() {
            propWindow.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }

    }

    private fun updateSearchQuery(searchParams: SearchParams) {
        var searchURL: String = when (searchParams.searchQuery) {
            "random" -> {
                searchParams.randomURL
            }
            "latest", "popular", "oldest" -> {
                searchParams.sortURL + "&order_by=${searchParams.searchQuery}"
            }
            else -> {
                searchParams.searchURL +"&query=${searchParams.searchQuery}"
            }
        }
        if (searchParams.orientation != "all") {
            searchURL += "&orientation=${searchParams.orientation}"
        }

        searchURL += "&page=${searchParams.pageNumber}"


        downloadData = DownloadData(this, lvPhotos, searchParams, window)
        downloadData?.execute(searchURL)
        Log.d("TEST", searchURL)
    }


    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

}



