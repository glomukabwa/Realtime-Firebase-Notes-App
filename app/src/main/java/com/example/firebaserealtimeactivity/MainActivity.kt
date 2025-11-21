package com.example.firebaserealtimeactivity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaserealtimeactivity.ui.theme.FirebaseRealtimeActivityTheme
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var saveButton: Button

    private val notesList = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        saveButton = findViewById(R.id.saveButton)

        database = FirebaseDatabase.getInstance().reference

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        noteAdapter = NoteAdapter(notesList)
        recyclerView.adapter = noteAdapter

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
                /*return@label is a labeled return, which lets you exit from a specific function or lambda without exiting
                from everything around it. It is basically saying,"Stop running this click listener(setOnClickListener right
                now, but don't leave the whole activity." So it just doesn't save whatever has been entered but the app
                still continues running. So it just waits for u to enter the right data. The lambda we are exiting here is
                all the code enclosed by the curly brackets that start at the line where saveButton is to the last line
                of the database.child code.*/
            }

            // Create unique ID
            val id = database.child("notes").push().key ?: return@setOnClickListener

            val note = Note(id, title, content)

            database.child("notes").child(id).setValue(note).addOnSuccessListener {
                /*The above line: The database.child("notes") goes to the Firebase node called notes, it looks for
                * the id that Firebase generates{child(id), it sets the values of the notes: the title, content{setValue(note)},
                * addOnSuccessListener does the code in the lines below if the note has been successfully added*/
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
                /*Toast creates a pop with the message Note Saved*/
                titleEditText.text.clear()/*This clears the title and the line that follows clears the content line*/
                contentEditText.text.clear()
            }.addOnFailureListener {/*If the addition is unsuccessful, add a pop that says Failed to Save*/
                Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
            }
        }

        database.child("notes").addValueEventListener(object : ValueEventListener {
            /*The line above, checks the notes node and listens for changes(incase of insertion, deletion etc)*/
            override fun onDataChange(snapshot: DataSnapshot) {
                /*This function is triggered if there is a change to the notes node*/
                notesList.clear()/*This part clears the list. Why? If we don’t clear the list, then every time onDataChange
                 runs, we’ll keep adding the same notes again, resulting in duplicates in our RecyclerView.*/
                snapshot.children.mapNotNullTo(notesList) { it.getValue(Note::class.java) }
                /*snapshot.children -> This is a list of all child nodes under notes in your Firebase database.
                Each child is essentially one note.
                .mapNotNullTo(notesList) { ... } -> This is a Kotlin function that: Takes each child (it) from
                snapshot.children and applies the function inside { } to it. In this case:
                it.getValue(Note::class.java), which converts the Firebase snapshot into a Note object.It only adds non-null
                results to notesList.notesList -> This is the mutable list of Note objects that your RecyclerView uses.*/
                noteAdapter.notifyDataSetChanged()
                /*After updating the list, we need to tell the RecyclerView adapter that the data changed,
                otherwise it won’t update the UI so this line does that*/
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error reading notes", error.toException())
                /*If there is an error, it is displayed in the log*/
            }
        })

    }
}


class NoteAdapter(private val notes: List<Note>) :
    RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val content: TextView = view.findViewById(R.id.tvContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.title.text = note.title
        holder.content.text = note.content
    }

    override fun getItemCount() = notes.size
}
