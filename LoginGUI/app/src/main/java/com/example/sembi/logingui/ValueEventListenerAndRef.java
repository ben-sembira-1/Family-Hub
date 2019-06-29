package com.example.sembi.logingui;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ValueEventListenerAndRef {
    private DatabaseReference ref;
    private ValueEventListener valueEventListener;

    public ValueEventListenerAndRef(DatabaseReference ref, ValueEventListener valueEventListener) {
        this.ref = ref;
        this.valueEventListener = valueEventListener;
    }

    public DatabaseReference getRef() {
        return ref;
    }

    public void setRef(DatabaseReference ref) {
        this.ref = ref;
    }

    public ValueEventListener getValueEventListener() {
        return valueEventListener;
    }

    public void setValueEventListener(ValueEventListener valueEventListener) {
        this.valueEventListener = valueEventListener;
    }
}
