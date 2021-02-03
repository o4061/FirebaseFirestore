package com.example.firebasefirestore

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class Repository {

    lateinit var response: MutableLiveData<String>
    lateinit var people: MutableLiveData<List<Person>>
    private val auth = Firebase().getInstance()

    suspend fun uploadPerson(person: Person) {
        response = MutableLiveData()
        try {
            auth.add(person).await()
            response.value = "Successfully saved data"
        } catch (e: Exception) {
            response.value = e.message
        }
    }

    suspend fun retrievePersons() {
        people = MutableLiveData()
        response = MutableLiveData()
        try {
            val peopleArray = ArrayList<Person>()
            val response = auth.get().await()
            response.documents.forEach {
                peopleArray.add(it.toObject<Person>()!!)
            }
            people.value = peopleArray
        } catch (e: Exception) {
            response.value = e.message
        }
    }

    suspend fun retrievePeopleWithAges(fromAge: Int, toAge: Int) {
        people = MutableLiveData()
        response = MutableLiveData()

        try {
            val peopleArray = ArrayList<Person>()
            val response = auth
                .whereGreaterThan("age", fromAge.toInt())
                .whereLessThan("age", toAge.toInt())
                .orderBy("age")
                .get()
                .await()

            response.documents.forEach {
                peopleArray.add(it.toObject<Person>()!!)
            }
            people.value = peopleArray

        } catch (e: Exception) {
            response.value = e.message
        }
    }

    fun retrieveRealTimePeople() {
        people = MutableLiveData()
        response = MutableLiveData()

        auth.addSnapshotListener { value, error ->
            error?.let {
                response.value = it.message
                return@addSnapshotListener
            }
            value?.let {
                val peopleArray = ArrayList<Person>()
                it.documents.forEach {
                    peopleArray.add(it.toObject<Person>()!!)
                }
                people.value = peopleArray
            }
        }
    }

    suspend fun updatePerson(oldPerson: Person, newPerson: Person) {
        people = MutableLiveData()
        response = MutableLiveData()

        val personQuery = auth
            .whereEqualTo("firstName", oldPerson.firstName)
            .whereEqualTo("lastName", oldPerson.lastName)
            .whereEqualTo("age", oldPerson.age)
            .get()
            .await()

        if (personQuery.documents.isNotEmpty()) {
            personQuery.forEach {
                try {
                    auth.document(it.id).set(
                        newPerson,
                        SetOptions.merge()
                    ).await()
                } catch (e: Exception) {
                    response.value = e.message
                }
            }
        } else {
            response.value = "No person matched the query"
        }
    }

    suspend fun deletePerson(person: Person) {
        people = MutableLiveData()
        response = MutableLiveData()

        val personQuery = auth
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("lastName", person.lastName)
            .whereEqualTo("age", person.age)
            .get()
            .await()

        if (personQuery.documents.isNotEmpty()) {
            personQuery.forEach {
                try {
                    auth.document(it.id).delete().await()
                } catch (e: Exception) {
                    response.value = e.message
                }
            }
        } else {
            response.value = "No person matched the query"
        }
    }

    fun changeName(personId: String, newFirstName: String, newLastName: String) {
        response = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.firestore.runBatch { batch ->
                    val personRef = auth.document(personId)
                    batch.update(personRef, "firstName", newFirstName)
                    batch.update(personRef, "lastName", newLastName)
                }.await()
            } catch (e: Exception) {
                response.value = e.message
            }
        }
    }

    fun birthday(personId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.firestore.runTransaction { transaction ->
                    val personRef = auth.document(personId)
                    val person = transaction.get(personRef)
                    val newAge = person["age"] as Long + 1
                    transaction.update(personRef, "age", newAge)
                    null
                }.await()
            } catch (e: Exception) {
                response.value = e.message
            }
        }
    }
}