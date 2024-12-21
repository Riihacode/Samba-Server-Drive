package com.dicoding.sambaserver.profile

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.sambaserver.R
import com.dicoding.sambaserver.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var rvAlgorithm: RecyclerView
    private val list = ArrayList<ProjectMembers>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        justifyText()
        setupRecyclerView()
        setupData()
    }
    private fun setupRecyclerView() {
        rvAlgorithm = binding.rvKelompok
        rvAlgorithm.layoutManager = LinearLayoutManager(this)

        // Menghubungkan adapter dan data ke RecyclerView
        val adapter = ProfileAdapter(list)
        //adapter.setOnItemClickCallback(this)  // Set callback
        rvAlgorithm.adapter = adapter
    }

    private fun justifyText() {
        val webView: WebView = binding.tvKelompokDeskripsi
        webView.settings.javaScriptEnabled = false
        webView.settings.domStorageEnabled = false
        webView.setBackgroundColor(0x00000000) // Transparansi latar belakang

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.setBackgroundColor(0x00000000) // Pastikan latar belakang tetap transparan
            }
        }

        val justifiedText = """
            <html>
            <head>
                <style>
                    body {
                        text-align: justify;
                        font-size: 16px;
                        line-height: 1.5;
                        background-color: transparent;
                        margin: 0;
                        padding: 0;
                    }
                </style>
            </head>
            <body>
                ${getString(R.string.deskripsi_samba_server_android)}
            </body>
            </html>
        """
        webView.loadDataWithBaseURL(null, justifiedText, "text/html", "utf-8", null)
    }

    private fun setupData() {
        val names = resources.getStringArray(R.array.member_name)
        val descriptionsNim = resources.getStringArray(R.array.member_description_nim)
        val descriptionsRole = resources.getStringArray(R.array.member_role)
        val descriptionsDuty = resources.getStringArray(R.array.member_duty)
        val typedArray = resources.obtainTypedArray(R.array.member_photo)

        for (i in names.indices) {
            val photoId = typedArray.getResourceId(i, -1)
            list.add(ProjectMembers(names[i], descriptionsNim[i], photoId, descriptionsRole[i], descriptionsDuty[i]))
        }
        typedArray.recycle() // Jangan lupa untuk membebaskan TypedArray
    }
}