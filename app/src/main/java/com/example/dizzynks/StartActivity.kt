package com.example.dizzynks

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.core.content.FileProvider
import java.io.File

class StartActivity : AppCompatActivity() {
    private var etName: EditText? = null
    private var btnStart: Button? = null
    private var rgShape: RadioGroup? = null
    private var rgSize: RadioGroup? = null
    private var selectedShape: RadioButton? = null
    private var selectedSize: RadioButton? = null
    private var switchControls: Switch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        etName = findViewById(R.id.et_name)

        if(Options.name != null) {
            etName!!.setText(Options.name)
        }

        btnStart = findViewById(R.id.btn_start)
        rgShape = findViewById(R.id.rg_shape)
        rgSize = findViewById(R.id.rg_size)
        switchControls = findViewById(R.id.switch_controls)

        btnStart!!.setOnClickListener {
            if(etName!!.text.isEmpty()) {
                Toast.makeText(this, "Enter name!", Toast.LENGTH_SHORT).show()
            }
            else {
                val intSelectShape: Int = rgShape!!.checkedRadioButtonId
                selectedShape = findViewById(intSelectShape)

                val intSelectSize: Int = rgSize!!.checkedRadioButtonId
                selectedSize = findViewById(intSelectSize)

                Options.name = etName!!.text.toString()
                Options.shape = selectedShape!!.text.toString()
                Options.size = selectedSize!!.text.toString()
                Options.controls = switchControls!!.isChecked

                if(Options.controls) {
                    val intent = Intent(this, ControlActivity::class.java)
                    startActivity(intent)
                }
                else {
                    val intent = Intent(this, DragActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.upload) {
            val fileContents = Logs.data
            val fileName = Options.name+"-dizzynks.csv"
            application.openFileOutput(fileName, Context.MODE_APPEND).use {
                it.write(fileContents.toByteArray())
            }

            try {
                //Open file
                var filelocation = File(filesDir, fileName)
                var path = FileProvider.getUriForFile(application, "com.example.dizzynks.fileprovider", filelocation)
                var fileIntent = Intent(Intent.ACTION_SEND)
                fileIntent.type = "text/csv"
                fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data")
                fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                fileIntent.putExtra(Intent.EXTRA_STREAM, path)
                startActivity(Intent.createChooser(fileIntent, "Send mail"))
            } catch(e: Exception) {
                e.printStackTrace()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
}