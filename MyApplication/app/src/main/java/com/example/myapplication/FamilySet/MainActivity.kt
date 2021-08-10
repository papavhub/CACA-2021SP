package com.example.myapplication.FamilySet

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.myapplication.HomeActivity
import com.example.myapplication.Mypage.MypageActivity
import com.example.myapplication.Notification.Notification
import com.example.myapplication.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card_layout.*
import kotlinx.android.synthetic.main.mypage_activity.*
import kotlinx.android.synthetic.main.signuppage.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private var isFabOpen = false

    var fbAuth = FirebaseAuth.getInstance() // 로그인
    var fbFire = FirebaseFirestore.getInstance()

    var uid = fbAuth?.uid.toString() // uid
    var uemail = fbAuth?.currentUser?.email.toString()

    val db: FirebaseFirestore = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // 기기 정보 저장

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()


            val deviceInfo = hashMapOf( // device Token DB에 넣기
                "deviceInfo" to msg.toString()
            )

            db.collection("Member").document(uid).collection("DEVICE").document("TOKEN").set(deviceInfo)



        })














        val db: FirebaseFirestore = Firebase.firestore // 여러 document 받아오기
        var a = ""
        var mutableList : MutableList<String> = mutableListOf("a")
        mutableList.clear()

        db.collection("Member").document(uid).collection("MYPAGE")
            //.whereEqualTo("Familys", true)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    mutableList.add(document.id)
                }

                val ContentView = arrayOfNulls<View>(mutableList.size)
                val cardView = arrayOfNulls<CardView>(mutableList.size)
                val item_title_d = arrayOfNulls<TextView>(mutableList.size)
                val card_item_image = arrayOfNulls<ImageView>(mutableList.size)

                val count = mutableList.size - 1

                for (layoutIdx in 0..count) {

                    if(mutableList[layoutIdx] == "MYPAGE"){ // Familys

                        val layoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val containView = layoutInflater.inflate(R.layout.defaultcard_layout, null) // mypage_content를 inflate
                        l_contain.addView(containView) // 추가

                        ContentView[layoutIdx] = containView as View

                        item_title_d[layoutIdx] = ContentView[layoutIdx]!!.findViewById(R.id.item_title_d) as TextView
                        val db: FirebaseFirestore = Firebase.firestore // 여러 field값 가져오기
                        val docRef1 = db.collection("Member").document(uid)
                        //item_title_d[layoutIdx]?.text = mutableList[layoutIdx]

                        cardView[layoutIdx] = ContentView[layoutIdx]!!.findViewById(R.id.cardView) as CardView
                        cardView[layoutIdx]?.setOnClickListener() {
                            val intent = Intent(application, MypageActivity::class.java)
                            startActivity(intent)
                        }

                    }else{ // 나머지

                        val layoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val containView = layoutInflater.inflate(R.layout.card_layout, null) // mypage_content를 inflate
                        l_contain.addView(containView) // 추가

                        ContentView[layoutIdx] = containView as View



                        item_title_d[layoutIdx] = ContentView[layoutIdx]!!.findViewById(R.id.item_title_d) as TextView // field로 가져오기
                        val docRef2 = db.collection("Member").document(uid).collection("MYPAGE").document(mutableList[layoutIdx]                        )
                        docRef2.get()
                            .addOnSuccessListener { document ->
                                if (document != null) {
                                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                                    item_title_d[layoutIdx]?.text = document.data?.get("name").toString() // family name 넣기

                                } else {
                                    Log.d(TAG, "No such document")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.d(TAG, "get failed with ", exception)
                            }




                        card_item_image[layoutIdx] = ContentView[layoutIdx]!!.findViewById(R.id.item_image) as ImageView // 가족별 Image 가져오기
                        //card_item_image[layoutIdx]?.setImageURI(ref.downloadUrl.toString().toUri())
                        var storage: FirebaseStorage = Firebase.storage
                        val imageRef1 = storage.getReferenceFromUrl(
                            "gs://cacafirebase-554ac.appspot.com/Family_Image/" + mutableList[layoutIdx]
                        )
                        card_item_image[layoutIdx]?.let { displayImageRef(imageRef1, it) }




                        cardView[layoutIdx] = ContentView[layoutIdx]!!.findViewById(R.id.cardView) as CardView
                        cardView[layoutIdx]?.setOnClickListener() {
                            val intent = Intent(application, HomeActivity::class.java)
                            intent.putExtra("FamilyName", mutableList[layoutIdx])
                            startActivity(intent)
                        }
                    }


                }

                val layoutInflater =
                    this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater // 동적으로 생성

                val containView = layoutInflater.inflate(
                    R.layout.defaultcard_layout,
                    null
                ) // mypage_content를 inflate // card_layout을 inflate
                val mVContentView = containView as View
                val FamilyNameText = mVContentView.findViewById(R.id.item_title_d) as TextView
                FamilyNameText.text = "가족 추가"
                

                val cardView_d = mVContentView.findViewById(R.id.cardView) as CardView
                cardView_d.setOnClickListener(){ // 가족 추가 클릭하면 가족 추가 activity로 이동
                    val intent = Intent(this, FamilySettingActivity::class.java)
                    startActivity(intent)
                }

                l_contain.addView(containView)

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }








        //test
        //test
        //test
//        val intent = Intent(this, Notification::class.java)
//        startActivity(intent)












    }

    private fun displayImageRef(imageRef: StorageReference?, view: ImageView) {
        imageRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            view.setImageBitmap(bmp)
        }?.addOnFailureListener {
            // Failed to download the image
        }
    }



}

