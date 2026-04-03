CREATE TABLE IF NOT EXISTS games (
  table_id          INTEGER PRIMARY KEY,
  player1_id        TEXT NOT NULL,
  player1_name      TEXT NOT NULL,
  player1_hero      TEXT NOT NULL,
  player1_faction   TEXT NOT NULL,
  player1_deck_name TEXT NOT NULL,
  player1_deck_id   TEXT NOT NULL,
  player2_id        TEXT NOT NULL,
  player2_name      TEXT NOT NULL,
  player2_hero      TEXT NOT NULL,
  player2_faction   TEXT NOT NULL,
  player2_deck_name TEXT NOT NULL,
  player2_deck_id   TEXT NOT NULL,
  winner_player_id  TEXT NOT NULL,
  played_at         INTEGER NOT NULL,
  created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
