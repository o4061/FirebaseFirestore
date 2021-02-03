package com.example.firebasefirestore

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasefirestore.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = Repository()

        binding.btnUploadData.setOnClickListener {
            val fName = binding.etFirstName.text.toString()
            val lName = binding.etLastName.text.toString()
            val age = binding.etAge.text.toString()

            if (fName.isNotEmpty() && lName.isNotEmpty() && age.isNotEmpty()) {
                val person = Person(fName, lName, age.toInt())
                CoroutineScope(Dispatchers.Main).launch {
                    repository.uploadPerson(person)
                    repository.response.observe(this@MainActivity, {
                        binding.tvPersons.text = it
                    })
                }
                clearEditText()
            }
        }


        binding.btnRetrieveData.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                repository.retrievePersons()
                repository.people.observe(this@MainActivity, {
                    binding.tvPersons.text = it.toString()
                })
            }
        }

        binding.btnUpdatePerson.setOnClickListener {
            val fName = binding.etFirstName.text.toString()
            val lName = binding.etLastName.text.toString()
            val age = binding.etAge.text.toString()

            var fNameNew = binding.etNewFirstName.text.toString()
            var lNameNew = binding.etNewLastName.text.toString()
            var ageNew = binding.etNewAge.text.toString()

            if (fName.isNotEmpty() && lName.isNotEmpty() && age.isNotEmpty()) {
                val oldPerson = Person(fName, lName, age.toInt())
                if (fNameNew.isEmpty()) {
                    fNameNew = fName
                }
                if (lNameNew.isEmpty()) {
                    lNameNew = lName
                }
                if (ageNew.isEmpty()) {
                    ageNew = age
                }
                val newPerson = Person(fNameNew, lNameNew, ageNew.toInt())

                CoroutineScope(Dispatchers.IO).launch {
                    repository.updatePerson(oldPerson, newPerson)
                }
            }
        }

        binding.btnDeletePerson.setOnClickListener {
            val fName = binding.etFirstName.text.toString()
            val lName = binding.etLastName.text.toString()
            val age = binding.etAge.text.toString()

            if (fName.isNotEmpty() && lName.isNotEmpty() && age.isNotEmpty()) {

                CoroutineScope(Dispatchers.Main).launch {
                    repository.deletePerson(Person(fName, lName, age.toInt()))
                    repository.people.observe(this@MainActivity, {
                        binding.tvPersons.text = it.toString()
                    })
                }
            }
        }

        binding.btnBatchWrite.setOnClickListener {
            val personId = "Dpe14PuXiL2EPlT0jrtH"
            val fName = "nikolas"
            val lName = "dimitriou"
            repository.changeName(personId,fName, lName)
        }

//        binding.btnRetrieveData.setOnClickListener {
//            val fromAge = binding.etFrom.text.toString()
//            val toAge = binding.etTo.text.toString()
//
//            if (fromAge.isNotEmpty() && toAge.isNotEmpty()) {
//                CoroutineScope(Dispatchers.Main).launch {
//                    repository.retrievePeopleWithAges(fromAge.toInt(), toAge.toInt())
//                    repository.people.observe(this@MainActivity, {
//                        binding.tvPersons.text = it.toString()
//                    })
//                }
//            }
//        }

        repository.retrieveRealTimePeople()
        repository.people.observe(this, {
            binding.tvPersons.text = it.toString()
        })
    }

    private fun clearEditText() {
        binding.etFirstName.text.clear()
        binding.etLastName.text.clear()
        binding.etAge.text.clear()
    }
}