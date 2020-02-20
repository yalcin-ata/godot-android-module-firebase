/**
 * Copyright 2020 Yalcin Ata. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.godotengine.godot;

import android.app.Activity;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.*;
import org.godotengine.godot.Dictionary;
import org.json.JSONException;
import org.json.JSONObject;

public class Firestore {

    private static Activity activity = null;
    private static Firestore instance = null;
    private FirebaseFirestore firebaseFirestore = null;
    private int scriptCallbackId = -1;
    private FirebaseApp firebaseApp = null;

    public Firestore(Activity activity) {
        this.activity = activity;
    }

    public static Firestore getInstance(Activity activity) {
        if (instance == null) {
            instance = new Firestore(activity);
        }

        return instance;
    }

    public void init(FirebaseApp firebaseApp) {
        this.firebaseApp = firebaseApp;

        FirebaseFirestore.setLoggingEnabled(true);
        firebaseFirestore = FirebaseFirestore.getInstance();

        Utils.logDebug("Firestore initialized");
    }

    public void loadDocuments(final String collectionName, final int callbackId) {

        firebaseFirestore.collection(collectionName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    JSONObject jsonObject = new JSONObject();

                    try {
                        JSONObject jObj = new JSONObject();

                        for (DocumentSnapshot document : task.getResult()) {
                            jObj.put(document.getId(), new JSONObject(document.getData()));
                        }
                        jsonObject.put(collectionName, jObj);
                    } catch (JSONException e) {
                        Utils.logDebug("Firestore loadDocuments() JSONException " + e.toString());
                    }

                    Utils.callScriptFunc("Firestore", "Documents", jsonObject.toString());
                } else {
                    Utils.logWarn("Firestore loadDocuments() error getting documents: " + task.getException());
                }
            }
        });
    }

    public void addDocument(final String name, final Dictionary data) {
        firebaseFirestore.collection(name).add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Utils.callScriptFunc("Firestore", "AddDocument", true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.callScriptFunc("Firestore", "AddDocument", false);
            }
        });
    }

    public void setDocumentData(final String colName, final String docName, final Dictionary data) {
        firebaseFirestore.collection(colName).document(docName).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Utils.callScriptFunc("Firestore", "SetDocument", true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.callScriptFunc("Firestore", "SetDocument", false);
            }
        });
    }
}
