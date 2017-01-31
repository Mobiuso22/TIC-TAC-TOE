package com.experiments.tictactoe.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;

import com.experiments.tictactoe.R;
import com.experiments.tictactoe.adapters.TilesAdapter;
import com.experiments.tictactoe.dialogs.DialogFactory;
import com.experiments.tictactoe.models.Player;
import com.experiments.tictactoe.models.Tile;
import com.experiments.tictactoe.utility.Constants;
import com.experiments.tictactoe.utility.GameStatus;
import com.experiments.tictactoe.utility.ItemClickListener;
import com.experiments.tictactoe.utility.SpacingItemDecoration;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.experiments.tictactoe.utility.TileStatus.BLANK;
import static com.experiments.tictactoe.utility.TileStatus.CROSS;
import static com.experiments.tictactoe.utility.TileStatus.ZERO;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

public class PlayGameActivity extends BaseActivity implements ItemClickListener<Tile> {

    private static final String TAG = "PlayGameActivity";
    public static final int DELAY_MS = 2000;
    @Bind(R.id.rvTiles)
    RecyclerView rvTiles;
    private DialogFactory dialogFactory;
    private FirebaseDatabase firebaseDatabase;


    private boolean isNewGame;

    private final List<Tile> tiles = new ArrayList<>(9);
    private TilesAdapter tilesAdapter;
    private DataSnapshot mCurrentPlayerData, mOppositePlayerData;
    private String roomId;
    //created winning pair for
    private String[][] winPairs = new String[][]{
            new String[]{"11", "22", "33"},
            new String[]{"13", "22", "31"},
            new String[]{"11", "21", "31"},
            new String[]{"12", "22", "32"},
            new String[]{"13", "23", "33"},
            new String[]{"11", "12", "13"},
            new String[]{"21", "22", "23"},
            new String[]{"31", "32", "33"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);
        ButterKnife.bind(this);
        init();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item = menu.add("Share");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Use this ID to join Tic Tac Toe Game");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
        return true;
    }
    @Override
    protected void init() {
        tiles.clear();
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                tiles.add(new Tile(BLANK, i + "" + j));
            }
        }
        rvTiles.setLayoutManager(new GridLayoutManager(this, 3));
        rvTiles.setHasFixedSize(true);
        rvTiles.addItemDecoration(new SpacingItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.list_spacing),
                getResources().getInteger(R.integer.list_preview_columns)));
        tilesAdapter = new TilesAdapter(this, tiles);
        tilesAdapter.setItemClickListener(this);
        rvTiles.setAdapter(tilesAdapter);

        firebaseDatabase = FirebaseDatabase.getInstance();
        dialogFactory = DialogFactory.create(this);
        Bundle extras = getIntent().getExtras();
        roomId = extras.getString(Constants.IntentExtras.ROOM_ID);
        isNewGame = extras.getBoolean(Constants.IntentExtras.IS_NEW_GAME);

        freezeGame();
        if (isNewGame) {
            //if new game, then show dialog to share room id
            showRoomIdDialog(roomId);
        }

        listenPlayersAvailability(new PlayersListener() {
            @Override
            public void onBothPlayersJoined(DataSnapshot currentPlayer, DataSnapshot oppositePlayer) {
                Log.d(TAG, "onBothPlayersJoined() called with: currentPlayer = [" + currentPlayer + "], oppositePlayer = [" + oppositePlayer + "]");
                unFreezeGame();
                listenPayerMoves(currentPlayer, oppositePlayer);
            }

            @Override
            public void onPlayerLeft(DataSnapshot player) {
                Log.d(TAG, "onPlayerLeft() called with: player = [" + player + "]");
                String playerName = player.getValue(Player.class).getPlayerName();
                String reason = getString(R.string.user_left_game, playerName);
                //showMessage(reason);

                terminateGame(reason);
            }

            @Override
            public void onError(Throwable error) {
                Log.d(TAG, "onError() called with: error = [" + error + "]");
                error.printStackTrace();
                terminateGame(getString(R.string.unknown_error));
            }
        });
    }


    private void listenPlayersAvailability(PlayersListener playersListener) {
        Log.d(TAG, "listenPlayersAvailability() called with: playersListener = [" + playersListener + "]");

        RoomChildEventListener roomChildEventListener = new RoomChildEventListener(playersListener);
        DatabaseReference roomsRef = firebaseDatabase.getReference(Constants.DataKeys.ROOMS).child(roomId);
        roomsRef.addChildEventListener(roomChildEventListener);
    }

    private void listenPayerMoves(final DataSnapshot currentPlayerData, final DataSnapshot oppositePlayerData) {
        Log.d(TAG, "listenPayerMoves() called with: currentPlayerData = [" + currentPlayerData + "], oppositePlayerData = [" + oppositePlayerData + "]");

        mCurrentPlayerData = currentPlayerData;
        mOppositePlayerData = oppositePlayerData;
        final Player currentPlayer = currentPlayerData.getValue(Player.class);
        final Player oppositePlayer = oppositePlayerData.getValue(Player.class);

        Log.e(TAG, "listenPayerMoves: currentPlayer" + currentPlayer);
        Log.e(TAG, "listenPayerMoves: oppositePlayer" + oppositePlayer);

        final List<String> currentPlayerMatrix = new ArrayList<>();
        final List<String> oppositePlayerMatrix = new ArrayList<>();

        currentPlayer.getGame().setMatrix(currentPlayerMatrix);
        oppositePlayer.getGame().setMatrix(oppositePlayerMatrix);

        DatabaseReference currentPlayerMatrixRef = currentPlayerData
                .child(Constants.DataKeys.GAME)
                .child(Constants.DataKeys.MATRIX)
                .getRef();
        DatabaseReference oppositePlayerMatrixRef = oppositePlayerData
                .child(Constants.DataKeys.GAME)
                .child(Constants.DataKeys.MATRIX)
                .getRef();

        currentPlayerMatrixRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(TAG, "currentPlayerMatrixRef onChildAdded() called with: dataSnapshot = [" + dataSnapshot.getKey() + " : " + dataSnapshot.getValue(String.class) + "], prevChildKey = [" + prevChildKey + "]");

                //whenever value added  by current then freeze game
                //freeze should be at the start of computation
                freezeGame();

                //adding last added value to matrix
                //  String lastAddedValue = dataSnapshot.child(String.valueOf(currentPlayerMatrix.size())).getValue(String.class);
                currentPlayerMatrix.add(dataSnapshot.getValue(String.class));

                //updating ui & checking win-loose conditions
                afterPlayerMove(currentPlayer, oppositePlayer);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(TAG, "onChildChanged() called with: dataSnapshot = [" + dataSnapshot + "], prevChildKey = [" + prevChildKey + "]");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved() called with: dataSnapshot = [" + dataSnapshot + "]");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(TAG, "onChildMoved() called with: dataSnapshot = [" + dataSnapshot + "], prevChildKey = [" + prevChildKey + "]");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled() called with: databaseError = [" + databaseError + "]");
            }
        });

        oppositePlayerMatrixRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(TAG, "oppositePlayerMatrixRef onChildAdded() called with: dataSnapshot = [" + dataSnapshot + dataSnapshot.getKey() + " : " + dataSnapshot.getValue(String.class) + "], prevChildKey = [" + prevChildKey + "]");

                //adding last added value to matrix
                //    String lastAddedValue = dataSnapshot.child(String.valueOf(oppositePlayerMatrix.size())).getValue(String.class);
                oppositePlayerMatrix.add(dataSnapshot.getValue(String.class));

                //updating ui & checking win-loose conditions
                afterPlayerMove(currentPlayer, oppositePlayer);

                //whenever value added  by opposite then unfreeze game
                //unfreeze should be at the end of computation
                unFreezeGame();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(TAG, "onChildChanged() called with: dataSnapshot = [" + dataSnapshot + "], prevChildKey = [" + prevChildKey + "]");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved() called with: dataSnapshot = [" + dataSnapshot + "]");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(TAG, "onChildMoved() called with: dataSnapshot = [" + dataSnapshot + "], prevChildKey = [" + prevChildKey + "]");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled() called with: databaseError = [" + databaseError + "]");
            }
        });
    }

    private void afterPlayerMove(Player currentPlayer, Player oppositePlayer) {
        Log.d(TAG, "afterPlayerMove() called with: currentPlayer = [" + currentPlayer + "], oppositePlayer = [" + oppositePlayer + "]");

        //update ui
        updateTiles(currentPlayer, oppositePlayer);

        //check if someone won
        GameStatus gameStatus = determineWinner(
                currentPlayer.getGame().getMatrix(),
                oppositePlayer.getGame().getMatrix()
        );
        switch (gameStatus) {
            case WON:
                freezeGame();
                showWinDialog();
                break;
            case LOOSE:
                freezeGame();
                showLooseDialog();
                break;
            case DRAW:
                freezeGame();
                showDrawDialog();
                break;
        }
    }

    private GameStatus determineWinner(List<String> currentPlayerMatrix, List<String> oppositePlayerMatrix) {
        Log.d(TAG, "determineWinner() called with: currentPlayerMatrix = [" + currentPlayerMatrix + "], oppositePlayerMatrix = [" + oppositePlayerMatrix + "]");

        GameStatus gameStatus = GameStatus.PLAYING;
        for (String[] winPair : winPairs) {
            List<String> pair = Arrays.asList(winPair);
            if (currentPlayerMatrix.containsAll(pair)) {
                gameStatus = GameStatus.WON;
                break;
            } else if (oppositePlayerMatrix.containsAll(pair)) {
                gameStatus = GameStatus.LOOSE;
                break;
            }
        }

        boolean areAllTilesFilled = (currentPlayerMatrix.size() + oppositePlayerMatrix.size()) == tiles.size();
        if (gameStatus == GameStatus.PLAYING && areAllTilesFilled) {
            gameStatus = GameStatus.DRAW;
        }

        return gameStatus;
    }


    private void updateTiles(Player currentPlayer, Player oppositePlayer) {
        Log.d(TAG, "updateTiles() called with: currentPlayer = [" + currentPlayer + "], oppositePlayer = [" + oppositePlayer + "]");

        List<String> currentPlayerMatrix = currentPlayer.getGame().getMatrix();
        List<String> oppositePlayerMatrix = oppositePlayer.getGame().getMatrix();

        for (String index : currentPlayerMatrix) {
            if (TextUtils.isEmpty(index)) continue;
            for (Tile tile : tiles) {
                if (tile.getMatrixIndex().equals(index)) {
                    tile.setTileStatus(isNewGame ? ZERO : CROSS);
                    tilesAdapter.notifyItemChanged(tiles.indexOf(tile));
                    Log.e(TAG, "updateTiles: tile " + tile);
                }
            }
        }
        for (String index : oppositePlayerMatrix) {
            if (TextUtils.isEmpty(index)) continue;
            for (Tile tile : tiles) {
                if (tile.getMatrixIndex().equals(index)) {
                    tile.setTileStatus(isNewGame ? CROSS : ZERO);
                    tilesAdapter.notifyItemChanged(tiles.indexOf(tile));
                    Log.e(TAG, "updateTiles: tile " + tile);
                }
            }
        }
    }

    private void freezeGame() {
        Log.d(TAG, "freezeGame() called");
        tilesAdapter.setItemClickListener(null);

    }

    private void unFreezeGame() {
        Log.d(TAG, "unFreezeGame() called");
        tilesAdapter.setItemClickListener(this);
    }

    private void showWinDialog() {
        dialogFactory.showWinLooseDialog(getString(R.string.winner_title), getString(R.string.win_msg), new DialogFactory.WinLooseDialogListener() {
            @Override
            public void onNewGame() {
                finish();
            }
        });
    }

    private void showLooseDialog() {
        dialogFactory.showWinLooseDialog(getString(R.string.loose_title), getString(R.string.loose_tmsg), new DialogFactory.WinLooseDialogListener() {
            @Override
            public void onNewGame() {
                finish();
            }
        });
    }


    private void showDrawDialog() {
        dialogFactory.showWinLooseDialog(getString(R.string.draw_title), getString(R.string.draw_message), new DialogFactory.WinLooseDialogListener() {
            @Override
            public void onNewGame() {
                finish();
            }
        });
    }


    private void showRoomIdDialog(final String roomId) {
        dialogFactory.showRoomIdDialog(roomId, getString(R.string.share_room_id_desc), new DialogFactory.RoomDialogListener() {
            @Override
            public void onCopy() {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(roomId, roomId);
                clipboard.setPrimaryClip(clip);
            }
        });
    }


    private void terminateGame(String reason) {
        Log.d(TAG, "terminateGame() called with: reason = [" + reason + "]");
        //freeze,show error and exit game
        freezeGame();
        showMessage(reason);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                exitGame();
            }
        }, DELAY_MS);
    }

    private void exitGame() {
        Log.d(TAG, "exitGame() called");
        //deleting game on room id and navigating back
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dialogFactory.dismissDialogs();
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

    @Override
    public void onItemClick(final Tile tile) {
        Log.d(TAG, "onItemClick() called with: tile = [" + tile + "]");

        if (mCurrentPlayerData == null || mOppositePlayerData == null) return;
        if (tile.getTileStatus() == BLANK) {

            final DatabaseReference currentPlayerMatrixRef = mCurrentPlayerData
                    .child(Constants.DataKeys.GAME)
                    .child(Constants.DataKeys.MATRIX)
                    .getRef();

            final DatabaseReference oppPlayerMatrixRef = mOppositePlayerData
                    .child(Constants.DataKeys.GAME)
                    .child(Constants.DataKeys.MATRIX)
                    .getRef();

            currentPlayerMatrixRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> matrix = new ArrayList<>();
                    Iterable<DataSnapshot> matrixIt = dataSnapshot.getChildren();
                    while (matrixIt.iterator().hasNext()) {
                        matrix.add(matrixIt.iterator().next().getValue(String.class));
                    }
                    String matrixIndex = tile.getMatrixIndex();
                    if (!matrix.contains(matrixIndex)) {
                        matrix.add(matrixIndex);
                    }
                    currentPlayerMatrixRef.setValue(matrix);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled() called with: databaseError = [" + databaseError + "]");
                }
            });
        }
    }

    private class RoomChildEventListener implements ChildEventListener {

        private final PlayersListener playersListener;

        public RoomChildEventListener(PlayersListener playersListener) {
            this.playersListener = playersListener;
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
            Log.d(TAG, "onChildAdded() called with: dataSnapshot = [" + dataSnapshot + "], prevChildKey = [" + prevChildKey + "]");

            if (dataSnapshot.getKey().equals(Constants.DataKeys.PLAYER_A) || dataSnapshot.getKey().equals(Constants.DataKeys.PLAYER_B)) {
                //checking if both players have joined the game
                dataSnapshot.getRef().getParent().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange() called with: dataSnapshot = [" + dataSnapshot + "]");

                        boolean hasPlayerA = dataSnapshot.hasChild(Constants.DataKeys.PLAYER_A);
                        boolean hasPlayerB = dataSnapshot.hasChild(Constants.DataKeys.PLAYER_B);
                        Log.e(TAG, "onChildAdded: hasPlayerA " + hasPlayerA + " hasPlayerB " + hasPlayerB);
                        if (hasPlayerA && hasPlayerB) {
                            DataSnapshot playerA = dataSnapshot.child(Constants.DataKeys.PLAYER_A);
                            DataSnapshot playerB = dataSnapshot.child(Constants.DataKeys.PLAYER_B);
                            if (isNewGame) {
                                playersListener.onBothPlayersJoined(playerA, playerB);
                            } else {
                                playersListener.onBothPlayersJoined(playerB, playerA);
                            }
                        }
                        //DataSnapshot player = dataSnapshot.child(Constants.DataKeys.PLAYER);
                        //if(hasPlayerA || hasPlayerB)
                       // {
                            //dataSnapshot.getRef().setValue(null);
                            //playersListener.onPlayerLeft(player);
                       // }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled() called with: databaseError = [" + databaseError + "]");
                    }
                });
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
            Log.d(TAG, "onChildChanged() called with: dataSnapshot = [" + dataSnapshot + "], prevChildKey = [" + prevChildKey + "]");
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onChildRemoved() called with: dataSnapshot = [" + dataSnapshot + "]");
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            Log.d(TAG, "onChildMoved() called with: dataSnapshot = [" + dataSnapshot + "], prevChildKey = [" + prevChildKey + "]");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled() called with: databaseError = [" + databaseError + "]");
        }
    }


    private interface PlayersListener {

        void onBothPlayersJoined(DataSnapshot currentPlayer, DataSnapshot oppositePlayer);

        void onPlayerLeft(DataSnapshot player);

        void onError(Throwable error);
    }
}
