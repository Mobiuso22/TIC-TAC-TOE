package com.experiments.tictactoe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.experiments.tictactoe.R;
import com.experiments.tictactoe.models.Game;
import com.experiments.tictactoe.models.Player;
import com.experiments.tictactoe.models.Room;
import com.experiments.tictactoe.utility.Constants;
import com.experiments.tictactoe.utility.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class EnterGameActivity extends BaseActivity {


    private static final String TAG = "EnterGameActivity";
    @Bind(R.id.edtCreatingUserName)
    AppCompatEditText edtCreatingUserName;
    @Bind(R.id.edtRoomId)
    AppCompatEditText edtRoomId;
    @Bind(R.id.edtJoiningUserName)
    AppCompatEditText edtJoiningUserName;


    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_game);
        ButterKnife.bind(this);
        init();
    }

    @Override
    protected void init() {
        hideKeyBoard();
        getSupportActionBar().setTitle(getString(R.string.enter_game));
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
    }


    @Override
    protected void onNetworkConnected() {
        Log.d(TAG, "onNetworkConnected: ");
    }

    @Override
    protected void onNetworkDisconnected() {
        Log.d(TAG, "onNetworkDisconnected: ");
        showMessage(getString(R.string.no_internet));
    }


    @OnClick({R.id.btnCreateRoom, R.id.btnJoinRoom})
    public void onClick(View view) {
        hideKeyBoard();
        if (!isInternetAvailable(this)) {
            showMessage(getString(R.string.no_internet));
            return;
        }
        switch (view.getId()) {
            case R.id.btnCreateRoom:
                startCreatingRoom();
                break;
            case R.id.btnJoinRoom:
                startJoiningRoom();
                break;
        }
    }

    private void authenticate(final Authenticator authenticator) {
        //anonymous sign in
        firebaseAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "signInAnonymously", task.getException());
                    showMessage(getString(R.string.auth_failed));
                    return;
                }
                authenticator.onAuthenticated();
            }
        });
    }

    private void startCreatingRoom() {
        //validating inputs,authenticating & creating room
        final String userName = edtCreatingUserName.getText().toString();
        if (!TextUtils.isEmpty(userName)) {
            authenticate(new Authenticator() {
                @Override
                public void onAuthenticated() {
                    createRoom(userName.trim());
                }
            });
        } else {
            showMessage(getString(R.string.please_enter_required_fields));
        }
    }

    private void createRoom(String userName) {
        //creating new room with given user name
        Room room = new Room();
        room.setCreatedAt(Utils.getCurrentTime());

        //adding creator as playerA
        Player creatingPlayer = new Player();
        creatingPlayer.setPlayerName(userName);
        creatingPlayer.setJoinedAt(Utils.getCurrentTime());

        //assigning zero to playerA
        Game game = new Game();
        game.setHasZero(true);

        room.setPlayerA(creatingPlayer);
        creatingPlayer.setGame(game);

        //pushing new room
        DatabaseReference roomsRef = firebaseDatabase.getReference(Constants.DataKeys.ROOMS);
        final String roomId = roomsRef.push().getKey();
        roomsRef.child(roomId).setValue(room);

        //navigating to play game
        playGame(roomId, true);
    }

    private void startJoiningRoom() {
        //validating inputs, authenticating & joining existing room
        final String roomId = edtRoomId.getText().toString();
        final String userName = edtJoiningUserName.getText().toString();
        if (!TextUtils.isEmpty(roomId) && !TextUtils.isEmpty(userName)) {
            authenticate(new Authenticator() {
                @Override
                public void onAuthenticated() {
                    joinRoom(roomId.trim(), userName.trim());
                }
            });
        } else {
            showMessage(getString(R.string.please_enter_required_fields));
        }
    }

    private void joinRoom(final String roomId, final String userName) {
        //checking room is available and joining that room with given user name
        checkRoomAvailable(roomId, new RoomChecker() {
            @Override
            public void onCheckingComplete(@Nullable Room room) {
                if (room == null) {
                    showMessage(getString(R.string.no_room_found));
                    return;
                }

                //adding joining player as playerB
                Player joiningPlayer = new Player();
                joiningPlayer.setPlayerName(userName);
                joiningPlayer.setJoinedAt(Utils.getCurrentTime());

                //assigning cross to player B
                Game game = new Game();
                game.setHasZero(false);

                room.setPlayerB(joiningPlayer);
                joiningPlayer.setGame(game);

                //updating room
                DatabaseReference roomsRef = firebaseDatabase.getReference(Constants.DataKeys.ROOMS);
                roomsRef.child(roomId).setValue(room);

                //navigating to play game
                playGame(roomId, false);
            }
        });
    }

    private void playGame(String roomId, boolean isNewGame) {
        Intent intent = new Intent(EnterGameActivity.this, PlayGameActivity.class);
        intent.putExtra(Constants.IntentExtras.IS_NEW_GAME, isNewGame);
        intent.putExtra(Constants.IntentExtras.ROOM_ID, roomId);
        startActivity(intent);
    }

    private void checkRoomAvailable(final String roomId, final RoomChecker roomChecker) {
        //checking if room was created before on this room id and is available
        DatabaseReference roomsRef = firebaseDatabase.getReference(Constants.DataKeys.ROOMS);
        roomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(roomId)) {
                    Room room = dataSnapshot.child(roomId).getValue(Room.class);
                    //if room has creating player but no joining player - then room is available
                    if (room.getPlayerA() != null && room.getPlayerB() == null) {
                        roomChecker.onCheckingComplete(room);
                    } else {
                        roomChecker.onCheckingComplete(null);
                    }
                } else {
                    roomChecker.onCheckingComplete(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError.getDetails());
                roomChecker.onCheckingComplete(null);
            }
        });
    }

    private interface Authenticator {
        void onAuthenticated();
    }

    private interface RoomChecker {
        void onCheckingComplete(@Nullable Room room);
    }

}
