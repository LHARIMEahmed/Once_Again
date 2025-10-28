package com.example.myapplication.fragments.Acceuil.chat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.adapters.adapterMessagerie
import com.example.myapplication.databinding.MessagingLayoutBinding
import com.example.myapplication.models.message
import com.example.myapplication.utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class chatFragment: Fragment() {
    lateinit var binding : MessagingLayoutBinding
    private val args: chatFragmentArgs by navArgs()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth : FirebaseAuth
    private var chatPath=""
    private var imageUri:Uri?=null
    private var monUid =""
    private var uid = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MessagingLayoutBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uid = args.sellerId
        firebaseAuth= FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("please wait")
        progressDialog.setCanceledOnTouchOutside(true)

        monUid= firebaseAuth.uid!!
        chatPath=utils.chatPath(uid,monUid)

        binding.toolbarBackbttn.setOnClickListener{
            findNavController().navigateUp()
        }
        binding.attachFile.setOnClickListener{
            pickImages()
        }
        binding.sendMsg.setOnClickListener{
            validateDate()
        }
        loadRecepteurInfos()
        loadMessages()



    }
    private fun loadRecepteurInfos(){
        val ref = FirebaseFirestore.getInstance().collection("users").whereEqualTo("uid", uid)
        ref.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Replace "nameField" with the name of the field in your documents that stores the name
                    val name = document.getString("nom")
                    if (name != null) {
                        // Update UI with the user's name
                        binding.apply {
                            sellerName.text =name
                        }
                        break; // Exit the loop after finding the first matching document
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }

    }
    private fun loadMessages() {
        val messageArrayList = ArrayList<message>()






        val db = FirebaseFirestore.getInstance()
        val messagesRef = db.collection("Messages")


        val query = messagesRef.whereEqualTo("chatPath", chatPath).orderBy("timestamp")


        val listener = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {

                return@addSnapshotListener
            }

            // Clear messageArrayList each time before adding new data
            messageArrayList.clear()

            // Add each message to messageArrayList
            snapshot?.documents?.forEach { document ->
                try {
                    // Convert document to ModelChat object
                    val modelChat = document.toObject(message::class.java)
                    modelChat?.let { messageArrayList.add(it) }
                } catch (e: Exception) {

                }
            }

            // Initialize/setup AdapterChat class and set it to RecyclerView
            val adapterChat = adapterMessagerie(requireContext(), messageArrayList)
            binding.recyclerViewMsg.adapter = adapterChat
        }
    }

    private val requestStoragePermission=registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted->
        if(isGranted){
            pickImages()
        }

    }

    private fun pickImages() {
            val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.type = "image/*"
        imageLauncher.launch(pickIntent)

    }
    private val imageLauncher=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        result->
        if(result.resultCode==Activity.RESULT_OK){
            val data =result.data
            imageUri=data!!.data
        saveImageFirebase()
        }else{

        }
    }
    private fun saveImageFirebase(){
        progressDialog.setMessage("loading")
        progressDialog.show()

        val timesStamp=utils.getTimeStamp()
        val filePathName = "ChatImages/$timesStamp"
        val ref = FirebaseStorage.getInstance().getReference(filePathName)
        ref.putFile(imageUri!!)
            .addOnProgressListener {snapshot ->
                val progress = 100.0*snapshot.bytesTransferred/snapshot.totalByteCount
                progressDialog.setMessage("Uploading image : Progress ${progress.toInt()} %")
            }
            .addOnSuccessListener {taskSnapshot->
                val uriTask =taskSnapshot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val uploadedImageUrl=uriTask.result.toString()

                if(uriTask.isSuccessful){
                    sendMessage("IMAGE",uploadedImageUrl,timesStamp)
                }
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()

            }
    }
    private fun validateDate(){
        val messgae = binding.msgEditText.text.toString().trim()
        val timeStamp= utils.getTimeStamp()


        if(messgae.isEmpty()){

        }else{
            sendMessage("TEXT",messgae,timeStamp)
        }
    }
    private fun sendMessage(messageType: String, messageContent: String, timestamp: Long) {
        progressDialog.setMessage("Sending message")
        progressDialog.show()

        // Reference to the Firestore collection
        val ref = FirebaseFirestore.getInstance().collection("Messages")

        // Generate a unique message ID
        val messageId = ref.document().id

        // Prepare the message data
        val messageData = hashMapOf(
            "messageId" to messageId,
            "messageType" to messageType,
            "messageContent" to messageContent,
            "fromUid" to monUid,
            "toUid" to uid,
            "timestamp" to timestamp,
            "chatPath" to chatPath  // Add this line to include chatPath in the message data
        )

        // Add the message data to Firestore
        ref.document(messageId).set(messageData)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d("TAG", "sendMessage: message sent")
                // Optional: Clear message edit text for a new message
                binding.msgEditText.text.clear()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                 }
    }
}
