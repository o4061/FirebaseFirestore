package com.example.firebasefirestore

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Firebase {

    fun getInstance(): CollectionReference {
        return Firebase.firestore.collection("people")
    }
}