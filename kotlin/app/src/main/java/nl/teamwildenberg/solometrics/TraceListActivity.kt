package nl.teamwildenberg.solometrics

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ExpandableListView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import nl.teamwildenberg.solometrics.Adapter.PaperTraceItem
import nl.teamwildenberg.solometrics.Adapter.TraceListAdapter
import nl.teamwildenberg.solometrics.Ble.BlueDevice
import nl.teamwildenberg.solometrics.Service.PaperTrace
import nl.teamwildenberg.solometrics.Service.StorageService
import nl.teamwildenberg.solometrics.Service.WindMeasurement


class TraceListActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private var storageBinding: StorageService.LocalBinder? = null
    var traceList: MutableList<PaperTraceItem> = mutableListOf()
    lateinit var traceAdapter: TraceListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var traceListView: ExpandableListView

        super.onCreate(savedInstanceState)
        setContentView(R.layout.trace_list_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        traceAdapter = TraceListAdapter(this, traceList)
        traceListView = findViewById<ExpandableListView>(R.id.traceListView)
        traceListView.setAdapter(traceAdapter)
        traceListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            // This is your listview's selected item
//            var selectedItem = parent.getItemAtPosition(position) as PaperTrace
//            var finishIntent = Intent(this, TraceListActivity::class.java)
//            finishIntent.putExtra("traceKey", selectedItem.key.toStringKey())
//            setResult(Activity.RESULT_OK, finishIntent)
//            this.finish();
        }

        traceListView.setOnGroupExpandListener { groupPosition: Int ->
            var service = storageBinding?.getService()
            var selectedItem = traceListView.getItemAtPosition(groupPosition) as PaperTraceItem
            if (service != null){
                if (selectedItem.PartionList == null) {
                    var partionList = service.GetPartionList(selectedItem.Trace)
                    selectedItem.PartionList = partionList
                    traceAdapter.notifyDataSetChanged()
                }}

        }

//
//        var metrics = DisplayMetrics()
//        getWindowManager().getDefaultDisplay().getMetrics(metrics)
//        var width = metrics.widthPixels
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            traceListView.setIndicatorBounds(
//                width - GetDipsFromPixel(50),
//                width - GetDipsFromPixel(10)
//            )
//        } else {
//            traceListView.setIndicatorBoundsRelative(
//                width - GetDipsFromPixel(50),
//                width - GetDipsFromPixel(10)
//            )
//        }

        var startButton = findViewById<FloatingActionButton>(R.id.startButton)
        startButton.setOnClickListener{
        }

        var storageServiceIntent = Intent(this, StorageService::class.java)
        startService(storageServiceIntent)
    }

    override fun onResume() {
        super.onResume()

        val bindStorageServiceIntent = Intent(this, StorageService::class.java)
        this.bindService(bindStorageServiceIntent, storageServiceConnection, Context.BIND_NOT_FOREGROUND)
    }

    override fun onPause() {
        super.onPause()
        this.unbindService(storageServiceConnection)
    }


    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    fun GetDipsFromPixel(pixels: Int): Int {
        // Get the screen's density scale
        val scale = resources.displayMetrics.density
        // Convert the dps to pixels, based on density scale
        return (pixels * scale + 0.5f).toInt()
    }

    /// KUDOS:
    /// https://stackoverflow.com/a/46499387/553589
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        var finishIntent = Intent(this, TraceListActivity::class.java)
        setResult(Activity.RESULT_CANCELED, finishIntent)
        this.finish();
    }

    private val storageServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            storageBinding = service as StorageService.LocalBinder
            var service = storageBinding!!.getService()
            var list = service?.GetTraceList()
            traceList.clear()
            list.forEach{trace: PaperTrace ->
                traceList.add(PaperTraceItem(trace, null))
            }

            traceAdapter.notifyDataSetChanged()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            storageBinding = null
        }
    }
}