package com.example.dizzynks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

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


                val intent = Intent(this, DragActivity::class.java)
                startActivity(intent)
            }
        }
    }
}