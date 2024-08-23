package com.example.collegefixit.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.collegefixit.model.Complaint
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class ComplaintRepository {

    private val db = FirebaseFirestore.getInstance()
    private val complaintsCollection = db.collection("complaints")


    suspend fun isUserComplaintOwner(complaintId: String, userId: String): Boolean {
        return try {
            val complaintDoc = complaintsCollection.document(complaintId).get().await()
            complaintDoc.getString("userId") == userId
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteComplaint(complaintId: String){
        try{
            complaintsCollection.document(complaintId).delete().await()

        }
        catch (e: Exception){
            throw e
        }
    }

    fun getPendingComplaintsRealtime(onUpdate: (List<Complaint>) -> Unit): ListenerRegistration {
        return complaintsCollection
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                val complaints = snapshot?.documents?.mapNotNull {
                    it.toObject(Complaint::class.java)?.copy(id = it.id)
                } ?: emptyList()
                onUpdate(complaints)
            }
    }

    suspend fun toggleUpvote(complaintId: String, userId: String) {
        try {
            val complaintRef = complaintsCollection.document(complaintId)
            db.runTransaction { transaction ->
                val complaintSnapshot = transaction.get(complaintRef)
                val currentUpvotes = complaintSnapshot.getLong("upvotes") ?: 0
                val upvotedBy = complaintSnapshot.get("upvotedBy") as? MutableList<String> ?: mutableListOf()

                if (userId in upvotedBy) {
                    // User has already upvoted, so remove their upvote
                    transaction.update(complaintRef, "upvotes", currentUpvotes - 1)
                    upvotedBy.remove(userId)
                } else {
                    // User hasn't upvoted yet, so add their upvote
                    transaction.update(complaintRef, "upvotes", currentUpvotes + 1)
                    upvotedBy.add(userId)
                }
                transaction.update(complaintRef, "upvotedBy", upvotedBy)
            }.await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun addComplaint(complaint: Complaint) {
        try {
            complaintsCollection.add(complaint).await()
        } catch (e: Exception) {
            throw e
        }
    }
}